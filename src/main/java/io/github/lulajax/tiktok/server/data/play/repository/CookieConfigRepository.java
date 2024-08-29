package io.github.lulajax.tiktok.server.data.play.repository;

import io.github.lulajax.tiktok.server.data.play.CookieConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CookieConfigRepository extends JpaRepository<CookieConfig, Integer>, JpaSpecificationExecutor<CookieConfig> {

}