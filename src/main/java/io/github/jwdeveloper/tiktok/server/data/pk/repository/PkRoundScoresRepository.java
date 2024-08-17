package io.github.jwdeveloper.tiktok.server.data.pk.repository;

import io.github.jwdeveloper.tiktok.server.data.pk.PkRoundScores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
@Repository
public interface PkRoundScoresRepository extends JpaRepository<PkRoundScores, Integer>, JpaSpecificationExecutor<PkRoundScores> {
}