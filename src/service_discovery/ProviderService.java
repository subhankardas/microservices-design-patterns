package service_discovery;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ProviderService {

    private static final String SERVICE_NAME = "provider-service";
    private static final String SERVICE_ADDR = "localhost";
    private static final int SERVICE_PORT = 8080;

    public static void main(String[] args) throws IOException, InterruptedException {

        // 1. Create service registry instance and register the service
        ServiceRegistry registry = ServiceRegistry.getInstance();
        registry.register(SERVICE_NAME, SERVICE_ADDR + ":" + SERVICE_PORT);

        System.out.println("Provider service registered at " + SERVICE_ADDR + ":" + SERVICE_PORT);

        // 2. Create server
        try (ServerSocket server = new ServerSocket(SERVICE_PORT)) {
            System.out.println("Provider service listening at " + SERVICE_ADDR + ":" + SERVICE_PORT);

            Socket socket = server.accept();
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            for (int idx = 1; idx <= 5; idx++) {
                out.println("Message " + idx + " [" + SERVICE_NAME + "]");
                System.out.println("Sent: " + "Message " + idx);
                Thread.sleep(1000);
            }
            socket.close();
        }

    }

}
