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
