package io.github.lulajax.tiktok.server.controller;


import io.github.lulajax.tiktok.server.response.CommonResult;
import io.github.lulajax.tiktok.server.response.RankListResponse;
import io.github.lulajax.tiktok.server.service.play.TiktokPlayRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tiktok-play")
public class TiktikPlayController {

    private final TiktokPlayRequestService tiktokPlayRequestService;

    @GetMapping("/ranklist")
    public CommonResult<RankListResponse> getRankList(@RequestParam("hostId") String hostId, @RequestParam("roomId") String roomId) {
        return CommonResult.success(tiktokPlayRequestService.getRankList(hostId, roomId));
    }

    @GetMapping("/rankscore")
    public CommonResult<Long> getRanListScore(@RequestParam("hostId") String hostId, @RequestParam("roomId") String roomId) {
        return CommonResult.success(tiktokPlayRequestService.getRanListScore(hostId, roomId));
    }
}
