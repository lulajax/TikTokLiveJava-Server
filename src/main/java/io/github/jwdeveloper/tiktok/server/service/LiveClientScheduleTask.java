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
package io.github.jwdeveloper.tiktok.server.service;

import io.github.jwdeveloper.tiktok.server.data.LiveClientConnect;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class LiveClientScheduleTask {
    private final LiveClientService liveClientService;
    private final LiveRoomService liveRoomService;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void checkLiveClient() {
        log.info("Checking clients");
        List<LiveClientConnect> connects = liveClientService.getClientConnectList(null).stream().toList();
        connects.forEach(x -> {
            try {
                var liveUserData = liveClientService.getLiveUserData(x.getHostName());
                if (liveUserData != null && liveUserData.isLiveOnline()) {
                    log.info("hostName:{} 正在直播中", x.getHostName());
                    var liveData = liveClientService.getLiveData(liveUserData.getRoomId());
                    liveRoomService.liveUpdateByRoomId(liveData, liveUserData.getRoomId());
                    liveClientService.createClientConnect(x.getHostName(), liveUserData.getRoomId());
                } else {
                    log.info("hostName:{} 不在直播中", x.getHostName());
                    liveClientService.disconnect(x.getHostName());
                }
            } catch (Exception e) {
                log.error("开播监控启动失败 hostName:{}", x.getHostName(), e);
            }
        });
    }
}
