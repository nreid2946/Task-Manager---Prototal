package management.task.prototal.task_manager.controller;

import management.task.prototal.task_manager.entity.Task;
import management.task.prototal.task_manager.exception.InvalidTaskException;
import management.task.prototal.task_manager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ExtendWith(MockitoExtension.class)
@WebFluxTest(value = TaskController.class)
class TaskControllerTest {

    @MockBean
    private TaskService taskService;

    @Autowired
    private WebTestClient webTestClient;

    @InjectMocks
    private TaskController taskController;

    Task task = new Task();

    @BeforeEach
    void setUp() {
        task.setId("a");
        task.setTitle("Title");
        task.setDescription("Desc");
    }

    @Test
    void testGetAllTasks() {
        Task task = new Task();
        task.setId("10");
        task.setTitle("Test Task");
        task.setDescription("Test Description");

        when(taskService.getAllTasks()).thenReturn(Flux.just(task));

        webTestClient.get().uri("/tasks/getAll")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Task.class).hasSize(1).contains(task);
    }

    @Test
    void testCreateTask() {
        Task task = new Task();
        task.setId("1");
        task.setTitle("Test Task");
        task.setDescription("Test Description");

        when(taskService.createTask(any(Task.class))).thenReturn(Mono.just(task));

        webTestClient.post().uri("/tasks/createTask")
                .bodyValue(task)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Task.class).isEqualTo(task);
    }

    @Test
    void testCreateTaskWithEmptyTitle() {
        Task task = new Task();
        task.setId("1");
        task.setTitle("");
        task.setDescription("Description");

        when(taskService.createTask(any(Task.class))).thenThrow(new InvalidTaskException("Task title cannot be empty"));

        webTestClient.post().uri("/tasks/createTask")
                .contentType(APPLICATION_JSON)
                .bodyValue(task)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testCreateTaskWithEmptyDescription() {
        Task task = new Task();
        task.setId("1");
        task.setTitle("Title");
        task.setDescription("");

        when(taskService.createTask(any(Task.class))).thenThrow(new InvalidTaskException("Task description cannot be empty"));

        webTestClient.post().uri("/tasks/createTask")
                .contentType(APPLICATION_JSON)
                .bodyValue(task)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testCreateTaskWithInvalidTaskException() {
        task.setId(null);
        task.setTitle(null);
        task.setDescription(null);
        when(taskService.createTask(any(Task.class)))
                .thenReturn(Mono.error(new InvalidTaskException("Task or task properties cannot be null")));

        assertThrows(InvalidTaskException.class, () -> {
            taskController.createTask(task).block();
        });
    }

    @Test
    void testGetTaskById() {
        Task task = new Task();
        task.setId("1");
        task.setTitle("Test Task");
        task.setDescription("Test Description");

        when(taskService.getTaskById("1")).thenReturn(Mono.just(task));

        webTestClient.get().uri("/tasks/getById/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Task.class).isEqualTo(task);
    }

    @Test
    void tesGetByIdNull() {
        task.setId(null);
        when(taskService.getTaskById(null))
                .thenReturn(Mono.error(new InvalidTaskException("Task or task properties cannot be null")));

        assertThrows(InvalidTaskException.class, () -> {
            taskController.getTaskById(null).block();
        });
    }

    @Test
    void testUpdateTask() {
        Task updatedTask = new Task();
        updatedTask.setId("1");
        updatedTask.setTitle("Updated Task");
        updatedTask.setDescription("Updated Description");

        // Mock the getTaskById to return the task when called
        when(taskService.getTaskById("1")).thenReturn(Mono.just(task));
        // Mock the updateTask to return the updatedTask when called
        when(taskService.updateTask(any(Task.class))).thenReturn(Mono.just(updatedTask));

        webTestClient.put().uri("/tasks/update/1")
                .bodyValue(updatedTask)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Task.class).isEqualTo(updatedTask);
    }

    @Test
    void testUpdateTaskWithNullTitle() {
        Task taskWithNullTitle = new Task();
        taskWithNullTitle.setId("1");
        taskWithNullTitle.setTitle(null);
        taskWithNullTitle.setDescription("Updated Description");

        when(taskService.updateTask(taskWithNullTitle))
                .thenReturn(Mono.error(new InvalidTaskException("Task or task properties cannot be null")));

        assertThrows(InvalidTaskException.class, () -> {
            taskController.updateTask(taskWithNullTitle.getId(), taskWithNullTitle).block();
        });
    }

    @Test
    void testUpdateTaskWithNullDescription() {
        Task taskWithNullDescription = new Task();
        taskWithNullDescription.setId("1");
        taskWithNullDescription.setTitle("Updated Task");
        taskWithNullDescription.setDescription(null);

        when(taskService.updateTask(taskWithNullDescription))
                .thenReturn(Mono.error(new InvalidTaskException("Task or task properties cannot be null")));

        assertThrows(InvalidTaskException.class, () -> {
            taskController.updateTask(taskWithNullDescription.getId(), taskWithNullDescription).block();
        });
    }

    @Test
    void testUpdateTaskNull() {
        Task task = new Task();
        task.setTitle(null);
        task.setDescription(null);
        when(taskService.updateTask(task))
                .thenReturn(Mono.error(new InvalidTaskException("Task or task properties cannot be null")));

        assertThrows(InvalidTaskException.class, () -> {
            taskController.updateTask(task.getId(), task).block();
        });
    }

    @Test
    void testDeleteTask() {
        when(taskService.deleteTask("1")).thenReturn(Mono.just(true));

        webTestClient.delete().uri("/tasks/deleteTask/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class).isEqualTo(true);
    }

    @Test
    void testDeleterTaskNull() {
        when(taskService.deleteTask(null))
                .thenReturn(Mono.error(new InvalidTaskException("Task or task properties cannot be null")));

        assertThrows(InvalidTaskException.class, () -> {
            taskController.deleteTask(null).block();
        });
    }

    @Test
    void testDeleteTaskWithStringNullId() {
        when(taskService.deleteTask("null"))
                .thenReturn(Mono.error(new InvalidTaskException("Id is null")));

        assertThrows(InvalidTaskException.class, () -> {
            taskController.deleteTask("null").block();
        });
    }
}