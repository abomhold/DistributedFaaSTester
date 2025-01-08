package execute;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Main {

    public static void main(String[] args) {
        List<JsonConfigs.Node> nodes = JsonConfigs.loadNodes("node.json");
        List<Map<String, Object>> payloads = JsonConfigs.loadPayloads("payload.json");
        HttpClient client = HttpClient.newHttpClient();

        payloads.forEach(payload -> {
            for (var node : nodes) {
                String jsonPayload = toJsonString(payload);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(node.ip()))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();


                CompletableFuture<HttpResponse<String>> futureResponse =
                        client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

                futureResponse.thenAccept(response -> {
                    System.out.println("Response for Node: " + node.id()
                            + "    Status Code: " + response.statusCode()
                            + "    Response Body: " + response.body());
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

//             .filter(node -> !"controller".equals(node.id()))
