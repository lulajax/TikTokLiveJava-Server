package io.github.lulajax.tiktok.server.schedule;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.util.IpUtil;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import io.github.lulajax.tiktok.server.data.LiveClientConnect;
import io.github.lulajax.tiktok.server.data.pk.PkRound;
import io.github.lulajax.tiktok.server.data.pk.repository.PkRoundRepository;
import io.github.lulajax.tiktok.server.service.LiveClientService;
import io.github.lulajax.tiktok.server.service.LiveRoomService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.http.HttpTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@AllArgsConstructor
@Component
public class JobHandle {

    private final LiveClientService liveClientService;
    private final LiveRoomService liveRoomService;
    private final PkRoundRepository pkRoundRepository;


    @XxlJob("checkLiveClient")
    public void checkLiveClient() {
        log.info("Checking clients");
        liveClientService.getClientConnectList(null).stream()
                .filter(x -> IpUtil.getIp().equals(x.getServerIp()))
                .filter(x -> !x.isDeleted())
                .filter(x -> x.getPriorityMonitor() == null || x.getPriorityMonitor() == 2)
                .peek(x -> log.info("checkLiveClient hostName:{}", x.getHostName()))
                .forEach(this::createClientConnect);
    }

    @XxlJob("checkLiveClientFast")
    public void checkLiveClientFast() {
        liveClientService.getClientConnectList(null).stream()
                .filter(x -> IpUtil.getIp().equals(x.getServerIp()))
                .filter(x -> !x.isDeleted())
                .filter(x -> x.getPriorityMonitor() != null && x.getPriorityMonitor() == 1)
                .peek(x -> log.info("checkLiveClientFast hostName:{}", x.getHostName()))
                .forEach(this::createClientConnect);
    }

    @XxlJob("checkLiveClientPKing")
    public void checkLiveClientPKing() {
        List<String> liveClientPKingHosts = pkRoundRepository.findAllPkingRoundList().stream().map(PkRound::getHostName).toList();
        if (liveClientPKingHosts.isEmpty()) {
            log.info("checkLiveClientPKing 没有PK中的直播间");
            return;
        }
        liveClientService.getClientConnects(liveClientPKingHosts).stream()
                .filter(x -> IpUtil.getIp().equals(x.getServerIp()))
                .filter(x -> !x.isDeleted())
                .peek(x -> log.info("checkLiveClientPKing hostName:{}", x.getHostName()))
                .forEach(this::createClientConnect);
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
                    liveClientService.disconnect(x.getHostName(), "连续两次检测到未开播，断开连接");
                }
            } else {
                log.info("createClientConnect hostName:{} 不存在", x.getHostName());
                ThreadUtil.safeSleep(6000);
            }
            liveClientService.setUserStatus(x.getHostName(), liveUserData.getUserStatus().name());
            lastConnectStatus.put(x.getHostName(), liveUserData.isLiveOnline());
        } catch (TikTokLiveRequestException e) {
            if (e.getCause() instanceof HttpTimeoutException) {
                log.error("开播监控启动失败 请求超超时 hostName:{}", x.getHostName(), e);
                ThreadUtil.safeSleep(6000);
            } else {
                log.info("开播监控启动失败 hostName:{}", x.getHostName(), e);
            }
        } catch (Exception e) {
            log.error("开播监控启动失败 hostName:{}", x.getHostName(), e);
        }
    }
}
