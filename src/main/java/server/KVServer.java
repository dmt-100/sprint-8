package main.java.server;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Постман: https://www.getpostman.com/collections/a83b61d9e1c81c10575c
 */

/*
Из ТЗ: Подсказка: как работает сервер KVServer
KVServer — это хранилище, где данные хранятся по принципу <ключ-значение>. Он умеет:
GET /register — регистрировать клиента и выдавать уникальный токен доступа (аутентификации).
Это нужно, чтобы хранилище могло работать сразу с несколькими клиентами.
POST /save/<ключ>?API_TOKEN= — сохранять содержимое тела запроса, привязанное к ключу.
GET /load/<ключ>?API_TOKEN= — возвращать сохранённые значение по ключу.
 */
public class KVServer {
    public static final int PORT = 8078;
    private final String apiToken;
    private final HttpServer httpServer;
    private final Map<String, String> data = new HashMap<>();

    public KVServer() throws IOException {
        apiToken = generateApiToken();
        httpServer = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        httpServer.createContext("/register", this::register);
        httpServer.createContext("/save", this::save);
        httpServer.createContext("/load", this::load);
    }

/*
Вам нужно дописать реализацию запроса load() — это метод, который отвечает за получение данных.
После этого запустите сервер и проверьте, что получение значения по ключу работает.
Для начальной отладки можно делать запросы без авторизации, используя код DEBUG.
 */
    // Вопрос "это метод, который отвечает за получение данных." почему тогда void??
    private void load(HttpExchange exchange) throws IOException {
        // TODO Добавьте получение значения по ключу
        try {
            System.out.println("\n/load");
            if (!hasAuth(exchange)) {
                System.out.println("Запрос неавторизован, нужен параметр в query API_TOKEN со значением апи-ключа");
                exchange.sendResponseHeaders(403, 0);
                return;
            }
            if ("GET".equals(exchange.getRequestMethod())) {
                String key = exchange.getRequestURI().getPath().substring("/load/".length());
                if (key.isEmpty()) {
                    System.out.println("!400: /load/{key}");
                    exchange.sendResponseHeaders(400, 0);
                    return;
                }
                String value = readBody(exchange);
                if (value.isEmpty()) {
                    System.out.println("!400");
                    exchange.sendResponseHeaders(400, 0);
                    return;
                }
//------------------------------------
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(apiToken.getBytes());
                }
//------------------------------------
                System.out.println("Значение для ключа " + key + " успешно загружено!");
                exchange.sendResponseHeaders(200, 0);
            } else {
                System.out.println("/load ждёт POST-запрос, а получил: " + exchange.getRequestMethod());
                exchange.sendResponseHeaders(405, 0);
            }
        } finally {
            exchange.close();
        }

    }


    private void save(HttpExchange h) throws IOException {
        try {
            System.out.println("\n/save");
            if (!hasAuth(h)) {
                System.out.println("Запрос неавторизован, нужен параметр в query API_TOKEN со значением апи-ключа");
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

    private void register(HttpExchange h) throws IOException {
        try {
            System.out.println("\n/register");
            if ("GET".equals(h.getRequestMethod())) {
                sendText(h, apiToken);
            } else {
                System.out.println("/register ждёт GET-запрос, а получил " + h.getRequestMethod());
                h.sendResponseHeaders(405, 0);
            }
        } finally {
            h.close();
        }
    }

    public void start() {
        System.out.println("Запускаем сервер на порту " + PORT);
        System.out.println("Открой в браузере http://localhost:" + PORT + "/");
        System.out.println("API_TOKEN: " + apiToken);
        httpServer.start();
    }

    private String generateApiToken() {
        return "" + System.currentTimeMillis();
    }

    protected boolean hasAuth(HttpExchange h) {
        String rawQuery = h.getRequestURI().getRawQuery();
        return rawQuery != null && (rawQuery.contains("API_TOKEN=" + apiToken) || rawQuery.contains("API_TOKEN=DEBUG"));
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

    public String getApiToken() {
        return apiToken;
    }

    public HttpServer getHttpServer() {
        return httpServer;
    }
}
