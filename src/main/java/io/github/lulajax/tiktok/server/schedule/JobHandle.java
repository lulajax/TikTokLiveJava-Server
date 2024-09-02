package io.github.lulajax.tiktok.server.schedule;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.util.IpUtil;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import io.github.lulajax.tiktok.server.data.LiveClientConnect;
import io.github.lulajax.tiktok.server.service.LiveClientService;
import io.github.lulajax.tiktok.server.service.LiveRoomService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@AllArgsConstructor
@Component
public class JobHandle {

    private final LiveClientService liveClientService;
    private final LiveRoomService liveRoomService;

    @XxlJob("checkLiveClient")
    public void checkLiveClient() {
        log.info("Checking clients");
        liveClientService.getClientConnectList(null).stream()
                .filter(x -> IpUtil.getIp().equals(x.getServerIp()))
                .filter(x -> !x.isDeleted())
                .forEach(this::createClientConnect);
    }

    @XxlJob("checkLiveClientFast")
    public void checkLiveClientFast() {
        String param = XxlJobHelper.getJobParam();
        log.info("Checking clients fast: {}", param);
        Arrays.asList(param.split(",")).forEach(hostName -> {
            var x = liveClientService.getClientConnect(hostName);
            if (x != null && IpUtil.getIp().equals(x.getServerIp()) && !x.isDeleted()) {
                createClientConnect(x);
            }
        });
    }

    private final Map<String, Boolean> lastConnectStatus = new HashMap<>();
    private void createClientConnect(LiveClientConnect x) {
        try {
            var liveUserData = liveClientService.getLiveUserData(x.getHostName());
            log.info("createClientConnect hostName:{} data={}", x.getHostName(), JSONUtil.toJsonStr(liveUserData));
            if (liveUserData.isLiveOnline()) {
                log.info("createClientConnect hostName:{} 正在直播中", x.getHostName());
                var liveData = liveClientService.getLiveData(liveUserData.getRoomId());
                liveRoomService.liveUpdateByRoomId(liveData, liveUserData.getRoomId());
                liveClientService.createClientConnect(x.getHostName(), liveUserData.getRoomId());
            } else if (liveUserData.isHostNameValid()){
                log.info("createClientConnect hostName:{} 不在直播中", x.getHostName());
                // 连续两次检测到未开播，断开连接
                if (lastConnectStatus.containsKey(x.getHostName()) && !lastConnectStatus.get(x.getHostName())) {
                    liveClientService.disconnect(x.getHostName());
                }
            } else {
                log.info("createClientConnect hostName:{} 不存在", x.getHostName());
                ThreadUtil.safeSleep(6000);
            }
            liveClientService.setUserStatus(x.getHostName(), liveUserData.getUserStatus().name());
            lastConnectStatus.put(x.getHostName(), liveUserData.isLiveOnline());
        } catch (TikTokLiveRequestException e) {
            log.info("开播监控启动失败 hostName:{}", x.getHostName(), e);
        } catch (Exception e) {
            log.error("开播监控启动失败 hostName:{}", x.getHostName(), e);
        }
    }
}
