package io.github.lulajax.tiktok.server.data;

import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "gift_msg", indexes = {
        @Index(name = "idx_gift_msg_roomId", columnList = "roomId"),
        @Index(name = "idx_gift_msg_hostId", columnList = "hostId"),
        @Index(name = "idx_gift_msg_messageId", columnList = "messageId", unique = true)
})
public class GiftMsg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;
    private Long hostId;
    private String hostName;

    private int giftId;

    private String giftName;

    private int diamondCost;

    @Column(name = "gift_picture_link")
    private String giftPictureLink;

    private Long userId;
    private String userName;
    private String userProfileName;

    @Column(name = "user_picture_link")
    private String userPictureLink;

    private Long touserId;
    private String touserName;

    @Column(name = "touser_picture_link")
    private String touserPictureLink;

    private int combo;

    private Long messageId;

    private Long timeStamp;

    public GiftMsg buildFrom(LiveClient liveClient, TikTokGiftEvent event) {
        this.roomId = liveClient.getRoomInfo().getRoomId();
        this.hostId = liveClient.getRoomInfo().getHost().getId();
        this.hostName = liveClient.getRoomInfo().getHost().getName();

        this.giftId = event.getGift().getId();
        this.giftName = event.getGift().getName();
        this.diamondCost = event.getGift().getDiamondCost();
        this.giftPictureLink = event.getGift().getPicture().getLink();
        this.userId = event.getUser().getId();
        this.userName = event.getUser().getName();
        this.userProfileName = event.getUser().getProfileName();
        this.userPictureLink = event.getUser().getPicture().getLink();
        this.touserId = event.getToUser().getId();
        this.touserName = event.getToUser().getName();
        this.touserPictureLink = event.getToUser().getPicture().getLink();
        this.combo = event.getCombo();
        this.messageId = event.getMessageId();
        this.timeStamp = event.getTimeStamp();
        return this;
    }
}
