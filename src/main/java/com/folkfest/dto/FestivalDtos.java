package com.folkfest.dto;
import com.folkfest.model.FestivalState;
import jakarta.validation.constraints.*; import java.time.LocalDate; import java.util.Set;
public class FestivalDtos {
  public record CreateFestivalRequest(@NotBlank String name, @NotBlank String description,
      @NotNull LocalDate startDate, @NotNull LocalDate endDate, @NotBlank String venue) {}
  public record UpdateFestivalRequest(String description, LocalDate startDate, LocalDate endDate,
      String venue, String venueLayout, String budgetInfo, String vendorInfo) {}
  public record FestivalResponse(String id, String name, String description, LocalDate startDate,
      LocalDate endDate, String venue, FestivalState state, Set<String> organizers, Set<String> staff) {}
}
