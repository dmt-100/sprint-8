package main.java;

import main.java.intefaces.TasksManager;
import main.java.managers.FileBackedTasksManager;
import main.java.managers.HttpTaskManager;
import main.java.managers.Managers;
import main.java.server.KVServer;
import main.java.service.*;
import main.java.tasks.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    private static final String sep = File.separator;
    private static final String saveTasksFilePath = String.join(sep, "src", "main", "java", "resources", "taskSaves" + ".csv");
    private static final File file = new File(saveTasksFilePath);



    public static void main(String[] args) throws IOException, InterruptedException {

//        new KVServer().start();

        Scanner scanner = new Scanner(System.in);
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(file);
        HttpTaskManager httpTaskManager = Managers.getDefault();

        // для тестов на пересечение
        LocalDateTime dateTimeTestTask1 = LocalDateTime.parse("2000-01-01T01:00:00");
        LocalDateTime dateTimeTestTask2 = LocalDateTime.parse("2000-01-01T02:00:00");
        LocalDateTime dateTimeTestTask3 = LocalDateTime.parse("2000-01-01T03:00:00");
        LocalDateTime dateTimeTestTask4 = LocalDateTime.parse("2000-01-01T04:00:00");

        LocalDateTime dateTimeTestEpic1 = LocalDateTime.parse("2000-01-01T05:00:00");
        LocalDateTime dateTimeTestSubtask1 = LocalDateTime.parse("2000-01-01T06:00:00");
        LocalDateTime dateTimeTestSubtask2 = LocalDateTime.parse("2000-01-01T07:40:00");

        UUID epicUuid = UUID.fromString("11111111-d496-48c2-bb4a-f4cf88f18e23");
//        UUID subtask1Uuid = UUID.fromString("22222222-d496-48c2-bb4a-f4cf88f18e23");
//        UUID subtask2Uuid = UUID.fromString("33333333-d496-48c2-bb4a-f4cf88f18e23");

        Task task1 = new Task(
                TaskType.TASK,
                "Переезд1",
                "Собрать коробки",
                Status.NEW,
                dateTimeTestTask1,
                50
        );

        List<UUID> subtasksList = new ArrayList<>();
        Epic epic1 = new Epic(
                epicUuid,
                TaskType.EPIC,
                "Эпик1",
                "Переезд",
                Status.NEW,
                dateTimeTestEpic1,
                0,
                subtasksList
        );

        Epic epic2 = new Epic(
                TaskType.EPIC,
                "Эпик2",
                "Переезд2",
                Status.NEW,
                subtasksList
        );


        boolean menu = true;
        while (menu) {
            printMenu();

            int userInput = scanner.nextInt();
            switch (userInput) {
                case 1: // Получение всех задач
                    printMenuCase1();
                    int userInputCase1 = scanner.nextInt();
                    switch (userInputCase1) {
                        case 1:
                            httpTaskManager.addTask(new Task(
                                    TaskType.TASK,
                                    "Задача1",
                                    "Собрать коробки",
                                    Status.NEW,
                                    dateTimeTestTask1,
                                    50
                            ));

                            httpTaskManager.addTask(new Task(
                                    TaskType.TASK,
                                    "Задача2",
                                    "Упаковать кошку",
                                    Status.NEW,
                                    dateTimeTestTask2,
                                    5
                            ));
                            httpTaskManager.addTask(new Task(
                                    TaskType.TASK,
                                    "Задача3",
                                    "Собрать коробки",
                                    Status.NEW,
                                    dateTimeTestTask3,
                                    50
                            ));
                            httpTaskManager.addTask(new Task(
                                    TaskType.TASK,
                                    "Задача4",
                                    "Упаковать кошку",
                                    Status.NEW,
                                    dateTimeTestTask4,
                                    5
                            ));
                            break;
                        case 2:
                            httpTaskManager.addTask(epic1);
                            break;
                        case 3:
//                            System.out.println("К какому эпику будет относиться ваша задача?");
//                            System.out.println("Введите id эпика:");
//                            UUID epicId = UUID.fromString(scanner.next());
                            Subtask subtask1 = new Subtask(
                                    TaskType.SUBTASK,
                                    "Подзадача1",
                                    "Собрать коробки",
                                    Status.NEW,
                                    dateTimeTestSubtask1,
                                    50,
                                    httpTaskManager.getTasks().get(epic1.getId()).getId()
                            );

                            Subtask subtask2 = new Subtask(
                                    TaskType.SUBTASK,
                                    "Подзадача2",
                                    "Упаковать кошку",
                                    Status.NEW,
                                    dateTimeTestSubtask2,
                                    15,
                                    httpTaskManager.getTasks().get(epic1.getId()).getId()
                            );
                            httpTaskManager.addTask(subtask1);
                            httpTaskManager.addTask(subtask2);
                            break;
                    }
                    break;

                case 2: // Получение всех задач
                    printMenuCase2();
                    TaskType taskType2;
                    int userInputCase2 = scanner.nextInt();
                    switch (userInputCase2) {
                        case 1:
                            taskType2 = TaskType.TASK;
                            System.out.println(httpTaskManager.getAllTasksByTaskType(taskType2));
                            break;
                        case 2:
                            taskType2 = TaskType.EPIC;
                            System.out.println(httpTaskManager.getAllTasksByTaskType(taskType2));
                            break;
                        case 3:
                            taskType2 = TaskType.SUBTASK;
                            System.out.println(httpTaskManager.getAllTasksByTaskType(taskType2));
                            break;
                    }
                    break;

                case 3: // Удаление всех задач
                    printMenuCase3();
                    TaskType taskType3;
                    int userInputCase3 = scanner.nextInt();
                    switch (userInputCase3) {
                        case 1:
                            taskType3 = TaskType.TASK;
                            httpTaskManager.removeTasksByTasktype(taskType3);
                            break;
                        case 2:
                            taskType3 = TaskType.EPIC;
                            httpTaskManager.removeTasksByTasktype(taskType3);
                            break;
                        case 3:
                            taskType3 = TaskType.SUBTASK;
                            httpTaskManager.removeTasksByTasktype(taskType3);
                            break;
                    }
                    break;

                case 4: // получение по id
                    System.out.println("Введите номер идентификатора");
                    UUID taskId = UUID.fromString(scanner.next());
                    httpTaskManager.getTask(taskId);

                    break;

                case 5: // обновление по id Посмотреть ТЗ по Id или сама задачи
                    printMenuCase5();
                    int userInputCase5 = scanner.nextInt();
                    System.out.println("Введите идентификатор той задачи которую хотите обновить");
                    String taskIdCase5 = scanner.next();
                    switch (userInputCase5) {
                        case 1:
                            task1.setDescription("Сказать слова прощания test Case5");
                            httpTaskManager.updateTask(task1);
                            break;
                        case 2:

                            break;
                        case 3:
                            Subtask subtaskTest2 = new Subtask(TaskType.SUBTASK, "тест1",
                                    "Собрать коробки", Status.NEW, LocalDateTime.now(), 50, epic1.getId()); // исправить
                            break;
                    }
                    break;

                case 6: // Удаление по идентификатору.
                    System.out.println("Введите идентификатор для удаления");
                    UUID id = UUID.fromString(scanner.next());
                    httpTaskManager.removeTaskById(id);
                    break;

                case 7: // Изменить статус
                    System.out.println("Введите id задачи, чей статус хотите поменять");
                    UUID statusId = UUID.fromString(scanner.next());
                    System.out.println("Назначьте статус, где:\n1 - Задача новая\n" + "2 - Задача выполнена\n3 - Задача в действии");
                    int check = scanner.nextInt();
                    Status status = null;
                    switch (check) {
                        case 1:
                            status = Status.NEW;
                            break;
                        case 2:
                            status = Status.DONE;
                            break;
                        case 3:
                            status = Status.IN_PROGRESS;
                            break;
                    }
                    httpTaskManager.changeStatusTask(statusId, status);
                    break;

                case 8: // Получение списка всех подзадач определённого эпика.
                    System.out.println("Получение списка всех подзадач определённого эпика\n" + "Введите id эпика, чтобы получить его подзадачи:");
                    UUID epicId = UUID.fromString(scanner.next());
                    httpTaskManager.getSubtasksFromEpic(epicId).forEach(System.out::println);
                    break;

                // ТЗ-4
                case 9: // Информация по просмотрам.
                    System.out.println("Какие задачи были просмотрены:");
                    httpTaskManager.getHistory().forEach(System.out::println);
                    break;

                case 10: // Получить все задачи (список)

                    break;

                case 11: // сортировка по стартовому времени
                    httpTaskManager.getPrioritizedTasks();
                    break;

                case 12: // Тест ТЗ-8
                    httpTaskManager.setHttpTaskManager(httpTaskManager);
                    httpTaskManager.test();

                    break;

                case 0: // Выход
                    menu = false;
                    break;
            }
        }
    }

    public static void printMenu() {
        System.out.println("1 - Добавить новую задачу");
        System.out.println("2 - Получить список всех задач");
        System.out.println("3 - Удалить все задачи");
        System.out.println("4 - Посмотреть задачу по идентификатору");
        System.out.println("5 - Обновить задачу по идентификатору");
        System.out.println("6 - Удалить задачу по идентификатору");
        System.out.println("7 - Изменить статус задачи");
        System.out.println("8 - Получение списка всех подзадач определённого эпика");
        System.out.println("9 - Информация по просмотрам задач");
        System.out.println("10 - Посмотреть все задачи");
        System.out.println("11 - Сортировать задачи по началу времени");
        System.out.println("12 - Тестирования httpTaskManager");

        System.out.println("0 - Выход");
    }

    public static void printMenuCase1() {
        System.out.println("1 - Добавить новую задачу");
        System.out.println("2 - Добавить новый эпик");
        System.out.println("3 - Добавить новую подзадачу");
    }

    public static void printMenuCase2() {
        System.out.println("1 - Получить все задачи");
        System.out.println("2 - Получить все эпики");
        System.out.println("3 - Получить все подзадачи");
    }

    public static void printMenuCase3() {
        System.out.println("1 - Удалить все задачи");
        System.out.println("2 - Удалить все эпикови");
        System.out.println("3 - Удалить все подзадачи");
    }

    public static void printMenuCase5() {
        System.out.println("1 - Обновление задачи по идентификатору");
        System.out.println("2 - Обновление епика по идентификатору");
        System.out.println("3 - Обновление подзадачи по идентификатору");
    }

}