package api_gateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MockCaller {

    private static final int GATEWAY_PORT = 8080;

    public static void main(String[] args) throws IOException {

        // Simulate a request to Service A
        sendRequest(GATEWAY_PORT, "GET /serviceA Body: Hello!");

        // Simulate a request to Service B
        sendRequest(GATEWAY_PORT, "GET /serviceB Body: Hi!");
        
    }

    private static void sendRequest(int gatewayPort, String request) throws IOException {
        try (Socket socket = new Socket("localhost", GATEWAY_PORT)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send the request to the API gateway
            out.println(request);

            // Receive and print the response from the API gateway
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}
