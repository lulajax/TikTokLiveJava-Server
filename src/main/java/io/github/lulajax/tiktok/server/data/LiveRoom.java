package io.github.lulajax.tiktok.server.data;

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
