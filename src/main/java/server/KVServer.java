package main.java.server;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import main.java.service.ManagerSaveException;
import main.java.tasks.Epic;
import main.java.tasks.Task;

/**
 * Постман: https://www.getpostman.com/collections/a83b61d9e1c81c10575c
 */


public class KVServer {
    public static final int PORT = 8078;
    private final String key;
    private final HttpServer httpServer;
    private final Map<String, String> data = new HashMap<>();
    List<String> tasks;
    List<String> epics;
    List<String> subtasks;
    List<String> history;

    public KVServer() throws IOException {
        key = generateApiToken();
        httpServer = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        httpServer.createContext("/register", this::register);
        httpServer.createContext("/save", this::save);
        httpServer.createContext("/load", this::load);
    }

    private void load(HttpExchange exchange) throws IOException { //это метод, который отвечает за получение данных.
        try {
            System.out.println("\n/load");
            if (!hasAuth(exchange)) {
                System.out.println("Запрос не авторизован, нужен параметр в query API_TOKEN со значением апи-ключа");
                exchange.sendResponseHeaders(403, 0);
                return;
            }

            if ("GET".equals(exchange.getRequestMethod())) {
                String key = exchange.getRequestURI().getPath().substring("/load/".length());

                if (key.isEmpty()) {
                    exchange.sendResponseHeaders(400, 0);
                    String response = "400 Bad Request";
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    return;
                }

                if (data.containsKey(key)) {
                    String responseData = data.get(key);
                    sendText(exchange, responseData); // *для себя: почему пропустил

                } else {
                    exchange.sendResponseHeaders(404, 0);
                    String response = "404 Not Found";
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                }

            } else {
                exchange.sendResponseHeaders(405, 0);
                String response = "405 Method Not Allowed";
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
            }
        } finally {
            exchange.close();
        }
    }


    private void save(HttpExchange h) throws IOException {
        try {
            System.out.println("\n/save");
            if (!hasAuth(h)) {
                System.out.println("Запрос не авторизован, нужен параметр в query API_TOKEN со значением апи-ключа");
                h.sendResponseHeaders(403, 0);
                return;
            }
            if ("POST".equals(h.getRequestMethod())) {
                String key = h.getRequestURI().getPath().substring("/save/".length());
                if (key.isEmpty()) {
                    System.out.println("Key для сохранения пустой. key указывается в пути: /save/{key}");
                    h.sendResponseHeaders(400, 0);
                    return;
                }

                String value = readBody(h);

                if (value.isEmpty()) {
                    System.out.println("Value для сохранения пустой. value указывается в теле запроса");
                    h.sendResponseHeaders(400, 0);
                    return;
                }

                data.put(key, value);

                System.out.println("Значение для ключа " + key + " успешно обновлено!");
                h.sendResponseHeaders(200, 0);
            } else {
                System.out.println("/save ждёт POST-запрос, а получил: " + h.getRequestMethod());
                h.sendResponseHeaders(405, 0);
            }
        } finally {
            h.close();
        }
    }

    private void register(HttpExchange h) {
        try (h) {
            System.out.println("\n/register");
            if ("GET".equals(h.getRequestMethod())) {
                sendText(h, key);
            } else {
                System.out.println("/register ждёт GET-запрос, а получил " + h.getRequestMethod());
                h.sendResponseHeaders(405, 0);
            }
        } catch (IOException e) {
            throw new ManagerSaveException(e);
        } finally {
            h.close();

        }
    }

    public void start() {
        System.out.println("Запускаем сервер на порту " + PORT);
        System.out.println("Открой в браузере http://localhost:" + PORT + "/");
        System.out.println("API_TOKEN: " + key);
        httpServer.start();
    }

    private String generateApiToken() {
        return "" + System.currentTimeMillis();
    }

    protected boolean hasAuth(HttpExchange h) {
        String rawQuery = h.getRequestURI().getRawQuery();
        return rawQuery != null && (rawQuery.contains("API_TOKEN=" + key) || rawQuery.contains("API_TOKEN=DEBUG"));
    }

    protected String readBody(HttpExchange h) throws IOException {
        return new String(h.getRequestBody().readAllBytes(), UTF_8);
    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
    }

}

/*
// ======================================= ПОДСКАЗКИ для меня =======================================



Из ТЗ: Подсказка: как работает сервер KVServer
KVServer — это хранилище, где данные хранятся по принципу <ключ-значение>. Он умеет:
GET /register — регистрировать клиента и выдавать уникальный токен доступа (аутентификации).
Это нужно, чтобы хранилище могло работать сразу с несколькими клиентами.
POST /save/<ключ>?API_TOKEN= — сохранять содержимое тела запроса, привязанное к ключу.
GET /load/<ключ>?API_TOKEN= —

ТЗ-8 Вам нужно дописать реализацию запроса load() — это метод, который отвечает за получение данных.
После этого запустите сервер и проверьте, что получение значения по ключу работает.
Для начальной отладки можно делать запросы без авторизации, используя код DEBUG.



// ==========================================================================================
 */