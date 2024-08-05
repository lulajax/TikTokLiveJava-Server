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

import io.github.jwdeveloper.tiktok.data.events.TikTokCommentEvent;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import jakarta.persistence.*;
import lombok.Data;

import java.util.stream.Collectors;

@Data
@Entity
@Table(name = "comment_msg", indexes = {
        @Index(name = "idx_comment_msg_roomId", columnList = "roomId"),
        @Index(name = "idx_comment_msg_hostId", columnList = "hostId")
})
public class CommentMsg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;
    private long hostId;
    private String hostName;

    @Lob
    @Column(name = "text", length = 1000)
    private String text;

    @Lob
    @Column(name = "link", length = 1000)
    private String link;

    private Long userId;
    private String userName;
    @Lob
    @Column(name = "user_picture_link", length = 1000)
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
        this.userPictureLink = event.getUser().getPicture().getLink();
        this.userLanguage = event.getUserLanguage();
        this.messageId = event.getMessageId();
        this.timeStamp = event.getTimeStamp();
        return this;
    }
}
