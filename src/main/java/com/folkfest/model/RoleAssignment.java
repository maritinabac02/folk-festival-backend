package com.folkfest.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data @NoArgsConstructor @AllArgsConstructor
@Document(collection = "user_roles")
@CompoundIndex(name = "user_festival_unique", def = "{'username':1,'festivalId':1}", unique = true)
public class RoleAssignment {
    @Id
    private String id;

    private String username;   // linked by username
    private String festivalId; // linked by festival id
    private Role role;
}
