package collect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;
import software.amazon.awssdk.services.cloudwatchlogs.paginators.DescribeLogStreamsIterable;
import software.amazon.awssdk.services.cloudwatchlogs.paginators.GetLogEventsIterable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

public class LogPool {
    public static final Map<String, Long> startTimesByRequestId = new HashMap<>();
    public static final Map<String, Long> endTimesByRequestId = new HashMap<>();
    public static final Map<String, String> requestObjectByRequestId = new HashMap<>();
    public static final Map<String, HashSet<String>> requestIdsByFunctionName = new HashMap<>();
    public final long startTime;
    public final long endTime;
    public final Region logRegion;
    public final LogGroup logGroup;
    public final ObjectMapper objectMapper;
    Pattern FunctionNamePattern = Pattern.compile("/([a-zA-Z0-9]+)\\[");

    public LogPool(Region logRegion, LogGroup logGroup, long startTime, long endTime) {
        this.logRegion = logRegion;
        this.logGroup = logGroup;
        this.startTime = startTime;
        this.endTime = endTime;
        this.objectMapper = new ObjectMapper();

        collectLogs();
    }

    private void collectLogs() {
        try (CloudWatchLogsClient cloudWatchLogsClient = CloudWatchLogsClient.builder().region(logRegion).build()) {
            DescribeLogStreamsRequest logStreamsRequest = DescribeLogStreamsRequest.builder()
                                                                                   .logGroupName(logGroup.logGroupName())
                                                                                   .build();

            DescribeLogStreamsIterable logStreamsIterable
                    = new DescribeLogStreamsIterable(cloudWatchLogsClient, logStreamsRequest);

            for (LogStream logStream : logStreamsIterable.logStreams()) {
                processLogStream(logStream, cloudWatchLogsClient);
            }
        } catch (CloudWatchLogsException e) {
            System.err.println("Error fetching logs: " + e.getMessage());
        }

    }

    private void processLogStream(LogStream logStream, CloudWatchLogsClient cloudWatchLogsClient) {
        String logStreamName = logStream.logStreamName();
        var matcher = FunctionNamePattern.matcher(logStreamName);
        matcher.find();
        var functionName = matcher.group(1);


        GetLogEventsRequest logEventsRequest = GetLogEventsRequest.builder()
                                                                  .logGroupName(logGroup.logGroupName())
                                                                  .logStreamName(logStreamName)
                                                                  .startTime(startTime)
                                                                  .endTime(endTime)
                                                                  .build();


        GetLogEventsIterable logEventsResponse = cloudWatchLogsClient.getLogEventsPaginator(logEventsRequest);

        for (OutputLogEvent event : logEventsResponse.events()) {
            LogEvent logEvent = parseLogEvent(event);
            if (logEvent != null) {
                processLogEvent(logEvent, functionName);
            }
        }
    }

    //Assumes only non default log is request object
    private void processLogEvent(LogEvent logEvent, String functionName) {
        if (logEvent.type != null) {
            switch (logEvent.type) {
                case "platform.start" -> startTimesByRequestId.put(logEvent.record.requestId, logEvent.EventTime);
                case "platform.report" -> endTimesByRequestId.put(logEvent.record.requestId, logEvent.EventTime);
            }
            requestIdsByFunctionName.computeIfAbsent(
                    functionName, k -> new HashSet<>()).add(logEvent.record.requestId);
        } else {
            requestObjectByRequestId.put(logEvent.awsRequestId, logEvent.message);
        }
    }

    private LogEvent parseLogEvent(OutputLogEvent event) {
        LogEvent log = null;
        try {
            log = objectMapper.readValue(event.message(), LogEvent.class);
            log.EventTime = event.timestamp();
        } catch (JsonProcessingException e) {
            System.out.println("Error parsing log event: " + e.getMessage());
        }
        return log;
    }

    public void writeJson(String path) throws IOException {
        var result = new HashMap<>();
        for (var func : requestIdsByFunctionName.keySet()) {
            for (var requestId : requestIdsByFunctionName.get(func)) {
                if (requestId != null) {
                    Map<String, Object> requestData = new HashMap<>();
                    requestData.put("Node", func);
                    Long startTimeValue = startTimesByRequestId.get(requestId);
                    requestData.put("Start", startTimeValue != null ? (startTimeValue - startTime) : "Not available");
                    requestData.put("Body", requestObjectByRequestId.getOrDefault(requestId, "Not available"));
                    result.put(requestId, requestData);
                }
            }

        }
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(path), result);
    }
}
