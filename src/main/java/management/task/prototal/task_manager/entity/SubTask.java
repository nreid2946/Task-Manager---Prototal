package management.task.prototal.task_manager.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SubTask {

    private String title;

    private String description;

    private List<SubTask> subTasks = new ArrayList<>();
}
