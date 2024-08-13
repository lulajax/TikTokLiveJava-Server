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
package io.github.jwdeveloper.tiktok.server.data;

import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "live_room", indexes = {
        @Index(name = "idx_live_room_roomId", columnList = "roomId"),
        @Index(name = "idx_live_room_hostId", columnList = "hostId"),
        @Index(name = "idx_live_room_hostName", columnList = "hostName")
})
public class LiveRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;
    private int likesCount;
    private int viewersCount;
    private int totalViewersCount;
    private long startTime;
    private long endTime;
    private Long hostId;
    private String hostName;

    public LiveRoom buildFrom(LiveRoomInfo roomInfo) {
        this.roomId = roomInfo.getRoomId();
        if (roomInfo.getLikesCount() > 0) {
            this.likesCount = roomInfo.getLikesCount();
        }
        if (roomInfo.getViewersCount() > 0) {
            this.viewersCount = roomInfo.getViewersCount();
        }
        if (roomInfo.getTotalViewersCount() > 0) {
            this.totalViewersCount = roomInfo.getTotalViewersCount();
        }
        this.startTime = roomInfo.getStartTime();
        this.hostId = roomInfo.getHost().getId();
        this.hostName = roomInfo.getHost().getName();
        return this;
    }
}
