package org.slieb.sparks;


import spark.Request;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SparkUtils {


    public static String getContentType(Request request) throws IOException {
        String path = request.pathInfo();
        if (path.endsWith("/")) {
            return "text/html";
        }
        return Files.probeContentType(Paths.get(path));
    }

    public static void addServerStopEndpoints(SparkWrapper wrapper, String path) {
        wrapper.get(path, SparkRoutes.stopGetRoute(path));
        wrapper.post(path, SparkRoutes.stopPostRoute(wrapper));
    }


}
