package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import saaf.Response;

import java.util.HashMap;
import java.util.Map;

public class Main implements RequestHandler<HashMap<String, Object>, HashMap<String, Object>> {

    private static void parseBody(Map<String, Object> request) {
        try {
            new ObjectMapper()
                    .readValue(request.remove("body").toString(), new TypeReference<Map<String, String>>() {
                    })
                    .entrySet()
                    .stream()
                    .forEach(e -> request.put(e.getKey(), e.getValue()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, Object> handleRequest(HashMap<String, Object> request, Context context) {
        ObjectMapper om = new ObjectMapper();
        Map<String,Object> eventlog = Map.of(
                "payload", request.get("body").toString(),
                "nodeId", context.getClientContext().getEnvironment()
        );
        om.writerWithDefaultPrettyPrinter().writeValue();
        context.getLogger().log("payload" + request.get("body").toString() + "" + );
        return new HashMap<>(Map.of("requestId", context.getAwsRequestId()));
    }
}

//        //Collect inital data
//        Inspector inspector = new Inspector();
//        inspector.inspectContainer();
//        inspector.inspectCPU();
//        inspector.inspectLinux();
//        inspector.inspectMemory(
//        inspector.consumeResponse(response);
//        inspector.inspectAllDeltas();

//        Response response = new Response();
//        response.setValue("Hello " + request.get("name") + "! This is from a response object!");
//        if (request.get("body") != null) {
//            parseBody(request);
//        }
//        Response response = new Response();
//        response.setValue("Hello " + request.get("name") + "! This is from a response object!");
