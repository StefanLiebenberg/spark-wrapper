package org.slieb.sparks;


import spark.Route;
import spark.RouteImpl;
import spark.SparkServer;
import spark.route.HttpMethod;
import spark.route.SimpleRouteMatcher;
import spark.ssl.SslStores;
import spark.webserver.JettyHandler;
import spark.webserver.JettySparkServer;
import spark.webserver.MatcherFilter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static org.slieb.sparks.SparkRoutes.wrap;

public class SparkWrapper implements AutoCloseable {


    private final SimpleRouteMatcher routeMatcher;
    private final CountDownLatch latch;

    private final String ipAddress;
    private final int port;
    private final String staticFileFolder;
    private final String externalStaticFileFolder;

    private final int maxThreads;
    private final int minThreads;
    private final int threadIdleTimeoutMillis;
    private final boolean hasMultipleHandler;

    private SparkServer sparkServer;
    private boolean initialized = false;
    private boolean stopped = false;
    private SslStores sslStores;
    private Map<String, Class<?>> webSocketHandlers;
    private Integer webSocketIdleTimeoutMillis;

    public SparkWrapper(String ipAddress,
                        int port,
                        String staticFileFolder,
                        String externalStaticFileFolder,
                        int maxThreads,
                        int minThreads,
                        int threadIdleTimeoutMillis,
                        boolean hasMultipleHandler,
                        SslStores sslStores,
                        Map<String, Class<?>> webSocketHandlers,
                        Integer webSocketIdleTimeoutMillis,
                        SimpleRouteMatcher simpleRouteMatcher,
                        CountDownLatch countDownLatch) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.staticFileFolder = staticFileFolder;
        this.externalStaticFileFolder = externalStaticFileFolder;
        this.maxThreads = maxThreads;
        this.minThreads = minThreads;
        this.threadIdleTimeoutMillis = threadIdleTimeoutMillis;
        this.hasMultipleHandler = hasMultipleHandler;
        this.sslStores = sslStores;
        this.webSocketHandlers = webSocketHandlers;
        this.webSocketIdleTimeoutMillis = webSocketIdleTimeoutMillis;
        this.routeMatcher = simpleRouteMatcher;
        this.latch = countDownLatch;
    }

    public SparkWrapper(String ipAddress,
                        int port,
                        String staticFileFolder,
                        String externalStaticFileFolder,
                        int maxThreads,
                        int minThreads,
                        int threadIdleTimeoutMillis,
                        boolean hasMultipleHandler,
                        SslStores sslStores,
                        Map<String, Class<?>> webSocketHandlers,
                        Integer webSocketIdleTimeoutMillis) {
        this(ipAddress, port, staticFileFolder, externalStaticFileFolder, maxThreads, minThreads,
             threadIdleTimeoutMillis, hasMultipleHandler, sslStores, webSocketHandlers, webSocketIdleTimeoutMillis,
             new SimpleRouteMatcher(), new CountDownLatch(1));
    }

    public SparkWrapper(String ipAddress, int port) {
        this(ipAddress, port, null, null, -1, -1, -1, false, null, null, null);


    }

    private synchronized void init() {
        if (!initialized) {
            new Thread(() -> {
                MatcherFilter matcherFilter = new MatcherFilter(routeMatcher, false, hasMultipleHandler);
                matcherFilter.init(null);
                JettyHandler handler = new JettyHandler(matcherFilter);
                sparkServer = new JettySparkServer(handler);
                sparkServer.ignite(this.ipAddress, this.port, this.sslStores, this.staticFileFolder,
                                   this.externalStaticFileFolder, this.latch, this.maxThreads, this.minThreads,
                                   this.threadIdleTimeoutMillis, this.webSocketHandlers,
                                   Optional.ofNullable(webSocketIdleTimeoutMillis));
            }).start();
            initialized = true;
        }
    }


    private static String getRouteString(final HttpMethod method, final String path) {
        return String.format("%s '%s'", method.name(), path);

    }


    public void parse(String routeString, RouteImpl route) {
        routeMatcher.parseValidateAddRoute(routeString, route.getAcceptType(), route);
    }

    public void get(String path, Route route) {
        init();
        parse(getRouteString(HttpMethod.get, path), wrap(path, route));
    }

    public void post(String path, Route route) {
        init();
        parse(getRouteString(HttpMethod.post, path), wrap(path, route));
    }


    public boolean isInitialized() {
        return initialized;
    }

    public boolean isStopped() {
        return stopped;
    }


    public void awaitInitialisation() {
        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void close() {
        if (!stopped && initialized) {
            sparkServer.stop();
        }
        stopped = true;
    }
}
