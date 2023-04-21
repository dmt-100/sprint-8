import main.java.intefaces.TaskManager;
import main.java.managers.FileBackedTasksManager;
import main.java.service.Status;
import main.java.service.TaskType;
import main.java.tasks.Epic;
import main.java.tasks.Subtask;
import main.java.tasks.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;

    abstract void setTaskManager();

    private static final String sep = File.separator;
    private static final String saveTasksFilePath = String.join(sep, "src", "main", "java", "resources", "taskSaves" + ".csv");
    private static File file = new File(saveTasksFilePath);
    FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(file);

    public void FileBackedTasksManagerTest(File file) {
        FileBackedTasksManager.file = file;
    }

    List<UUID> subtasks = new ArrayList<>();

    UUID epicUuid = UUID.fromString("11111111-d496-48c2-bb4a-f4cf88f18e23");
    UUID testUuid = UUID.fromString("00000000-0000-48c2-bb4a-f4cf88f18e23");
    UUID wrongUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");



    Task task = new Task(
            TaskType.TASK,
            "Task1",
            "Собрать коробки",
            Status.NEW,
            LocalDateTime.parse("2000-01-01T00:00:00"),
            50
    );

    Task task2 = new Task(
            TaskType.TASK,
            "Task2",
            "Собрать коробки",
            Status.NEW,
            LocalDateTime.parse("2000-01-01T00:00:00"),
            50
    );

    Task epic = new Epic(
            TaskType.EPIC,
            "Эпик1",
            "Переезд",
            Status.NEW,
            LocalDateTime.parse("2000-01-01T02:00:00"),
            0,
            subtasks
    );


    Task epic2 = new Epic(
            TaskType.EPIC,
            "Эпик1",
            "Переезд",
            Status.NEW,
            LocalDateTime.parse("2000-01-01T02:00:00"),
            0,
            subtasks
    );


    Task subtask = new Subtask(
            TaskType.SUBTASK,
            "Subtask1",
            "Собрать коробки",
            Status.NEW,
            LocalDateTime.parse("2000-01-01T04:00:00"),
            50,
            epicUuid
    );

    Task subtask2 = new Subtask(
            TaskType.SUBTASK,
            "Subtask2",
            "Собрать коробки",
            Status.NEW,
            LocalDateTime.parse("2000-01-01T04:00:00"),
            50,
            epicUuid
    );


    Task taskUpdate;
    static UUID uuidTask;
    static UUID randomUuid = UUID.randomUUID();


    // 1) все в порядке, задача достается,
    @BeforeEach
    void createFirst() {
        fileBackedTasksManager.addNewTask(task);
        fileBackedTasksManager.addNewTask(epic);
        subtask.setEpicId(epic.getId());
        fileBackedTasksManager.addNewTask(subtask);
//        epic.setSubtasks(subtask.getId());
    }

    void get() {
        fileBackedTasksManager.getTask(task.getId());
        fileBackedTasksManager.getTask(epic.getId());
        fileBackedTasksManager.getTask(subtask.getId());
    }

    @AfterEach
    void clearHistory() {
        if (fileBackedTasksManager.getTasks().containsKey(task.getId())) {
            fileBackedTasksManager.removeTaskById(task.getId());
        }
        if (fileBackedTasksManager.getTasks().containsKey(subtask.getId())) {
            fileBackedTasksManager.removeTaskById(subtask.getId());
        }
        if (fileBackedTasksManager.getTasks().containsKey(epic.getId())) {
            fileBackedTasksManager.removeTaskById(epic.getId());
        }
        fileBackedTasksManager.getTasks().clear();
    }

// =================================== /case TimeCrossing/ private boolean checkTimeCrossing(Task task)=============
    // task1 time "2000-01-01T00:00:00" duration 50
    LocalDateTime testDateTimeTask = LocalDateTime.parse("2000-01-01T00:01:00");

    // epic1 time "2000-01-01T02:00:00" duration 0
    LocalDateTime testDateTimeEpic = LocalDateTime.parse("2000-01-01T02:01:00");

    // subtask1 time "2000-01-01T04:00:00" duration 50
    LocalDateTime testDateTimeSubtask = LocalDateTime.parse("2000-01-01T04:01:00");

    @Test
    void checkTimeCrossingTestTaskByStartAndEndTime() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        task2.setStartTime(testDateTimeTask); // начальное время попадает в отрезок времени task
        fileBackedTasksManager.addNewTask(task2);

        String expectedOutput = "Для задачи: " + task2.getName() + ", нужно  другое стартовое время.";
        String actualOutput = outContent.toString().trim();

        assertEquals(expectedOutput, actualOutput);
// ============================
        ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent2));

        task2.setStartTime(testDateTimeTask.minusMinutes(2)); // конечное время попадает в отрезок времени task
        task2.setDuration(50);
        fileBackedTasksManager.addNewTask(task2);

        String expectedOutput2 = "Для задачи: " + task2.getName() + ", нужно  другое конечное время.";
        String actualOutput2 = outContent2.toString().trim();

        assertEquals(expectedOutput2, actualOutput2);
    }

    @Test
    void checkTimeCrossingTestTaskByEquals() { // проверка на совпадение времени
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        task2.setStartTime(testDateTimeTask.minusMinutes(1));
        fileBackedTasksManager.addNewTask(task2);

        String expectedOutput = "Для задачи: " + task2.getName() + ", нужно  другое стартовое время.";
        String actualOutput = outContent.toString().trim();

        assertEquals(expectedOutput, actualOutput);
// ==========================
        ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent2));

        task2.setStartTime(testDateTimeTask.minusMinutes(10));
        task2.setDuration(49); // совпадает с конечным временем на .equals
        fileBackedTasksManager.addNewTask(task2);

        String expectedOutput2 = "Для задачи: " + task2.getName() + ", нужно  другое конечное время.";
        String actualOutput2 = outContent2.toString().trim();

        assertEquals(expectedOutput2, actualOutput2);
    }

    @Test
    void checkTimeCrossingTestTaskBetweenTime() { // весь отрезок времени находится в уже добавленной задачи
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        task2.setStartTime(testDateTimeTask);
        task2.setDuration(48);
        fileBackedTasksManager.addNewTask(task2);

        String expectedOutput = "Для задачи: " + task2.getName() + ", нужно  другое стартовое время.";
        String actualOutput = outContent.toString().trim();
        assertEquals(expectedOutput, actualOutput);
    }
//================================================= Subtask

    @Test
    void checkTimeCrossingTestTaskByStartAndEndTimeSubtask() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        subtask2.setStartTime(testDateTimeTask); // начальное время попадает в отрезок времени task
        fileBackedTasksManager.addNewTask(subtask2);

        String expectedOutput = "Для задачи: " + subtask2.getName() + ", нужно  другое стартовое время.";
        String actualOutput = outContent.toString().trim();

        assertEquals(expectedOutput, actualOutput);
// ============================
        ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent2));

        subtask2.setStartTime(testDateTimeTask.minusMinutes(2)); // конечное время попадает в отрезок времени task
        subtask2.setDuration(50);
        fileBackedTasksManager.addNewTask(subtask2);

        String expectedOutput2 = "Для задачи: " + subtask2.getName() + ", нужно  другое конечное время.";
        String actualOutput2 = outContent2.toString().trim();

        assertEquals(expectedOutput2, actualOutput2);
    }

    @Test
    void checkTimeCrossingTestTaskByEqualsSubtask() { // проверка на совпадение времени
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        subtask2.setStartTime(testDateTimeTask.minusMinutes(1));
        fileBackedTasksManager.addNewTask(subtask2);

        String expectedOutput = "Для задачи: " + subtask2.getName() + ", нужно  другое стартовое время.";
        String actualOutput = outContent.toString().trim();

        assertEquals(expectedOutput, actualOutput);
// ==========================
        ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent2));

        subtask2.setStartTime(testDateTimeTask.minusMinutes(10));
        subtask2.setDuration(49); // совпадает с конечным временем на .equals
        fileBackedTasksManager.addNewTask(subtask2);

        String expectedOutput2 = "Для задачи: " + subtask2.getName() + ", нужно  другое конечное время.";
        String actualOutput2 = outContent2.toString().trim();

        assertEquals(expectedOutput2, actualOutput2);
    }

    @Test
    void checkTimeCrossingTestTaskBetweenTimeSubtask() { // весь отрезок времени находится в уже добавленной задачи
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        subtask2.setStartTime(testDateTimeTask);
        subtask2.setDuration(48);
        fileBackedTasksManager.addNewTask(subtask2);

        String expectedOutput = "Для задачи: " + subtask2.getName() + ", нужно  другое стартовое время.";
        String actualOutput = outContent.toString().trim();
        assertEquals(expectedOutput, actualOutput);
    }
//================================================= Epic

    @Test
    void checkTimeCrossingEpicEndTime() { // конечное время эпика задается суммой duration сабтасков

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        LocalDateTime actualEpicEndTime = subtask.getEndTime();
        LocalDateTime expectedEpicEndTime = fileBackedTasksManager.getTasks().get(epic.getId()).getEndTime();

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
//        fileBackedTasksManager.addNewTask(task);
        assertEquals(task, fileBackedTasksManager.getTasks().get(task.getId()));
    }

    @Test
    void testCase1AddNewTask2() { // b. С пустым списком задач.
        fileBackedTasksManager.getTasks().clear();
        Task taskActual = fileBackedTasksManager.getTask(task.getId());
        assertNull(taskActual);
    }

    @Test
    void testCase1AddNewTask3() { // c. С неверным идентификатором задачи (пустой и/или несуществующий идентификатор).
        Subtask taskActual = (Subtask) fileBackedTasksManager.getTask(wrongUuid);
        assertNull(taskActual);
    }
// ====================== /case 2/ List<Task> getAllTasksByTaskType(TaskType taskType) ======================

    @Test
    void testCase2GetAllTasksByTaskTypeWithStandartCondition() {   // a. Со стандартным поведением. (из ТЗ)
        List<Task> epics;
        epics = fileBackedTasksManager.getAllTasksByTaskType(TaskType.EPIC); // return List<Task>
        assertEquals(epics.get(0), fileBackedTasksManager.getTasks().get(epic.getId()));

        fileBackedTasksManager.getTasks().clear();
        epics = fileBackedTasksManager.getAllTasksByTaskType(TaskType.EPIC);

        assertEquals(new ArrayList<>(), epics);
    }

    @Test
    void testCase2GetAllTasksByTaskTypeFromEmptyMap() {  // b. С пустым списком задач.
        clearHistory();
        List<Task> epics;
        epics = fileBackedTasksManager.getAllTasksByTaskType(TaskType.EPIC);

        assertEquals(new ArrayList<>(), epics);
    }

    // ============================= /case 3/ void removeTasksByTasktype(TaskType taskType) =============================
    @Test
    void testCase3RemoveTasksByTasktypeWithStandartCondition() {  // a. Со стандартным поведением. (из ТЗ)
        assertEquals(TaskType.EPIC, fileBackedTasksManager.getTasks().get(epic.getId()).getTaskType());
    }

    @Test
    void testCase3RemoveTasksByTasktypeFromEmptyMap() {  // b. С пустым списком задач.
        clearHistory();
        Task taskActual = fileBackedTasksManager.getTask(task.getId());

        assertNull(taskActual);
    }

// ===================================== /case 4/ Task getTask(UUID taskId) =====================================

    // case 4
    @Test
    void testCase4GetTaskWithStandardCondition() { // 1) все в порядке, задача достается,
        Task taskActual = fileBackedTasksManager.getTask(task.getId());
        assertEquals(task, taskActual);
    }

    @Test
    void testCase4getTaskFromEmptyMap() { // 2) список пуст, соответветственно задача не достанется,
        clearHistory();
        Task taskActual = fileBackedTasksManager.getTask(task.getId());
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
        Subtask taskActual = (Subtask) fileBackedTasksManager.getTask(taskTest.getId());
        assertNull(taskActual);
    }


// ===================================== /case 5/ void updateTask(Task task) =====================================

    @Test
    void testCase5UpdateTaskWithStandardCondition() { // a. Со стандартным поведением. (из ТЗ)
        UUID uuidForTestCase5 = task.getId();
        Task taskTest = new Task(
                uuidForTestCase5,
                TaskType.TASK,
                "Переезд TECT",
                "Собрать коробки",
                Status.NEW,
                LocalDateTime.parse("2000-01-01T00:00:00"),
                50);
        fileBackedTasksManager.updateTask(taskTest);
        Task taskActual = fileBackedTasksManager.getTask(taskTest.getId());
        assertEquals(taskTest, taskActual);
    }

    @Test
    void testCase5UpdateTaskWhenEmptyMap() { // b. С пустым списком задач.
        clearHistory();
        fileBackedTasksManager.updateTask(task);
        Map<UUID, Task> shouldBeEmpty = new HashMap<>(fileBackedTasksManager.getTasks());
        assertEquals(new HashMap<>(), shouldBeEmpty);
    }

    @Test
    void testCase5UpdateTaskWithoutOurTaskInMap() { // c. С неверным идентификатором задачи (пустой и/или несуществующий идентификатор).
        clearHistory();
        Task taskTest = new Task(
                testUuid,
                TaskType.TASK,
                "Переезд",
                "Собрать коробки",
                Status.NEW,
                LocalDateTime.parse("2000-01-01T00:00:00"),
                50);
        fileBackedTasksManager.updateTask(taskTest);
        Map<UUID, Task> shouldBeEmpty = new HashMap<>(fileBackedTasksManager.getTasks());
        assertEquals(new HashMap<>(), shouldBeEmpty);
    }

// ===================================== /case 6/ void removeTaskById(UUID id) =====================================

    @Test
    void testCase6RemoveTaskByIdWithStandardCondition() { // a. Со стандартным поведением. (из ТЗ)
        fileBackedTasksManager.removeTaskById(task.getId());
        assertNull(fileBackedTasksManager.getTasks().get(task.getId()));
    }

    @Test
    void testCase6RemoveTaskByIdWhenEmptyMap() { // b. С пустым списком задач.
        clearHistory();
        assertNull(fileBackedTasksManager.getTasks().get(task.getId()));
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
            fileBackedTasksManager.removeTaskById(taskTest.getId());
        });
        assertTrue(ex.getMessage().contentEquals("Неверный идентификатор задачи"));
    }

// ============================ /case 7/ void changeStatusTask(UUID id, Status status) ============================

    @Test
    void testCase7changeStatusTaskWithStandardCondition() { // a. Со стандартным поведением. (из ТЗ)
        fileBackedTasksManager.changeStatusTask(task.getId(), Status.IN_PROGRESS);
        assertEquals(Status.IN_PROGRESS, fileBackedTasksManager.getTasks().get(task.getId()).getStatus());
    }

    @Test
    void testCase7changeStatusTaskWhenEmptyMap() { // b. С пустым списком задач.
        clearHistory();

        assertNull(fileBackedTasksManager.getTasks().get(task.getId()));
        NullPointerException ex = assertThrows(NullPointerException.class, () -> {
            fileBackedTasksManager.changeStatusTask(task.getId(), Status.IN_PROGRESS);
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
            fileBackedTasksManager.changeStatusTask(epic.getId(), Status.IN_PROGRESS);
        });
        assertTrue(ex.getMessage().contentEquals("Неверный идентификатор задачи"));
    }

// ============================ /case 8/ List<Task> getSubtasksFromEpic(UUID epicId) ============================

    @Test
    void testCase8GetSubtasksFromEpicWithStandardCondition() { // a. Со стандартным поведением. (из ТЗ)
        List<Task> subtasks = new ArrayList<>(fileBackedTasksManager.getSubtasksFromEpic(epic.getId()));
        boolean flag = fileBackedTasksManager.getTask(subtask.getId()).equals(subtasks.get(0));
        assertTrue(flag);
    }

    @Test
    void testCase8GetSubtasksFromEpicWhenEmptyMap() { // b. С пустым списком задач.
        clearHistory();

        assertEquals(new ArrayList<>(), fileBackedTasksManager.getSubtasksFromEpic(randomUuid)); // "Мапа пуста"
    }

    @Test
    void testCase8GetSubtasksFromEpicWithWrongId() { // c. С неверным идентификатором задачи (пустой и/или несуществующий идентификатор).
        NullPointerException ex = assertThrows(NullPointerException.class, () -> {
            fileBackedTasksManager.getSubtasksFromEpic(randomUuid);
        });
        assertTrue(ex.getMessage().contentEquals("Неверный идентификатор задачи"));
    }

// =================================== /case 9/ List<Task> getHistory() ===================================

    @Test
    void testCase9GetHistoryWithStandardCondition() { // a. Со стандартным поведением. (из ТЗ)
        fileBackedTasksManager.getTask(epic.getId()); // заполняем историю по порядку
        fileBackedTasksManager.getTask(task.getId());
        List<Task> tasksByHistory = fileBackedTasksManager.getHistory();
        boolean flag = (tasksByHistory.get(0).getTaskType().equals(TaskType.EPIC) &&
                tasksByHistory.get(1).getTaskType().equals(TaskType.TASK));
        assertTrue(flag);
    }

    @Test
    void testCase9GetHistoryWhenEmptyMap() { // b. С пустой мапой задач.

        List<Task> tasksByHistory = fileBackedTasksManager.getHistory();
        assertEquals(new ArrayList<>(), tasksByHistory);
    }

// =================================== /case 11/ Set<Task> getPrioritizedTasks() ===================================

    @Test
    void testCase11GetPrioritizedTasksWithStandardCondition() { // a. Со стандартным поведением. (из ТЗ)
        clearHistory();
        fileBackedTasksManager.addNewTask(task);
        fileBackedTasksManager.addNewTask(epic);
        subtask.setEpicId(epic.getId());
        fileBackedTasksManager.addNewTask(subtask);

        boolean flag = false;
        List<Task> tasks = new ArrayList<>(fileBackedTasksManager.getPrioritizedTasks());
        for (Task task1 : tasks) {
            if (task1.getTaskType().equals(TaskType.TASK) || task1.getTaskType().equals(TaskType.SUBTASK)) { // Эпики в prioritizedTasks не нужны
                flag = true;
            }
        }
        assertTrue(flag);    }

    @Test
    void testCase11GetPrioritizedTasksWhenEmptyMap() {  // b. С пустой мапой задач.
        clearHistory();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        fileBackedTasksManager.prioritizeTasks();

        String expectedOutput = "Нужно ещё больше задач";
        String actualOutput = outContent.toString().trim();

        assertEquals(expectedOutput, actualOutput);

    }
}