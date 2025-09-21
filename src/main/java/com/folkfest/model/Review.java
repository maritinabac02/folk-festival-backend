package com.folkfest.model;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class Review {
    private Integer score;     // 0..100
    private String comments;   // detailed comments
}
