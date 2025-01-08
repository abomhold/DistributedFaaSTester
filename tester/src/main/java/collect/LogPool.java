package collect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogStream;
import software.amazon.awssdk.services.cloudwatchlogs.paginators.DescribeLogStreamsIterable;

import java.util.ArrayList;
import java.util.List;

public class LogPool implements AWSLogs {
    final long startTime;
    final long endTime;
    List<LogStream> logStreams = new ArrayList<>();
    List<LogEntry> logs = new ArrayList<>();
    Region logRegion;
    LogGroup logGroup;

    public LogPool(Region logRegion, LogGroup logGroup, long startTime, long endTime) {
        this.logGroup = logGroup;
        this.startTime = startTime;
        this.endTime = endTime;

        collectLogs(logRegion, logGroup, startTime, endTime);
    }

    private void collectLogs(Region logRegion, LogGroup logGroup, long startTime, long endTime) {
        try (var cloudWatchLogsClient = CloudWatchLogsClient.builder().region(logRegion).build()) {
            var reqStreams = DescribeLogStreamsRequest.builder()
                    .logGroupName(logGroup.logGroupName())
                    .build();
            var resStreamsItt = new DescribeLogStreamsIterable(cloudWatchLogsClient, reqStreams);
            var objectMapper = new ObjectMapper();

            for (var logStream : resStreamsItt.logStreams()) {
//                System.out.println(logStream);
                logStreams.add(logStream);

                var reqEvent = GetLogEventsRequest.builder()
                        .logGroupName(logGroup.logGroupName())
                        .logStreamName(logStream.logStreamName())
                        .startTime(startTime)
                        .endTime(endTime)
                        .build();

                var eventsIterable = cloudWatchLogsClient.getLogEventsPaginator(reqEvent);
                for (var event : eventsIterable.events()) {
                    try {
                        PlatformLog platformLog = objectMapper.readValue(event.message(), PlatformLog.class);
                        logs.add(platformLog);
                        System.out.println(platformLog);
                    } catch (JsonProcessingException e) {
                        // If platform log parsing fails, try message log
                        try {
                            MessageLog messageLog = objectMapper.readValue(event.message(), MessageLog.class);
                            logs.add(messageLog);
                            System.out.println(messageLog);
                        } catch (JsonProcessingException e2) {
                            // If both fail, log the error with the original message
                            System.err.println("Failed to parse log message as either type: " + event.message());
                            System.err.println("Error: " + e2.getMessage());
                        }
                    } catch (Exception e) {
                        System.err.println("Unexpected error processing log message: " + e.getMessage());
                    }
                }
            }
        }
    }

    public List<LogEntry> getLogs() {
        return logs;
    }

    public List<PlatformLog> getPlatformLogs() {
        return logs.stream()
                .filter(PlatformLog.class::isInstance)
                .map(PlatformLog.class::cast)
                .toList();
    }

    public List<MessageLog> getMessageLogs() {
        return logs.stream()
                .filter(MessageLog.class::isInstance)
                .map(MessageLog.class::cast)
                .toList();
    }
}