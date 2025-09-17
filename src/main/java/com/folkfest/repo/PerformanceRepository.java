package com.folkfest.repo;
import com.folkfest.model.Performance; import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.*;
public interface PerformanceRepository extends MongoRepository<Performance, String> {
  Optional<Performance> findByFestivalIdAndNameIgnoreCase(String festivalId, String name);
  List<Performance> findByFestivalId(String festivalId);
}
