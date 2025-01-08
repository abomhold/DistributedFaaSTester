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
import software.amazon.awssdk.services.cloudwatchlogs.paginators.GetLogEventsIterable;

import java.util.ArrayList;
import java.util.List;

public class LogPool implements AWSLogs {
    final long startTime;
    final long endTime;
    List<LogStream> logStreams = new ArrayList<>();
    List<AWSLogs.PlatformLog> platformLogs = new ArrayList<>();
    Region logRegion;
    LogGroup logGroup;


    public LogPool(Region logRegion, LogGroup logGroup, long startTime, long endTime) {
        this.logRegion = logRegion;
        this.logGroup = logGroup;
        this.startTime = startTime;
        this.endTime = endTime;
        var cloudWatchLogsClient = CloudWatchLogsClient.builder().region(logRegion).build();

        var reqStreams = DescribeLogStreamsRequest.builder()
                                                  .logGroupName(logGroup.logGroupName())
                                                  .build();
        var resStreamsItt = new DescribeLogStreamsIterable(cloudWatchLogsClient, reqStreams);
        var objectMapper = new ObjectMapper();

        for (var logStream : resStreamsItt.logStreams()) {
            System.out.println(logStream);
            logStreams.add(logStream);

            var reqEvent = GetLogEventsRequest.builder()
                    .logGroupName(logGroup.logGroupName())
                    .logStreamName(logStream.logStreamName())
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            var eventsIterable = cloudWatchLogsClient.getLogEventsPaginator(reqEvent);
//            var events = cloudWatchLogsClient.getLogEvents(reqEvent).events();

            for (var event : eventsIterable.events()) {
                try {
                    AWSLogs.PlatformLog platformLog = objectMapper.readValue(event.message(), AWSLogs.PlatformLog.class);
                    platformLogs.add(platformLog);
                    System.out.println(platformLog);
                } catch (JsonProcessingException e) {
                    System.err.println("Failed to parse log message: " + e.getMessage());
                }
            }
        }
        cloudWatchLogsClient.close();
    }
}
