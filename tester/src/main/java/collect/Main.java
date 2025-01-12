package collect;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        var logPool = new LogPool(
                Region.US_EAST_2,
                LogGroup.builder().logGroupName("/trace").build(),
                1736517244238L,
                1736517245L);

        try {
            logPool.writeJson("out_simple.json");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

