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

import io.github.jwdeveloper.tiktok.data.requests.LiveUserData;
import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import io.github.jwdeveloper.tiktok.models.ConnectionState;
import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "live_client_connect")
public class LiveClientConnect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;
    private int likesCount;
    private int viewersCount;
    private int totalViewersCount;
    private long startTime;
    private boolean ageRestricted;
    private long hostId;
    private String hostName;
    private String hostProfileName;
    @Lob
    @Column(name = "host_picture_link", length = 1000)
    private String hostPictureLink;

    private long hostFollowing;
    private long hostFollowers;

    private String title;
    private String language;
    private String connectionState;

    public LiveClientConnect buildFrom(LiveRoomInfo roomInfo) {
        this.roomId = roomInfo.getRoomId();
        this.likesCount = roomInfo.getLikesCount();
        this.viewersCount = roomInfo.getViewersCount();
        this.totalViewersCount = roomInfo.getTotalViewersCount();
        this.startTime = roomInfo.getStartTime();
        this.ageRestricted = roomInfo.isAgeRestricted();
        this.hostId = roomInfo.getHost().getId();
        this.hostName = roomInfo.getHost().getName();
        this.hostProfileName = roomInfo.getHost().getProfileName();
        this.hostPictureLink = roomInfo.getHost().getPicture().getLink();
        this.hostFollowing = roomInfo.getHost().getFollowing();
        this.hostFollowers = roomInfo.getHost().getFollowers();
        this.title = roomInfo.getTitle();
        this.connectionState = roomInfo.getConnectionState().name();
        return this;
    }

    public LiveClientConnect buildFrom(LiveUserData.Response liveUserData) {
        this.roomId = liveUserData.getRoomId();
        this.startTime = liveUserData.getStartTime();
        this.hostId = liveUserData.getUser().getId();
        this.hostName = liveUserData.getUser().getName();
        this.hostProfileName = liveUserData.getUser().getProfileName();
        this.hostPictureLink = liveUserData.getUser().getPicture().getLink();
        this.hostFollowing = liveUserData.getUser().getFollowing();
        this.hostFollowers = liveUserData.getUser().getFollowers();
        this.connectionState =  liveUserData.isLiveOnline() ? ConnectionState.CONNECTED.name() : ConnectionState.DISCONNECTED.name();
        return this;
    }
}
