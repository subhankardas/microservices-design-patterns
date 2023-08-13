package service_discovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class ConsumerService {

    private static final String SERVICE_NAME = "provider-service";
    private static final String SERVICE_ADDR = "localhost";
    private static final int SERVICE_PORT = 8080;

    public static void main(String[] args) throws UnknownHostException, IOException {

        // 1. Create service registry instance and get service address
        ServiceRegistry registry = ServiceRegistry.getInstance();
        registry.register(SERVICE_NAME, SERVICE_ADDR + ":" + SERVICE_PORT); // Simulate service registration
        String address = registry.getAddress(SERVICE_NAME);

        // 2. Connect to the service
        if (address != null) {
            String host = address.split(":")[0];
            int port = Integer.parseInt(address.split(":")[1]);

            try (Socket socket = new Socket(host, port)) {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println("Received: " + msg);
                }
            }
        } else {
            System.out.println("Service not found: " + SERVICE_NAME);
        }

    }

}
