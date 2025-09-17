package com.folkfest.dto;
import com.folkfest.model.PerformanceState; import jakarta.validation.constraints.*; import java.util.List;
public class PerformanceDtos {
  public record CreatePerformanceRequest(@NotBlank String festivalId, @NotBlank String name,
      @NotBlank String description, @NotBlank String genre, @NotNull Integer durationMinutes) {}
  public record UpdatePerformanceRequest(String name, String description, String genre, Integer durationMinutes,
      List<String> bandMembers, String technicalRequirements, List<String> merchandiseItems,
      List<String> setlist, List<String> preferredRehearsalTimes, List<String> preferredPerformanceSlots) {}
  public record AssignStaffRequest(@NotBlank String staffUserId) {}
  public record ReviewRequest(@NotNull Integer score, @NotBlank String comments) {}
  public record FinalSubmissionRequest(List<String> setlist, List<String> preferredRehearsalTimes,
      List<String> preferredPerformanceSlots) {}
  public record SearchRequest(String name, String artists, String genre) {}
  public record PerformanceResponse(String id, String festivalId, String name, String description, String genre,
      int durationMinutes, List<String> bandMembers, List<String> setlist, String scheduledTime,
      String scheduledStage, PerformanceState state) {}
}
