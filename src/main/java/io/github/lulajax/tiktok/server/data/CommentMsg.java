package io.github.lulajax.tiktok.server.data;

import io.github.jwdeveloper.tiktok.data.events.TikTokCommentEvent;
import io.github.jwdeveloper.tiktok.data.events.TikTokEmoteEvent;
import io.github.jwdeveloper.tiktok.data.models.Emote;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import jakarta.persistence.*;
import lombok.Data;

import java.util.stream.Collectors;

@Data
@Entity
@Table(name = "comment_msg", indexes = {
        @Index(name = "idx_comment_msg_roomId", columnList = "roomId"),
        @Index(name = "idx_comment_msg_hostId", columnList = "hostId"),
        @Index(name = "idx_comment_msg_messageId", columnList = "messageId", unique = true)
})
public class CommentMsg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;
    private Long hostId;
    private String hostName;

    @Column(name = "text")
    private String text;

    @Column(name = "link")
    private String link;

    private Long userId;
    private String userName;
    private String userProfileName;
    @Column(name = "user_picture_link")
    private String userPictureLink;
    private String userLanguage;

    private Long messageId;
    private Long timeStamp;

    public CommentMsg buildFrom(LiveClient liveClient, TikTokCommentEvent event) {
        this.roomId = liveClient.getRoomInfo().getRoomId();
        this.hostId = liveClient.getRoomInfo().getHost().getId();
        this.hostName = liveClient.getRoomInfo().getHost().getName();
        this.text = event.getText();
        this.link = event.getPictures().stream().map(Picture::getLink).collect(Collectors.joining(","));
        this.userId = event.getUser().getId();
        this.userName = event.getUser().getName();
        this.userProfileName = event.getUser().getProfileName();
        this.userPictureLink = event.getUser().getPicture().getLink();
        this.userLanguage = event.getUserLanguage();
        this.messageId = event.getMessageId();
        this.timeStamp = event.getTimeStamp();
        return this;
    }

    public CommentMsg buildFrom(LiveClient liveClient, TikTokEmoteEvent event) {
        this.roomId = liveClient.getRoomInfo().getRoomId();
        this.hostId = liveClient.getRoomInfo().getHost().getId();
        this.hostName = liveClient.getRoomInfo().getHost().getName();
        this.link = event.getEmotes().stream().map(Emote::getPicture).map(Picture::getLink).collect(Collectors.joining(","));
        this.userId = event.getUser().getId();
        this.userName = event.getUser().getName();
        this.userPictureLink = event.getUser().getPicture().getLink();
        this.messageId = event.getMessageId();
        this.timeStamp = event.getTimeStamp();
        return this;
    }
}
