package org.slieb.sparks;


import spark.Request;
import spark.Routable;
import spark.SparkInstance;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;

public class Sparks {

    public static String getContentType(Request request) throws IOException {
        String path = request.pathInfo();
        if (path.endsWith("/")) {
            return "text/html";
        }
        return Files.probeContentType(Paths.get(path));
    }


    public static void addServerStopEndpoints(Routable routable, String path, SparkRoutes.VoidFunction stopFunction) {
        routable.get(path, SparkRoutes.stopGetRoute(path));
        routable.post(path, SparkRoutes.stopPostRoute(stopFunction));
    }

    public static void addServerStopEndpoints(SparkInstance routable, String path) {
        addServerStopEndpoints(routable, path, routable::stop);
    }

    public static void addServerStopEndpoints(SparkWrapper routable, String path) {
        addServerStopEndpoints(routable.getSparkInstance(), path);
    }


    public static void addBinaryRoute(final Routable routable,
                                      final String path,
                                      final Function<String, Optional<InputStream>> inputStreamFunction) {
        routable.get(path, SparkRoutes.binaryRoute(inputStreamFunction));
    }


}
