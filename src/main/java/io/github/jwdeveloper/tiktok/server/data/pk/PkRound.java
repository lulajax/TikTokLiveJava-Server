package io.github.jwdeveloper.tiktok.server.data.pk;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import lombok.Data;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Data
@Table(name="pk_round")
public class PkRound implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "config_id",nullable = false)
    private Integer configId;

    private String roomId;

    private String hostName;

    @Column(name = "status")
    private Integer status;

    @Column(name = "start_time")
    private Long startTime;

    @Column(name = "end_time")
    private Long endTime;

    public void copy(PkRound source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}