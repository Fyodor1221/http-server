package ru.netology.server;

import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
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
//        System.out.println("Current Handlers: \n" + handlers);
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
//            System.out.println("Reading request...");
//            String line;
//            while ((line = in.readLine()) != null) {
//                System.out.println(line);
//            }
//            while (in.ready()) {
//                System.out.println(in.readLine());
//            }
//            System.out.println("Request has been read");
            System.out.println("Parsing request...");
            Request request = new Request();
            try {
                request = parseRequest(in, request);
            } catch (IOException e) {
                e.printStackTrace();
                out.write(e.getMessage().getBytes());
                out.flush();
                socket.close();
            }
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
        System.out.println(Arrays.toString(parts));
        // must be in form GET /path HTTP/1.1
        if (parts.length != 3) {
            throw new IOException("Incorrect request line");
        }

        request.setMethod(parts[0]);
        request.setProtocol(parts[2]);

        if (parts[1].contains("?")) {
            var url = parts[1].split("\\?");
            request.setPath(url[0]);
            request.addQueryParams(URLEncodedUtils.parse(url[1], StandardCharsets.UTF_8));
        } else {
            request.setPath(parts[1]);
        }

        if (!validPaths.contains(request.getPath())) {
            throw new IOException("HTTP/1.1 404 Not Found\r\n" +
                    "Content-Length: 0\r\n" +
                    "Connection: close\r\n" +
                    "\r\n");
        }

        String nextLine;
        var headers = new ArrayList<String>();
        while (true) {
            nextLine = in.readLine();
            if (nextLine.equals("")) {
                break;
            }
            headers.add(nextLine);
            request.addHeader(nextLine);
        }
        System.out.println(headers);

//        StringBuilder body = new StringBuilder();
//        while ((line = in.readLine()) != null) {
//            System.out.println("reading body...");
//            body.append(line);
//            request.setBody(body.toString());
//        }

//        if (in.ready()) {
//            System.out.println("We have a body");
//            request.setBody(in.readLine());
//        }
//        else {
//            System.out.println("We don't have a body");
//        }

//        if (in.ready()) {
//            StringBuilder bodyBuilder = new StringBuilder();
//            char[] cbuf = new char[4096];
//            while (in.read(cbuf) > 0) {
//                bodyBuilder.append(cbuf);
//                System.out.println(bodyBuilder);
//            }
//            request.setBody(bodyBuilder.toString());
//        }

        if (in.ready()) {
            System.out.println("yes body");
            StringBuilder bodyBuilder = new StringBuilder();
            int ch;
            while ((ch = in.read()) > 0) {
                bodyBuilder.append((char)ch);
                System.out.println(bodyBuilder);
            }
            request.setBody(bodyBuilder.toString());
        }
        else {
            System.out.println("no body");
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
