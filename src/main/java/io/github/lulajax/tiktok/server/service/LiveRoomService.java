package io.github.lulajax.tiktok.server.service;

import cn.hutool.core.date.DateUtil;
import io.github.jwdeveloper.tiktok.data.requests.LiveData;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.lulajax.tiktok.server.data.CommentMsg;
import io.github.lulajax.tiktok.server.data.GiftMsg;
import io.github.lulajax.tiktok.server.data.LiveRoom;
import io.github.lulajax.tiktok.server.data.LiveRoomRankUser;
import io.github.lulajax.tiktok.server.data.repository.CommentMsgRepository;
import io.github.lulajax.tiktok.server.data.repository.GiftMsgRepository;
import io.github.lulajax.tiktok.server.data.repository.LiveRoomRankUserRepository;
import io.github.lulajax.tiktok.server.data.repository.LiveRoomRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class LiveRoomService {
    private final LiveRoomRepository liveRoomRepository;
    private final GiftMsgRepository giftMsgRepository;
    private final CommentMsgRepository commentMsgRepository;
    private final LiveRoomRankUserRepository liveRoomRankUserRepository;


    public void liveStartUpdate(LiveClient liveClient) {
        String roomId = liveClient.getRoomInfo().getRoomId();
        LiveRoom liveRoom = liveRoomRepository.findByRoomId(roomId);
        if (liveRoom == null) {
            liveRoom = new LiveRoom().buildFrom(liveClient.getRoomInfo());
            liveRoom.setEndTime(DateUtil.currentSeconds());
            liveRoomRepository.save(liveRoom);
        }
    }

    public void liveUpdateByRoomId(LiveData.Response liveData, String roomId) {
        LiveRoom liveRoom = liveRoomRepository.findByRoomId(roomId);
        if (liveRoom != null) {
            if (liveData.getLikes() > 0) {
                liveRoom.setLikesCount(liveData.getLikes());
            }
            if (liveData.getViewers() > 0) {
                liveRoom.setViewersCount(liveData.getViewers());
            }
            if (liveData.getTotalViewers() > 0) {
                liveRoom.setTotalViewersCount(liveData.getTotalViewers());
            }
            liveRoom.setEndTime(DateUtil.currentSeconds());
            liveRoomRepository.save(liveRoom);
        }
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

    public List<LiveRoomRankUser> getLiveRoomRankUserList(String roomId) {
        return liveRoomRankUserRepository.findAllByRoomIdOrderByTimeStampDescRank(roomId);
    }

    public void remove(Long hostId) {
        liveRoomRepository.deleteAllByHostId(hostId);
    }
}
