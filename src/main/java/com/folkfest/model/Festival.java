package com.folkfest.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Document("festivals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Festival {

  @Id
  private String id;

  private String name;
  private String description;
  private LocalDate startDate;
  private LocalDate endDate;
  private String venue;

  // --- ΝΕΑ πεδία που ζητά το service ---
  /** Layout/διάταξη χώρου (π.χ. χάρτης σκηνών, ζωνών κοινού κ.λπ.) */
  private String venueLayout;
  /** Πληροφορίες budget (π.χ. κόστος σκηνών, security, κ.λπ.) */
  private String budgetInfo;
  /** Πληροφορίες vendors (π.χ. λίστα προμηθευτών/food trucks) */
  private String vendorInfo;
  // --------------------------------------

  private FestivalState state;
  private Instant createdAt;

  // ΠΟΤΕ null — ασφαλές για add(...)
  @Builder.Default
  private Set<String> organizerUserIds = new HashSet<>();

  @Builder.Default
  private Set<String> staffUserIds = new HashSet<>();
}
