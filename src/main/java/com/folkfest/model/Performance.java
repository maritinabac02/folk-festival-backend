package com.folkfest.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
@Document(collection = "performances")
@CompoundIndex(name = "festival_name_unique", def = "{'festivalId':1,'name':1}", unique = true)
public class Performance {
    @Id
    private String id;

    // relations by id / username
    private String festivalId;

    private String name;
    private String description;
    private String genre;
    private Integer durationMinutes;

    private Instant createdAt = Instant.now();
    private PerformanceState state = PerformanceState.CREATED;

    // artists
    private String mainArtist;                  // username of creator
    private List<String> bandMembers = new ArrayList<>();

    // tech & content
    private List<String> technicalRequirements = new ArrayList<>();
    private List<String> setlist = new ArrayList<>();

    // preferences (strings; could be ISO date-times, but kept generic)
    private List<String> preferredRehearsalTimes = new ArrayList<>();
    private List<String> preferredPerformanceSlots = new ArrayList<>();

    // assignment / review
    private String assignedStaff;               // username
    private Integer reviewScore;                // mirror of Review.score (for quick access)
    private String reviewComments;              // mirror of Review.comments

    // final decision / scheduling
    private String rejectionReason;
    private String scheduledTime;
    private String scheduledStage;

    // optional embedded review object (if θες να το χρησιμοποιήσεις αργότερα)
    private Review review;

    /** Χρήσιμο helper για επισκέπτες: κρατά μόνο δημόσιες πληροφορίες. */
    public void stripSensitiveForVisitor() {
        // αφήνουμε μόνο ό,τι χρειάζεται ο VISITOR
        this.description = null;
        this.durationMinutes = null;
        this.technicalRequirements = null;
        this.setlist = null;
        this.preferredRehearsalTimes = null;
        this.preferredPerformanceSlots = null;
        this.assignedStaff = null;
        this.reviewScore = null;
        this.reviewComments = null;
        this.rejectionReason = null;
        this.review = null;
        this.bandMembers = null; // μόνο mainArtist εκτίθεται
        // κρατάμε: name, genre, scheduledTime, scheduledStage, mainArtist, state, festivalId, id
    }
}
