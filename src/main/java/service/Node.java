package main.java.service;

public class Node<Task> {
    public Task data;
    public Node<Task> prev;
    public Node<Task> next;

    public Node(Node<Task> prev, Task data, Node<Task> next) {
        this.prev = prev;
        this.data = data;
        this.next = next;
    }

    public Task get() {
        return data;
    }
}
