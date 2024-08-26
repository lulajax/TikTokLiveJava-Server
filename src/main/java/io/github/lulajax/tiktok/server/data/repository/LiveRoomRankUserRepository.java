package io.github.lulajax.tiktok.server.data.repository;

import io.github.lulajax.tiktok.server.data.LiveRoomRankUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LiveRoomRankUserRepository extends JpaRepository<LiveRoomRankUser, Long> {
    List<LiveRoomRankUser> findAllByRoomIdOrderByTimeStampDescRank(String roomId);

    LiveRoomRankUser findFirstByRoomIdAndUserId(String roomId, Long userId);

    void deleteAllByRoomId(String roomId);
}
