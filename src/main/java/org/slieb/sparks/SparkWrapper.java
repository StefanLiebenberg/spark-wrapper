package org.slieb.sparks;


import spark.*;

import java.util.Map;

public class SparkWrapper extends Routable implements AutoCloseable {

    private final SparkInstance sparkInstance;

    public SparkWrapper(String ipAddress,
                        int port,
                        String staticFileFolder,
                        String externalStaticFileFolder,
                        int maxThreads,
                        int minThreads,
                        int threadIdleTimeoutMillis,
                        String keystoreFile,
                        String keystorePassword,
                        String truststoreFile,
                        String truststorePassword,
                        Map<String, Class<?>> webSocketHandlers,
                        Integer webSocketIdleTimeoutMillis) {
        sparkInstance = new SparkInstance();
        sparkInstance.ipAddress(ipAddress);
        sparkInstance.port(port);
        sparkInstance.staticFileLocation(staticFileFolder);
        sparkInstance.externalStaticFileLocation(externalStaticFileFolder);
        sparkInstance.threadPool(maxThreads, minThreads, threadIdleTimeoutMillis);
        if (keystoreFile != null || keystorePassword != null || truststoreFile != null || truststorePassword != null) {
            sparkInstance.secure(keystoreFile, keystorePassword, truststoreFile, truststorePassword);
        }
        if (webSocketHandlers != null) {
            webSocketHandlers.forEach(sparkInstance::webSocket);
            sparkInstance.webSocketIdleTimeoutMillis(webSocketIdleTimeoutMillis);
        }
    }


    public SparkWrapper(String ipAddress, int port) {
        this(ipAddress, port, null, null, -1, -1, -1, null, null, null, null, null, null);
    }

    @Override
    protected void addRoute(String httpMethod, RouteImpl route) {
        sparkInstance.addRoute(httpMethod, route);
    }

    @Override
    protected void addFilter(String httpMethod, FilterImpl filter) {
        sparkInstance.addFilter(httpMethod, filter);
    }

    @Override
    public void get(String path, Route route) {
        sparkInstance.get(path, route);
    }

    @Override
    public void post(String path, Route route) {
        sparkInstance.post(path, route);
    }

    public SparkInstance getSparkInstance() {
        return sparkInstance;
    }


    public void awaitInitialisation() {
        sparkInstance.awaitInitialization();
    }

    public void stop() {
        sparkInstance.stop();
    }

    @Override
    public void close() {
        this.stop();
    }
}
