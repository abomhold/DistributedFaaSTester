import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClientBuilder;
import software.amazon.awssdk.services.cloudwatch.internal.CloudWatchServiceClientConfigurationBuilder;
import software.amazon.awssdk.services.cloudwatch.model.CloudWatchRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.model.CloudWatchException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;

public class collect {
    public static void main(String[] args) {

        final String usage = """
                
                Usage:
                  <logStreamName> <logGroupName>
                
                Where:
                  logStreamName - The name of the log stream (for example, mystream).
                  logGroupName - The name of the log group (for example, myloggroup).
                """;

        if (args.length != 2) {
            System.out.print(usage);
            System.exit(1);
        }

        String logStreamName = args[0];
        String logGroupName = args[1];
        Region region = Region.US_WEST_2;
        CloudWatchLogsClient cloudWatchLogsClient = CloudWatchLogsClient.builder()
                .region(region)
                .build();
//        var logStreamRequest = G
//        getCWLogEvents(cloudWatchLogsClient, logGroupName, logStreamName);
        cloudWatchLogsClient.close();
    }

    public static void getCWLogEvents(CloudWatchLogsClient cloudWatchLogsClient, String logGroupName,
                                      String logStreamName) {
        try {
            GetLogEventsRequest getLogEventsRequest = GetLogEventsRequest.builder()
                    .logGroupName(logGroupName)
                    .logStreamName(logStreamName)
                    .startFromHead(true)
                    .build();

            int logLimit = cloudWatchLogsClient.getLogEvents(getLogEventsRequest).events().size();
            for (int c = 0; c < logLimit; c++) {
                System.out.println(cloudWatchLogsClient.getLogEvents(getLogEventsRequest).events().get(c).message());
            }

            System.out.println("Successfully got CloudWatch log events!");

        } catch (CloudWatchException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }
}
