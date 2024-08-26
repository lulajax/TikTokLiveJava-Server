package io.github.lulajax.tiktok.server.data;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "connect_log", indexes = {
        @Index(name = "idx_connect_log_roomId", columnList = "roomId"),
        @Index(name = "idx_connect_log_hostId", columnList = "hostId"),
        @Index(name = "idx_connect_log_hostName", columnList = "hostName")
})
public class ConnectLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;
    private Long hostId;
    private String hostName;
    private String connectionState;
    private Long timeStamp;

    public ConnectLog(String roomId, Long hostId, String hostName, String connectionState) {
        this.roomId = roomId;
        this.hostId = hostId;
        this.hostName = hostName;
        this.connectionState = connectionState;
        this.timeStamp = System.currentTimeMillis();
    }

    public ConnectLog() {

    }
}
