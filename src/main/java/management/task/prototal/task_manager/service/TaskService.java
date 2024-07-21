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
        return reactiveMongoTemplate.save(task);
    }

    public Mono<Void> deleteTask(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        return reactiveMongoTemplate.remove(query, Task.class).then();
    }

    public Flux<Task> findTasksByTitle(String title) {
        Query query = new Query(Criteria.where("title").is(title));
        return reactiveMongoTemplate.find(query, Task.class);
    }

    public Mono<Task> deleteSubTask(String taskId, String subTaskTitle) {
        Query query = new Query(Criteria.where("_id").is(taskId));
        Update update = new Update().pull("subTasks", new Query(Criteria.where("title").is(subTaskTitle)));
        return reactiveMongoTemplate.findAndModify(query, update, Task.class)
                .flatMap(task -> {
                    if (task == null) {
                        return Mono.error(new RuntimeException("Task not found"));
                    }
                    return Mono.just(task);
                });
    }

    public Mono<Task> addSubTask(String taskId, SubTask subTask) {
        Query query = new Query(Criteria.where("_id").is(taskId));
        Update update = new Update().addToSet("subTasks", subTask);
        return reactiveMongoTemplate.findAndModify(query, update, Task.class)
                .flatMap(task -> {
                    if (task == null) {
                        return Mono.error(new RuntimeException("Task not found"));
                    }
                    return Mono.just(task);
                });
    }
}
