package com.folkfest.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class PerformanceDtos {

    public static class CreatePerformanceRequest {
        @NotBlank public String festivalId;
        @NotBlank public String name;
        @NotBlank public String description;
        @NotBlank public String genre;
        @NotNull @Min(1) public Integer durationMinutes;
    }

    public static class UpdatePerformanceRequest {
        public String name;
        public String description;
        public String genre;
        public Integer durationMinutes;
        public List<String> bandMembers;
        public List<String> technicalRequirements;
        public List<String> setlist;
        public List<String> preferredRehearsalTimes;
        public List<String> preferredPerformanceSlots;
    }

    public static class ReviewRequest {
        @NotNull @Min(0) @Max(100) public Integer score;
        @NotBlank public String comments;
    }

    public static class FinalSubmissionRequest {
        public List<String> setlist;
        public List<String> rehearsalTimes;
        public List<String> performanceSlots;
    }

    public static class SearchRequest {
        public String name;
        public String artists;
        public String genre;
    }
}
