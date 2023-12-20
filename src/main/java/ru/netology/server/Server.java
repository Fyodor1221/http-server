package ru.netology.server;

import org.apache.http.Header;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Server {
    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private final ExecutorService threadPool = Executors.newFixedThreadPool(64);
    private static ConcurrentMap<String, ConcurrentMap<String, Handler>> handlers = new ConcurrentHashMap<>();

    public void listen(int port) {

        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                threadPool.submit(() -> awaitConnection(socket));
            }
        } catch (IOException e) {
            threadPool.shutdown();
            e.printStackTrace();
        }
    }

    public void awaitConnection(Socket socket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            System.out.println(Thread.currentThread());
            in.lines().forEach(System.out::println);
            System.out.println(parseRequest(in).toString());

            Request request = new Request();
            try {
                request = parseRequest(in);
            } catch (IOException e) {
                out.write(e.getMessage().getBytes());
                out.flush();
                socket.close();
            }
            handlers.get(request.getMethod()).get(request.getPath()).handle(request, out);

//            final var filePath = Path.of(".", "public", path);
//            final var mimeType = Files.probeContentType(filePath);
//
//
//            // special case for classic
//            if (path.equals("/classic.html")) {
//                final var template = Files.readString(filePath);
//                final var content = template.replace(
//                        "{time}",
//                        LocalDateTime.now().toString()
//                ).getBytes();
//                out.write((
//                        "HTTP/1.1 200 OK\r\n" +
//                                "Content-Type: " + mimeType + "\r\n" +
//                                "Content-Length: " + content.length + "\r\n" +
//                                "Connection: close\r\n" +
//                                "\r\n"
//                ).getBytes());
//                out.write(content);
//                out.flush();
//                socket.close();
//            }
//
//            final var length = Files.size(filePath);
//            out.write((
//                    "HTTP/1.1 200 OK\r\n" +
//                            "Content-Type: " + mimeType + "\r\n" +
//                            "Content-Length: " + length + "\r\n" +
//                            "Connection: close\r\n" +
//                            "\r\n"
//            ).getBytes());
//            Files.copy(filePath, out);
//            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Request parseRequest(BufferedReader in) throws IOException {
        var request = new Request();
        final var requestLines = in.lines().collect(Collectors.toList());
        final var requestLine = requestLines.get(0);
        final var parts = requestLine.split(" ");
        // must be in form GET /path HTTP/1.1
        if (parts.length != 3) {
            throw new IOException("Incorrect request line");
        }
        if (!validPaths.contains(parts[1])) {
            throw new IOException("HTTP/1.1 404 Not Found\r\n" +
                    "Content-Length: 0\r\n" +
                    "Connection: close\r\n" +
                    "\r\n");
        }
        request.setMethod(parts[0]);
        request.setPath(parts[1]);
        request.setProtocol(parts[2]);

        for (int i = 1; i < requestLines.size(); i++) {
            if (requestLines.get(i).equals("\r\n")) {
                break;
            }
            var headerParts = requestLines.get(i).split(": ");
            request.addHeader(headerParts[0], headerParts[1]);
        }

        final var lastLine = requestLines.get(requestLines.size() - 1);
        if (!lastLine.equals("\r\n\r\n")) {
            request.setBody(lastLine);
        }

        return request;
    }

    public void addHandler(String method, String path, Handler handler) throws IOException {
        if (!handlers.containsKey(method)) {
            var paths = new ConcurrentHashMap<String, Handler>();
            paths.put(path, handler);
            handlers.put(method, paths);
        } else {
            if (!handlers.get(method).containsKey(path)) {
                handlers.get(method).put(path, handler);
            } else {
                throw new IOException("this Handler is already exist");
            }
        }
    }
}
