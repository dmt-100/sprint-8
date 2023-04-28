package main.java.managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.java.intefaces.TasksManager;
import main.java.server.KVTaskClient;
import main.java.service.TaskType;
import main.java.tasks.Task;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HttpTaskManager extends FileBackedTasksManager implements TasksManager {
    private final Gson gson;
    private final KVTaskClient client;

    private final String task = "Tasks";
    private final String epic = "Epics";
    private final String subtask = "Subtasks";
    private final String history = "History";
    private List<Task> tasks;

    public HttpTaskManager(URI url) {
        super();
        gson = Managers.getGson();
        this.client = new KVTaskClient(url);
    }

    @Override
    public void save() {

        String tasks = gson.toJson(getAllTasks()
                .stream()
                .filter(t -> t.getTaskType().equals(TaskType.TASK))
                .collect(Collectors.toList()));
        client.put("tasks", gson.toJson(tasks));

        String subtasks = gson.toJson(getAllTasks()
                .stream()
                .filter(t -> t.getTaskType().equals(TaskType.SUBTASK))
                .collect(Collectors.toList()));
        client.put("subtasks", gson.toJson(subtasks));

        String epics = gson.toJson(getAllTasks()
                .stream()
                .filter(t -> t.getTaskType().equals(TaskType.EPIC))
                .collect(Collectors.toList()));
        client.put("epics", gson.toJson(epics));

        String history = gson.toJson(getHistory().stream().map(Task::getId).collect(Collectors.toList()));
        client.put("epics", gson.toJson(history));

    }

    private void load() {
        // что должно быть ключом для метода load(String key), в Пачке пишут, что тип задач.
        String tasks = gson.fromJson(client.load("tasks"),
                new TypeToken<ArrayList<Task>>() {}.getType());
        this.tasks = new ArrayList<>(Integer.parseInt(tasks));
    }

    @Override
    public List<Task> getAllTasksByTaskType(TaskType taskType) {
        load();
        return (List<Task>) tasks.stream().filter(t -> t.getTaskType().equals(taskType));
    }


    @Override
    public List<Task> prioritizeTasks() {
        load();
        List<Task> prioritizedTasks;
        if (tasks.size() > 1) {
            prioritizedTasks = new ArrayList<>(
                    tasks.stream()
                            .filter(t -> (!t.getTaskType().equals(TaskType.EPIC)))
                            .sorted(Comparator.comparing(Task::getStartTime))
                            .collect(Collectors.toCollection(ArrayList::new)));

            for (Task prioritizedTask : prioritizedTasks) {
                System.out.println(prioritizedTask);
            }
        } else {
            prioritizedTasks = null;
        }
        return prioritizedTasks;
    }

    @Override
    public List<Task> getHistory() {
        load();
        List<Task> history = super.getHistory();
        save();
        return history;
    }



}





/*
// ======================================= ПОДСКАЗКИ для меня =======================================


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