// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.DescribeLogStreamsRequest;
import com.amazonaws.services.logs.model.DescribeLogStreamsResult;
import com.amazonaws.services.logs.model.LogStream;
import com.amazonaws.services.logs.model.OrderBy;

/**
 * Lists CloudWatch subscription filters associated with a log group.
 */
public class DescribeSubscriptionFilters {

    public static void main(String[] args) {

        String log_group = "/trace";

        final AWSLogs logs = AWSLogsClientBuilder.defaultClient();
        boolean done = false;

        var request = new DescribeLogStreamsRequest(log_group).withOrderBy(OrderBy.LastEventTime);


        while (!done) {

            DescribeLogStreamsResult result = logs.describeLogStreams(request);
            for (LogStream stream : result.getLogStreams()) {
                System.out.println(stream.getLogStreamName());
            }

            request.setNextToken(result.getNextToken());

            if (result.getNextToken() == null) {
                done = true;
            }
        }
    }
}
