package io.github.lulajax.tiktok.server.service;

import cn.hutool.core.date.DateUtil;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.lulajax.tiktok.server.data.CommentMsg;
import io.github.lulajax.tiktok.server.data.GiftMsg;
import io.github.lulajax.tiktok.server.data.LiveRoom;
import io.github.lulajax.tiktok.server.data.repository.CommentMsgRepository;
import io.github.lulajax.tiktok.server.data.repository.GiftMsgRepository;
import io.github.lulajax.tiktok.server.data.repository.LiveRoomRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class LiveUserService {
    private final LiveRoomRepository liveRoomRepository;
    private final GiftMsgRepository giftMsgRepository;
    private final CommentMsgRepository commentMsgRepository;

    public void liveUpdate(LiveClient liveClient) {
        String roomId = liveClient.getRoomInfo().getRoomId();
        LiveRoom liveRoom = liveRoomRepository.findByRoomId(roomId);
        if (liveRoom == null) {
            liveRoom = new LiveRoom().buildFrom(liveClient.getRoomInfo());
        }
        liveRoom.setEndTime(DateUtil.currentSeconds());
        liveRoomRepository.save(liveRoom);
    }

    public List<LiveRoom> getLiveRooms(String hostName) {
        return liveRoomRepository.findAllByHostName(hostName);
    }

    public List<GiftMsg> getGiftMsgList(String roomId) {
        return giftMsgRepository.findAllByRoomIdOrderByIdDesc(roomId);
    }

    public List<CommentMsg> getCommentMsgList(String roomId) {
        return commentMsgRepository.findAllByRoomIdOrderByIdDesc(roomId);
    }
}
