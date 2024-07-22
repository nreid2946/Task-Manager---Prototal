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

//    @Test
//    void testCreateTaskWithInvalidTaskException() {
//        Task task = new Task();
//
//        when(taskService.createTask(any(Task.class)))
//                .thenThrow(new InvalidTaskException("Task or task properties cannot be null"));
//
//        webTestClient.post().uri("/tasks/createTask")
//                .bodyValue(task)
//                .exchange()
//                .expectStatus().isBadRequest()
//                .expectBody()
//                .jsonPath("$.error").isEqualTo("Bad Request")
//                .jsonPath("$.message").isEqualTo("Task or task properties cannot be null");
//    }

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
}