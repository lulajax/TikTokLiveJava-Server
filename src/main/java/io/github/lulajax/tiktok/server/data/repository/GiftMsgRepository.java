package io.github.lulajax.tiktok.server.data.repository;


import io.github.lulajax.tiktok.server.data.GiftMsg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GiftMsgRepository extends JpaRepository<GiftMsg, Long> {

    List<GiftMsg> findAllByRoomIdOrderByIdDesc(String roomId);

    void deleteAllByHostId(Long hostId);
}
