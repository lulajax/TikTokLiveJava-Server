package io.github.lulajax.tiktok.server.service;

import io.github.lulajax.tiktok.server.data.repository.LiveRoomRankUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LiveRoomRankUserServiceTest {

    @Autowired
    LiveRoomRankUserRepository liveRoomRankUserRepository;
    @Test
    void updateRoomRankList() {
        liveRoomRankUserRepository.findFirstByRoomIdAndUserId("7402252429175360264", 1L);
    }
}