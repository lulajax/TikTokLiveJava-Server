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

import com.xxl.job.core.util.IpUtil;
import io.github.jwdeveloper.tiktok.models.ConnectionState;
import io.github.jwdeveloper.tiktok.server.data.LiveClientConnect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class LiveClientConnectRecoveryRunner implements ApplicationRunner {
    private final LiveClientService liveClientService;

    public LiveClientConnectRecoveryRunner(LiveClientService liveClientService) {
        this.liveClientService = liveClientService;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {
        log.info("--------------------初始化客户端socket连接池---------------------");
        liveClientService.getClientConnectList(null).stream()
                .filter(x -> IpUtil.getIp().equals(x.getServerIp()))
                .filter(x -> ConnectionState.CONNECTED.name().equals(x.getConnectionState()))
                .forEach(x -> {
                    try {
                        liveClientService.createClientConnect(x.getHostName());
                    } catch (Exception e) {
                        log.error("初始化客户端socket连接池失败 hostName:{}", x.getHostName(), e);
                    }
                });
        log.info("--------------------初始化客户端socket连接池完成---------------------");
    }
}
