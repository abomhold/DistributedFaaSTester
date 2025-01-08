package entities;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;

import java.util.List;

public class PlatformLogs {

    /**
     * Sealed interface for the 'record' field in each log.
     * The three permitted record types are defined inside `PlatformLog` below.
     */
    public sealed interface BaseRecord
        permits PlatformLog.InitStartRecord, PlatformLog.StartRecord, PlatformLog.ReportRecord {
    }

    /**
     * The top-level log record. Notice how we declare nested records for
     * InitStartRecord, StartRecord, and ReportRecord inside it.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlatformLog(
        String time,
        String type,

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = InitStartRecord.class, name = "platform.initStart"),
            @JsonSubTypes.Type(value = StartRecord.class,      name = "platform.start"),
            @JsonSubTypes.Type(value = ReportRecord.class,     name = "platform.report")
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
        ) implements BaseRecord { }

        /**
         * Record subtype for "platform.start".
         */
        @JsonTypeName("platform.start")
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record StartRecord(
            String requestId,
            String version
        ) implements BaseRecord { }

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
            ) { }
        }
    }

    /**
     * A small demo 'main' method showing how to deserialize a JSON array of logs
     * into a List of PlatformLog using Jackson.
     */
    public static void main(String[] args) throws Exception {
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
        // Create an ObjectMapper with Java records support
        // Deserialize the JSON array into a List of PlatformLog
        List<PlatformLog> logs = objectMapper.readValue(
            json,
            objectMapper.getTypeFactory().constructCollectionType(List.class, PlatformLog.class)
        );

        // Print out each log and downcast the BaseRecord to the known subtypes
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

}
