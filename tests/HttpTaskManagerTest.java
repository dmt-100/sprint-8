import main.java.intefaces.TasksManager;
import main.java.managers.HttpTaskManager;
import main.java.managers.Managers;
import main.java.server.KVServer;
import main.java.service.Status;
import main.java.service.TaskType;
import main.java.tasks.Epic;
import main.java.tasks.Subtask;
import main.java.tasks.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {

    private KVServer kvServer;
    URI uri = URI.create("http://localhost:8078/");

    LocalDateTime dateTimeTestTask1 = LocalDateTime.parse("2000-01-01T01:00:00");
    LocalDateTime dateTimeTestTask2 = LocalDateTime.parse("2000-01-01T02:00:00");
    LocalDateTime dateTimeTestTask3 = LocalDateTime.parse("2000-01-01T03:00:00");
    LocalDateTime dateTimeTestTask4 = LocalDateTime.parse("2000-01-01T04:00:00");

    LocalDateTime dateTimeTestEpic1 = LocalDateTime.parse("2000-01-01T05:00:00");
    LocalDateTime dateTimeTestSubtask1 = LocalDateTime.parse("2000-01-01T06:00:00");
    LocalDateTime dateTimeTestSubtask2 = LocalDateTime.parse("2000-01-01T07:40:00");

    UUID epicUuid = UUID.fromString("11111111-d496-48c2-bb4a-f4cf88f18e23");

    Task task1;
    Epic epic1;
    Subtask subtask1;
    Subtask subtask2;


    TasksManager httpTaskManager;

    @Override
    void setManager() {
        taskManager = new HttpTaskManager(URI.create("http://localhost:8078/"), false);
    }

    @Override
    @BeforeEach
    void init() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
        setManager();
// ----------------------------------------
        httpTaskManager = new HttpTaskManager(uri, false);

        task1 = new Task(
                TaskType.TASK,
                "Task1",
                "Collect boxes",
                Status.NEW,
                dateTimeTestTask1,
                50
        );

        List<UUID> subtasksList = new ArrayList<>();
        epic1 = new Epic(
                epicUuid,
                TaskType.EPIC,
                "Epic1",
                "Relocation",
                Status.NEW,
                dateTimeTestEpic1,
                0,
                subtasksList
        );

        subtask1 = new Subtask(
                TaskType.SUBTASK,
                "Subtask1",
                "Collect boxes",
                Status.NEW,
                dateTimeTestSubtask1,
                50,
                epicUuid
        );

        Subtask subtask2 = new Subtask(
                TaskType.SUBTASK,
                "Subtask1",
                "Pack the cat",
                Status.NEW,
                dateTimeTestSubtask2,
                15,
                epicUuid
        );

        httpTaskManager = Managers.getDefault();
        httpTaskManager.save();
        httpTaskManager = new HttpTaskManager(uri, true);

    }

    @Override
    void beforeEachAddTasks() {
        httpTaskManager.addTask(task1);
        httpTaskManager.addTask(epic1);
        httpTaskManager.addTask(subtask1);
        httpTaskManager.addTask(subtask2);
        httpTaskManager.save();
    }

    @AfterEach
    void afterEachRemoveTasks() {
        kvServer.stop();
    }
    @Test
    void checkTasks() {
        HttpTaskManager load = new HttpTaskManager(URI.create("http://localhost:8078/"), true);
        assertEquals(taskManager.getAllTasks(), load.getAllTasks(),
                "Задачи после выгрузки не совпадают");
        assertEquals(taskManager.prioritizeTasks(), load.prioritizeTasks(),
                "Отсортированный список не совпадает");
        assertEquals(taskManager.getHistory(), load.getHistory(),
                "Список задач в истории не совпадает");
    }

    @Test
    void checkAddedTasks() {
        new HttpTaskManager(uri, true);

        List<Task> afterLoad = httpTaskManager
                .getAllTasks().stream().toList();
        int expected = afterLoad.size();
        assertEquals(expected, httpTaskManager.getAllTasks().size(),
                "Задачи после выгрузки не совпадают");
        assertEquals(4, httpTaskManager.prioritizeTasks().size(), // эпики не входят в сортировку!
                "Отсортированный список не совпадает");
    }

    @Test
    void checkHistory() {
        new HttpTaskManager(uri, true);

        List<Task> expected = httpTaskManager.getHistory();
        assertEquals(expected.size(), httpTaskManager.getHistory().size(),
                "Список задач в истории не совпадает");
    }





//    private KVServer kvServer;
//
//    URI uri = URI.create("http://localhost:8078/");
//
//    LocalDateTime dateTimeTestTask1 = LocalDateTime.parse("2000-01-01T01:00:00");
//    LocalDateTime dateTimeTestTask2 = LocalDateTime.parse("2000-01-01T02:00:00");
//    LocalDateTime dateTimeTestTask3 = LocalDateTime.parse("2000-01-01T03:00:00");
//    LocalDateTime dateTimeTestTask4 = LocalDateTime.parse("2000-01-01T04:00:00");
//    LocalDateTime dateTimeTestEpic1 = LocalDateTime.parse("2000-01-01T05:00:00");
//    LocalDateTime dateTimeTestSubtask1 = LocalDateTime.parse("2000-01-01T06:00:00");
//    LocalDateTime dateTimeTestSubtask2 = LocalDateTime.parse("2000-01-01T07:40:00");
//    UUID epicUuid = UUID.fromString("11111111-d496-48c2-bb4a-f4cf88f18e23");
//    Task task1;
//    Task task2;
//    Epic epic1;
//    Subtask subtask1;
//    Subtask subtask2;
//
//    HttpTaskManager httpTaskManager;
//
//    @Override
//    void setManager() {
//        httpTaskManager = new HttpTaskManager(uri, false);
//    }
//
//    @Override
//    void init() {
//        try {
//            kvServer = new KVServer();
//            kvServer.start();
//            setManager();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//// ----------------------------------------
//
//        task1 = new Task(
//                TaskType.TASK,
//                "Task1",
//                "Collect boxes",
//                Status.NEW,
//                dateTimeTestTask1,
//                50
//        );
//
//        task2 = new Task(
//                TaskType.TASK,
//                "Task2",
//                "Pack the cat",
//                Status.NEW,
//                dateTimeTestTask2,
//                5
//        );
//
//        List<UUID> subtasksList = new ArrayList<>();
//        epic1 = new Epic(
//                epicUuid,
//                TaskType.EPIC,
//                "Epic1",
//                "Relocation",
//                Status.NEW,
//                dateTimeTestEpic1,
//                0,
//                subtasksList
//        );
//
//        subtask1 = new Subtask(
//                TaskType.SUBTASK,
//                "Subtask1",
//                "Collect boxes",
//                Status.NEW,
//                dateTimeTestSubtask1,
//                50,
//                epicUuid
//        );
//
//        subtask2 = new Subtask(
//                TaskType.SUBTASK,
//                "Subtask1",
//                "Pack the cat",
//                Status.NEW,
//                dateTimeTestSubtask2,
//                15,
//                epicUuid
//        );
//    }
//
//    @Override
//    @BeforeEach
//    void beforeEachAddTasks() throws IOException {
//        kvServer = new KVServer();
//
//        httpTaskManager.addTask(task1);
//        httpTaskManager.addTask(epic1);
//        httpTaskManager.addTask(subtask1);
//        httpTaskManager.addTask(subtask2);
//
//        httpTaskManager = Managers.getDefault();
//
//        httpTaskManager = new HttpTaskManager(uri, true);
//        httpTaskManager.save();
//    }
//
//    @Override
//    @AfterEach
//    void afterEachRemoveTasks() {
//        httpTaskManager.removeTaskById(task1.getId());
//        httpTaskManager.removeTaskById(task2.getId());
//        httpTaskManager.removeTaskById(epic1.getId());
//        httpTaskManager.removeTaskById(subtask1.getId());
//        httpTaskManager.removeTaskById(subtask2.getId());
//
//        httpTaskManager.save();
//
//        kvServer.stop();
//    }
//
//    @Test
//    void checkAddedTasks() {
//        new HttpTaskManager(uri, true);
//
//        List<Task> afterLoad = httpTaskManager
//                .getAllTasks().stream().toList();
//        int expected = afterLoad.size();
//        assertEquals(expected, httpTaskManager.getAllTasks().size(),
//                "Задачи после выгрузки не совпадают");
//        assertEquals(4, httpTaskManager.prioritizeTasks().size(), // эпики не входят в сортировку!
//                "Отсортированный список не совпадает");
//    }
//
//    @Test
//    void checkHistory() {
//        new HttpTaskManager(uri, true);
//
//        List<Task> expected = httpTaskManager.getHistoryManager().getTasksInHistory();
//        assertEquals(expected.size(), httpTaskManager.getHistory().size(),
//                "Список задач в истории не совпадает");
//    }
}














