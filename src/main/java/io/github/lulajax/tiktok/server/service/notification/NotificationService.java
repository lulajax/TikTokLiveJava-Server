package io.github.lulajax.tiktok.server.service.notification;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import io.github.lulajax.tiktok.server.response.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;

@Slf4j
@Service
public class NotificationService {

    @Value("${tiktoklive.registry.server.url:https://ttadmin-service.xiaooutech.com}")
    private String registryServerUrl;

    public CommonResult<String> send(CustomSocketMsg socketMsg, String hostName) {
        String url = registryServerUrl + "/api/tiktok-notification/send/" + hostName;
        log.info("send hostName:{} socketMsg:{} url:{}", hostName, socketMsg, url);
        String response = HttpUtil.post(url, JSONUtil.toJsonStr(socketMsg), 5000);
        log.info("send hostName:{} response:{}", hostName, response);
        try {
            Type type = new TypeReference<CommonResult<String>>(){}.getType();
            return JSONUtil.toBean(response, type, false);
        } catch (Exception e) {
            log.error("解析失败: {}", e.getMessage(), e);
            return null;
        }
    }
}
