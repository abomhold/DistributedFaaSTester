package collect;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;

public class Main {
    public static void main(String[] args) {
       var logPool = new LogPool(
               Region.US_EAST_2,
               LogGroup.builder().logGroupName("/trace").build(),
              1736317095000L,
               1736317098000L);
       for (var log : logPool.platformLogs){
           System.out.println(log);
       }
    }
}
