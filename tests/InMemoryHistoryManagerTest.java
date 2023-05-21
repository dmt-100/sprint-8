import main.java.managers.InMemoryHistoryManager;
import main.java.service.Status;
import main.java.service.TaskType;
import main.java.tasks.Epic;
import main.java.tasks.Subtask;
import main.java.tasks.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    InMemoryHistoryManager inMemoryHistoryManager = new InMemoryHistoryManager();
    Task task1;
    Epic epic1;
    Subtask subtask1;
    static UUID uuidTask = UUID.fromString("11111111-d496-48c2-bb4a-f4cf88f18e23");
    static UUID uuidEpic = UUID.fromString("22222222-d496-48c2-bb4a-f4cf88f18e23");
    static UUID uuidSubtask = UUID.fromString("33333333-d496-48c2-bb4a-f4cf88f18e23");

    LocalDateTime startTimeTestTask1;
    LocalDateTime endTimeTestTask1;
    LocalDateTime startTimeTestEpic1;
    LocalDateTime endTimeTestEpic1;
    LocalDateTime startTimeTestSubtask1;
    LocalDateTime endTimeTestSubtask1;

    @BeforeEach
    void create() { // по три задачи

        startTimeTestTask1 = LocalDateTime.parse("2000-01-01T00:00:00");
        endTimeTestTask1 = LocalDateTime.parse("2000-01-01T00:00:00");

        startTimeTestEpic1 = LocalDateTime.parse("2000-01-01T00:00:00");
        endTimeTestEpic1 = LocalDateTime.parse("2000-01-01T00:00:00");

        startTimeTestSubtask1 = LocalDateTime.parse("2000-01-01T00:00:00");
        endTimeTestSubtask1 = LocalDateTime.parse("2000-01-01T00:00:00");

        List<UUID> subtasksList = new ArrayList<>();

        task1 = new Task(
                uuidTask,
                TaskType.TASK,
                "Переезд",
                "Собрать коробки",
                Status.NEW,
                startTimeTestTask1,
                50
        );
        inMemoryHistoryManager.add(task1);

        epic1 = new Epic(
                uuidEpic,
                TaskType.EPIC,
                "Переезд",
                "Переезд",
                Status.NEW,
                subtasksList
        );
        inMemoryHistoryManager.add(epic1);

        subtask1 = new Subtask(
                uuidSubtask,
                TaskType.SUBTASK,
                "тест1",
                "Собрать коробки",
                Status.NEW,
                startTimeTestSubtask1,
                20,
                epic1.getId()
        );
        inMemoryHistoryManager.add(subtask1);

        subtasksList.add(subtask1.getId());

        uuidTask = task1.getId();
    }

    @AfterEach
    void clearHistory() {
        if (inMemoryHistoryManager.getUuidNodes().containsKey(task1.getId())) {
            inMemoryHistoryManager.remove(task1.getId());
        }
        if (inMemoryHistoryManager.getUuidNodes().containsKey(subtask1.getId())) {
            inMemoryHistoryManager.remove(subtask1.getId());
        }
        if (inMemoryHistoryManager.getUuidNodes().containsKey(epic1.getId())) {
            inMemoryHistoryManager.remove(epic1.getId());
        }
        inMemoryHistoryManager.getUuidNodes().clear();
    }

    void clearHistory1() {
        if (inMemoryHistoryManager.getUuidNodes().containsKey(task1.getId())) {
            inMemoryHistoryManager.remove(task1.getId());
        }
        if (inMemoryHistoryManager.getUuidNodes().containsKey(subtask1.getId())) {
            inMemoryHistoryManager.remove(subtask1.getId());
        }
        if (inMemoryHistoryManager.getUuidNodes().containsKey(epic1.getId())) {
            inMemoryHistoryManager.remove(epic1.getId());
        }
        inMemoryHistoryManager.getUuidNodes().clear();
    }

    @Test
    void testGetCustomLinkedListIfHistoryIsEmpty() {
        create();
        assertEquals(3, inMemoryHistoryManager.getTasksInHistory().size());
    }


    @Test
    void testCheckForEmptyHistoryAfterDeleteTaskFromHistory() {
        // по заданию как помню не было требования на поле мапы в customLinkedList'е просто для удобства вытаскивания задач,
        // так то ее надо будет удалить, тк задачи хранятся и в нодах и в мапе, но опять же просто для удобства
        for (Task task : inMemoryHistoryManager.getTasksInHistory()) {
            System.out.println(task);
        }
        clearHistory();
        assertEquals(0, inMemoryHistoryManager.getTasksInHistory().size());
        assertEquals(0, inMemoryHistoryManager.getUuidNodes().size());
    }


    // с. Удаление из истории: начало, середина, конец.
    @Test
    void testRemoveFirstTaskFromHistory() {
        create();
        inMemoryHistoryManager.remove(task1.getId());
        for (Task task : inMemoryHistoryManager.getTasksInHistory()) {
            System.out.println(task);
        }
            /*
            Output:
Epic{id=3fc03f9d-e08e-4b70-ba34-f63f37b0c538, taskType=EPIC, name='Переезд', description='Переезд', status='NEW', startTime='2015-12-22T08:15:30', endTime='null', duration='0', subtasksList=[0ab90058-e23d-4e1d-9fd8-e9b1b1f950aa]}
Subtask{id=0ab90058-e23d-4e1d-9fd8-e9b1b1f950aa, taskType=SUBTASK, name='тест1', description='Собрать коробки', status='NEW', startTime='2016-12-22T10:20:30', endTime='2016-12-22T10:40:30', duration='20', epicId=3fc03f9d-e08e-4b70-ba34-f63f37b0c538}
Process finished with exit code 0
             */
        assertEquals(2, inMemoryHistoryManager.getUuidNodes().size());
        clearHistory();
    }

    @Test
    void testRemoveMiddleTaskFromHistory() {
        create();
        inMemoryHistoryManager.remove(epic1.getId());
        for (Task task : inMemoryHistoryManager.getTasksInHistory()) {
            System.out.println(task);
        }
        /*
        Output:
Task{id=09f2e104-8b51-4f19-9ddb-67a7d0ee3698, taskType=TASK, name='Переезд', description='Собрать коробки', status='NEW', startTime='2014-12-22T05:10:30', endTime='2014-12-22T06:00:30', duration='50'}
Subtask{id=50f7b3e9-1c64-47b1-81ea-bb0720fc91c6, taskType=SUBTASK, name='тест1', description='Собрать коробки', status='NEW', startTime='2016-12-22T10:20:30', endTime='2016-12-22T10:40:30', duration='20', epicId=a85f22a4-2b85-45c5-abe4-256fd44ab49b}
         */
        assertEquals(2, inMemoryHistoryManager.getUuidNodes().size());
        clearHistory();
    }

    @Test
    void testRemoveLastTaskFromHistory() {
        create();
        inMemoryHistoryManager.remove(subtask1.getId());
        for (Task task : inMemoryHistoryManager.getTasksInHistory()) {
            System.out.println(task);
        }
        /*
        Output:
Task{id=0eac8ce3-5704-487c-8f9c-fab573dcbd50, taskType=TASK, name='Переезд', description='Собрать коробки', status='NEW', startTime='2014-12-22T05:10:30', endTime='2014-12-22T06:00:30', duration='50'}
Epic{id=92cde998-d9ca-497e-b604-058fe8fef042, taskType=EPIC, name='Переезд', description='Переезд', status='NEW', startTime='2015-12-22T08:15:30', endTime='null', duration='0', subtasksList=[86190d71-3fd8-406c-9732-93f328f8fc30]}
         */
        assertEquals(2, inMemoryHistoryManager.getUuidNodes().size());
        clearHistory();
    }

    /*
        3. Для HistoryManager — тесты для всех методов интерфейса. Граничные условия:
         a. Пустая история задач.
         b. Дублирование.
         с. Удаление из истории: начало, середина, конец.
     */
// =============================== public void add(Task task) ===============================
    @Test
    void testAddWithEmptyHistory() { // a. Пустая история задач.
        clearHistory1();
        inMemoryHistoryManager.add(task1);
        inMemoryHistoryManager.getUuidNodes().get(task1.getId());
        Task taskActual = null;
        for (Task task : inMemoryHistoryManager.getTasksInHistory()) {
            if (task.equals(task1)) {
                taskActual = task;
            }
            assertEquals(task1, taskActual);
        }
    }

    @Test
    void testAddWithDoubleTask() { // b. Дублирование.
        clearHistory1();
        inMemoryHistoryManager.add(task1);
        task1.setStartTime(LocalDateTime.parse("2000-01-01T00:11:11"));
        inMemoryHistoryManager.add(task1);
        LocalDateTime expectedTime = LocalDateTime.parse("2000-01-01T00:11:11");
        LocalDateTime actualTime = inMemoryHistoryManager.getTasksInHistory().get(0).getStartTime();

        assertEquals(expectedTime, actualTime);
    }

    @Test
    void testAddRemoveFirstTask() { // с. Удаление из истории: начало, середина, конец.
        boolean flag = false;
        inMemoryHistoryManager.remove(task1.getId());
        for (Task task : inMemoryHistoryManager.getTasksInHistory()) {
            System.out.println(task);
        }
        if (inMemoryHistoryManager.getUuidNodes().containsKey(epic1.getId()) &&
                inMemoryHistoryManager.getUuidNodes().containsKey(subtask1.getId()) &&
                inMemoryHistoryManager.getUuidNodes().size() == 2) {
            flag = true;
        }
        assertTrue(flag);
    }

// =============================== public ArrayList<Task> getTasksInHistory()  ===============================

    @Test
    void testGetTasksInHistoryWithEmptyHistory() { // a. Пустая история задач.
        clearHistory1();
        assertEquals(new ArrayList<>(), inMemoryHistoryManager.getTasksInHistory());
    }

    @Test
    void testGetTasksInHistoryWithDoubleTask() { // b. Дублирование.
        LocalDateTime actualTime = null;
        inMemoryHistoryManager.add(task1);
        task1.setStartTime(LocalDateTime.parse("2000-01-01T00:11:11"));
        inMemoryHistoryManager.add(task1);
        LocalDateTime expectedTime = LocalDateTime.parse("2000-01-01T00:11:11");
        for (Task task : inMemoryHistoryManager.getTasksInHistory()) {
            if (task.getId().equals(task1.getId())) {
                actualTime = task.getStartTime();
            }
        }
        assertEquals(expectedTime, actualTime);
    }

    @Test
    void testGetTasksInHistoryRemoveFirstTask() { // с. Удаление из истории: начало, середина, конец.
        inMemoryHistoryManager.remove(task1.getId());
        boolean flag = inMemoryHistoryManager.getUuidNodes().containsKey(epic1.getId()) &&
                inMemoryHistoryManager.getUuidNodes().containsKey(subtask1.getId()) &&
                inMemoryHistoryManager.getUuidNodes().size() == 2;
        assertTrue(flag);
    }

    @Test
    void testGetTasksInHistoryRemoveMiddleTask() { // с. Удаление из истории: середина
        inMemoryHistoryManager.remove(epic1.getId());
        boolean flag = inMemoryHistoryManager.getUuidNodes().containsKey(task1.getId()) &&
                inMemoryHistoryManager.getUuidNodes().containsKey(subtask1.getId()) &&
                inMemoryHistoryManager.getUuidNodes().size() == 2;
        assertTrue(flag);
    }

    @Test
    void testGetTasksInHistoryRemoveLastTask() { // с. Удаление из истории: конец.
        inMemoryHistoryManager.remove(subtask1.getId());
        boolean flag = inMemoryHistoryManager.getUuidNodes().containsKey(task1.getId()) &&
                inMemoryHistoryManager.getUuidNodes().containsKey(epic1.getId()) &&
                inMemoryHistoryManager.getUuidNodes().size() == 2;
        assertTrue(flag);
    }

// =============================== public String remove(UUID id)  ===============================

    @Test
    void testRemoveWithEmptyHistory() { // a. Пустая история задач.
        clearHistory1();
        inMemoryHistoryManager.remove(task1.getId());
        assertEquals("Задачи с таким id в истории нет", inMemoryHistoryManager.remove(task1.getId()));
    }
    @Test
    void testRemoveWithDoubleTask() { // b. Дублирование.
        inMemoryHistoryManager.remove(task1.getId());
        inMemoryHistoryManager.remove(task1.getId());
        assertEquals("Задачи с таким id в истории нет", inMemoryHistoryManager.remove(task1.getId()));
    }
}