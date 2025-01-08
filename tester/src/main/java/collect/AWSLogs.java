package collect;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public interface AWSLogs {

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
                  },
                  {
                      "timestamp": "2025-01-08T21:21:30.201Z",
                      "message": "{headers={x-amzn-tls-cipher-suite=TLS_AES_128_GCM_SHA256, content-length=16, x-amzn-tls-version=TLSv1.3, x-amzn-trace-id=Root=1-677eec59-42ebac255c28fff034b4fd48, x-forwarded-proto=https, host=txgbuoqwcp3nukwy62iqbmwnsi0jeasg.lambda-url.us-east-2.on.aws, x-forwarded-port=443, content-type=application/json, x-forwarded-for=24.17.173.226, user-agent=Java-http-client/23.0.1}, isBase64Encoded=false, rawPath=/, routeKey=$default, requestContext={accountId=anonymous, apiId=txgbuoqwcp3nukwy62iqbmwnsi0jeasg, domainName=txgbuoqwcp3nukwy62iqbmwnsi0jeasg.lambda-url.us-east-2.on.aws, domainPrefix=txgbuoqwcp3nukwy62iqbmwnsi0jeasg, http={method=POST, path=/, protocol=HTTP/1.1, sourceIp=24.17.173.226, userAgent=Java-http-client/23.0.1}, requestId=fed29e7c-5b0b-4f4a-8030-a7a166498dd4, routeKey=$default, stage=$default, time=08/Jan/2025:21:21:29 +0000, timeEpoch=1736371289433}, body={\\"name\\":\\"India\\"}, version=2.0, rawQueryString=}",
                      "level": "UNDEFINED",
                      "AWSRequestId": "fed29e7c-5b0b-4f4a-8030-a7a166498dd4"
                  }
                ]
                """;

        ObjectMapper objectMapper = new ObjectMapper();
        List<LogEntry> logs = objectMapper.readValue(
                json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, LogEntry.class)
        );

        for (LogEntry log : logs) {
            if (log instanceof PlatformLog platformLog) {
                System.out.println("Time: " + platformLog.time());
                System.out.println("Type: " + platformLog.type());
                var record = platformLog.record();

                if (record instanceof PlatformLog.InitStartRecord init) {
                    System.out.println("  [initStart] initializationType = " + init.initializationType());
                } else if (record instanceof PlatformLog.StartRecord start) {
                    System.out.println("  [start] requestId = " + start.requestId());
                } else if (record instanceof PlatformLog.ReportRecord report) {
                    System.out.println("  [report] status = " + report.status());
                }
            } else if (log instanceof MessageLog messageLog) {
                System.out.println("Timestamp: " + messageLog.timestamp());
                System.out.println("Level: " + messageLog.level());
                System.out.println("RequestId: " + messageLog.AWSRequestId());
                System.out.println("Message: " + messageLog.message());
            }

            System.out.println("------------------------------------------------------");
        }
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.DEDUCTION
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(PlatformLog.class),
            @JsonSubTypes.Type(MessageLog.class)
    })
    sealed interface LogEntry permits PlatformLog, MessageLog {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MessageLog(
            String timestamp,
            String message,
            String level,
            String AWSRequestId
    ) implements LogEntry {
    }

    sealed interface BaseRecord
            permits PlatformLog.InitStartRecord, PlatformLog.StartRecord, PlatformLog.ReportRecord {
    }

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
    ) implements LogEntry {

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

        @JsonTypeName("platform.start")
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record StartRecord(
                String requestId,
                String version
        ) implements BaseRecord {
        }

        @JsonTypeName("platform.report")
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ReportRecord(
                String requestId,
                Metrics metrics,
                String status
        ) implements BaseRecord {

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