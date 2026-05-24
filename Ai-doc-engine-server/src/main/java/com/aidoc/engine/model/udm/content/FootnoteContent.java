package com.aidoc.engine.model.udm.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FootnoteContent {

    private String id;
    private String text;

    public static FootnoteContent of(String id, String text) {
        return FootnoteContent.builder().id(id).text(text).build();
    }
}
