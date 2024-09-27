package io.github.lulajax.tiktok.server.data;

import io.github.jwdeveloper.tiktok.data.events.social.TikTokJoinEvent;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "enter_room_msg")
public class EnterRoomMsg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;
    private Long hostId;
    private String hostName;

    private Long userId;
    private String userName;
    private String userProfileName;
    @Column(name = "user_picture_link")
    private String userPictureLink;

    private Long messageId;
    private Long timeStamp;

    public EnterRoomMsg buildFrom(LiveClient liveClient, TikTokJoinEvent event) {
        this.roomId = liveClient.getRoomInfo().getRoomId();
        this.hostId = liveClient.getRoomInfo().getHost().getId();
        this.hostName = liveClient.getRoomInfo().getHost().getName();
        this.userId = event.getUser().getId();
        this.userName = event.getUser().getName();
        this.userProfileName = event.getUser().getProfileName();
        this.userPictureLink = event.getUser().getPicture().getLink();
        this.messageId = event.getMessageId();
        this.timeStamp = event.getTimeStamp();
        return this;
    }
}
