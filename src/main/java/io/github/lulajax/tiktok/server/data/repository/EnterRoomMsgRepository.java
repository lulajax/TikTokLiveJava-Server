package io.github.lulajax.tiktok.server.data.repository;


import io.github.lulajax.tiktok.server.data.EnterRoomMsg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnterRoomMsgRepository extends JpaRepository<EnterRoomMsg, Long> {
    List<EnterRoomMsg> findAllByRoomId(String roomId);
}
