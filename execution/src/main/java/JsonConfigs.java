import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

class JsonConfigs {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        List<Node> nodes = loadNodes("node.json");
        System.out.println("Loaded Nodes:");
        nodes.forEach(System.out::println);

        List<Map<String, Object>> payloads = loadPayloads("payload.json");
        System.out.println("Loaded Payloads:");
        payloads.forEach(System.out::println);
    }

    public static List<Node> loadNodes(String resourceName) {
        try (InputStream inputStream = JsonConfigs.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new RuntimeException("Resource not found: " + resourceName);
            }
            Map<String, List<Node>> root = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
            return root.getOrDefault("nodes", List.of()); // Get "nodes" or return an empty list
        } catch (Exception e) {
            throw new RuntimeException("Failed to load nodes from resource: " + resourceName, e);
        }
    }


    public static List<Map<String, Object>> loadPayloads(String resourceName) {
        try (InputStream inputStream = JsonConfigs.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new RuntimeException("Resource not found: " + resourceName);
            }
            Map<String, List<Map<String, Object>>> root = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
            return root.getOrDefault("payloads", List.of());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load payloads from resource: " + resourceName, e);
        }
    }

    public record Node(String id,
                       String ip,
                       String platform,
                       String nodeType,
                       int tcpPort,
                       int sshPort,
                       int numCores,
                       double weight) {
    }
}
