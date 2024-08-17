package io.github.jwdeveloper.tiktok.server.data.pk.repository;

import io.github.jwdeveloper.tiktok.server.data.pk.PkTargetGift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PkTargetGiftRepository extends JpaRepository<PkTargetGift, Integer>, JpaSpecificationExecutor<PkTargetGift> {

    List<PkTargetGift> findAllByConfigId(int configId);
}