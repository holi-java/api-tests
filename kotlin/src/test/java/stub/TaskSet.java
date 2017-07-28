package stub;

import java.util.HashSet;
import java.util.Set;

public class TaskSet {
    private Set<Runnable> tasks = new HashSet<>();

    public boolean add(Runnable task) {
        return tasks.add(task);
    }

    public boolean contains(Runnable task) {
        return tasks.contains(task);
    }

    public int size() {
        return tasks.size();
    }
}
