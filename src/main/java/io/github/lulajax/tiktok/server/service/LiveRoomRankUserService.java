package io.github.lulajax.tiktok.server.service;

import cn.hutool.core.date.DateUtil;
import io.github.jwdeveloper.tiktok.data.models.RankingUser;
import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import io.github.lulajax.tiktok.server.data.LiveRoom;
import io.github.lulajax.tiktok.server.data.LiveRoomRankUser;
import io.github.lulajax.tiktok.server.data.repository.LiveRoomRankUserRepository;
import io.github.lulajax.tiktok.server.data.repository.LiveRoomRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class LiveRoomRankUserService {
    private final LiveRoomRepository liveRoomRepository;
    private final LiveRoomRankUserRepository liveRoomRankUserRepository;

    @Transactional(rollbackFor = Exception.class)
    public void updateRoomRankList(LiveRoomInfo roomInfo) {
        long timeStamp = System.currentTimeMillis();
        for (var rankUser : roomInfo.getUsersRanking()) {
            if (rankUser.getUser() == null || rankUser.getScore() <= 0) {
                continue;
            }
            var user = getLiveRoomRankUser(roomInfo, rankUser, timeStamp);
            liveRoomRankUserRepository.save(user);
        }
        updateRoomLiveData(roomInfo);
    }

    public void updateRoomLiveData(LiveRoomInfo roomInfo) {
        LiveRoom liveRoom = liveRoomRepository.findByRoomId(roomInfo.getRoomId());
        if (liveRoom != null) {
            if (roomInfo.getLikesCount() > 0) {
                liveRoom.setLikesCount(roomInfo.getLikesCount());
            }
            if (roomInfo.getViewersCount() > 0) {
                liveRoom.setViewersCount(roomInfo.getViewersCount());
            }
            if (roomInfo.getTotalViewersCount() > 0) {
                liveRoom.setTotalViewersCount(roomInfo.getTotalViewersCount());
            }
            liveRoom.setEndTime(DateUtil.currentSeconds());
            liveRoomRepository.save(liveRoom);
        }
    }

    private LiveRoomRankUser getLiveRoomRankUser(LiveRoomInfo roomInfo, RankingUser rankUser, long timeStamp) {
        var user = liveRoomRankUserRepository.findFirstByRoomIdAndUserId(roomInfo.getRoomId(), rankUser.getUser().getId());
        if (user == null) {
            user = new LiveRoomRankUser();
        }

        user.setHostId(roomInfo.getHost().getId());
        user.setHostName(roomInfo.getHost().getName());
        user.setRoomId(roomInfo.getRoomId());
        user.setRank(rankUser.getRank());
        user.setScore(rankUser.getScore());
        user.setUserId(rankUser.getUser().getId());
        user.setUserName(rankUser.getUser().getName());
        user.setUserPictureLink(rankUser.getUser().getPicture().getLink());
        user.setTimeStamp(timeStamp);
        return user;
    }
}
