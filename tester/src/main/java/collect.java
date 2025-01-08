import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import entities.CloudWatch;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;
import software.amazon.awssdk.services.cloudwatchlogs.paginators.DescribeLogStreamsIterable;

import java.util.Arrays;

public class collect {

    public static void main(String[] args) {
        String logGroupName = "/trace";
        Region region = Region.US_EAST_2;

        var cloudWatchLogsClient = CloudWatchLogsClient
                .builder()
                .region(region)
                .build();

        var reqGroup = DescribeLogGroupsRequest
                .builder()
                .logGroupNamePrefix(logGroupName)
                .build();

        var resGroup = cloudWatchLogsClient
                .describeLogGroups(reqGroup)
                .logGroups();

        System.out.println("\n############### LOG GROUP INFO ################\n");
        for (LogGroup logGroup : resGroup) {
            System.out.println(CloudWatch.LogGroupInfo.fromLogGroup(logGroup));
        }


        var reqStreams = DescribeLogStreamsRequest.builder()
                .logGroupName(logGroupName)
                .build();
        var resStreams = cloudWatchLogsClient.describeLogStreams(reqStreams);

        var resStreamsItt = new DescribeLogStreamsIterable(cloudWatchLogsClient, reqStreams);


        System.out.println("\n############### LOG STREAM INFO ################\n");

        for (var logStream : resStreamsItt.logStreams()) {
            System.out.println(CloudWatch.LogStreamInfo.fromLogStream(logStream));
        }

        if (!resStreams.logStreams().isEmpty()) {
            var logStream = resStreams.logStreams().get(0);
            var reqEvent = GetLogEventsRequest.builder()
                    .logGroupName(logGroupName)
                    .logStreamName(logStream.logStreamName())
                    .startFromHead(true)
                    .build();

            var events = cloudWatchLogsClient.getLogEvents(reqEvent).events();
            ObjectMapper objectMapper = new ObjectMapper();

            System.out.println("\n############### LOG EVENTS ################\n");

            for (var log : events) {
                System.out.println("Ingestion Time: " + log.ingestionTime());
                System.out.println("Message: " + log.message());

                try {
                    CloudWatch.PlatformLog platformLog = objectMapper.readValue(log.message(), CloudWatch.PlatformLog.class);
                    System.out.printf(" -> time: %s  |  type: %s%n", platformLog.time(), platformLog.type());

                    var record = platformLog.record();
                    if (record instanceof CloudWatch.PlatformLog.InitStartRecord initRec) {
                        System.out.printf("    [initStart] Function: %s, Runtime: %s%n",
                            initRec.functionName(), initRec.runtimeVersion());
                    } else if (record instanceof CloudWatch.PlatformLog.StartRecord startRec) {
                        System.out.printf("    [start] RequestId: %s, Version: %s%n",
                            startRec.requestId(), startRec.version());
                    } else if (record instanceof CloudWatch.PlatformLog.ReportRecord reportRec) {
                        System.out.printf("    [report] Status: %s, Duration: %.2fms%n",
                            reportRec.status(), reportRec.metrics().durationMs());
                    }
                } catch (JsonProcessingException e) {
                    System.err.println("Failed to parse log message: " + e.getMessage());
                }

                System.out.println("-------------------------------------------\n");
            }
        }

        cloudWatchLogsClient.close();
    }
}