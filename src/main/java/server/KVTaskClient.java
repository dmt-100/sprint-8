package main.java.server;

import main.java.service.ManagerSaveException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    private final String tokenId;
    private final String uri;

    public KVTaskClient(URI uri){
        this.uri = String.valueOf(uri);
        tokenId = register();
    }

    public void put(String key, String json) {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri + "save/" + key + "?API_TOKEN=" + this.tokenId))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<Void> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() != 200) {
                throw new ManagerSaveException("Не удалось выполнить запрос. Код статуса: " +
                        response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("Что-то не так в методе put");
        }
    }

    public String load(String key) {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri + "load/" + key + "?API_TOKEN=" + tokenId))
                .GET()
                .build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ManagerSaveException("Не удалось выполнить запрос. Код статуса: " +
                        response.statusCode());
            } else {
                return response.body();
            }
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("Что-то не так в методе load");
        }
    }

    private String register() {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri + "register"))
                .GET()
                .build();

        HttpResponse<String> response;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ManagerSaveException("Не удалось выполнить запрос. Код статуса: " +
                        response.statusCode());
            } else {
                return response.body();
            }
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("Что-то не так в методе load");
        }
    }

}
