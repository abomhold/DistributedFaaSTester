package entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogStream;

import java.util.List;

public interface CloudWatch {

    static void main(String[] args) throws Exception {
        String json = """
                [
                  {
                    "time": "2025-01-07T19:49:07.089Z",
                    "type": "platform.initStart",
                    "record": {
                      "initializationType": "on-demand",
                      "phase": "init",
                      "runtimeVersion": "java:21.v27",
                      "runtimeVersionArn": "arn:aws:lambda:us-east-2::runtime:1fe198c3...",
                      "functionName": "worker10",
                      "functionVersion": "$LATEST",
                      "instanceId": "2025/01/07/worker10[$LATEST]002cf7829acf404ea7f6995e1910b96b",
                      "instanceMaxMemory": 1879048192
                    }
                  },
                  {
                    "time": "2025-01-07T19:49:07.595Z",
                    "type": "platform.start",
                    "record": {
                      "requestId": "06e8554f-3109-426f-8992-0c834de2ac20",
                      "version": "$LATEST"
                    }
                  },
                  {
                    "time": "2025-01-07T19:49:07.924Z",
                    "type": "platform.report",
                    "record": {
                      "requestId": "06e8554f-3109-426f-8992-0c834de2ac20",
                      "metrics": {
                        "durationMs": 329.177,
                        "billedDurationMs": 330,
                        "memorySizeMB": 1792,
                        "maxMemoryUsedMB": 125,
                        "initDurationMs": 503.083
                      },
                      "status": "success"
                    }
                  }
                ]
                """;

        ObjectMapper objectMapper = new ObjectMapper();
        List<PlatformLog> logs = objectMapper.readValue(
                json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, PlatformLog.class)
        );

        for (PlatformLog log : logs) {
            System.out.println("Time: " + log.time());
            System.out.println("Type: " + log.type());
            var record = log.record();

            if (record instanceof PlatformLog.InitStartRecord init) {
                System.out.println("  [initStart] initializationType = " + init);
            } else if (record instanceof PlatformLog.StartRecord start) {
                System.out.println("  [start] requestId = " + start);
            } else if (record instanceof PlatformLog.ReportRecord report) {
                System.out.println("  [report] status = " + report);
            }

            System.out.println("------------------------------------------------------");
        }
    }


    sealed interface BaseRecord
            permits PlatformLog.InitStartRecord, PlatformLog.StartRecord, PlatformLog.ReportRecord {
    }

    record LogGroupInfo(
            String logGroupName,
            long creationTime,
            int retentionInDays,
            int metricFilterCount,
            String arn,
            long storedBytes,
            String kmsKeyId,
            String dataProtectionStatus,
            String[] inheritedProperties,
            String logGroupClass,
            String logGroupArn

    ) {
        public static LogGroupInfo fromLogGroup(LogGroup lg) {
            return new LogGroupInfo(
                    lg.logGroupName(),
                    lg.creationTime(),
                    lg.retentionInDays(),
                    lg.metricFilterCount(),
                    lg.arn(),
                    lg.storedBytes(),
                    lg.kmsKeyId(),
                    lg.dataProtectionStatusAsString(),
                    lg.inheritedPropertiesAsStrings().toArray(new String[0]),
                    lg.logGroupClassAsString(),
                    lg.logGroupArn()
            );

        }
    }

    record LogStreamInfo(
            String logStreamName,
            long creationTime,
            long firstEventTimestamp,
            long lastEventTimestamp,
            long lastIngestionTime,
            String uploadSequenceToken,
            String arn
    ) {
        public static LogStreamInfo fromLogStream(LogStream ls) {
            return new LogStreamInfo(
                    ls.logStreamName(),
                    ls.creationTime(),
                    ls.firstEventTimestamp(),
                    ls.lastEventTimestamp(),
                    ls.lastIngestionTime(),
                    ls.uploadSequenceToken(),
                    ls.arn()
            );
        }
    }

    /**
     * The top-level log record.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record PlatformLog(
            String time,
            String type,

            @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
            @JsonSubTypes({
                    @JsonSubTypes.Type(value = InitStartRecord.class, name = "platform.initStart"),
                    @JsonSubTypes.Type(value = StartRecord.class, name = "platform.start"),
                    @JsonSubTypes.Type(value = ReportRecord.class, name = "platform.report")
            })
            BaseRecord record
    ) {

        /**
         * Record subtype for "platform.initStart".
         */
        @JsonTypeName("platform.initStart")
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record InitStartRecord(
                String initializationType,
                String phase,
                String runtimeVersion,
                String runtimeVersionArn,
                String functionName,
                String functionVersion,
                String instanceId,
                long instanceMaxMemory
        ) implements BaseRecord {
        }

        /**
         * Record subtype for "platform.start".
         */
        @JsonTypeName("platform.start")
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record StartRecord(
                String requestId,
                String version
        ) implements BaseRecord {
        }

        /**
         * Record subtype for "platform.report".
         */
        @JsonTypeName("platform.report")
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ReportRecord(
                String requestId,
                Metrics metrics,
                String status
        ) implements BaseRecord {

            /**
             * Nested metrics record, used by "platform.report".
             */
            @JsonIgnoreProperties(ignoreUnknown = true)
            public record Metrics(
                    double durationMs,
                    double billedDurationMs,
                    long memorySizeMB,
                    long maxMemoryUsedMB,
                    double initDurationMs
            ) {
            }
        }
    }

}