package com.folkfest.repo;

import com.folkfest.model.Performance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.*;
import java.util.stream.Collectors;

public interface PerformanceRepository extends MongoRepository<Performance, String> {

    boolean existsByFestivalIdAndNameIgnoreCase(String festivalId, String name);

    List<Performance> findByFestivalId(String festivalId);

   
    default List<Performance> searchByCriteria(String festivalId, String name, String artists, String genre) {
        List<Performance> all = findByFestivalId(festivalId);
        // Normalise inputs
        List<String> nameWords = splitWords(name);
        List<String> artistWords = splitWords(artists);
        List<String> genreWords = splitWords(genre);

        return all.stream()
                .filter(p -> containsAllWords(p.getName(), nameWords)
                        && containsAllWords(p.getGenre(), genreWords)
                        && artistMatch(p, artistWords))
                .sorted(Comparator
                        .comparing(Performance::getGenre, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(Performance::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.toList());
    }

    private static List<String> splitWords(String s){
        if (s == null || s.isBlank()) return List.of();
        return Arrays.stream(s.trim().split("\\s+"))
                .map(String::toLowerCase).toList();
    }

    private static boolean containsAllWords(String field, List<String> words){
        if (words.isEmpty()) return true;
        String base = field == null ? "" : field.toLowerCase();
        for (String w : words) if (!base.contains(w)) return false;
        return true;
    }

    private static boolean artistMatch(Performance p, List<String> words){
        if (words.isEmpty()) return true;
        // ελέγχουμε mainArtist + bandMembers
        String main = p.getMainArtist() == null ? "" : p.getMainArtist().toLowerCase();
        List<String> band = p.getBandMembers() == null ? List.of() :
                p.getBandMembers().stream().filter(Objects::nonNull).map(String::toLowerCase).toList();
        for (String w : words) {
            boolean inMain = main.contains(w);
            boolean inBand = band.stream().anyMatch(b -> b.contains(w));
            if (!inMain && !inBand) return false;
        }
        return true;
    }
}
