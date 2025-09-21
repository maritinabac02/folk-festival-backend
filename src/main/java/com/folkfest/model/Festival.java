package com.folkfest.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
@Document(collection = "festivals")
public class Festival {
    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;
    private String venue;
    private LocalDate startDate;
    private LocalDate endDate;

    private FestivalState state = FestivalState.CREATED;
}
