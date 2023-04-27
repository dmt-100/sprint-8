package main.java.managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.java.intefaces.TasksManager;
import main.java.server.KVTaskClient;
import main.java.tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class HttpTaskManager extends FileBackedTasksManager implements TasksManager {
    private Gson gson;
    private KVTaskClient client;

    public HttpTaskManager(URI url) throws IOException {
        super();
        gson = Managers.getGson();
        this.client = new KVTaskClient(url);
    }

    @Override
    public void save() {
//        String jsonTasks = gson.toJson(new ArrayList<>(getAllTasks()));
        String jsonTasks = gson.toJson(new ArrayList<>(getTasks().values()));
        client.put("tasks", jsonTasks);
        gson.toJson(getHistory().stream().map(Task::getId).collect(Collectors.toList()));

    }

    private void load() {
        ArrayList<Task> tasks = gson.fromJson(client.load("tasks"), new TypeToken<ArrayList<Task>>() {
        }.getType());

    }
}


/*
// ======================================= ПОДСКАЗКИ для меня =======================================



ТЗ-8
Конструктор HttpTaskManager должен будет вместо имени файла принимать URL к серверу KVServer.
Также HttpTaskManager создаёт KVTaskClient, из которого можно получить исходное состояние менеджера.
Вам нужно заменить вызовы сохранения состояния в файлах на вызов клиента.
В конце обновите статический метод getDefault() в утилитарном классе Managers, чтобы он возвращал HttpTaskManager.



// ==========================================================================================
*/