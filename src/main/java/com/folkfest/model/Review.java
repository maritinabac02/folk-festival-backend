package com.folkfest.model;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {
  private String staffUserId; private int score; private String comments;
}
