//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
//import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
//import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
//import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;
//import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;
//import software.amazon.awssdk.services.cloudwatchlogs.paginators.DescribeLogStreamsIterable;
//
//public class collect {
//
//    public static void main(String[] args) {
//        String logGroupName = "/trace";
//        Region region = Region.US_EAST_2;
//        var cloudWatchLogsClient = CloudWatchLogsClient.builder().region(region).build();
//
//        System.out.println("\n############### LOG GROUP INFO ################\n");
//        var reqGroup = DescribeLogGroupsRequest.builder().logGroupNamePrefix(logGroupName).build();
//        var resGroup = cloudWatchLogsClient.describeLogGroups(reqGroup).logGroups();
//        for (LogGroup logGroup : resGroup) {
//            System.out.println(collect.AWSLogs.LogGroupInfo.fromLogGroup(logGroup));
//        }
//
//
//        var reqStreams = DescribeLogStreamsRequest.builder().logGroupName(logGroupName).build();
//        var resStreamsItt = new DescribeLogStreamsIterable(cloudWatchLogsClient, reqStreams);
//        for (var logStream : resStreamsItt.logStreams()) {
//            System.out.println("\n############### LOG STREAM INFO ################\n");
//            System.out.println(collect.AWSLogs.LogStreamInfo.fromLogStream(logStream));
//            var reqEvent = GetLogEventsRequest.builder()
//                                              .logGroupName(logGroupName)
//                                              .logStreamName(logStream.logStreamName())
//                                              .startFromHead(false)
//                                              .build();
//
//            var events = cloudWatchLogsClient.getLogEvents(reqEvent).events();
//            ObjectMapper objectMapper = new ObjectMapper();
//
//            System.out.println("\n######## LOG EVENTS #########\n");
//
//            for (var log : events) {
//                System.out.println("Ingestion Time: " + log.ingestionTime());
//                System.out.println("Message: " + log.message());
//
//                try {
//                    collect.AWSLogs.PlatformLog platformLog = objectMapper.readValue(log.message(), collect.AWSLogs.PlatformLog.class);
//
//                    System.out.printf(" -> time: %s  |  type: %s%n", platformLog.time(), platformLog.type());
//
//                } catch (JsonProcessingException e) {
//                    System.err.println("Failed to parse log message: " + e.getMessage());
//                }
//
//                System.out.println("-------------------------------------------\n");
//            }
//        }
//
//        cloudWatchLogsClient.close();
//    }
//}

LogStream(
        LogStreamName=2025/01/08/worker10[$LATEST]19dd386b47514caeb98295dd3f974aed,
        CreationTime=1736376683773,
        FirstEventTimestamp=1736376679970,
        LastEventTimestamp=1736376680426,
        LastIngestionTime=1736376683781,
        UploadSequenceToken=49039859615748942777344389947211247933342356110702016942,
        Arn=arn:aws:logs:us-east-2:980921732919:log-group:/trace:log-stream:2025/01/08/worker10[$LATEST]19dd386b47514caeb98295dd3f974aed,
        StoredBytes=0)
