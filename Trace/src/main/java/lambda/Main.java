package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import saaf.Inspector;
import saaf.Response;

import java.util.Arrays;
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
//        //Collect inital data
        Inspector inspector = new Inspector();
//        inspector.inspectContainer();
//        inspector.inspectCPU();
//        inspector.inspectLinux();
//        inspector.inspectMemory(

        if (request.get("body") != null) {
            parseBody(request);
        }


        Response response = new Response();
        response.setValue("Hello " + request.get("name") + "! This is from a response object!");
        inspector.consumeResponse(response);
//        inspector.inspectAllDeltas();
        return inspector.finish();
    }
}
