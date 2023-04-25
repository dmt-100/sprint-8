package main.java.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import main.java.intefaces.TasksManager;
import main.java.managers.Managers;
import main.java.service.TaskType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpTaskServer {
//    private final HttpClient client = HttpClient.newHttpClient();

    public static final int PORT = 8080;
    private final TasksManager fileBackedTaskManager = Managers.getDefaultFileBacked();
    private Gson gson;
    private HttpServer httpServer;

    public HttpTaskServer() throws IOException {
        gson = Managers.getGson();
        httpServer = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        httpServer.createContext("/tasks", this::handleTasks);
    }

    public TasksManager getFileBackedTaskManager() { // для тестов
        return fileBackedTaskManager;
    }

    private void handleTasks(HttpExchange httpExchange) {
        try {
            String path = httpExchange.getRequestURI().getPath();
            String uri = String.valueOf(httpExchange.getRequestURI());

//            int lastIndex = 0;
//            String type = null;
//            UUID uuid = null;
//            if (uri.contains("=")) {
//                lastIndex = uri.lastIndexOf('=');
//                type = uri.substring(lastIndex + 1);
//                uuid = UUID.fromString(type);
//            }

            String[] commands = uri.split("/");

            UUID uuid = null;
            String command = commands[2].toUpperCase();
            if (uri.contains("id")) {
                String[] sp = uri.split("=");
                uuid = UUID.fromString(sp[1]);
            }

            String requestMethod = httpExchange.getRequestMethod() + "_" + command.toUpperCase();

            if (uri.contains("id")) {
                requestMethod = httpExchange.getRequestMethod() + "_" + command.toUpperCase() + "_ID";
            }
            switch (requestMethod) {
                case "GET_TASK": {
                    if (Pattern.matches("^/tasks/task$", path)) {
                        String response = gson.toJson(fileBackedTaskManager.getAllTasks());
                        sendText(httpExchange, response);
                    }
                    break;
                }
                case "GET_TASK_ID": {
                    if (Pattern.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", String.valueOf(uuid))) {
                        String response = gson.toJson(fileBackedTaskManager.getTask(uuid));
                        sendText(httpExchange, response);
                    } else {
                        httpExchange.sendResponseHeaders(405, 0);
                        String response = "Ответ 405!";
                        OutputStream os = httpExchange.getResponseBody();
                        os.write(response.getBytes());
                    }
                    break;
                }
                case "DELETE_TASK_ID": {
                    if (Pattern.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", String.valueOf(uuid))) {
                        String taskName = fileBackedTaskManager.getTasks().get(uuid).getName();
                        fileBackedTaskManager.removeTaskById(uuid);
                        System.out.println("Задача и назаванием: " + taskName + " удалена.");
                        httpExchange.sendResponseHeaders(200, 0);
                    }
                    break;
                }
                case "DELETE_TASK": {
                    if (TaskType.TASK.equals(TaskType.valueOf(command))) {
                        if (Pattern.matches("^/tasks/" + command.toLowerCase() + "$", path)) {
                            fileBackedTaskManager.removeTasksByTasktype(TaskType.valueOf(command));
                            System.out.println("Все задачи типа <" + command + "> удалены");
                            httpExchange.sendResponseHeaders(200, 0);
                            return;
                        }
                    } else {
                        httpExchange.sendResponseHeaders(405, 0);
                    }
                    break;
                }
                case "DELETE_EPIC": {
                    if (TaskType.EPIC.equals(TaskType.valueOf(command))) {
                        if (Pattern.matches("^/tasks/" + command.toLowerCase() + "$", path)) {
                            fileBackedTaskManager.removeTasksByTasktype(TaskType.valueOf(command));
                            System.out.println("Все задачи типа <" + command + "> удалены");
                            httpExchange.sendResponseHeaders(200, 0);
                            return;
                        }
                    } else {
                        httpExchange.sendResponseHeaders(405, 0);
                    }
                    break;
                }
                case "DELETE_SUBTASK": {
                    if (TaskType.SUBTASK.equals(TaskType.valueOf(command))) {
                        if (Pattern.matches("^/tasks/" + command.toLowerCase() + "$", path)) {
                            fileBackedTaskManager.removeTasksByTasktype(TaskType.valueOf(command));
                            System.out.println("Все задачи типа <" + command + "> удалены");
                            httpExchange.sendResponseHeaders(200, 0);
                            return;
                        }
                    } else {
                        httpExchange.sendResponseHeaders(405, 0);
                    }
                    break;
                }
                case "GET_HISTORY": {
                    if (Pattern.matches("^/tasks/" + command.toLowerCase() + "$", path)) {
                        fileBackedTaskManager.getHistory();
                        httpExchange.sendResponseHeaders(200, 0);
                        return;
                    } else {
                        httpExchange.sendResponseHeaders(405, 0);
                    }
                    break;
                }

                default: {
                    System.out.println("Ждем GET или DELETE запрос, а получили - " + requestMethod);
                    httpExchange.sendResponseHeaders(405, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpExchange.close();
        }
    }

    private int parsePathId(String path) {
        try {
            return Integer.parseInt(path);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void start() {
        System.out.println("Started TaskServer " + PORT);
        System.out.println("http://localhost:" + PORT + "/tasks");
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("----------------------- Server stopped on the port " + PORT + " -----------------------");
    }

    private String readText(HttpExchange h) throws IOException {
        return new String(h.getRequestBody().readAllBytes(), UTF_8);
    }

    private void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
    }


    static class PostsHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

        }
    }


}