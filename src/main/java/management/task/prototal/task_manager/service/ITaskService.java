package management.task.prototal.task_manager.service;

import management.task.prototal.task_manager.entity.Task;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ITaskService {

    Mono<Task> createTask(Task task);

    Mono<Task> getTaskById(String id);

    Mono<Task> updateTask(Task task);

    Mono<Boolean> deleteTask(String id);

    Flux<Task> getAllTasks();
}
