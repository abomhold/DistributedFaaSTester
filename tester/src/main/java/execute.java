import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class execute {

    public static void main(String[] args) {
        // Load nodes and payloads
        List<JsonConfigs.Node> nodes = JsonConfigs.loadNodes("node.json");
        List<Map<String, Object>> payloads = JsonConfigs.loadPayloads("payload.json");

        // Create HttpClient instance - remove the try-with-resources
        HttpClient client = HttpClient.newHttpClient();

        // Iterate through nodes and payloads
        nodes.stream()
             .filter(node -> !"controller".equals(node.id())) // Filter out nodes with id == "controller"
             .forEach(node -> {
                 for (Map<String, Object> payload : payloads) {
                     String jsonPayload = toJsonString(payload);
                     HttpRequest request = HttpRequest.newBuilder()
                                                      .uri(URI.create(node.ip())) // Use node.ip as the URL
                                                      .header("Content-Type", "application/json")
                                                      .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                                                      .build();


                     // Send asynchronous request
                     CompletableFuture<HttpResponse<String>> futureResponse =
                             client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

                     // Handle response asynchronously
                     futureResponse.thenAccept(response -> {
                         System.out.println("Response for Node: " + node.ip());
                         System.out.println("Status Code: " + response.statusCode());
                         System.out.println("Response Body: " + response.body());
                     }).exceptionally(e -> {
                         System.err.println("Error for Node: " + node.ip());
                         e.printStackTrace();
                         return null;
                     });
                 }
             });

        // Prevent the program from exiting immediately
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String toJsonString(Map<String, Object> payload) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert payload to JSON", e);
        }
    }
}

