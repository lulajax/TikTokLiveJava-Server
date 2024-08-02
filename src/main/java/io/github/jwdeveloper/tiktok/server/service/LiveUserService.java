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

import cn.hutool.core.date.DateUtil;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.server.data.CommentMsg;
import io.github.jwdeveloper.tiktok.server.data.GiftMsg;
import io.github.jwdeveloper.tiktok.server.data.LiveRoom;
import io.github.jwdeveloper.tiktok.server.data.repository.CommentMsgRepository;
import io.github.jwdeveloper.tiktok.server.data.repository.GiftMsgRepository;
import io.github.jwdeveloper.tiktok.server.data.repository.LiveRoomRepository;
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
