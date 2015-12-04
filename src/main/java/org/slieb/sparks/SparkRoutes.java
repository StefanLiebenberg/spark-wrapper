package org.slieb.sparks;

import org.apache.commons.io.IOUtils;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.RouteImpl;

import javax.servlet.ServletOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Function;

import static org.slieb.sparks.SparkUtils.getContentType;

public class SparkRoutes {

    protected static final String DEFAULT_ACCEPT_TYPE = "*/*";

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

    public static Route stopPostRoute(SparkWrapper wrapper) {
        return (request, response) -> {
            try (ServletOutputStream outputStream = response.raw().getOutputStream()) {
                outputStream.print("Server has been stopped");
            }
            response.raw().flushBuffer();
            wrapper.close();
            return "";
        };
    }


    /**
     * Wraps the route in RouteImpl
     *
     * @param path  the path
     * @param route the route
     * @return the wrapped route
     */
    protected static RouteImpl wrap(final String path, final Route route) {
        return wrap(path, DEFAULT_ACCEPT_TYPE, route);
    }

    /**
     * Wraps the route in RouteImpl
     *
     * @param path       the path
     * @param acceptType the accept type
     * @param route      the route
     * @return the wrapped route
     */
    protected static RouteImpl wrap(final String path, final String acceptType, final Route route) {
        return new RouteImpl(path, acceptType != null ? acceptType : DEFAULT_ACCEPT_TYPE) {
            @Override
            public Object handle(final Request request, final Response response) throws Exception {
                return route.handle(request, response);
            }
        };
    }


}
