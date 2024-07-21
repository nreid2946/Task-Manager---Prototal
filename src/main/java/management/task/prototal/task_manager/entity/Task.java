package management.task.prototal.task_manager.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "tasks")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Task {

    @Id
    private String id;

    private String title;

    private String description;

    private List<SubTask> subTasks = new ArrayList<>();
}
