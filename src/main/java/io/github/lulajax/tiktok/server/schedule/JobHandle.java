package io.github.lulajax.tiktok.server.schedule;

import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.util.IpUtil;
import io.github.lulajax.tiktok.server.data.pk.PkRound;
import io.github.lulajax.tiktok.server.data.pk.repository.PkRoundRepository;
import io.github.lulajax.tiktok.server.service.LiveClientService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@AllArgsConstructor
@Component
public class JobHandle {

    private final LiveClientService liveClientService;
    private final PkRoundRepository pkRoundRepository;


    @XxlJob("checkLiveClient")
    public void checkLiveClient() {
        log.info("Checking clients");
        liveClientService.getClientConnectList(null).stream()
                .filter(x -> IpUtil.getIp().equals(x.getServerIp()))
                .filter(x -> !x.isDeleted())
                .filter(x -> x.getPriorityMonitor() == null || x.getPriorityMonitor() == 2)
                .peek(x -> log.info("checkLiveClient hostName:{}", x.getHostName()))
                .forEach(x -> liveClientService.createClientConnectBySchedule(x.getHostName()));
    }

    @XxlJob("checkLiveClientFast")
    public void checkLiveClientFast() {
        liveClientService.getClientConnectList(null).stream()
                .filter(x -> IpUtil.getIp().equals(x.getServerIp()))
                .filter(x -> !x.isDeleted())
                .filter(x -> x.getPriorityMonitor() != null && (x.getPriorityMonitor() == 1 || x.getPriorityMonitor() == 3))
                .peek(x -> log.info("checkLiveClientFast hostName:{}", x.getHostName()))
                .forEach(x -> liveClientService.createClientConnectBySchedule(x.getHostName()));
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
                .filter(x -> x.getPriorityMonitor() != null && x.getPriorityMonitor() == 3)
                .peek(x -> log.info("checkLiveClientPKing hostName:{}", x.getHostName()))
                .forEach(x -> liveClientService.createClientConnectBySchedule(x.getHostName()));
    }
}
