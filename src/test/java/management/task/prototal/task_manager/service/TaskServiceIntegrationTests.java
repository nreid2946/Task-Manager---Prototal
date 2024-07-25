package management.task.prototal.task_manager.service;

import management.task.prototal.task_manager.entity.Task;
import management.task.prototal.task_manager.exception.DuplicateTaskException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class TaskServiceIntegrationTests {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    private Task task;

    @BeforeEach
    void setUp() {
        reactiveMongoTemplate.remove(Task.class).all().block();

        task = new Task();
        task.setId("1");
        task.setTitle("Test Task");
        task.setDescription("Test Description");

        reactiveMongoTemplate.save(task).block();
    }

    @BeforeEach
    void cleanDatabase() {
        reactiveMongoTemplate.dropCollection(Task.class).block();
    }

    @Test
    void testCreateTask() {
        Task newTask = new Task();
        newTask.setId("2");
        newTask.setTitle("New Task");
        newTask.setDescription("New Description");

        Mono<Task> result = taskService.createTask(newTask);

        StepVerifier.create(result)
                .expectNextMatches(savedTask -> savedTask.getId().equals("2") &&
                        savedTask.getTitle().equals("New Task") &&
                        savedTask.getDescription().equals("New Description"))
                .verifyComplete();
    }

    @Test
    void testGetAllTasks() {
        StepVerifier.create(taskService.getAllTasks())
                .expectNextMatches(retrievedTask -> retrievedTask.getId().equals("1") &&
                        retrievedTask.getTitle().equals("Test Task") &&
                        retrievedTask.getDescription().equals("Test Description"))
                .verifyComplete();
    }

    @Test
    void testGetTaskById() {
        Mono<Task> result = taskService.getTaskById("1");

        StepVerifier.create(result)
                .expectNextMatches(retrievedTask -> retrievedTask.getId().equals("1") &&
                        retrievedTask.getTitle().equals("Test Task") &&
                        retrievedTask.getDescription().equals("Test Description"))
                .verifyComplete();
    }

    @Test
    void testCreateTaskWithExistingId() {
        Task duplicateTask = new Task();
        duplicateTask.setId("1");
        duplicateTask.setTitle("Duplicate Task");
        duplicateTask.setDescription("Duplicate Description");

        Mono<Task> result = taskService.createTask(duplicateTask);

        StepVerifier.create(result)
                .expectError(DuplicateTaskException.class)
                .verify();
    }

    @Test
    void testGetTaskByNonExistingId() {
        Mono<Task> result = taskService.getTaskById("non-existing-id");

        StepVerifier.create(result)
                .expectComplete()
                .verify();
    }

    @Test
    void testUpdateTask() {
        Task originalTask = new Task();
        originalTask.setId("2");
        originalTask.setTitle("Original Task");
        originalTask.setDescription("Original Description");

        taskService.createTask(originalTask).block();

        Task updatedTask = new Task();
        updatedTask.setId("2");
        updatedTask.setTitle("Updated Task");
        updatedTask.setDescription("Updated Description");

        taskService.updateTask(updatedTask).block();

        StepVerifier.create(taskService.getTaskById("2"))
                .expectNextMatches(task ->
                        task.getId().equals("2") &&
                                task.getTitle().equals("Updated Task") &&
                                task.getDescription().equals("Updated Description")
                )
                .verifyComplete();
    }

    @Test
    void testUpdateTaskWithNonExistingId() {
        Task nonExistingTask = new Task();
        nonExistingTask.setId("non-existing-id");
        nonExistingTask.setTitle("Non-Existing Task");
        nonExistingTask.setDescription("Non-Existing Description");

        Mono<Task> result = taskService.updateTask(nonExistingTask);

        StepVerifier.create(result)
                .expectNextMatches(task -> task.getId().equals("non-existing-id") &&
                        task.getTitle().equals("Non-Existing Task") &&
                        task.getDescription().equals("Non-Existing Description"))
                .verifyComplete();
    }

    @Test
    void testDeleteTaskWithInvalidId() {
        Mono<Boolean> result = taskService.deleteTask("invalid-id");

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void testDeleteTaskWithNonExistingId() {
        Mono<Boolean> result = taskService.deleteTask("non-existing-id");

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }
}
