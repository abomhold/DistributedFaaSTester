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
        printCollectedData();
    }

    private void collectLogs() {
        try (CloudWatchLogsClient cloudWatchLogsClient = CloudWatchLogsClient.builder().region(logRegion).build()) {
            DescribeLogStreamsRequest logStreamsRequest = DescribeLogStreamsRequest.builder()
                    .logGroupName(logGroup.logGroupName())
                    .build();

            DescribeLogStreamsIterable logStreamsIterable = new DescribeLogStreamsIterable(cloudWatchLogsClient, logStreamsRequest);

            for (LogStream logStream : logStreamsIterable.logStreams()) {
                processLogStream(logStream, cloudWatchLogsClient);
            }
        } catch (CloudWatchLogsException e) {
            System.err.println("Error fetching logs: " + e.getMessage());
        }

    }

    private void processLogStream(LogStream logStream, CloudWatchLogsClient cloudWatchLogsClient) {
        String logStreamName = logStream.logStreamName();
        System.out.println("Log stream name: " + logStreamName);
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
            requestIdsByFunctionName.computeIfAbsent(functionName, k -> new HashSet<>()).add(logEvent.record.requestId);
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


    private void printCollectedData() {
        for (var func : requestIdsByFunctionName.keySet()) {
            System.out.print("Function: " + func);
            for (var requestId : requestIdsByFunctionName.get(func)) {
                if (requestId != null) {
                    System.out.println();
                    System.out.println("    RequestId: " + requestId);
                    System.out.println("    StartTime: " + startTimesByRequestId.get(requestId));
                    System.out.println("    EndTime: " + endTimesByRequestId.get(requestId));
                    System.out.println("    DelayFromTraceStart: " + (startTimesByRequestId.get(requestId) - startTime));
                    System.out.println("    TotalRunTime: " + (endTimesByRequestId.get(requestId) - startTimesByRequestId.get(requestId)));
                    System.out.println("    Message: " + requestObjectByRequestId.get(requestId));
                    System.out.println();
                }
            }
        }
    }

    public void writeJson(String path) throws IOException {
        Map<String, Object> result = new HashMap<>();

        for (var func : requestIdsByFunctionName.keySet()) {
            for (var requestId : requestIdsByFunctionName.get(func)) {
                if (requestId != null) {
                    Map<String, Object> requestData = new HashMap<>();
                    requestData.put("functionName", func);
                    requestData.put("requestId", requestId);

                    Long startTimeValue = startTimesByRequestId.get(requestId);
                    Long endTimeValue = endTimesByRequestId.get(requestId);

//                    requestData.put("RequestId", requestId);
//                    requestData.put("StartTime", startTimeValue != null ? startTimeValue : "Not available");
//                    requestData.put("EndTime", endTimeValue != null ? endTimeValue : "Not available");
                    requestData.put("DelayFromTraceStart",
                            startTimeValue != null ? (startTimeValue - startTime) : "Not available");
//                    requestData.put("TotalRunTime",
//                            (startTimeValue != null && endTimeValue != null)
//                                    ? (endTimeValue - startTimeValue) : "Not available");
                    requestData.put("Message", requestObjectByRequestId.getOrDefault(requestId, "Not available"));

                    functionData.put(requestId, requestData);
                }
            }

            result.put(func, functionData);
        }
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(path), result);
    }
}
