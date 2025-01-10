package collect;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
            if (log instanceof PlatformInitStartLog initStartLog) {
                System.out.println("  [initStart]" + initStartLog);
            } else if (log instanceof PlatformStartLog startLog) {
                System.out.println("  [start]" + startLog);
            } else if (log instanceof PlatformReportLog reportLog) {
                System.out.println("  [report]" + reportLog);
            } else if (log instanceof MessageLog messageLog) {
                System.out.println("Message: " + messageLog);
            }
        }
    }
    record Message(
            String initializationType,
            String phase,
            String runtimeVersion,
            String runtimeVersionArn,
            String functionName,
            String functionVersion,
            String instanceId,
            long instanceMaxMemory,
            String requestId,
            String version,
            String status
    ){}

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type",
            defaultImpl = MessageLog.class // Use MessageLog as the default when `type` is missing
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = PlatformInitStartLog.class, name = "platform.initStart"),
            @JsonSubTypes.Type(value = PlatformStartLog.class, name = "platform.start"),
            @JsonSubTypes.Type(value = PlatformReportLog.class, name = "platform.report"),
            @JsonSubTypes.Type(value = MessageLog.class, name = "message")
    })
    sealed interface LogEntry permits PlatformInitStartLog, PlatformStartLog, PlatformReportLog, MessageLog {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MessageLog(
            String timestamp,
            String message,
            String level,
            String AWSRequestId
    ) implements LogEntry {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PlatformInitStartLog(
            String time,
            String type,
            InitStartRecord record
    ) implements LogEntry {

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
        ) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PlatformStartLog(
            String time,
            String type,
            StartRecord record
    ) implements LogEntry {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record StartRecord(
                String requestId,
                String version
        ) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PlatformReportLog(
            String time,
            String type,
            ReportRecord record
    ) implements LogEntry {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ReportRecord(
                String requestId,
                Metrics metrics,
                String status
        ) {

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
