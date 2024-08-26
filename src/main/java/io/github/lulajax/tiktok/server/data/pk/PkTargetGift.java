package io.github.lulajax.tiktok.server.data.pk;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name="pk_target_gift")
public class PkTargetGift implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "config_id",nullable = false)
    private Integer configId;

//    @JoinColumn(name = "config_id")
//    @ManyToOne(fetch = FetchType.LAZY)
//    private PkConfig pkConfig;

    @Column(name = "player")
    private String player;

    @Column(name = "gift_id")
    private Integer giftId;

    @Column(name = "gift_name")
    private String giftName;

    @Column(name = "gift_picture_link")
    private String giftPictureLink;

    public void copy(PkTargetGift source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}