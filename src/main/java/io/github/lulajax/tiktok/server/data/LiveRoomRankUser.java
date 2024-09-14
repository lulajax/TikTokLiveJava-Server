package io.github.lulajax.tiktok.server.data;

import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "live_room_rank_user", indexes = {
        @Index(name = "idx_live_room_rank_user_roomId", columnList = "roomId"),
        @Index(name = "idx_live_room_rank_user_hostId", columnList = "hostId")
})
public class LiveRoomRankUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;
    private Long hostId;
    private String hostName;

    @Column(name = "rank_value")
    private Integer rank;
    private Integer score;
    private Long userId;
    private String userName;
    private String userProfileName;
   
    @Column(name = "user_picture_link")
    private String userPictureLink;
    private Long timeStamp;
}
