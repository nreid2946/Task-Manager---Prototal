package management.task.prototal.task_manager.controller;

import management.task.prototal.task_manager.entity.Task;
import management.task.prototal.task_manager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskControllerIntegrationTests {

    private static final Logger logger = LoggerFactory.getLogger(TaskControllerIntegrationTests.class);

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task();
        task.setId("1");
        task.setTitle("Test Task");
        task.setDescription("Test Description");

        reactiveMongoTemplate.save(task).block();

        // Log to confirm the task is saved
        reactiveMongoTemplate.findById("1", Task.class).doOnNext(savedTask -> {
            if (savedTask != null) {
                System.out.println("Task saved: " + savedTask);
            } else {
                System.out.println("Task not found!");
            }
        }).block();
    }

    @BeforeEach
    void cleanDatabase() {
        reactiveMongoTemplate.dropCollection(Task.class).block();
    }

    @Test
    void testGetAllTasks() {
        webTestClient.get().uri("/tasks/getAll")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Task.class)
                .hasSize(1)
                .contains(task);

        reactiveMongoTemplate.save(task).block();
    }

    @Test
    void testCreateTask() {
        task.setId("5");
        webTestClient.post().uri("/tasks/createTask")
                .bodyValue(task)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Task.class).isEqualTo(task);
    }

    @Test
    void testCreateTaskWithInvalidData() {
        // Create a task with invalid data
        Task invalidTask = new Task();
        invalidTask.setId("2");
        invalidTask.setTitle(null);  // Invalid title
        invalidTask.setDescription(null);  // Invalid description

        webTestClient.post().uri("/tasks/createTask")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidTask)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testGetTaskById() {
        webTestClient.get().uri("/tasks/getById/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Task.class)
                .isEqualTo(task);
    }

    @Test
    void testGetTaskByIdNotFound() {
        webTestClient.get().uri("/tasks/getById/")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testUpdateTask() {
        task.setTitle("Test Task");

        webTestClient.put().uri("/tasks/update/" + task.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(task)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Task.class)
                .isEqualTo(task);
    }

    @Test
    void testUpdateTaskWithInvalidData() {
        Task invalidTask = new Task();
        invalidTask.setId("1");
        invalidTask.setTitle(null);
        invalidTask.setDescription(null);

        webTestClient.put().uri("/tasks/update/" + invalidTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidTask)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testDeleteTask() {
        webTestClient.delete().uri("/tasks/deleteTask/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    void testDeleteTaskWithNullId() {
        webTestClient.delete().uri("/tasks/deleteTask/")
                .exchange()
                .expectStatus().isNotFound();
    }
}
