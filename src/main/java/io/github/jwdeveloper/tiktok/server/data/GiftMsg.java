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

    @Lob
    @Column(name = "gift_picture_link", length = 1000)
    private String giftPictureLink;


    private Long userId;
    private String userName;
    private String userProfileName;

    @Lob
    @Column(name = "user_picture_link", length = 1000)
    private String userPictureLink;

    private Long touserId;
    private String touserName;

    @Lob
    @Column(name = "touser_picture_link", length = 1000)
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
