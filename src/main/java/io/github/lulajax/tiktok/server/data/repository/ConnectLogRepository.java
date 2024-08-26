package io.github.lulajax.tiktok.server.data.repository;


import io.github.lulajax.tiktok.server.data.ConnectLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConnectLogRepository extends JpaRepository<ConnectLog, Long> {
    List<ConnectLog> findAllByRoomId(String roomId);
}
