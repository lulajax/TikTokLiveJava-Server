/*
 * Copyright (c) 2023-2023 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.jwdeveloper.tiktok.server.controller;


import io.github.jwdeveloper.tiktok.server.data.CommentMsg;
import io.github.jwdeveloper.tiktok.server.data.GiftMsg;
import io.github.jwdeveloper.tiktok.server.data.LiveRoom;
import io.github.jwdeveloper.tiktok.server.data.LiveRoomRankUser;
import io.github.jwdeveloper.tiktok.server.response.CommonResult;
import io.github.jwdeveloper.tiktok.server.service.LiveRoomService;
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
