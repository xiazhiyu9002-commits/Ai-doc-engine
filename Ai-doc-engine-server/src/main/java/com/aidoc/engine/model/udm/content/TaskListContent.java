package com.aidoc.engine.model.udm.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskListContent {

    private List<TaskItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskItem {
        private String text;
        private boolean checked;
        private List<TaskItem> nestedItems;
    }

    public static TaskListContent of(List<TaskItem> items) {
        return TaskListContent.builder().items(items).build();
    }
}
