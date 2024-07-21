package management.task.prototal.task_manager.service;

import management.task.prototal.task_manager.entity.SubTask;
import org.springframework.beans.factory.annotation.Autowired;
import management.task.prototal.task_manager.entity.Task;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    /**
     * saveTask can also be used to update since it has cross-functionality with updateTask through
     * .save().
     * @param task
     * @return
     */
    public Mono<Task> createTask(Task task) {
        return reactiveMongoTemplate.save(task);
    }

    public Flux<Task> getAllTasks() {
        return reactiveMongoTemplate.findAll(Task.class);
    }

    public Mono<Task> getTaskById(String id) {
        return reactiveMongoTemplate.findById(id, Task.class);
    }

    public Mono<Task> updateTask(Task task) {
        Query query = new Query(Criteria.where("_id").is(task.getId()));
        Update update = new Update()
                .set("title", task.getTitle())
                .set("description", task.getDescription())
                .set("subTasks", task.getSubTasks());
        return reactiveMongoTemplate.findAndModify(query, update, Task.class)
                .defaultIfEmpty(task)
                .flatMap(updatedTask -> {
                    if (updatedTask == null) {
                        return Mono.error(new RuntimeException("Task not found"));
                    }
                    return Mono.just(updatedTask);
                });
    }

    public Mono<Boolean> deleteTask(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        return reactiveMongoTemplate.remove(Query.query(Criteria.where("id").is(id)), Task.class)
                .flatMap(deleteResult -> {
                    if (deleteResult.getDeletedCount() > 0) {
                        return Mono.just(true);
                    } else {
                        return Mono.just(false);
                    }
                });
    }

    public Flux<Task> findTasksByTitle(String title) {
        return reactiveMongoTemplate.findAll(Task.class)
                .filter(task -> taskContainsTitle(task, title));
    }

    public Mono<Task> removeSubTask(String taskId, String subTaskTitle) {
        return reactiveMongoTemplate.findById(taskId, Task.class)
                .flatMap(task -> removeSubTaskRecursive(task.getSubTasks(), subTaskTitle)
                        .then(Mono.just(task)))
                .flatMap(task -> reactiveMongoTemplate.save(task));
    }

    /**
     * Utility method for removing a sub task.
     * @param subTasks
     * @param subTaskTitle
     * @return
     */
    private Mono<Boolean> removeSubTaskRecursive(List<SubTask> subTasks, String subTaskTitle) {
        if (subTasks == null || subTasks.isEmpty()) {
            return Mono.just(false);
        }

        // Collect the tasks to remove in a separate list to avoid concurrent modification
        List<SubTask> tasksToRemove = new ArrayList<>();

        return Flux.fromIterable(subTasks)
                .flatMap(subTask -> {
                    if (subTask.getTitle().equals(subTaskTitle)) {
                        tasksToRemove.add(subTask);
                        return Mono.just(true);
                    } else {
                        return removeSubTaskRecursive(subTask.getSubTasks(), subTaskTitle);
                    }
                })
                .then(Mono.defer(() -> {
                    subTasks.removeAll(tasksToRemove);
                    return Mono.just(!tasksToRemove.isEmpty());
                }));
    }

    /**
     * Utility methods for us to find sub-tasks by title.
     * Currently, it also fetches the rest of the JSON object.
     * @param task
     * @param title
     * @return
     */
    private boolean taskContainsTitle(Task task, String title) {
        if (task.getTitle().equalsIgnoreCase(title)) {
            return true;
        }
        return subTaskContainsTitle(task.getSubTasks(), title);
    }

    private boolean subTaskContainsTitle(List<SubTask> subTasks, String title) {
        if (subTasks == null) {
            return false;
        }
        for (SubTask subTask : subTasks) {
            if (subTask.getTitle().equalsIgnoreCase(title)) {
                return true;
            }
            if (subTaskContainsTitle(subTask.getSubTasks(), title)) {
                return true;
            }
        }
        return false;
    }
}
