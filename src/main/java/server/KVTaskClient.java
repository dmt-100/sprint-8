package main.java.server;

import com.google.gson.Gson;
import main.java.managers.Managers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

/*
из ТЗ: напишите HTTP-клиент. С его помощью мы переместим хранение состояния менеджера из файлов на отдельный сервер.
 */
public class KVTaskClient {
//--------------------------------------------------------------
/* Добрый день. ТЗ конечно не готово, так как у меня очень смутные представления как вообще должен работать этот класс,
да и как работает класс KVServer, даже благодаря этому слайду https://i.ibb.co/X3F4Ys8/image.png
Может быть ТЗ само не так описано в совокупе каких то пробелов в знаниях у меня, но точно нужна какая-то дополнительная помощь
В пачке посоветовали еще раз переписать ТЗ с начала, что бы лучше понять, но я в этом уже сомневаюсь что поможет

* из-за проблем с gson'ом при вытаскивании эпика (было два поля startTime и Task.startTime), пришлось переделать метод addTask по
добавлению id для эпика теперь эпик заменяется на новый с новым полем startTime от сабтаска вместо использования сеттеров.
 Там некоторые прошлые тесты надо будет переписать. Для меня сейчас важно понять как работать с API
*/
//--------------------------------------------------------------
    /*
    из Пачки: Руслан Родионов 12:07
        Можешь сохранить под ключом А значение В. Такая удалённая мапа. Как и что хранить там решай самостоятельно.
        Хранить отдельные задачи под ключами их id не совсем экономно с точки зрения трафика и времени получения данных.
     */

    KVServer kvServer;
    HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = Managers.getGson();
    private String API_TOKEN;
    private static URI BASE_URL = URI.create("http://localhost:8078/"); // здесь нужно указать URL сервера
//    HttpRequest request = HttpRequest.newBuilder().uri(BASE_URL).GET().build();
//    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    public KVTaskClient(URI uri) throws IOException, InterruptedException {
        BASE_URL = uri;
        new KVServer().start();
    }

// ТЗ-8 put должен сохранять состояние менеджера задач через запрос POST /save/<ключ>?API_TOKEN=
    public void put(String key, String json) throws IOException, InterruptedException {
        if (Objects.equals(key, API_TOKEN)) {
            API_TOKEN = kvServer.getApiToken();

            URI uri = URI.create("http://localhost:8080/load/key/" + API_TOKEN + "?API_TOKEN=" + API_TOKEN);

            var request = HttpRequest.newBuilder().uri(uri).POST(HttpRequest.BodyPublishers.ofString(json)).build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }

// ТЗ-8: Метод String load(String key) должен возвращать состояние менеджера задач через запрос GET /load/<ключ>?API_TOKEN=
    String load(String key) throws IOException, InterruptedException {
        API_TOKEN = kvServer.getApiToken();

//        URI uri = URI.create("http://localhost:8080/load/key/" + API_TOKEN + "?API_TOKEN=" + API_TOKEN);
        URI uri = URI.create("http://localhost:8080/load/key/" + API_TOKEN + "?API_TOKEN=" + API_TOKEN );
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getAPI_TOKEN() {
        return API_TOKEN;
    }
}
