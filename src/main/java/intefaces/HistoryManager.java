package main.java.intefaces;

import main.java.service.Node;
import main.java.tasks.Task;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface HistoryManager {

    void add(Task task);

    String remove(UUID id);

    List<Task> getTasksInHistory();

    Map<UUID, Node<Task>> getUuidNodes();

}
