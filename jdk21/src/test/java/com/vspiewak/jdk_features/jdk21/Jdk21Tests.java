package com.vspiewak.jdk_features.jdk21;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.SequencedCollection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class Jdk21Tests {

    @Test
    void canUseVirtualThreads() throws Exception {
        // create an ExecutorService that uses a new virtual thread per task
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Boolean>> results = new ArrayList<>();

            // submit 10 tasks that check they're running on a virtual thread
            for (int i = 0; i < 10; i++) {
                results.add(executor.submit(() -> Thread.currentThread().isVirtual()));
            }

            // all tasks should report true
            for (Future<Boolean> f : results) {
                assertThat(f.get(1, TimeUnit.SECONDS)).isTrue();
            }
        }
    }

    @Test
    void canUseVirtualThreadDirectly() throws InterruptedException {
        // start a virtual thread directly
        Thread vt = Thread.startVirtualThread(() -> {
            // simulate work
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        });

        vt.join();  // wait for it to finish

        // verify it's indeed a virtual thread
        assertThat(vt.isVirtual()).isTrue();
    }

    // JEP 400: UTF-8 by Default
    @Test
    void canUseUtf8ByDefault() {
        assertThat(Charset.defaultCharset()).isEqualTo(StandardCharsets.UTF_8);
    }

    // JEP 408: Simple Web Server
    @Test
    void canUseFeatureSimpleWebServer() throws Exception {

        Path dir = Files.createTempDirectory("web");
        Path index = dir.resolve("index.html");
        Files.writeString(index, "<h1>Hello, Java 21</h1>");

        InetSocketAddress addr = new InetSocketAddress("localhost", 8080);

        HttpServer server = SimpleFileServer.createFileServer(addr, dir, SimpleFileServer.OutputLevel.NONE);
        server.start();
        try {
            int port = server.getAddress().getPort();
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:" + port + "/index.html"))
                    .GET()
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            assertThat(resp.statusCode()).isEqualTo(200);
            assertThat(resp.body()).contains("Hello, Java 21");
        } finally {
            server.stop(0);
        }

    }

    // JEP 431: Sequenced Collections
    @Test
    void canUseFeatureSequencedCollections() {
        List<String> list = List.of("a", "b", "c");
        assertThat(list).isInstanceOf(SequencedCollection.class);
        SequencedCollection<String> seq = (SequencedCollection<String>) list;
        assertThat(seq.getFirst()).isEqualTo("a");
        assertThat(seq.getLast()).isEqualTo("c");
    }

    @Test
    void canUseFeatureRecordPatterns() {
        Object p = new Pair(5, 7);
        String res = switch (p) {
            case String s -> s;
            case Pair(int a, int b) -> "x=" + a + ",y=" + b;
            default -> "none";
        };
        assertThat(res).isEqualTo("x=5,y=7");
    }

    // JEP 441: Pattern Matching for switch
    @Test
    void canUseFeaturePatternMatchingForSwitch() {
        Object o = "hello";
        String r = switch (o) {
            case String s -> s.toUpperCase();
            default -> "";
        };
        assertThat(r).isEqualTo("HELLO");
    }

    record Pair(int x, int y) {
    }

}