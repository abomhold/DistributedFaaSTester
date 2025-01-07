import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class execute {

    public static void main(String[] args) throws Exception {
        URL url = new URL("https://jsonplaceholder.typicode.com/posts");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Set request method and properties
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        // Read the response
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(response.toString());
    }
}

