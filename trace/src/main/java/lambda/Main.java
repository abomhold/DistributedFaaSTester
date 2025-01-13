/**
 * AWS Lambda function that logs incoming API Gateway requests with client IP information.
 * <p>
 * This lambda acts as a request logging middleware, capturing:
 * - Client IP address (from x-forwarded-for header)
 * - Full request payload (from request body)
 * <p>
 * Input:
 * - Expects an API Gateway proxy integration request object (aws default)
 * - Request body should be a valid JSON object
 * <p>
 * Output:
 * - Returns the parsed request body unchanged
 * - Logs a JSON object containing:
 *   {
 *     "clientIp": "<IP from x-forwarded-for header or 'unknown'>",
 *     "payload": <parsed request body object>
 *   }
 * <p>
 * Error Handling:
 * - Throws RuntimeException if request body parsing fails
 * - Throws RuntimeException if unable to serialize log output
 */
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


        // Extract client IP from http headers
        Map<String, Object> headers = (Map<String, Object>) request.get("headers");
        String clientIp = headers != null && headers.get("x-forwarded-for") != null
                ? headers.get("x-forwarded-for").toString()
                : "unknown";

        // Parse request body
        HashMap<String, Object> payload = parseBody(request);

        // Log as JSON
        try {
            String jsonLog = objectMapper.writeValueAsString(new HashMap<>(Map.of(
                "clientIp", clientIp,
                "payload", payload)));
            context.getLogger().log(jsonLog);
        } catch (JsonProcessingException e) {
            context.getLogger().log("Error serializing trace details: " + e.getMessage());
            throw new RuntimeException("Failed to log trace details", e);
        }

        return payload;
    }
}
