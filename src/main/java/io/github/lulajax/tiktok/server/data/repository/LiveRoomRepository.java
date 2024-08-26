package io.github.lulajax.tiktok.server.data.repository;

import io.github.lulajax.tiktok.server.data.LiveRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LiveRoomRepository extends JpaRepository<LiveRoom, Long> {
    LiveRoom findByRoomId(String roomId);
    List<LiveRoom> findAllByHostName(String hostName);

    void deleteAllByHostId(Long hostId);
}
