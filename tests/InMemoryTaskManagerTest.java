import main.java.intefaces.TasksManager;
import main.java.managers.HttpTaskManager;
import main.java.managers.InMemoryTaskManager;
import main.java.server.KVServer;
import main.java.service.Status;
import main.java.service.TaskType;
import main.java.tasks.Epic;
import main.java.tasks.Subtask;
import main.java.tasks.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    List<UUID> subtasks = new ArrayList<>();

    UUID testUuid = UUID.fromString("00000000-0000-48c2-bb4a-f4cf88f18e23");
    UUID wrongUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
    UUID randomUuid = UUID.randomUUID();

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

    TasksManager inMemoryTaskManager;

    @Override
    void setManager() {
        inMemoryTaskManager = new InMemoryTaskManager();
    }



    @Override
    void init() {
        setManager();

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

        subtask2 = new Subtask(
                TaskType.SUBTASK,
                "Subtask1",
                "Pack the cat",
                Status.NEW,
                dateTimeTestSubtask2,
                15,
                epicUuid
        );

    }

    @Override
    @BeforeEach
    void beforeEachAddTasks() {
        init();
        inMemoryTaskManager.addTask(task1);
        inMemoryTaskManager.addTask(epic1);
        inMemoryTaskManager.addTask(subtask1);
        inMemoryTaskManager.addTask(subtask2);

    }

    @Override
    @AfterEach
    void afterEachRemoveTasks() {

        if (inMemoryTaskManager.getTasks().containsKey(task1.getId())) {
            inMemoryTaskManager.removeTaskById(task1.getId());
        }
        if (inMemoryTaskManager.getTasks().containsKey(subtask1.getId())) {
            inMemoryTaskManager.removeTaskById(subtask1.getId());
        }
        if (inMemoryTaskManager.getTasks().containsKey(subtask2.getId())) {
            inMemoryTaskManager.removeTaskById(subtask2.getId());
        }
        if (inMemoryTaskManager.getTasks().containsKey(epic1.getId())) {
            inMemoryTaskManager.removeTaskById(epic1.getId());
        }
    }

    void get() {
        inMemoryTaskManager.getTask(task1.getId());
        inMemoryTaskManager.getTask(epic1.getId());
        inMemoryTaskManager.getTask(subtask1.getId());
    }


    // ----------------------------------------------------------------------------------------
    // Блок тестов на изменения статусов
    @Test
    void allSubtasksWithStatusNew() { // b.   Все подзадачи со статусом NEW.
        boolean flag = false; // флаг на прохождение фильтра
        for (Task subtask : inMemoryTaskManager.getTasks().values()) {
            if (subtask.getTaskType().equals(TaskType.SUBTASK) && subtask.getStatus().equals(Status.NEW)) {
                flag = true;
                assertTrue(flag);
            }
        }
        inMemoryTaskManager.getTasks().clear();

    }

    @Test
    void allSubtasksWithStatusNewAndDone() { // d.    Подзадачи со статусами NEW и DONE.
        int statusDoneOrNew = 0;
        for (Task subtask : inMemoryTaskManager.getTasks().values()) {
            if (subtask.getTaskType().equals(TaskType.SUBTASK)) {
                subtask.setStatus(Status.DONE); // первому сабтаску назначаем DONE и break;
                break;
            }
        }
        for (Task subtask : inMemoryTaskManager.getTasks().values()) {
            if (subtask.getTaskType().equals(TaskType.SUBTASK)) {

                if (subtask.getStatus().equals(Status.DONE) || subtask.getStatus().equals(Status.NEW)) {
                    statusDoneOrNew++;
                }
            }
        }
        assertEquals(2, statusDoneOrNew);
        inMemoryTaskManager.getTasks().clear();
    }

    @Test
    void allSubtasksWithStatusInprogress() { // e.    Подзадачи со статусом IN_PROGRESS.
        int statusInprogress = 0;
        for (Task subtask : inMemoryTaskManager.getTasks().values()) {
            if (subtask.getTaskType().equals(TaskType.SUBTASK)) {
                subtask.setStatus(Status.IN_PROGRESS); // меняем задачу на IN_PROGRESS
                statusInprogress++;
            }
        }
        assertEquals(2, statusInprogress);
        inMemoryTaskManager.getTasks().clear();
    }


    @Test
    void allSubtasksWithStatusDone() { // c.    Все подзадачи со статусом DONE.
        boolean flag;
        for (Task subtask : inMemoryTaskManager.getTasks().values()) {
            if (subtask.getTaskType().equals(TaskType.SUBTASK)) {
                inMemoryTaskManager.changeStatusTask(subtask.getId(), Status.DONE); // меняем статус подзадач
            }
        }
        for (Task epicAndSubtasks : inMemoryTaskManager.getTasks().values()) {
            if (epicAndSubtasks.getTaskType().equals(TaskType.SUBTASK) || epicAndSubtasks.getTaskType().equals(TaskType.EPIC)
                    && epicAndSubtasks.getStatus().equals(Status.DONE)) { // проверка и Эпика, так как его статус тоже меняется если все подзадачи этого эпика равны DONE
                flag = true;
                assertTrue(flag);
            }
        }
        inMemoryTaskManager.getTasks().clear();
    }

    @Test
    void cleanSubtaskIds() {
        boolean flag = true;
        inMemoryTaskManager.removeTasksByTasktype(TaskType.SUBTASK);
        for (Task task : inMemoryTaskManager.getTasks().values()) {
            if (task.getTaskType().equals(TaskType.SUBTASK)) {
                flag = false;
                break;
            }
        }
        assertTrue(flag);
    }

    // -----------------------------------------------------------------------------------------------

    @Test
    void testPrioritizeTasksWithStandardCondition() { // a. Со стандартным поведением.
        inMemoryTaskManager.prioritizeTasks();
        LocalDateTime testTime = LocalDateTime.parse("1999-01-01 00:00:00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US));
        boolean flag = false;
        for (Task task : inMemoryTaskManager.getTasks().values()) {
            if (task.getStartTime().isAfter(testTime)) {
                flag = true;
                testTime = task.getStartTime(); // обновляем время
            }
        }
        assertTrue(flag);
    }

    @Test
    void testPrioritizeTasksWhenEmptyMap() { // b. С пустым списком задач.
        inMemoryTaskManager.getTasks().clear();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        inMemoryTaskManager.prioritizeTasks();

        String expectedOutput = "Нужно больше задач";
        String actualOutput = outContent.toString().trim();

        assertEquals(expectedOutput, actualOutput);

    }


    // ====================================================================================================

    LocalDateTime testDateTimeTask = LocalDateTime.parse("2000-01-01T00:01:00");

    @Test
    void checkTimeCrossingTestTaskByStartAndEndTime() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        subtask1.setStartTime(testDateTimeTask); // начальное время попадает в отрезок времени task1
        inMemoryTaskManager.addTask(subtask1);

        String expectedOutput = "Для задачи: " + subtask1.getName() + ", нужно другое стартовое время.";
        String actualOutput = outContent.toString().trim();

        assertEquals(expectedOutput, actualOutput);
// ============================
        ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent2));

        subtask1.setStartTime(testDateTimeTask.minusMinutes(2)); // конечное время попадает в отрезок времени task
        subtask1.setDuration(50);
        inMemoryTaskManager.addTask(subtask1);

        String expectedOutput2 = "Для задачи: " + subtask1.getName() + ", нужно другое стартовое время.";
        String actualOutput2 = outContent2.toString().trim();

        assertEquals(expectedOutput2, actualOutput2);
    }

    @Test
    void checkTimeCrossingTestTaskByEquals() { // проверка на совпадение времени
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        subtask1.setStartTime(testDateTimeTask.minusMinutes(1));
        inMemoryTaskManager.addTask(subtask1);

        String expectedOutput = "Для задачи: " + subtask1.getName() + ", нужно другое стартовое время.";
        String actualOutput = outContent.toString().trim();

        assertEquals(expectedOutput, actualOutput);
// ==========================
        ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent2));

        subtask1.setStartTime(testDateTimeTask.minusMinutes(10));
        subtask1.setEndTime(subtask1.getStartTime().plusMinutes(50)); // совпадает с конечным временем на .equals
        subtask1.setDuration(50);
        inMemoryTaskManager.addTask(subtask1);

        String expectedOutput2 = "Для задачи: " + subtask1.getName() + ", нужно другое стартовое время.";
        String actualOutput2 = outContent2.toString().trim();

        assertEquals(expectedOutput2, actualOutput2);
    }

    @Test
    void checkTimeCrossingTestTaskBetweenTime() { // весь отрезок времени находится в уже добавленной задачи
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        subtask1.setStartTime(testDateTimeTask); //"2000-01-01T00:01:00"
        subtask1.setDuration(48);
        inMemoryTaskManager.addTask(subtask1);

        String expectedOutput = "Для задачи: " + subtask1.getName() + ", нужно другое стартовое время.";
        String actualOutput = outContent.toString().trim();
        assertEquals(expectedOutput, actualOutput);
    }
//================================================= Subtask

    @Test
    void checkTimeCrossingTestTaskByStartAndEndTimeSubtask() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        subtask2.setStartTime(testDateTimeTask); // начальное время попадает в отрезок времени task
        inMemoryTaskManager.addTask(subtask2);

        String expectedOutput = "Для задачи: " + subtask2.getName() + ", нужно другое стартовое время.";
        String actualOutput = outContent.toString().trim();

        assertEquals(expectedOutput, actualOutput);
// ============================
        ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent2));

        subtask2.setStartTime(testDateTimeTask.minusMinutes(2)); // конечное время попадает в отрезок времени task
        subtask2.setDuration(50);
        inMemoryTaskManager.addTask(subtask2);

        String expectedOutput2 = "Для задачи: " + subtask2.getName() + ", нужно другое стартовое время.";
        String actualOutput2 = outContent2.toString().trim();

        assertEquals(expectedOutput2, actualOutput2);
    }

    @Test
    void checkTimeCrossingTestTaskByEqualsSubtask() { // проверка на совпадение времени
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        subtask2.setStartTime(testDateTimeTask.minusMinutes(1));
        inMemoryTaskManager.addTask(subtask2);

        String expectedOutput = "Для задачи: " + subtask2.getName() + ", нужно другое стартовое время.";
        String actualOutput = outContent.toString().trim();

        assertEquals(expectedOutput, actualOutput);
// ==========================
        ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent2));

        subtask2.setStartTime(testDateTimeTask.minusMinutes(10));
        subtask2.setDuration(49); // совпадает с конечным временем на .equals
        inMemoryTaskManager.addTask(subtask2);

        String expectedOutput2 = "Для задачи: " + subtask2.getName() + ", нужно другое стартовое время.";
        String actualOutput2 = outContent2.toString().trim();

        assertEquals(expectedOutput2, actualOutput2);
    }

    @Test
    void checkTimeCrossingTestTaskBetweenTimeSubtask() { // весь отрезок времени находится в уже добавленной задачи
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        subtask2.setStartTime(testDateTimeTask);
        subtask2.setDuration(48);
        inMemoryTaskManager.addTask(subtask2);

        String expectedOutput = "Для задачи: " + subtask2.getName() + ", нужно другое стартовое время.";
        String actualOutput = outContent.toString().trim();
        assertEquals(expectedOutput, actualOutput);
    }
//================================================= Epic

    @Test
    void checkTimeCrossingEpicEndTime() { // конечное время эпика задается суммой duration сабтасков

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        LocalDateTime actualEpicEndTime = subtask1.getEndTime();
        LocalDateTime expectedEpicEndTime = inMemoryTaskManager.getTasks().get(epic1.getId()).getEndTime();

        assertEquals(actualEpicEndTime, expectedEpicEndTime);
    }

    // =================================== /case 1/ void addNewTask(Task task) ===================================
/*
  a. Со стандартным поведением. (из ТЗ)
  b. С пустым списком задач.
  c. С неверным идентификатором задачи (пустой и/или несуществующий идентификатор).
 */
    @Test
    void testCase1AddNewTask1() { // a. Со стандартным поведением. (из ТЗ)
//        inMemoryTaskManager.addNewTask(task);
        assertEquals(task1, inMemoryTaskManager.getTasks().get(task1.getId()));
    }

    @Test
    void testCase1AddNewTask2() { // b. С пустым списком задач.
        inMemoryTaskManager.getTasks().clear();
        Task taskActual = inMemoryTaskManager.getTask(task1.getId());
        assertNull(taskActual);
    }

    @Test
    void testCase1AddNewTask3() { // c. С неверным идентификатором задачи (пустой и/или несуществующий идентификатор).
        Subtask taskActual = (Subtask) inMemoryTaskManager.getTask(wrongUuid);
        assertNull(taskActual);
    }
// ====================== /case 2/ List<Task> getAllTasksByTaskType(TaskType taskType) ======================

    @Test
    void testCase2GetAllTasksByTaskTypeWithStandartCondition() {   // a. Со стандартным поведением. (из ТЗ)
        List<Task> epics;
        epics = inMemoryTaskManager.getAllTasksByTaskType(TaskType.EPIC); // return List<Task>
        assertEquals(epics.get(0), inMemoryTaskManager.getTasks().get(epic1.getId()));

        inMemoryTaskManager.getTasks().clear();
        epics = inMemoryTaskManager.getAllTasksByTaskType(TaskType.EPIC);

        assertEquals(new ArrayList<>(), epics);
    }

    @Test
    void testCase2GetAllTasksByTaskTypeFromEmptyMap() {  // b. С пустым списком задач.
        afterEachRemoveTasks();
        List<Task> epics;
        epics = inMemoryTaskManager.getAllTasksByTaskType(TaskType.EPIC);

        assertEquals(new ArrayList<>(), epics);
    }

    // ============================= /case 3/ void removeTasksByTasktype(TaskType taskType) =============================
    @Test
    void testCase3RemoveTasksByTasktypeWithStandartCondition() {  // a. Со стандартным поведением. (из ТЗ)
        assertEquals(TaskType.EPIC, inMemoryTaskManager.getTasks().get(epic1.getId()).getTaskType());
    }

    @Test
    void testCase3RemoveTasksByTasktypeFromEmptyMap() {  // b. С пустым списком задач.
        afterEachRemoveTasks();
        Task taskActual = inMemoryTaskManager.getTask(task1.getId());

        assertNull(taskActual);
    }

// ===================================== /case 4/ Task getTask(UUID taskId) =====================================

    // case 4
    @Test
    void testCase4GetTaskWithStandardCondition() { // 1) все в порядке, задача достается,
        Task taskActual = inMemoryTaskManager.getTask(task1.getId());
        assertEquals(task1, taskActual);
    }

    @Test
    void testCase4getTaskFromEmptyMap() { // 2) список пуст, соответветственно задача не достанется,
        afterEachRemoveTasks();
        Task taskActual = inMemoryTaskManager.getTask(task1.getId());
        assertNull(taskActual);
    }

    @Test
    void testCase4getTaskWithoutOurTaskInMap() { // 3) в списке что-то есть, но нет нашей задачи, задача не достанется
        Task taskTest = new Task(
                testUuid,
                TaskType.TASK,
                "Переезд",
                "Собрать коробки",
                Status.NEW,
                LocalDateTime.parse("2000-01-01T00:00:00"),
                50);
        Subtask taskActual = (Subtask) inMemoryTaskManager.getTask(taskTest.getId());
        assertNull(taskActual);
    }


// ===================================== /case 5/ void updateTask(Task task) =====================================

    @Test
    void testCase5UpdateTaskWithStandardCondition() { // a. Со стандартным поведением. (из ТЗ)
        UUID uuidForTestCase5 = task1.getId();
        Task taskTest = new Task(
                uuidForTestCase5,
                TaskType.TASK,
                "Переезд TECT",
                "Собрать коробки",
                Status.NEW,
                LocalDateTime.parse("2000-01-01T00:00:00"),
                50);
        inMemoryTaskManager.updateTask(taskTest);
        Task taskActual = inMemoryTaskManager.getTask(taskTest.getId());
        assertEquals(taskTest, taskActual);
    }

    @Test
    void testCase5UpdateTaskWhenEmptyMap() { // b. С пустым списком задач.
        afterEachRemoveTasks();
        inMemoryTaskManager.updateTask(task1);
        Map<UUID, Task> shouldBeEmpty = new HashMap<>(inMemoryTaskManager.getTasks());
        assertEquals(new HashMap<>(), shouldBeEmpty);
    }

    @Test
    void testCase5UpdateTaskWithoutOurTaskInMap() { // c. С неверным идентификатором задачи (пустой и/или несуществующий идентификатор).
        afterEachRemoveTasks();
        Task taskTest = new Task(
                testUuid,
                TaskType.TASK,
                "Переезд",
                "Собрать коробки",
                Status.NEW,
                LocalDateTime.parse("2000-01-01T00:00:00"),
                50);
        inMemoryTaskManager.updateTask(taskTest);
        Map<UUID, Task> shouldBeEmpty = new HashMap<>(inMemoryTaskManager.getTasks());
        assertEquals(new HashMap<>(), shouldBeEmpty);
    }

// ===================================== /case 6/ void removeTaskById(UUID id) =====================================

    @Test
    void testCase6RemoveTaskByIdWithStandardCondition() { // a. Со стандартным поведением. (из ТЗ)
        inMemoryTaskManager.removeTaskById(task1.getId());
        assertNull(inMemoryTaskManager.getTasks().get(task1.getId()));
    }

    @Test
    void testCase6RemoveTaskByIdWhenEmptyMap() { // b. С пустым списком задач.
        afterEachRemoveTasks();
        assertNull(inMemoryTaskManager.getTasks().get(task1.getId()));
    }

    @Test
    void testCase6RemoveTaskByIdWithoutOurTaskInMap() { // c. С неверным идентификатором задачи (пустой и/или несуществующий идентификатор).
        Task taskTest = new Task(
                testUuid,
                TaskType.TASK,
                "Переезд",
                "Собрать коробки",
                Status.NEW,
                LocalDateTime.parse("2000-01-01T00:00:00"),
                50);
        NullPointerException ex = assertThrows(NullPointerException.class, () -> {
            inMemoryTaskManager.removeTaskById(taskTest.getId());
        });
        assertTrue(ex.getMessage().contentEquals("Неверный идентификатор задачи"));
    }

// ============================ /case 7/ void changeStatusTask(UUID id, Status status) ============================

    @Test
    void testCase7changeStatusTaskWithStandardCondition() { // a. Со стандартным поведением. (из ТЗ)
        inMemoryTaskManager.changeStatusTask(task1.getId(), Status.IN_PROGRESS);
        assertEquals(Status.IN_PROGRESS, inMemoryTaskManager.getTasks().get(task1.getId()).getStatus());
    }

    @Test
    void testCase7changeStatusTaskWhenEmptyMap() { // b. С пустым списком задач.
        afterEachRemoveTasks();

        assertNull(inMemoryTaskManager.getTasks().get(task1.getId()));
        NullPointerException ex = assertThrows(NullPointerException.class, () -> {
            inMemoryTaskManager.changeStatusTask(task1.getId(), Status.IN_PROGRESS);
        });
        assertTrue(ex.getMessage().contentEquals("Неверный идентификатор задачи"));
    }

    @Test
    void testCase7changeStatusTaskWithoutOurTaskInMap() { // c. С неверным идентификатором задачи (пустой и/или несуществующий идентификатор).
        Epic epic = new Epic(
                testUuid, // тестовый, то есть задача другая
                TaskType.EPIC,
                "Переезд",
                "Переезд",
                Status.NEW,
                LocalDateTime.parse("2000-01-01T00:00:00"),
                0,
                subtasks);
        NullPointerException ex = assertThrows(NullPointerException.class, () -> {
            inMemoryTaskManager.changeStatusTask(epic.getId(), Status.IN_PROGRESS);
        });
        assertTrue(ex.getMessage().contentEquals("Неверный идентификатор задачи"));
    }

// ============================ /case 8/ List<Task> getSubtasksFromEpic(UUID epicId) ============================

    @Test
    void testCase8GetSubtasksFromEpicWithStandardCondition() { // a. Со стандартным поведением. (из ТЗ)
        List<Task> subtasks = new ArrayList<>(inMemoryTaskManager.getSubtasksFromEpic(epic1.getId()));
        boolean flag = inMemoryTaskManager.getTask(subtask1.getId()).equals(subtasks.get(0));
        assertTrue(flag);
    }

    @Test
    void testCase8GetSubtasksFromEpicWhenEmptyMap() { // b. С пустым списком задач.
        afterEachRemoveTasks();
        List<Task> subtask = inMemoryTaskManager.getSubtasksFromEpic(randomUuid);
        assertEquals(new ArrayList<>(), subtask); // "Мапа пуста"
    }

    @Test
    void testCase8GetSubtasksFromEpicWithWrongId() { // c. С неверным идентификатором задачи (пустой и/или несуществующий идентификатор).
        NullPointerException ex = assertThrows(NullPointerException.class, () -> {
            inMemoryTaskManager.getSubtasksFromEpic(randomUuid);
        });
        assertTrue(ex.getMessage().contentEquals("Неверный идентификатор задачи"));
    }

// =================================== /case 9/ List<Task> getHistory() ===================================

    @Test
    void testCase9GetHistoryWithStandardCondition() { // a. Со стандартным поведением. (из ТЗ)
        inMemoryTaskManager.getTask(epic1.getId()); // заполняем историю по порядку
        inMemoryTaskManager.getTask(task1.getId());
        List<Task> tasksByHistory = inMemoryTaskManager.getHistory();
        boolean flag = (tasksByHistory.get(0).getTaskType().equals(TaskType.EPIC) &&
                tasksByHistory.get(1).getTaskType().equals(TaskType.TASK));
        assertTrue(flag);
    }

    @Test
    void testCase9GetHistoryWhenEmptyMap() { // b. С пустой мапой задач.

        List<Task> tasksByHistory = inMemoryTaskManager.getHistory();
        assertEquals(new ArrayList<>(), tasksByHistory);
    }

// =================================== /case 11/ Set<Task> getPrioritizedTasks() ===================================

    @Test
    void testCase11GetPrioritizedTasksWithStandardCondition() { // a. Со стандартным поведением. (из ТЗ)

        boolean flag = false;
        List<Task> tasks = new ArrayList<>(inMemoryTaskManager.prioritizeTasks());
        for (Task task1 : tasks) {
            if (task1.getTaskType().equals(TaskType.TASK) || task1.getTaskType().equals(TaskType.SUBTASK)) { // Эпики в prioritizedTasks не нужны
                flag = true;
            }
        }
        assertTrue(flag);    }

    @Test
    void testCase11GetPrioritizedTasksWhenEmptyMap() {  // b. С пустой мапой задач.
        afterEachRemoveTasks();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        inMemoryTaskManager.prioritizeTasks();

        String expectedOutput = "Нужно больше задач";
        String actualOutput = outContent.toString().trim();

        assertEquals(expectedOutput, actualOutput);

    }

    @Test
    void testGetHistoryTasksAndCheckWithMapOnMatchingWithStandardCondition() { // case-9
        boolean flag = false;
        get();
        List<Task> tasks;
        tasks = inMemoryTaskManager.getHistory();
        for (Task task : tasks) {
            if (inMemoryTaskManager.getTasks().containsKey(task.getId())) {
                flag = true;
            }
        }
        assertTrue(flag);
    }

    @Test
    void testGetHistoryTasksAndCheckWithMapOnMatchingWhenEmptyMap() {
        afterEachRemoveTasks();
        get();
        List<Task> tasks;
        tasks = inMemoryTaskManager.getHistory();
        assertEquals(new ArrayList<>(), tasks);
    }

}