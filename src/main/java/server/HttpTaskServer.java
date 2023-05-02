package main.java.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import main.java.intefaces.TasksManager;
import main.java.managers.Managers;
import main.java.service.TaskType;
import main.java.tasks.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpTaskServer {
    private static final int PORT = 8078;
    private final TasksManager fileBackedTaskManager = Managers.getDefaultFileBacked();
    private final Gson gson;
    private final HttpServer httpServer;

    public HttpTaskServer() throws IOException {
        gson = Managers.getGson();
        InetSocketAddress socket = new InetSocketAddress(8078);
        httpServer = HttpServer.create(new InetSocketAddress(socket.getAddress(), PORT), 0);
        httpServer.createContext("/tasks", this::handleTasks);
    }

    public TasksManager getFileBackedTaskManager() { // для тестов
        return fileBackedTaskManager;
    }

    private void handleTasks(HttpExchange httpExchange) {
        try (httpExchange) {

            String uri = String.valueOf(httpExchange.getRequestURI());
            String path = httpExchange.getRequestURI().getPath();

            UUID uuid = null;
            String[] splitUries = uri.split("/");
            String type = splitUries[2].toUpperCase();
            if (uri.contains("id")) {
                String[] sp = uri.split("=");
                uuid = UUID.fromString(sp[1]);
            }

            String requestMethod = httpExchange.getRequestMethod() + "_" + type.toUpperCase();

            if (uri.contains("subtask") && uri.contains("epic")) {
                requestMethod = requestMethod.concat("_").concat(splitUries[3].toUpperCase());
            }
            if (uri.contains("id")) {
                requestMethod = requestMethod + "_ID";
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
                case "POST_TASK": {
                    String body = new String(httpExchange.getRequestBody().readAllBytes(), UTF_8);
                    if (body.isEmpty()) {
                        httpExchange.sendResponseHeaders(400, 0);
                        return;
                    }
                    Type typePost = new TypeToken<Task>() {}.getType();
                    Task task = gson.fromJson(body, typePost);
                    fileBackedTaskManager.addTask(task);

                    Task taskPost = fileBackedTaskManager.getTask(task.getId());
                    String json = gson.toJson(taskPost);
                    sendText(httpExchange, json);
                    System.out.println("Задача отправлена обратно");
                    break;
                }
                case "DELETE_TASK_ID": {
                    if (Pattern.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
                            String.valueOf(uuid))) {
                        String taskName = fileBackedTaskManager.getTasks().get(uuid).getName();
                        fileBackedTaskManager.removeTaskById(uuid);
                        System.out.println("Задача и назаванием: " + taskName + " удалена.");
                        httpExchange.sendResponseHeaders(200, 0);
                    }
                    break;
                }
                case "DELETE_TASK": {
                    if (TaskType.TASK.equals(TaskType.valueOf(type))) {
                        if (Pattern.matches("^/tasks/" + type.toLowerCase() + "$", path)) {
                            fileBackedTaskManager.removeTasksByTasktype(TaskType.valueOf(type));
                            System.out.println("Все задачи типа <" + type + "> удалены");
                            httpExchange.sendResponseHeaders(200, 0);
                            return;
                        }
                    } else {
                        httpExchange.sendResponseHeaders(405, 0);
                    }
                    break;
                }
                case "DELETE_EPIC": {
                    if (TaskType.EPIC.equals(TaskType.valueOf(type))) {
                        if (Pattern.matches("^/tasks/" + type.toLowerCase() + "$", path)) {
                            fileBackedTaskManager.removeTasksByTasktype(TaskType.valueOf(type));
                            System.out.println("Все задачи типа <" + type + "> удалены");
                            httpExchange.sendResponseHeaders(200, 0);
                            return;
                        }
                    } else {
                        httpExchange.sendResponseHeaders(405, 0);
                    }
                    break;
                }
                case "DELETE_SUBTASK": {
                    if (TaskType.SUBTASK.equals(TaskType.valueOf(type))) {
                        if (Pattern.matches("^/tasks/" + type.toLowerCase() + "$", path)) {
                            fileBackedTaskManager.removeTasksByTasktype(TaskType.valueOf(type));
                            System.out.println("Все задачи типа <" + type + "> удалены");
                            httpExchange.sendResponseHeaders(200, 0);
                            return;
                        }
                    } else {
                        httpExchange.sendResponseHeaders(405, 0);
                    }
                    break;
                }
                case "GET_SUBTASK_EPIC_ID": {
                    if (Pattern.matches("^/tasks/subtask/epic/$", path)) {
                        String response = gson.toJson(fileBackedTaskManager.getSubtasksFromEpic(uuid));
                        sendText(httpExchange, response);
                        return;
                    } else {
                        httpExchange.sendResponseHeaders(405, 0);
                        String response = "Ответ 405!";
                        OutputStream os = httpExchange.getResponseBody();
                        os.write(response.getBytes());
                    }
                }
                case "GET_HISTORY": {
                    if (Pattern.matches("^/tasks/" + type.toLowerCase() + "$", path)) {
                        fileBackedTaskManager.getHistory();
                        httpExchange.sendResponseHeaders(200, 0);
                        return;
                    } else {
                        httpExchange.sendResponseHeaders(405, 0);
                    }
                    break;
                }
                case "GET_PRIORITIZED": {
                    if (Pattern.matches("^/tasks/prioritized$", path)) {
                        String response = gson.toJson(fileBackedTaskManager.prioritizeTasks());
                        sendText(httpExchange, response);
                    } else {
                        httpExchange.sendResponseHeaders(405, 0);
                    break;
                    }
                }

                default: {
                    System.out.println("Ждем GET или DELETE запрос, а получили - " + requestMethod);
                    httpExchange.sendResponseHeaders(405, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
