import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;

import java.util.HashMap;
import java.util.Map;

public class collect {

    public static void main(String[] args) {
//        String logStreamName = args[0];
        String logGroupName = "/trace";
        Region region = Region.US_EAST_2;
        CloudWatchLogsClient cloudWatchLogsClient = CloudWatchLogsClient.builder().region(region).build();
        var reqGroup = DescribeLogGroupsRequest.builder().logGroupNamePrefix(logGroupName).build();


        var resGroup = cloudWatchLogsClient.describeLogGroups(reqGroup).logGroups();

        System.out.println("LOG GROUP INFO: ");

        // Print all log group information
        for (LogGroup logGroup : resGroup) {
            for (var field : logGroup.sdkFields()) {
                System.out.println(" " + field.unmarshallLocationName() + " = " + field.getValueOrDefault(logGroup));
            }
        }

        var reqStreams = DescribeLogStreamsRequest.builder().logGroupName(logGroupName).build();

        var resStreams = cloudWatchLogsClient.describeLogStreams(reqStreams);

        //var streamsIterable = new DescribeLogStreamsIterable(cloudWatchLogsClient, reqStreams);

        System.out.println("LOG STREAM INFO: ");
        // Print all log stream information
        var logStream = resStreams.logStreams().get(0);
        for ( var field : logStream.sdkFields()) {
            System.out.println(" " + field.unmarshallLocationName() + " = " + field.getValueOrDefault(logStream));
        }

        var reqEvent = GetLogEventsRequest.builder()
                                          .logGroupName(logGroupName)
                                          .logStreamName(logStream.logStreamName())
                                          .startFromHead(true)
                                          .build();

        System.out.println(System.lineSeparator() + "############### LOG EVENTS ################" + System.lineSeparator());
        for (var log : cloudWatchLogsClient.getLogEvents(reqEvent).events()) {
            System.out.println("datetime=" + log.ingestionTime());
            System.out.println(log.message());
//            ObjectMapper objectMapper = new ObjectMapper();
//            try {
//                map = objectMapper.readValue(log.message(), new TypeReference<Map<String, String>>() {});
//            } catch (JsonProcessingException e) {
//                System.out.println(e.toString());
//            }
//
//
//            HashMap<String, String> data = new HashMap<>();
//            try {
//                var res = new ObjectMapper().readValue(log.message(), new TypeReference<HashMap<String, String>>() {});
//                System.out.println(res.get("record"));
//                data.putAll(res);
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//
//            }
//            try {
//                data.putAll(new ObjectMapper().readValue(data.get("record").toString(), new TypeReference<HashMap<String, Object>>() {}));
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }

//
//            var record = data.get("record").toString().substring(1, data.get("record").toString().length() - 1);
//
//            for (var pair : record.split(", ")) {
//                String[] keyValue = pair.split("=");
//                if (keyValue.length == 2) {
//                    data.put(keyValue[0], keyValue[1]);
//                }
//            }
        }


        cloudWatchLogsClient.close();
    }

}

//        if (e.getKey().equals("record")) {
//        try {
//        data.putAll(new ObjectMapper().readValue(e.getValue()
//                                                                                        .toString(), new TypeReference<Map<String, String>>() {
//        }));
//        } catch (JsonProcessingException ex) {
//        throw new RuntimeException(ex);
//                                          }
