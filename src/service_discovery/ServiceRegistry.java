package service_discovery;

import java.util.HashMap;
import java.util.Map;

/**
 * The ServiceRegistry stores registered services' addresses and provides
 * methods for registration, discovery, and un-registration.
 */
public class ServiceRegistry {

    private static Map<String, String> registry = new HashMap<>();

    public void register(String name, String address) {
        registry.put(name, address);
    }

    public void unregister(String name) {
        registry.remove(name);
    }

    public String getAddress(String name) {
        return registry.get(name);
    }

    private static ServiceRegistry instance;

    /** Create a singleton instance of the service registry. */
    public static ServiceRegistry getInstance() {
        if (instance == null) {
            instance = new ServiceRegistry();
        }
        return instance;
    }

}
