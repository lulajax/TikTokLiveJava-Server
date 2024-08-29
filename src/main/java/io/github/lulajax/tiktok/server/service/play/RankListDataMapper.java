package io.github.lulajax.tiktok.server.service.play;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import io.github.lulajax.tiktok.server.response.RankListResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RankListDataMapper {
    public RankListResponse map(String json) {
        try {
            var jsonObject = JsonParser.parseString(json).getAsJsonObject();

            var statusCode = jsonObject.get("status_code").getAsInt();

            if (statusCode != 0) {
                throw new TikTokLiveRequestException("Failed to get rank list data");
            }

            var element = jsonObject.get("data");
            if (element.isJsonNull()) {
                return new RankListResponse(new ArrayList<>());
            }
            var data = element.getAsJsonObject();
            var ranks = data.getAsJsonArray("ranks");

            List<RankListResponse.RankUser> rankList = new ArrayList<>();
            for (JsonElement rankElement : ranks) {
                var rankObject = rankElement.getAsJsonObject();
                var rank = rankObject.get("rank").getAsInt();
                var score = rankObject.get("score").getAsInt();
                var userObject = rankObject.getAsJsonObject("user");

                rankList.add(new RankListResponse.RankUser(rank, score, userObject.get("id").getAsLong(), userObject.get("nickname").getAsString()));
            }

            return new RankListResponse(rankList);
        } catch (JsonSyntaxException | IllegalStateException e) {
            log.error("Malformed Json: '"+json+"' - Error Message: "+e.getMessage());
            return new RankListResponse(new ArrayList<>());
        }
    }
}