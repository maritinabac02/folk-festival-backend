package com.folkfest.model;
import lombok.*; import org.springframework.data.annotation.Id; import org.springframework.data.mongodb.core.mapping.Document;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Document("users")
public class User {
  @Id private String id;
  private String username;
  private String fullName;
  private String passwordHash;
  private boolean active;
}
