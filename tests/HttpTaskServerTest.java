import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.java.intefaces.TasksManager;
import main.java.managers.Managers;
import main.java.server.HttpTaskServer;
import main.java.service.Status;
import main.java.service.TaskType;
import main.java.tasks.Epic;
import main.java.tasks.Subtask;
import main.java.tasks.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HttpTaskServerTest  {

    private final Gson gson = Managers.getGson();
    HttpTaskServer taskServer = new HttpTaskServer();
    TasksManager fileBackedTasksManager =  taskServer.getFileBackedTaskManager();
    LocalDateTime dateTimeTestTask1 = LocalDateTime.parse("2000-01-01T01:00:00");
    LocalDateTime dateTimeTestSubtask1 = LocalDateTime.parse("2000-01-01T06:00:00");
    Task task1 = new Task(
            TaskType.TASK,
            "Переезд1",
            "Собрать коробки",
            Status.NEW,
            dateTimeTestTask1,
            50
    );

    Task taskToPost = new Task(
            TaskType.TASK,
            "Переезд2",
            "Собрать коробки",
            Status.NEW,
            dateTimeTestTask1,
            50
    );

    List<UUID> subtasksList = new ArrayList<>();
    Epic epic1 = new Epic(
            TaskType.EPIC,
            "Эпик1",
            "Переезд",
            Status.NEW,
            subtasksList
    );

    UUID epicUuid;
    Subtask subtask1 = new Subtask(
            TaskType.SUBTASK,
            "Подзадача1",
            "Собрать коробки",
            Status.NEW,
            dateTimeTestSubtask1,
            50,
            epicUuid
    );

    HttpTaskServerTest() throws IOException {
    }

    @BeforeEach
    void init() {
        fileBackedTasksManager.addTask(task1);
        fileBackedTasksManager.addTask(epic1);
        epicUuid = fileBackedTasksManager.getTasks().get(epic1.getId()).getId();
        subtask1.setEpicId(epicUuid);
        fileBackedTasksManager.addTask(subtask1);
       taskServer.start();
    }

    @AfterEach
    void tearDown() {
        if (fileBackedTasksManager.getTasks().containsKey(task1.getId())) {
            fileBackedTasksManager.removeTaskById(task1.getId());
        }
        if (fileBackedTasksManager.getTasks().containsKey(epic1.getId())) {
            fileBackedTasksManager.removeTaskById(epic1.getId());
        }
        if (fileBackedTasksManager.getTasks().containsKey(subtask1.getId())) {
            fileBackedTasksManager.removeTaskById(subtask1.getId());
        }
        taskServer.stop();
    }

    @Test
    void getTasks() throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();

        URI uri = URI.create("http://localhost:8078/tasks/task");

        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Output of response.body(): " + response.body());
        System.out.println("Output of response.uri(): " + response.uri());
        System.out.println("Output of response.headers(): " + response.headers());
        System.out.println("Output of response.version(): " + response.version());

        assertEquals(200, response.statusCode());

        Type userType = new TypeToken<ArrayList<Task>>() {}.getType();

        List<Task> actual = gson.fromJson(response.body(), userType);

        List<Task> expected = new ArrayList<>();
        actual.stream().map(expected::add).collect(Collectors.toList());

        assertNotNull(actual, "Задачи не возвращаются");
        assertEquals(3, actual.size(), "Не верное количество задач");
        assertEquals(expected, actual);
    }

    @Test
    void getTaskById() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String uuid = String.valueOf(fileBackedTasksManager.getTask(task1.getId()).getId());
        URI uri = URI.create("http://localhost:8078/tasks/task/?id=" + uuid);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Output of response.body(): " + response.body());
        assertEquals(200, response.statusCode());

        Type userType = new TypeToken<Task>() {}.getType();
        Task actual = gson.fromJson(response.body(), userType);

        assertNotNull(actual, "Задача не возвращается");
        assertEquals(task1.toString(), actual.toString());
    }
//==================================== POST ====================================

    @Test
    void postTask() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String json = gson.toJson(task1);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8078/tasks/task/"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Output of response.body(): " + response.body());
        assertEquals(200, response.statusCode());

        Type userType = new TypeToken<Task>() {}.getType();
        Task actual = gson.fromJson(response.body(), userType);

        assertNotNull(actual, "Задача не возвращается");
        assertEquals(task1.toString(), actual.toString());
    }

//==================================== REMOVE ====================================
    @Test
    void removeAllTasksByTaskType_TASK() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        URI uri = URI.create("http://localhost:8078/tasks/task");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Output of response.body(): " + response.body());
        assertEquals(200, response.statusCode());

        Type taskType = new TypeToken<ArrayList<Task>>() {}.getType();
        List<Task> actual = gson.fromJson(response.body(), taskType);
        assertNotNull(actual, "Задачи не возвращаются");

        URI uriToRemove = URI.create("http://localhost:8078/tasks/task");
        HttpRequest requestToRemove = HttpRequest.newBuilder().uri(uriToRemove).DELETE().build();
        client.send(requestToRemove, HttpResponse.BodyHandlers.discarding());

//        assertEquals(fileBackedTasksManager.getTasks().size(), fileBackedTasksManager.getAllTasksByTaskType(TaskType.TASK));
        assertEquals(new ArrayList<>(), fileBackedTasksManager.getAllTasksByTaskType(TaskType.TASK));
    }

    @Test
    void removeAllTasksByTaskType_EPIC() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        URI uri = URI.create("http://localhost:8078/tasks/task");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Output of response.body(): " + response.body());
        assertEquals(200, response.statusCode());

        Type taskType = new TypeToken<ArrayList<Task>>() {}.getType();
        List<Task> actual = gson.fromJson(response.body(), taskType);
        assertNotNull(actual, "Задачи не возвращаются");

        URI uriToRemove = URI.create("http://localhost:8078/tasks/epic");
        HttpRequest requestToRemove = HttpRequest.newBuilder().uri(uriToRemove).DELETE().build();
        client.send(requestToRemove, HttpResponse.BodyHandlers.discarding());

        assertEquals(new ArrayList<>(), fileBackedTasksManager.getAllTasksByTaskType(TaskType.EPIC));
    }

    @Test
    void removeAllTasksByTaskType_SUBTASK() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        URI uri = URI.create("http://localhost:8078/tasks/task");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Output of response.body(): " + response.body());
        assertEquals(200, response.statusCode());

        Type taskType = new TypeToken<ArrayList<Task>>() {}.getType();
        List<Task> actual = gson.fromJson(response.body(), taskType);
        assertNotNull(actual, "Задачи не возвращаются");

        URI uriToRemove = URI.create("http://localhost:8078/tasks/subtask");
        HttpRequest requestToRemove = HttpRequest.newBuilder().uri(uriToRemove).DELETE().build();
        client.send(requestToRemove, HttpResponse.BodyHandlers.discarding());


        assertEquals(new ArrayList<>(), fileBackedTasksManager.getAllTasksByTaskType(TaskType.SUBTASK));
    }

    @Test
    void getSubtasksUuudsByEpic() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String uuid = String.valueOf(fileBackedTasksManager.getTasks().get(epic1.getId()).getId());
        URI uri = URI.create("http://localhost:8078/tasks/subtask/epic/?id=" + uuid);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ArrayList<Task> actual;

        Type taskType = new TypeToken<ArrayList<Task>>() {}.getType();
        actual = gson.fromJson(response.body(), taskType);

        assertEquals(subtask1.getId(), actual.get(0).getId());
    }

//==================================== History ====================================
    @Test
    void getHistory() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        URI uri = URI.create("http://localhost:8078/tasks/task");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type taskType = new TypeToken<ArrayList<Task>>() {}.getType();
        List<Task> actual = gson.fromJson(response.body(), taskType);
        assertNotNull(actual, "Задачи не возвращаются");

        URI uri2 = URI.create("http://localhost:8078/tasks/history");
        HttpRequest request2 = HttpRequest.newBuilder().uri(uri2).GET().build();
        client.send(request2, HttpResponse.BodyHandlers.ofString());

    }
//==================================== Prioritized ====================================

    //Не могу понять как избежать переопределения типа объекта https://i.ibb.co/C16G2b6/image.png
    @Test
    void getPrioritizedTasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8078/tasks/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> expected = fileBackedTasksManager.prioritizeTasks();
        Type taskType = new TypeToken<ArrayList<Task>>() {}.getType();
        List<Task> actual = gson.fromJson(response.body(), taskType);
        assertNotNull(actual, "Задачи не возвращаются");
        assertEquals(expected, actual);
    }
}