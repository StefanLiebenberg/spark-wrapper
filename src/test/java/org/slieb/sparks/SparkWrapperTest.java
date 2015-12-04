package org.slieb.sparks;

import org.junit.Test;
import spark.utils.IOUtils;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;

import static org.junit.Assert.assertEquals;


@SuppressWarnings("unused")
public class SparkWrapperTest {

    private SparkWrapper createGreeter(final URL url,
                                             final String message) {
        SparkWrapper server = new SparkWrapper(url.getHost(), url.getPort());
        server.get(url.getPath(), (request, response) -> message);
        server.awaitInitialisation();
        return server;
    }


    @Test
    public void testCreateInstance() throws Exception {
        URL url = new URL("http://localhost:5043/greeting");
        try (SparkWrapper server = createGreeter(url, "hello");
             InputStream inputStream = url.openStream()) {
            assertEquals(IOUtils.toString(inputStream), "hello");
        }
    }

    @Test
    public void testDualCreateInstance() throws Exception {
        URL urlA = new URL("http://localhost:5051/greeting");
        URL urlB = new URL("http://localhost:5052/greeting");
        try (SparkWrapper serverA = createGreeter(urlA, "hiA");
             SparkWrapper serverB = createGreeter(urlB, "hiB");
             InputStream inputStreamA = urlA.openStream();
             InputStream inputStreamB = urlB.openStream()) {
            assertEquals(IOUtils.toString(inputStreamA), "hiA");
            assertEquals(IOUtils.toString(inputStreamB), "hiB");
        }
    }

    @Test(expected = ConnectException.class)
    public void testClosable() throws Exception {
        URL url = new URL("http://localhost:5050/greeting");
        try (SparkWrapper server = createGreeter(url, "hello")) {
            server.close();
        }
        url.openStream();
    }

}