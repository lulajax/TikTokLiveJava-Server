package io.github.lulajax.tiktok.server.service.play;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import io.github.lulajax.tiktok.server.data.play.CookieConfig;
import io.github.lulajax.tiktok.server.data.play.repository.CookieConfigRepository;
import io.github.lulajax.tiktok.server.response.RankListResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class TiktokPlayRequestService {

    private final RankListDataMapper rankListDataMapper = new RankListDataMapper();
    private final CookieConfigRepository cookieConfigRepository;


    public Long getRanListScore(String hostId, String roomId) {
        var ranks = getRankList(hostId, roomId).getRanks();
        return ranks.stream().mapToLong(RankListResponse.RankUser::getScore).sum();
    }

    public RankListResponse getRankList(String hostId, String roomId) {
        CookieConfig cookieConfig = getCookieConfig();
        if (cookieConfig == null) {
            log.error("cookieConfig is null");
            return new RankListResponse(new ArrayList<>());
        }
        String url = cookieConfig.getRanklistOInlineAudienceUrl();
        // 替换 url中的 anchor_id和room_id
        url = url.replace("{anchor_id}", hostId).replace("{room_id}", roomId);
        HttpRequest request = HttpRequest.get(url).cookie(cookieConfig.getCookie()).headerMap(getHeaders(cookieConfig.getCookie()), true);

        try (HttpResponse response = request.execute(false)){
            if (response.isOk()) {
                log.info("rankList success");
                String body = response.body();
                log.info("rankList body: {}", body);
                return rankListDataMapper.map(body);
            } else {
                log.error("rankList error");
            }
        }
        return new RankListResponse(new ArrayList<>());
    }

    

    private CookieConfig getCookieConfig() {
        return cookieConfigRepository.findAll().stream().filter(x -> !x.isDeleted()).findFirst().orElse(null);
    }

    private Map<String, String> getHeaders(String cookie) {
        Map<String, String> headers = new HashMap<>();
        headers.put("accept", "*/*");
        headers.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        headers.put("cookie", cookie);
        headers.put("origin", "https://www.tiktok.com");
        headers.put("priority", "u=1, i");
        headers.put("referer", "https://www.tiktok.com/");
        headers.put("sec-ch-ua", "\"Not)A;Brand\";v=\"99\", \"Microsoft Edge\";v=\"127\", \"Chromium\";v=\"127\"");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("sec-ch-ua-platform", "\"Windows\"");
        headers.put("sec-fetch-dest", "empty");
        headers.put("sec-fetch-mode", "cors");
        headers.put("sec-fetch-site", "same-site");
        headers.put("sec-gpc", "1");
        headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36 Edg/127.0.0.0");
        return headers;
    }
}
