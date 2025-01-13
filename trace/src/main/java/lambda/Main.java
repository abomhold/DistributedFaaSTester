package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class Main implements RequestHandler<HashMap<String, Object>, HashMap<String, Object>> {

    private static HashMap<String, Object> parseBody(Map<String, Object> request) {
        HashMap<String, Object> payload;
        try {
            payload = new ObjectMapper().readValue(
                    request.remove("body").toString(),
                    new TypeReference<>() {}
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing request body: " + e.getMessage(), e);
        }
        return payload;
    }

    @Override
    public HashMap<String, Object> handleRequest(HashMap<String, Object> request, Context context) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> headers = (Map<String, Object>) request.get("headers");

        // Extract client IP
        String clientIp = headers != null && headers.get("x-forwarded-for") != null
                ? headers.get("x-forwarded-for").toString()
                : "unknown";

        // Parse request body
        HashMap<String, Object> payload = parseBody(request);

        // Prepare trace details
        HashMap<String, Object> traceDetails = new HashMap<>(Map.of(
                "clientIp", clientIp,
                "payload", payload
        ));
//        String message = traceDetails.toString();
//        context.getLogger().log(message);
//        Log as JSON
        try {
            String jsonLog = objectMapper.writeValueAsString(traceDetails);
            context.getLogger().log(jsonLog);
        } catch (JsonProcessingException e) {
            context.getLogger().log("Error serializing trace details: " + e.getMessage());
            throw new RuntimeException("Failed to log trace details", e);
        }

        // Return trace details
        return traceDetails;
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
