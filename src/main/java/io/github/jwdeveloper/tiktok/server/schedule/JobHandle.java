package io.github.jwdeveloper.tiktok.server.schedule;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.github.jwdeveloper.tiktok.server.data.LiveClientConnect;
import io.github.jwdeveloper.tiktok.server.service.LiveClientService;
import io.github.jwdeveloper.tiktok.server.service.LiveRoomService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


@Slf4j
@AllArgsConstructor
@Component
public class JobHandle {

    private final LiveClientService liveClientService;
    private final LiveRoomService liveRoomService;

    @XxlJob("checkLiveClient")
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

    @XxlJob("checkLiveClientFast")
    public void checkLiveClientFast() {
        String param = XxlJobHelper.getJobParam();
        log.info("Checking clients fast: {}", param);
        Arrays.asList(param.split(",")).forEach(hostName -> {
            try {
                var liveUserData = liveClientService.getLiveUserData(hostName);
                if (liveUserData != null && liveUserData.isLiveOnline()) {
                    log.info("hostName:{} 正在直播中", hostName);
                    var liveData = liveClientService.getLiveData(liveUserData.getRoomId());
                    liveRoomService.liveUpdateByRoomId(liveData, liveUserData.getRoomId());
                    liveClientService.createClientConnect(hostName, liveUserData.getRoomId());
                } else {
                    log.info("hostName:{} 不在直播中", hostName);
                    liveClientService.disconnect(hostName);
                }
            } catch (Exception e) {
                log.error("开播监控启动失败 hostName:{}", hostName, e);
            }
        });
    }
}
