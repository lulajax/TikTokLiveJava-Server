package io.github.lulajax.tiktok.server.response;

import io.github.jwdeveloper.tiktok.data.models.users.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RankListResponse {

    private List<RankUser> ranks;

    @Data
    @AllArgsConstructor
    public static class RankUser {
        private int rank;
        private int score;
        private long userId;
        private String nickname;
    }
}
