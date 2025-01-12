package collect;

import java.util.List;

class FunctionCall implements Comparable<FunctionCall> {
    String requestId;
    String source;
    List<String> requests;
    private long startTime = 0L;
    private long endTime = 0L;

    public FunctionCall(String requestId, String source) {
        this.requestId = requestId;
        this.source = source;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getDelay() {
        return endTime - startTime;
    }

    @Override
    public int compareTo(FunctionCall o) {
        return (int) (o.startTime - this.startTime);
    }
}
