package io.github.lulajax.tiktok.server.controller;


import io.github.lulajax.tiktok.server.data.CommentMsg;
import io.github.lulajax.tiktok.server.data.GiftMsg;
import io.github.lulajax.tiktok.server.data.LiveRoom;
import io.github.lulajax.tiktok.server.data.LiveRoomRankUser;
import io.github.lulajax.tiktok.server.response.CommonResult;
import io.github.lulajax.tiktok.server.service.LiveRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tiktok-live/room")
public class LiveRoomController {

    private final LiveRoomService liveRoomService;

    @GetMapping("/list")
    public CommonResult<List<LiveRoom>> rooms(@RequestParam("hostName") String hostName) {
        return CommonResult.success(liveRoomService.getLiveRooms(hostName));
    }

    @GetMapping("/giftMsg/list")
    public CommonResult<List<GiftMsg>> giftMsgList(@RequestParam("roomId") String roomId) {
        return CommonResult.success(liveRoomService.getGiftMsgList(roomId));
    }

    @GetMapping("/commentMsg/list")
    public CommonResult<List<CommentMsg>> commentMsgList(@RequestParam("roomId") String roomId) {
        return CommonResult.success(liveRoomService.getCommentMsgList(roomId));
    }

    @GetMapping("/rankUser/list")
    public CommonResult<List<LiveRoomRankUser>> liveRoomRankUserList(@RequestParam("roomId") String roomId) {
        return CommonResult.success(liveRoomService.getLiveRoomRankUserList(roomId));
    }
}
