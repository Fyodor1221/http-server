package ru.netology.server;

//import org.apache.http.Header;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private final ExecutorService threadPool = Executors.newFixedThreadPool(64);
    private static final ConcurrentMap<String, ConcurrentMap<String, Handler>> handlers = new ConcurrentHashMap<>();

    public void listen(int port) {
        System.out.println("Current Handlers: \n" + handlers);
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
            System.out.println("Parsing request...");
            Request request = new Request();
            try {
                request = parseRequest(in, request);
            } catch (IOException e) {
                out.write(e.getMessage().getBytes());
                out.flush();
                socket.close();
            }
            System.out.println("Parsed request:");
            System.out.println(request.toString());

            handlers.get(request.getMethod()).get(request.getPath()).handle(request, out);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Request parseRequest(BufferedReader in, Request request) throws IOException {
        final var requestLine = in.readLine();
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

        String nextLine;
        while (true) {
            nextLine = in.readLine();
            if (nextLine.equals("")) {
                break;
            }
            request.addHeader(nextLine);
        }

        if (!request.getMethod().equals("GET")) {
            request.setBody(in.readLine());
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
                throw new IOException("Handler for " + method + "->" + path + " is already exist");
            }
        }
    }

    public List<String> getValidPaths() {
        return validPaths;
    }
}
