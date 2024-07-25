package management.task.prototal.task_manager.service;


import com.mongodb.client.result.DeleteResult;
import management.task.prototal.task_manager.entity.Task;
import management.task.prototal.task_manager.exception.DuplicateTaskException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TaskServiceTest {

    @Mock
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @InjectMocks
    private TaskService taskService;

    private Task task;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        task = new Task();
        task.setId("a");
        task.setTitle("Title");
        task.setDescription("Desc");
    }

    @Test
    void testCreateTask() {
        when(reactiveMongoTemplate.exists(any(Query.class), eq(Task.class))).thenReturn(Mono.just(false));
        when(reactiveMongoTemplate.save(any(Task.class))).thenReturn(Mono.just(task));

        Mono<Task> result = taskService.createTask(task);

        StepVerifier.create(result)
                .expectNext(task)
                .verifyComplete();
    }

    @Test
    void testCreateTaskWithExistingId() {
        when(reactiveMongoTemplate.exists(any(Query.class), eq(Task.class))).thenReturn(Mono.just(true));

        Mono<Task> result = taskService.createTask(task);

        StepVerifier.create(result)
                .expectError(DuplicateTaskException.class)
                .verify();
    }

    @Test
    void testGetAllTasks() {
        when(reactiveMongoTemplate.findAll(Task.class)).thenReturn(Flux.just(task));

        Flux<Task> result = taskService.getAllTasks();

        StepVerifier.create(result)
                .expectNext(task)
                .verifyComplete();
    }

    @Test
    void testGetTaskById() {
        when(reactiveMongoTemplate.findById(eq("a"), eq(Task.class))).thenReturn(Mono.just(task));

        Mono<Task> result = taskService.getTaskById("a");

        StepVerifier.create(result)
                .expectNext(task)
                .verifyComplete();
    }

    @Test
    void testUpdateTask() {
        when(reactiveMongoTemplate.findAndModify(any(Query.class), any(), eq(Task.class))).thenReturn(Mono.just(task));

        Mono<Task> result = taskService.updateTask(task);

        StepVerifier.create(result)
                .expectNext(task)
                .verifyComplete();
    }

    @Test
    void testDeleteTask() {
        DeleteResult deleteResult = mock(DeleteResult.class);
        when(deleteResult.getDeletedCount()).thenReturn(1L);
        when(reactiveMongoTemplate.remove(any(Query.class), eq(Task.class))).thenReturn(Mono.just(deleteResult));

        Mono<Boolean> result = taskService.deleteTask("a");

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void testDeleteTaskNotFound() {
        DeleteResult deleteResult = mock(DeleteResult.class);
        when(deleteResult.getDeletedCount()).thenReturn(0L);
        when(reactiveMongoTemplate.remove(any(Query.class), eq(Task.class))).thenReturn(Mono.just(deleteResult));

        Mono<Boolean> result = taskService.deleteTask("a");

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }
}

