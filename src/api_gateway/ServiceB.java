package api_gateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServiceB {

    private static final int PORT = 8082;

    public static void main(String[] args) throws IOException {

        // Create server and listen to port
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Service B listening on port " + PORT);

            while (true) {
                // Connection for receiving request
                Socket socketRead = server.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socketRead.getInputStream()));
                String request = in.readLine();
                System.out.println("Service B received request: " + request);
                socketRead.close();

                // Connection for sending response
                Socket socketWrite = server.accept();
                PrintWriter out = new PrintWriter(socketWrite.getOutputStream(), true);
                out.println("Response from Service B");
                socketWrite.close();
            }
        }

    }

}
