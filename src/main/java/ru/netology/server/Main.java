package ru.netology.server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();

        try {
            server.addHandler("GET", "/messages", (request, responseStream) -> {
                // TODO: handlers code
            });
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        try {
            server.addHandler("POST", "/messages", (request, responseStream) -> {
                // TODO: handlers code
            });
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        server.listen(9999);}
}


