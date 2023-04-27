package main.java.server;

import main.java.service.ManagerSaveException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


// ТЗ-8: напишите HTTP-клиент. С его помощью мы переместим хранение состояния менеджера из файлов на отдельный сервер.

public class KVTaskClient {
    private final String apiToken;
    private URI uri;
 /*
 Из ТЗ-8 "Конструктор принимает URL к серверу хранилища и регистрируется. При регистрации выдаётся токен (API_TOKEN),
 который нужен при работе с сервером."
 Вопрос выдается клиенту?
"Также хорошо было бы сделать final, так как его инициализация будет происходить при вызове метода register в теле конструктора."
 Что-то я запутался с апи, это ваша фраза в этом классе, идет речь про метод register в классе KVServer?
 То есть его нужно оттуда как-то достать, вопрос как, и записать в поле клиента? Так как Класс KVServer уже готовый шаблон
  инициализация апи идет там через System.currentTimeMillis()
  Пока отправлю ТЗ в таком варианте
*/

    public KVTaskClient(URI uri){
        this.uri = uri;

//        apiToken = register();
    }

    public void put(String key, String json) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri + "save/+" + key + "?API_TOKEN=" + apiToken))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
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
                .uri(URI.create(uri + "save/+" + key + "?API_TOKEN=" + apiToken))
                .GET()
                .build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ManagerSaveException("Не удалось выполнить запрос. Код статуса: " +
                        response.statusCode());
            } else {
                return String.valueOf(response);
            }
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("Что-то не так в методе load");
        }
    }

//    public String register() {
//        return String.valueOf(java.util.UUID.randomUUID());
//    }

}
