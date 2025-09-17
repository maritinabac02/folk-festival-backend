package com.folkfest.model;
import lombok.*; import org.springframework.data.annotation.Id; import org.springframework.data.mongodb.core.mapping.Document;
@Document("role_assignments")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RoleAssignment {
  @Id private String id;
  private String userId;
  private String festivalId;
  private Role role;
}
