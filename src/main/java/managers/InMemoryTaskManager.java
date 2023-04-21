package main.java.managers;

import main.java.intefaces.HistoryManager;
import main.java.intefaces.TaskManager;
import main.java.service.Status;
import main.java.service.TaskType;
import main.java.tasks.Epic;
import main.java.tasks.Task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private final Map<UUID, Task> tasks = new HashMap<>();
    private List<Task> prioritizedTasks = new ArrayList<>();
//    private List<Task> prioritizedTasks2 = new ArrayList<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public void addNewTask(Task task) {
        task.setId(java.util.UUID.randomUUID());

        LocalDateTime startTime = task.getStartTime();
        LocalDateTime endTime = task.getEndTime();

        TaskType taskType = task.getTaskType();

        try {
            if (!tasks.isEmpty()) {
                if (taskType.equals(TaskType.TASK) || taskType.equals(TaskType.EPIC)) {

                    if (checkTimeCrossing(startTime, endTime, task.getName())) {
                        tasks.put(task.getId(), task);
                        System.out.println("Задача: " + task.getName() + ", успешно добавлена");
                    }
                } else {

                    if (checkTimeCrossing(startTime, endTime, task.getName())) {
                        tasks.put(task.getId(), task);
                        System.out.println("Подзадача: " + task.getName() + ", успешно добавлена");

                        Epic epic = (Epic) tasks.get(task.getEpicId());

                        List<UUID> subtasksUuids = tasks.get(task.getEpicId()).getSubtasks();
                        if (subtasksUuids.size() == 0) {
                            epic.setStartTime(startTime);
                            epic.setEndTime(endTime);

                        } else {
                            for (UUID subtasksUuid : subtasksUuids) {
                                if (tasks.get(subtasksUuid).getStartTime().isAfter(startTime)) {
                                    epic.setStartTime(startTime); // назначение времени если новая подзадача по стартовому времени впереди всех остальных подзадач
                                }
                                if (tasks.get(subtasksUuid).getEndTime().isBefore(endTime)) {
                                    epic.setEndTime(endTime); // последнее время последней по времени подзадачи
                                }
                            }
                        }
                        epic.setSubtasks(task.getId());
                        epic.setDuration(epic.getDuration() + task.getDuration()); //сумма продолжительности подзадач

                        updateTask(epic);
                    }
                }
            } else {
                tasks.put(task.getId(), task);
                System.out.println("Задача: " + task.getName() + ", успешно добавлена");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private boolean checkTimeCrossing(LocalDateTime startTime, LocalDateTime endTime, String name) {

        for (Task taskInMap : tasks.values()) {
            LocalDateTime taskStartTime = taskInMap.getStartTime();
            LocalDateTime taskEndTime = taskInMap.getEndTime();

            if (taskStartTime.isEqual(startTime)) {
                System.out.println("Для задачи: " + name + ", нужно  другое стартовое время.");
                return false;
            }
            if (taskEndTime.isEqual(endTime)) {
                System.out.println("Для задачи: " + name + ", нужно  другое конечное время.");
                return false;
            }

            if ((taskStartTime.isBefore(startTime) && taskEndTime.isAfter(startTime))) {
                System.out.println("Для задачи: " + name + ", нужно  другое стартовое время.");
                return false;
            }
            if (taskStartTime.isBefore(endTime) && taskEndTime.isAfter(endTime)) {
                System.out.println("Для задачи: " + name + ", нужно  другое конечное время.");
                return false;
            }
        }
        return true;
    }

    // case 2: Получение списка всех задач.-------------------------------------
    @Override
    public List<Task> getAllTasksByTaskType(TaskType taskType) {
        List<Task> list;
        if (!tasks.isEmpty()) {
            list = tasks.values().stream().filter(task -> task
                            .getTaskType().equals(taskType))
                    .collect(Collectors.toList());

        } else {
            return Collections.emptyList();
        }
        return list;
    }

    // case 3: Удаление всех задач по типу.---------------------------------------
    @Override
    public void removeTasksByTasktype(TaskType taskType) {
        if (taskType.equals(TaskType.SUBTASK)) {
            tasks.values().forEach(t -> t.getSubtasks().clear());
            tasks.values().forEach(t -> historyManager.remove(t.getId()));

            LocalDateTime defaultTime = LocalDateTime.parse("2000-01-01 00:00:00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US));

            tasks.values().stream()
                    .filter(t -> t.getTaskType().equals(TaskType.EPIC))
                    .forEach(t -> t.setStartTime(defaultTime));
            tasks.values().stream()
                    .filter(t -> t.getTaskType().equals(TaskType.EPIC))
                    .forEach(t -> t.setEndTime(defaultTime));
            tasks.values().stream()
                    .filter(t -> t.getTaskType().equals(TaskType.EPIC))
                    .forEach(t -> t.setDuration(0));
        }
        tasks.entrySet().removeIf(entry -> taskType.equals(entry.getValue().getTaskType()));

    }

    // case 4:get методы-------------------------------------------------------------
    @Override
    public Task getTask(UUID idInput) {
        Task task = null;

        if (tasks.containsKey(idInput)) {
            task = tasks.get(idInput);
            historyManager.add(task);
            System.out.println(tasks.get(idInput));
        } else if (tasks.isEmpty()) {
            System.out.println("Мапа пуста");
        } else {
            System.out.println("Неверный идентификатор задачи");
        }
        return task;
    }

    // case 5: Обновление.
    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
            System.out.println("Обновление задачи прошло успешно");

        }
    }

    // case 6: Удалить по идентификатору. ----------------------------------------
    @Override
    public void removeTaskById(UUID id) {
        Epic epic;
        try {
            if (tasks.get(id).getTaskType().equals(TaskType.EPIC)) {

                tasks.get(id).removeSubtask(id);
                tasks.get(id).cleanSubtaskIds();
                for (Task subtask : getSubtasksFromEpic(id)) {
                    tasks.remove(subtask.getId());
                }
            }

            tasks.keySet().removeIf(u -> u.equals(id)); // Predicate

            historyManager.remove(id);
            System.out.println("Задача удалена");

            if (tasks.containsKey(id)) {
                if (tasks.get(id).getTaskType().equals(TaskType.SUBTASK)) {
                    for (Task value : tasks.values()) {
                        if (value.getTaskType().equals(TaskType.SUBTASK)) {
                            updateEpicStatus(id);
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            throw new NullPointerException("Неверный идентификатор задачи");
        }
    }

    // case 7: Изменить статус --------------------------------------------------
    @Override
    public void changeStatusTask(UUID id, Status status) {
        try {

            if (tasks.get(id).getTaskType().equals(TaskType.EPIC)) {
                System.out.println("Статус Эпика зависит от статусов его подзадач(и) и самому изменить невозможно");
                return;
            }
            if (tasks.get(id).getTaskType().equals(TaskType.SUBTASK)) {
                tasks.get(id).setStatus(status);
                updateEpicStatus(id);
            } else {
                tasks.get(id).setStatus(status);
            }
            System.out.println("Статус изменён");
        } catch (NullPointerException e) {
            throw new NullPointerException("Неверный идентификатор задачи");
        }
    }

    // case 8: Получение списка всех подзадач определённого эпика. -----------------------------
    @Override
    public List<Task> getSubtasksFromEpic(UUID epicId) {
        List<Task> subtasks = new ArrayList<>();
        try {
            if (tasks.isEmpty()) {
                System.out.println("Мапа пуста");
            } else {
                for (UUID subtaskUUID : tasks.get(epicId).getSubtasks()) {
                    historyManager.add(tasks.get(subtaskUUID));
                    subtasks.add(tasks.get(subtaskUUID));
                }
            }
        } catch (NullPointerException e) {
            throw new NullPointerException("Неверный идентификатор задачи");
        }
        return subtasks;
    }

    // case 9: получение списка просмотренных задач
    @Override
    public List<Task> getHistory() {
        return historyManager.getTasksInHistory();
    }



    // case 11: сортировка задач по стартовому времени
    @Override
    public void prioritizeTasks() {

        if (tasks.size() > 1) {

            prioritizedTasks = tasks.values().stream()
                    .filter(t -> (!t.getTaskType().equals(TaskType.EPIC)))
                    .sorted(Comparator.comparing(Task::getStartTime))
                    .collect(Collectors.toCollection(ArrayList::new));

            for (Task prioritizedTask : prioritizedTasks) {
                System.out.println(prioritizedTask);
            }

        } else {
            System.out.println("Нужно ещё больше задач");
        }
    }

    // ==========================   Getters       ==========================
    @Override
    public List<Task> getPrioritizedTasks() {
        prioritizeTasks();
        return prioritizedTasks;
    }

    public Map<UUID, Task> getTasks() {
        return tasks;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }
// ==========================   Getters End   ==========================

    public void updateEpicStatus(UUID id) {
        UUID epicId = tasks.get(id).getEpicId();
        List<UUID> uuidsSubtasks = new ArrayList<>(tasks.get(epicId).getSubtasks());
        int tasksWithStatusDone = 0;
        int tasksWithStatusNew = 0;
        int subtasksInEpic = tasks.get(epicId).getSubtasks().size();

        for (UUID uuidsSubtask : uuidsSubtasks) {

            Status status = tasks.get(uuidsSubtask).getStatus();
            switch (status) {
                case NEW:
                    tasksWithStatusNew++;
                    break;
                case DONE:
                    tasksWithStatusDone++;
                    break;
            }
        }

        if (tasksWithStatusNew < subtasksInEpic) {
            tasks.get(epicId).setStatus(Status.IN_PROGRESS);
        } else {
            tasks.get(epicId).setStatus(Status.NEW);
        }
        if (tasksWithStatusDone == subtasksInEpic) {
            tasks.get(epicId).setStatus(Status.DONE);
        }
        System.out.println("Обновление статуса эпика прошло успешно");
    }
}