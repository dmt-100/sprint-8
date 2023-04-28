package main.java.managers;

import main.java.intefaces.HistoryManager;
import main.java.service.ManagerSaveException;
import main.java.service.Status;
import main.java.service.TaskType;
import main.java.tasks.Epic;
import main.java.tasks.Subtask;
import main.java.tasks.Task;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class FileBackedTasksManager extends InMemoryTaskManager {

    private static final String sep = File.separator;
    private static final String saveTasksFilePath = String.join(sep, "src", "main", "java", "resources", "taskSaves" + ".csv");
    public static File file = new File(saveTasksFilePath);

    public FileBackedTasksManager(File file) {
        FileBackedTasksManager.file = file;
    }

    public FileBackedTasksManager() {
    }

    private static FileBackedTasksManager loadFromFile(File file) {
        var manager = new FileBackedTasksManager(file);
        try {
            manager.historyFromString(file);
        } catch (IOException e) {
            throw new ManagerSaveException("Файл с состоянием таск менеджера не найден или поврежден");
        }
        return manager;
    }


    protected void save() {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {

            out.write("id,type,name,description,status,startTime,endTime,duration,epic\n");

            for (Task task : getTasks().values()) {

                out.write(task.toCsvFormat() + "\n");

            }
            out.write("\n"); // пустая строка после задач
            var lastLine = historyToString(getHistoryManager());
            if (!getTasks().isEmpty()) {

                out.write(lastLine);

            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerSaveException("файл не найден");
        }
    }

    private static String historyToString(HistoryManager manager) {
        return manager.getTasksInHistory().stream()
                .map(task -> task.getId().toString()).collect(Collectors.joining(","));
    }

    // метод возвращает последнюю строку с просмотренными задачами из файла
    private List<String> historyFromString(File file) throws IOException {
        BufferedReader br;
        List<String> listString = new ArrayList<>();
        br = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line = br.readLine()) != null) {
            if (line.isEmpty()) {
                line = br.readLine();
                break;
            }
        }
        if (line != null && !line.isEmpty()) {
            listString = Arrays.asList(line.split(","));
        } else {
            return listString;
        }
        return listString;
    }

    // ТЗ-6. Напишите метод создания задачи из строки
    private List<Task> taskfromString() {
        List<Task> tasks = new ArrayList<>(); // Добавил задачи сразу в лист
        Task task;
        Epic epic;
        Subtask subtask;

        UUID id;
        TaskType taskType;
        String name;
        String description;
        Status status;
        LocalDateTime startTime;
        LocalDateTime endTime;
        int duration = 0;
        List<UUID> list = new ArrayList<>();
        UUID epicId = null;

        boolean check = true;
        while (check) {

            Map<UUID, UUID> subtasksOfEpicField = new HashMap<>();
            List<UUID> epicsSubtasks = new ArrayList<>();
            List<Epic> epicList = new ArrayList<>();

            for (List<String> line : readFromCsvTasks()) {
                if (!(line.get(0).isEmpty())) {

                    id = UUID.fromString(String.valueOf(line.get(0)));
                    taskType = TaskType.valueOf(line.get(1));
                    name = line.get(2);
                    description = line.get(3);
                    status = Status.valueOf(line.get(4));
                    startTime = LocalDateTime.parse(line.get(5));
                    endTime = LocalDateTime.parse(line.get(6));
                    duration = Integer.parseInt(line.get(7));
                    if (line.size() == 9) {
                        epicId = UUID.fromString(line.get(8));
                    }

                    if (line.get(1).equals(TaskType.TASK.toString())) {
                        task = new Task(id, taskType, name, description, status, startTime, endTime, duration);
                        tasks.add(task);
                    } else if (line.get(1).equals(TaskType.EPIC.toString())) {
                        epic = new Epic(id, taskType, name, description, status, startTime, endTime, duration, list);
                        epicList.add(epic);
                    }

                    if (line.get(1).equals(TaskType.SUBTASK.toString())) {
                        subtask = new Subtask(id, taskType, name, description, status, startTime, endTime, duration, epicId);
                        tasks.add(subtask);
                        subtasksOfEpicField.put(id, epicId);
                    }
                }
            }
            for (Epic ep : epicList) {
                epicsSubtasks = subtasksOfEpicField.entrySet()
                        .stream()
                        .filter(e -> Objects.equals(e.getValue(), ep.getId()))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
                for (UUID uuid : epicsSubtasks) {
                    ep.setSubtasks(uuid);
                }
                tasks.add(ep);
            }
            check = false;
        }
        return tasks;
    }

    private List<List<String>> readFromCsvTasks() {
        List<List<String>> addedTasks = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            List<String> innerList;
            while ((line = br.readLine()) != null) {
                innerList = new ArrayList<>(Arrays.asList(line.split(",")));
                addedTasks.add(innerList);
            }
            addedTasks.remove(0);
            addedTasks.remove(addedTasks.size() - 1);
            addedTasks.remove(addedTasks.size() - 1);
            return addedTasks;
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }

    public List<Task> getHistoryTasks() {
        List<Task> tasks;
        if (readCsvHistoryFromFile().size() > 1) {
            tasks = readCsvHistoryFromFile().stream()
                    .flatMap(s -> taskfromString().stream()
                            .filter(task -> task.getId().toString().equals(s)))
                    .collect(Collectors.toList());
            save();
        } else {
            tasks = new ArrayList<>();
        }
        return tasks;
    }

    private List<String> readCsvHistoryFromFile() {
        List<UUID> listOfAddedTasks = null;
        List<String> listOfStrings;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String lastLine = null;
            String line = br.readLine();
            while (line != null) {
                lastLine = line;
                line = br.readLine();
            }
            if (lastLine == null) {
                throw new ManagerSaveException("File is empty.");
            }
            listOfStrings = new ArrayList<>(Arrays.asList(lastLine.split(",")));
            line = br.readLine();
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
        return listOfStrings;
    }

// переопределенные методы InMemoryTaskManager
    @Override
    public void addTask(Task task) { // добавление
        super.addTask(task);
        save();
    }

    @Override
    public Task getTask(UUID idInput) { // просмотр
        Task task = super.getTask(idInput);
        save();
        return task;
    }

    @Override
    public void updateTask(Task task) { // обновление
        super.updateTask(task);
        save();
    }

    @Override
    public void removeTaskById(UUID id) { // удаление
        super.removeTaskById(id);
        save();
    }

    @Override
    public List<Task> getAllTasksByTaskType(TaskType taskType) { // просмотр всех задач
        List<Task> allTasksByTaskType = super.getAllTasksByTaskType(taskType);
        save();
        return allTasksByTaskType;
    }

    @Override
    public void removeTasksByTasktype(TaskType taskType) { // удаление всех задач
        super.removeTasksByTasktype(taskType);
        save();
    }

    @Override
    public void changeStatusTask(UUID id, Status status) { // изменение статуса
        super.changeStatusTask(id, status);
        save();
    }

    @Override
    public List<Task> getSubtasksFromEpic(UUID epicId) { // Получение списка всех подзадач определённого эпика
        List<Task> subtasks = super.getSubtasksFromEpic(epicId);
        save();
        return subtasks;
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = super.getHistory();
        save();
        return history;
    }

    @Override
    public List<Task> getAllTasks() {
        save();
        return super.getAllTasks();
    }

    @Override
    public List<Task> prioritizeTasks() {
        save();
        return super.prioritizeTasks();
    }

}