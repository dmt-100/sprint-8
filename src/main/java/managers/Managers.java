package main.java.managers;

import main.java.intefaces.HistoryManager;

public abstract class Managers {

    public static  HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

}
