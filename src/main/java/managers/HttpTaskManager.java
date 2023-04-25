package main.java.managers;

import com.google.gson.Gson;
import main.java.intefaces.TasksManager;
import main.java.server.KVTaskClient;
import main.java.tasks.Task;

import java.io.IOException;
import java.net.URI;

public class HttpTaskManager extends FileBackedTasksManager implements TasksManager {

    public HttpTaskManager httpTaskManager;
    private Gson gson;

    private KVTaskClient client;


/* ТЗ-8
Конструктор HttpTaskManager должен будет вместо имени файла принимать URL к серверу KVServer.
Также HttpTaskManager создаёт KVTaskClient, из которого можно получить исходное состояние менеджера. Вам нужно заменить вызовы сохранения состояния в файлах на вызов клиента.
В конце обновите статический метод getDefault() в утилитарном классе Managers, чтобы он возвращал HttpTaskManager.
 */
    public HttpTaskManager(URI BASE_URL) throws IOException, InterruptedException {
        super();
        gson = Managers.getGson();
        this.client = new KVTaskClient(BASE_URL);
    }


//    public void test() throws IOException, InterruptedException {
////        String toJsonHttpTaskManager = gson.toJson(httpTaskManager);// падает на JsonIOException GPT пишет нужен адаптер
//        client.put(client.getAPI_TOKEN(), toJsonHttpTaskManager);
//    }

    public void setHttpTaskManager(HttpTaskManager httpTaskManager) {
        this.httpTaskManager = httpTaskManager;
    }

    public void save() {

    }

    @Override
    public void addTask(Task task) { // добавление
        super.addTask(task);
        save();
    }
}
