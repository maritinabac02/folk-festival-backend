package com.folkfest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class FestivalDtos {
    public static class CreateFestivalRequest {
        @NotBlank public String name;
        @NotBlank public String description;
        @NotNull public LocalDate startDate;
        @NotNull public LocalDate endDate;
        @NotBlank public String venue;
    }
    public static class UpdateFestivalRequest {
        public String name;
        public String description;
        public String venue;
        // extra fields (layout/budget/vendor) μπορούν να μπουν εδώ
    }
    public static class ChangeFestivalStateRequest {
        @NotBlank public String next; // e.g. SUBMISSION, ASSIGNMENT ...
    }
    public static class FestivalResponse {
        public String id;
        public String name;
        public String description;
        public String venue;
        public LocalDate startDate;
        public LocalDate endDate;
        public String state;
    }
}
