package management.task.prototal.task_manager.controller;

import management.task.prototal.task_manager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import management.task.prototal.task_manager.entity.Task;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping("createTask")
    public Mono<Task> createTask (@RequestBody Task task) {
        return taskService.createTask(task);
    }

    @GetMapping("getAll")
    public Flux<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("getById/{id}")
    public Mono<Task> getTaskById(@PathVariable String id) {
        return taskService.getTaskById(id);
    }

    // This seems to only update the id. Change this. Dummy method. Will change.
    @PutMapping("/{id}")
    public Mono<Task> updateTask(@PathVariable String id, @RequestBody Task task) {
        task.setId(id);
        return taskService.updateTask(task);
    }

    @GetMapping("getByTitle/{title}")
    public Flux<Task> findTasksByTitle(@PathVariable String title) {
        return taskService.findTasksByTitle(title);
    }

    @DeleteMapping("deleteTask/{id}")
    public Mono<Boolean> deleteTask(@PathVariable String id) {
        return taskService.deleteTask(id);
    }

    @DeleteMapping("/sub-tasks/deleteSubTask/{taskId}/{subTaskTitle}")
    public Mono<Task> deleteSubTask(@PathVariable String taskId, @PathVariable String subTaskTitle) {
        return taskService.removeSubTask(taskId, subTaskTitle);
    }
}
