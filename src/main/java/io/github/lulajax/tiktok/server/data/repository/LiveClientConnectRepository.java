package io.github.lulajax.tiktok.server.data.repository;


import io.github.lulajax.tiktok.server.data.LiveClientConnect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveClientConnectRepository extends JpaRepository<LiveClientConnect, Long> {

    LiveClientConnect findByHostName(String hostName);
    LiveClientConnect findByHostId(Long hostId);
}
