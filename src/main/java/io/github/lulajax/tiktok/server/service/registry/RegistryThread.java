package io.github.lulajax.tiktok.server.service.registry;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import io.github.lulajax.tiktok.server.response.CommonResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RegistryThread {
    @Getter
    private static RegistryThread instance = new RegistryThread();

    private Thread registryThread;
    private volatile boolean toStop = false;
    public void start(final String ip, final int port, final String serverGroup, final String registryServerUrl){

        registryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // registry
                while (!toStop) {
                    try {
                        try {
                            CommonResult<String> registryResult = registry(ip, port, serverGroup, registryServerUrl);
                            if (registryResult != null && registryResult.isSuccess()) {
                                log.debug(">>>>>>>>>>> live client registry success, ip:{}, port:{}, registryServerUrl:{}", ip, port, registryServerUrl);
                            } else {
                                log.info(">>>>>>>>>>> live client registry fail, ip:{}, port:{}, registryServerUrl:{}", ip, port, registryServerUrl);
                            }
                        } catch (Exception e) {
                            log.info(">>>>>>>>>>> live client registry error, ip:{}, port:{}, registryServerUrl:{}", ip, port, registryServerUrl, e);
                        }
                    } catch (Exception e) {
                        if (!toStop) {
                            log.error(e.getMessage(), e);
                        }
                    }

                    try {
                        if (!toStop) {
                            TimeUnit.SECONDS.sleep(30);
                        }
                    } catch (InterruptedException e) {
                        if (!toStop) {
                            log.warn(">>>>>>>>>>> live client, executor registry thread interrupted, error msg:{}", e.getMessage());
                        }
                    }
                }

                // registry remove
                try {
                    try {
                        CommonResult<String> registryResult = registryRemove(ip, port, registryServerUrl);
                        if (registryResult != null && registryResult.isSuccess()) {
                            log.info(">>>>>>>>>>> live client registry-remove success, ip:{}, port:{}, registryServerUrl:{}", ip, port, registryServerUrl);
                        } else {
                            log.info(">>>>>>>>>>> live client registry-remove fail, ip:{}, port:{}, registryServerUrl:{}", ip, port, registryServerUrl);
                        }
                    } catch (Exception e) {
                        if (!toStop) {
                            log.info(">>>>>>>>>>> live client registry-remove error, ip:{}, port:{}, registryServerUrl:{}", ip, port, registryServerUrl, e);
                        }
                    }
                } catch (Exception e) {
                    if (!toStop) {
                        log.error(e.getMessage(), e);
                    }
                }
                log.info(">>>>>>>>>>> live client, executor registry thread destroy.");

            }
        });
        registryThread.setDaemon(true);
        registryThread.setName("live client, executor RegistryThread");
        registryThread.start();
    }

    private CommonResult<String> registry(final String ip, final int port, final String serverGroup, final String registryServerUrl) {
        String url = registryServerUrl + "/api/tiktok-live/registry?ip=" + ip + "&port=" + port + "&serverGroup=" + serverGroup;
        String response = HttpUtil.get(url, 5000);
        try {
            Type type = new TypeReference<CommonResult<String>>(){}.getType();
            return JSONUtil.toBean(response, type, false);
        } catch (Exception e) {
            log.error("解析失败: {}", e.getMessage(), e);
            return null;
        }
    }
    private CommonResult<String> registryRemove(final String ip, final int port, final String registryServerUrl) {
        String url = registryServerUrl + "/api/tiktok-live/client/registry/remove?ip=" + ip + "&port=" + port;
        String response = HttpUtil.get(url, 5000);
        try {
            Type type = new TypeReference<CommonResult<String>>(){}.getType();
            return JSONUtil.toBean(response, type, false);
        } catch (Exception e) {
            log.error("解析失败: {}", e.getMessage(), e);
            return null;
        }
    }

    public void toStop() {
        toStop = true;

        // interrupt and wait
        if (registryThread != null) {
            registryThread.interrupt();
            try {
                registryThread.join();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }

    }
}
