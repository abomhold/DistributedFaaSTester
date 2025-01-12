package collect;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;




@JsonIgnoreProperties(ignoreUnknown = true)
public class LogEvent {

    @JsonProperty("EventTime")
    public long EventTime;

    @JsonProperty("time")
    public String time;

    @JsonProperty("type")
    public String type;

    @JsonProperty("record")
    public Record record;

    @JsonProperty("timestamp")
    public String timestamp;

    @JsonProperty("message")
    public String message;

    @JsonProperty("level")
    public String level;

    @JsonProperty("AWSRequestId")
    public String awsRequestId;


    public static class Record {
        @JsonProperty("requestId")
        public String requestId;

        @JsonProperty("version")
        public String version;

        @JsonProperty("metrics")
        public Metrics metrics;

        @JsonProperty("status")
        public String status;

        @JsonProperty("initializationType")
        public String initializationType;

        @JsonProperty("phase")
        public String phase;

        @JsonProperty("runtimeVersion")
        public String runtimeVersion;

        @JsonProperty("runtimeVersionArn")
        public String runtimeVersionArn;

        @JsonProperty("functionName")
        public String functionName;

        @JsonProperty("functionVersion")
        public String functionVersion;

        @JsonProperty("instanceId")
        public String instanceId;

        @JsonProperty("instanceMaxMemory")
        public Long instanceMaxMemory;

        public static class Metrics {

            @JsonProperty("durationMs")
            public Double durationMs;

            @JsonProperty("billedDurationMs")
            public Integer billedDurationMs;

            @JsonProperty("memorySizeMB")
            public Integer memorySizeMB;

            @JsonProperty("maxMemoryUsedMB")
            public Integer maxMemoryUsedMB;

            @JsonProperty("initDurationMs")
            public Double initDurationMs;
        }
    }
}