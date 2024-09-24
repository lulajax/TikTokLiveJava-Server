package io.github.lulajax.tiktok.server.config;


import com.xxl.job.core.util.IpUtil;
import io.github.lulajax.tiktok.server.service.registry.LiveClientRegistryExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class LiveClientRegistryConfig {
    @Value("${server.port}")
    private int port;

    @Value("${tiktoklive.registry.open:false}")
    private boolean isOpen;
    @Value("${tiktoklive.server.group:NORMAL}")
    private String serverGroup;
    @Value("${tiktoklive.registry.server.url:https://ttadmin-service.xiaooutech.com}")
    private String registryServerUrl;


    @Bean
    public LiveClientRegistryExecutor liveClientRegistryExecutor() {
        if (isOpen) {
            log.info(">>>>>>>>>>> live client config init.");
            String ip = IpUtil.getIp();
            return new LiveClientRegistryExecutor(ip, port, serverGroup, registryServerUrl);
        }
        return null;
    }
}
