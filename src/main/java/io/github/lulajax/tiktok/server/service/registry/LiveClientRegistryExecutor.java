package io.github.lulajax.tiktok.server.service.registry;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;

public class LiveClientRegistryExecutor implements SmartInitializingSingleton, DisposableBean {

    private final String ip;
    private final int port;
    private final String serverGroup;
    private final String registryServerUrl;

    public LiveClientRegistryExecutor(String ip, int port, String serverGroup, String registryServerUrl) {
        this.ip = ip;
        this.port = port;
        this.serverGroup = serverGroup;
        this.registryServerUrl = registryServerUrl;
    }

    @Override
    public void destroy() {
        stopRegistry();
    }

    @Override
    public void afterSingletonsInstantiated() {
        try {
            startRegistry();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void startRegistry() {
        // start registry
        RegistryThread.getInstance().start(ip, port, serverGroup, registryServerUrl);
    }

    private void stopRegistry() {
        // stop registry
        RegistryThread.getInstance().toStop();
    }
}
