package io.github.lulajax.tiktok.server.service;

import com.xxl.job.core.util.IpUtil;
import io.github.jwdeveloper.tiktok.models.ConnectionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

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
                .filter(x -> !x.isDeleted())
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
