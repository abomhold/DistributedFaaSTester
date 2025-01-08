import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import entities.CloudWatch;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;
import software.amazon.awssdk.services.cloudwatchlogs.paginators.DescribeLogStreamsIterable;

public class collect {

    public static void main(String[] args) {
        String logGroupName = "/trace";
        Region region = Region.US_EAST_2;
        var cloudWatchLogsClient = CloudWatchLogsClient.builder().region(region).build();

        System.out.println("\n############### LOG GROUP INFO ################\n");
        var reqGroup = DescribeLogGroupsRequest.builder().logGroupNamePrefix(logGroupName).build();
        var resGroup = cloudWatchLogsClient.describeLogGroups(reqGroup).logGroups();
        for (LogGroup logGroup : resGroup) {
            System.out.println(CloudWatch.LogGroupInfo.fromLogGroup(logGroup));
        }


        var reqStreams = DescribeLogStreamsRequest.builder().logGroupName(logGroupName).build();
        var resStreamsItt = new DescribeLogStreamsIterable(cloudWatchLogsClient, reqStreams);
        for (var logStream : resStreamsItt.logStreams()) {
            System.out.println("\n############### LOG STREAM INFO ################\n");
            System.out.println(CloudWatch.LogStreamInfo.fromLogStream(logStream));
            var reqEvent = GetLogEventsRequest.builder()
                                              .logGroupName(logGroupName)
                                              .logStreamName(logStream.logStreamName())
                                              .startFromHead(false)
                                              .build();

            var events = cloudWatchLogsClient.getLogEvents(reqEvent).events();
            ObjectMapper objectMapper = new ObjectMapper();

            System.out.println("\n######## LOG EVENTS #########\n");

            for (var log : events) {
                System.out.println("Ingestion Time: " + log.ingestionTime());
                System.out.println("Message: " + log.message());

                try {
                    CloudWatch.PlatformLog platformLog = objectMapper.readValue(log.message(), CloudWatch.PlatformLog.class);

                    System.out.printf(" -> time: %s  |  type: %s%n", platformLog.time(), platformLog.type());

                } catch (JsonProcessingException e) {
                    System.err.println("Failed to parse log message: " + e.getMessage());
                }

                System.out.println("-------------------------------------------\n");
            }
        }

        cloudWatchLogsClient.close();
    }
}