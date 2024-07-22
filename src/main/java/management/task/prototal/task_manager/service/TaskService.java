package management.task.prototal.task_manager.service;

import com.mongodb.client.result.DeleteResult;
import management.task.prototal.task_manager.exception.DuplicateTaskException;
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
        Query query = new Query(Criteria.where("_id").is(task.getId()));
        return reactiveMongoTemplate.exists(query, Task.class)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateTaskException("Task with ID " + task.getId() + " already exists."));
                    } else {
                        return reactiveMongoTemplate.save(task);
                    }
                });
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
                .defaultIfEmpty(task);
    }

    public Mono<Boolean> deleteTask(String id) {
        return reactiveMongoTemplate.remove(Query.query(Criteria.where("_id").is(id)), Task.class)
                .defaultIfEmpty(DeleteResult.unacknowledged())
                .flatMap(deleteResult -> {
                    if (deleteResult.getDeletedCount() > 0) {
                        return Mono.just(true);
                    } else {
                        return Mono.just(false);
                    }
                });
    }
}
