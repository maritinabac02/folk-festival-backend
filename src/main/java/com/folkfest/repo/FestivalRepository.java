package com.folkfest.repo;
import com.folkfest.model.Festival; import org.springframework.data.mongodb.repository.MongoRepository; import java.util.Optional;
public interface FestivalRepository extends MongoRepository<Festival, String> {
  Optional<Festival> findByNameIgnoreCase(String name);
}
