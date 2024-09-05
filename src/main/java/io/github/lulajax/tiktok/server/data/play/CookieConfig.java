package io.github.lulajax.tiktok.server.data.play;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name="tt_cookie_config")
public class CookieConfig implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "webcast_feed_url")
    private String webcastFeedUrl;

    @Column(name = "ranklist_online_audience_url")
    private String ranklistOInlineAudienceUrl;

    @Column(name = "webcast_room_enter_url")
    private String webcastRoomEnterUrl;

    @Column(name = "`cookie`")
    private String cookie;

    @Column(name = "create_time_stamp")
    private Long createTimeStamp;

    @Column(name = "update_time_stamp")
    private Long updateTimeStamp;

    @Column(name = "deleted")
    private boolean deleted;
}