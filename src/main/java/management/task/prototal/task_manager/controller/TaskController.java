package management.task.prototal.task_manager.controller;

import management.task.prototal.task_manager.exception.InvalidTaskException;
import management.task.prototal.task_manager.service.ITaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import management.task.prototal.task_manager.entity.Task;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final ITaskService taskService;

    @Autowired
    public TaskController(ITaskService iTaskService) {
        this.taskService = iTaskService;
    }

    @PostMapping("createTask")
    public Mono<Task> createTask (@RequestBody Task task) {
        try {
            if (task.getDescription() == null || task.getTitle() == null) {
                throw new InvalidTaskException("Task or task properties cannot be null");
            }
            return taskService.createTask(task);
        } catch (InvalidTaskException e) {
            return Mono.error(e);
        }
    }

    @GetMapping("getById/{id}")
    public Mono<Task> getTaskById(@PathVariable String id) {
        try {
            if (id == null) {
                throw new InvalidTaskException("ID is null");
            }
            return taskService.getTaskById(id);
        } catch (InvalidTaskException e) {
            return Mono.error(e);
        }
    }

    @PutMapping("/update/{id}")
    public Mono<Task> updateTask(@PathVariable String id, @RequestBody Task task) { //
        try {
            if (task.getDescription() == null || task.getTitle() == null) {
                throw new InvalidTaskException("Task or task properties cannot be null");
            }
            task.setId(id);
            return taskService.updateTask(task);
        } catch (InvalidTaskException e) {
            return Mono.error(e);
        }
    }

    @DeleteMapping("deleteTask/{id}")
    public Mono<Boolean> deleteTask(@PathVariable String id) {
        try {
            if (id == null || id.equals("null")) {
                throw new InvalidTaskException("Id is null");
            }
            return taskService.deleteTask(id);
        } catch (InvalidTaskException e) {
            return Mono.error(e);
        }
    }

    /**
     * I know this method is superfluous. But it makes it easier for someone
     * unfamiliar with MongoDB to get the ids for use in the other methods
     * @return Flux
     */
    @GetMapping("getAll")
    public Flux<Task> getAllTasks() {
        return taskService.getAllTasks();
    }
}
