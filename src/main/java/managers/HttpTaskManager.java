package main.java.managers;

import com.google.gson.Gson;
import main.java.intefaces.TasksManager;
import main.java.server.KVTaskClient;
import main.java.tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpTaskManager extends FileBackedTasksManager implements TasksManager {

    public HttpTaskManager httpTaskManager;
    private Gson gson;

    private KVTaskClient client;


/* ТЗ-8
Конструктор HttpTaskManager должен будет вместо имени файла принимать URL к серверу KVServer.
Также HttpTaskManager создаёт KVTaskClient, из которого можно получить исходное состояние менеджера.
Вам нужно заменить вызовы сохранения состояния в файлах на вызов клиента.
В конце обновите статический метод getDefault() в утилитарном классе Managers, чтобы он возвращал HttpTaskManager.
 */
    public HttpTaskManager(URI BASE_URL) throws IOException, InterruptedException {
        super();
        gson = Managers.getGson();
        this.client = new KVTaskClient(BASE_URL); // ТЗ-8 Также HttpTaskManager создаёт KVTaskClient
    }

    public void setHttpTaskManager(HttpTaskManager httpTaskManager) {
        this.httpTaskManager = httpTaskManager;
    }

    public void save() {

    }
// ТЗ-8 Новая реализация менеджера задач... Вам нужно заменить вызовы сохранения состояния в файлах на вызов клиента

// Что именно тут нужно сделать? если задача успешно добавлена, сериализовать её в gson и отправить в put??
    @Override
    public void addTask(Task task) { // добавление
        super.addTask(task);
        save();
        client.getAPI_TOKEN();
//        HttpClient client = HttpClient.newHttpClient();
        String uuid = String.valueOf(httpTaskManager.getTask(task.getId()).getId());
        URI uri = URI.create("http://localhost:8080/tasks/task/?=" + uuid);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

//        HttpResponse<String> response = client.put(request, HttpResponse.BodyHandlers.ofString());
//        System.out.println("Output of response.body(): " + response.body());

    }
}
