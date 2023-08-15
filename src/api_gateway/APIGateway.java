package api_gateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class APIGateway {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {

        // Create gateway server and listen to port
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("API Gateway listening on port " + PORT);

            while (true) {
                Socket client = server.accept();
                new Thread(new APIGatewayHandler(client)).start(); // Launch thread to accept incoming requestsO
            }
        }

    }

}

class APIGatewayHandler extends Thread {

    private Socket client;

    public APIGatewayHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            // Read request from gateway client
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String request = in.readLine();

            System.out.println("API Gateway received request: " + request);

            // Get target service from request
            String targetService = getTargetService(request);

            // Forward request to target service
            forwardRequestToService(request, targetService);

            // Receive response from target service
            String response = receiveResponseFromService(targetService);

            // Write response to gateway client
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            out.println(response);

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Acts as service name resolver from incoming requests.
    private String getTargetService(String request) {
        if (request.contains("/serviceA")) {
            return "ServiceA";
        } else if (request.contains("/serviceB")) {
            return "ServiceB";
        }
        return null;
    }

    // Acts as service registry to return target service port.
    private int getTargetServicePort(String targetService) {
        switch (targetService) {
            case "ServiceA":
                return 8081;
            case "ServiceB":
                return 8082;
        }
        return 0;
    }

    // Writes request to the target service connection.
    private void forwardRequestToService(String request, String targetService)
            throws UnknownHostException, IOException {
        // Forward the request to the target service
        int targetPort = getTargetServicePort(targetService);
        Socket socketWrite = new Socket("localhost", targetPort);

        PrintWriter out = new PrintWriter(socketWrite.getOutputStream(), true);
        out.println(request);

        socketWrite.close();
    }

    // Reads request from the target service connection.
    private String receiveResponseFromService(String targetService) throws IOException {
        // Receive response from the target service
        int servicePort = getTargetServicePort(targetService);
        Socket socketRead = new Socket("localhost", servicePort);

        BufferedReader in = new BufferedReader(new InputStreamReader(socketRead.getInputStream()));
        String response = in.readLine();

        socketRead.close();
        return response;
    }

}