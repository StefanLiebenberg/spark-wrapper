package org.slieb.sparks;

import org.apache.commons.io.IOUtils;
import spark.Route;

import javax.servlet.ServletOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Function;

import static org.slieb.sparks.Sparks.getContentType;

public class SparkRoutes {

    public static Route binaryRoute(final Function<String, Optional<InputStream>> inputStreamFunction) {
        return ((request, response) -> {
            response.type(getContentType(request));
            Optional<InputStream> optionalInputStream = inputStreamFunction.apply(request.pathInfo());
            if (optionalInputStream.isPresent()) {
                try (final InputStream inputStream = optionalInputStream.get();
                     final OutputStream outputStream = response.raw().getOutputStream()) {
                    IOUtils.copy(inputStream, outputStream);
                }
                return "";
            } else {
                return null;
            }
        });
    }


    public static Route stopGetRoute(String path) {
        return (request, response) -> "<form method=POST action=\"" + path + "\"><input type=\"submit\" " +
                "value=\"Stop Server\"></form>";
    }

    public static Route stopPostRoute(VoidFunction stopAction) {
        return (request, response) -> {
            try (ServletOutputStream outputStream = response.raw().getOutputStream()) {
                outputStream.print("Server has been stopped");
            }
            response.raw().flushBuffer();
            stopAction.apply();
            return "";
        };
    }


    @FunctionalInterface
    public interface VoidFunction {

        void apply();

    }
}
