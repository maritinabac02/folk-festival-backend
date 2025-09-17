package com.folkfest.model;
import lombok.*; import org.springframework.data.annotation.Id; import org.springframework.data.mongodb.core.mapping.Document;
import java.util.*; import java.time.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Document("performances")
public class Performance {
  @Id private String id;
  private String festivalId;
  private String name;
  private String description;
  private String genre;
  private int durationMinutes;
  private List<String> bandMembers = new ArrayList<>();
  private List<String> artistsUserIds = new ArrayList<>();
  private String technicalRequirements;
  private List<String> merchandiseItems = new ArrayList<>();
  private List<String> setlist = new ArrayList<>();
  private List<String> preferredRehearsalTimes = new ArrayList<>();
  private List<String> preferredPerformanceSlots = new ArrayList<>();
  private String assignedStaffUserId;
  private Review review;
  private PerformanceState state;
  private Instant createdAt;
  private String rejectionReason;
  private String scheduledTime;
  private String scheduledStage;
}
