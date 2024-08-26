package io.github.lulajax.tiktok.server.data.repository;


import io.github.lulajax.tiktok.server.data.CommentMsg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentMsgRepository extends JpaRepository<CommentMsg, Long> {

    List<CommentMsg> findAllByRoomIdOrderByIdDesc(String roomId);

    void deleteAllByHostId(Long hostId);
}
