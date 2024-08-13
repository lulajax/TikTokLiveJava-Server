/*
 * Copyright (c) 2023-2023 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.jwdeveloper.tiktok.server.service;

import io.github.jwdeveloper.tiktok.data.models.RankingUser;
import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import io.github.jwdeveloper.tiktok.server.data.LiveRoomRankUser;
import io.github.jwdeveloper.tiktok.server.data.repository.LiveRoomRankUserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class LiveRoomRankUserService {
    private final LiveRoomRankUserRepository liveRoomRankUserRepository;

    @Transactional(rollbackFor = Exception.class)
    public void updateRoomRankList(LiveRoomInfo roomInfo) {
        long timeStamp = System.currentTimeMillis();
        for (var rankUser : roomInfo.getUsersRanking()) {
            if (rankUser.getUser() == null || rankUser.getScore() <= 0) {
                continue;
            }
            if (roomInfo.getHost() == null) {
                continue;
            }
            var user = getLiveRoomRankUser(roomInfo, rankUser, timeStamp);
            liveRoomRankUserRepository.save(user);
        }
    }

    private LiveRoomRankUser getLiveRoomRankUser(LiveRoomInfo roomInfo, RankingUser rankUser, long timeStamp) {
        var user = liveRoomRankUserRepository.findFirstByRoomIdAndUserName(roomInfo.getRoomId(), rankUser.getUser().getName());
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
