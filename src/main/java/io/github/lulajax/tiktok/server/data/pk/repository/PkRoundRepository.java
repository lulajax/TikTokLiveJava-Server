package io.github.lulajax.tiktok.server.data.pk.repository;

import io.github.lulajax.tiktok.server.data.pk.PkRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PkRoundRepository extends JpaRepository<PkRound, Integer>, JpaSpecificationExecutor<PkRound> {

    @Query("SELECT p FROM PkRound p WHERE p.status = 1")
    List<PkRound> findAllPkingRoundList();
}