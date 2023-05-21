package main.java.managers;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import main.java.server.KVServer;
import main.java.server.KVTaskClient;
import main.java.service.Status;
import main.java.service.TaskType;
import main.java.tasks.Epic;
import main.java.tasks.Subtask;
import main.java.tasks.Task;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

public class HttpTaskManager extends FileBackedTasksManager {
    private final Gson gson = Managers.getGson();
    private KVTaskClient client;

// ---------------------------------------------- TEST ----------------------------------------------
    public static void main(String[] args) throws IOException {
        LocalDateTime dateTimeTestTask1 = LocalDateTime.parse("2000-01-01T01:00:00");

        Task task1 = new Task(
                TaskType.TASK,
                "Task1",
                "Collect boxes",
                Status.NEW,
                dateTimeTestTask1,
                50
        );

        KVServer kvServer = new KVServer();
        kvServer.start();
        HttpTaskManager httpTaskManager = new HttpTaskManager(URI.create("http://localhost:8078/"), false);
        httpTaskManager.addTask(task1);
    }
// ---------------------------------------------- TEST ----------------------------------------------


    public HttpTaskManager(URI uri, boolean load) {
        this.client = new KVTaskClient(uri);
        if (load) {
            load();
        }
    }

    @Override
    public void save() {

        String tasks = gson.toJson(getTasks().values()
                .stream()
                .filter(t -> t.getTaskType().equals(TaskType.TASK))
                .collect(Collectors.toList()));
        client.put("tasks", gson.toJson(tasks));

        String subtasks = gson.toJson(getTasks().values()
                .stream()
                .filter(t -> t.getTaskType().equals(TaskType.SUBTASK))
                .collect(Collectors.toList()));
        client.put("subtasks", gson.toJson(subtasks));

        String epics = gson.toJson(getTasks().values()
                .stream()
                .filter(t -> t.getTaskType().equals(TaskType.EPIC))
                .collect(Collectors.toList()));
        client.put("epics", gson.toJson(epics));

        String history = gson.toJson(getHistoryManager().getTasksInHistory()
                .stream()
                .map(Task::getId)
                .collect(Collectors.toList()));
        client.put("history", gson.toJson(history));
    }

    private void load() {
        ArrayList<Task> tasks = gson.fromJson(removeQuotesAndUnescape(client.load("tasks")),
                new TypeToken<ArrayList<Task>>() {}.getType());
        for (Task task : tasks) {
            getTasks().put(task.getId(), task);
        }

        ArrayList<Epic> epics = gson.fromJson(removeQuotesAndUnescape(client.load("epics")),
                new TypeToken<ArrayList<Epic>>() {
                }.getType());
        for (Epic epic : epics) {
            getTasks().put(epic.getId(), epic);
        }

        ArrayList<Subtask> subtasks = gson.fromJson(removeQuotesAndUnescape(client.load("subtasks")),
                new TypeToken<ArrayList<Subtask>>() {
                }.getType());
        for (Subtask subtask : subtasks) {
            getTasks().put(subtask.getId(), subtask);
        }

        ArrayList<UUID> history = gson.fromJson(removeQuotesAndUnescape(client.load("history")),
                new TypeToken<ArrayList<UUID>>() {
                }.getType());
        for (UUID uuid : history) {
            Task task = getTasks().get(uuid);
            getHistoryManager().add(task);
        }
    }

    private String removeQuotesAndUnescape(String uncleanJson) {
        String noQuotes = uncleanJson.replaceAll("^\"|\"$", "");
        return StringEscapeUtils.unescapeJava(noQuotes);
    }
}










/*
// ======================================= ПОДСКАЗКИ для меня =======================================

Этот код помог избавитьcя от ошибки. как написали, возможно сконвертировано в json дважды
    private String removeQuotesAndUnescape(String uncleanJson) {
        String noQuotes = uncleanJson.replaceAll("^\"|\"$", "");
        return StringEscapeUtils.unescapeJava(noQuotes);
    }


В этом классе должны быть методы load и save. save - переопределенный метод класса-родителя, в котором происходит преборазование коллекций задач/подзадач/эпиков в json. И дальнейшее их сохранение через client.
Примерная запись для задач будет такой)
       String jsonTasks = gson.toJson(new ArrayList<>(tasks.values()));
        client.put("tasks", jsonTasks);
Примерный вид того, как должно выглядеть сохранения задач) Аналогично следует поступить с подзадачами и эпиками
Так же не стоит забывать об истории, коллекцию которой лучше хранить в виде идентификаторов, чтобы не занимать лишнее пространство в памяти)
С помощью стримов можно быстро преобразовать коллекцию объектов Task к коллекции идентификаторов
gson.toJson(getHistory().stream().map(Task::getId).collect(Collectors.toList()));
Метод load должен быть приватным, так как используется только для внутренней реализации класса, в котором он находится) Так же он не должен быть статик и должен быть void. Он ничего не должен принимать.
Примерно так будет выглядеть извлечение задач)
       ArrayList<Task> tasks = gson.fromJson(client.load("tasks"), new TypeToken<ArrayList<Task>>() {
        }.getType());
Далее нужно заполнить текущий менеджер данными из полученной коллекции)
Аналогично следует поступить с подзадачами, эпиками) Ну и не стоит забывать об отсортированном списке, который будет сформирован на основе задач/подзадач и истории)

//        gson.toJson(getHistory().stream().map(Task::getId).collect(Collectors.toList()));
ТЗ-8
Конструктор HttpTaskManager должен будет вместо имени файла принимать URL к серверу KVServer.
Также HttpTaskManager создаёт KVTaskClient, из которого можно получить исходное состояние менеджера.
Вам нужно заменить вызовы сохранения состояния в файлах на вызов клиента.
В конце обновите статический метод getDefault() в утилитарном классе Managers, чтобы он возвращал HttpTaskManager.



// ==========================================================================================
*/