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

import io.github.jwdeveloper.tiktok.data.requests.LiveData;
import io.github.jwdeveloper.tiktok.data.requests.LiveUserData;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.server.data.LiveClientConnect;
import io.github.jwdeveloper.tiktok.server.response.CommonResult;
import io.github.jwdeveloper.tiktok.server.service.LiveClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tiktok-live")
public class LiveClientController {

    private final LiveClientService liveClientService;

    @GetMapping("/connect")
    public CommonResult<LiveClientConnect> createClientConnect(@RequestParam("hostName") String hostName) {
        try {
            return CommonResult.success(liveClientService.createClientConnect(hostName));
        } catch (TikTokLiveException e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @GetMapping("/disconnect")
    public CommonResult<LiveClientConnect> disconnect(@RequestParam("hostName") String hostName) {
        try {
            return CommonResult.success(liveClientService.disconnect(hostName));
        } catch (TikTokLiveException e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @GetMapping("/create")
    public CommonResult<LiveClientConnect> create(@RequestParam("hostName") String hostName) {
        try {
            return CommonResult.success(liveClientService.create(hostName));
        } catch (TikTokLiveException e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @DeleteMapping("/remove")
    public CommonResult<Boolean> remove(@RequestParam("hostName") String hostName) {
        try {
            return CommonResult.success(liveClientService.remove(hostName));
        } catch (TikTokLiveException e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @GetMapping("/connect/list")
    public CommonResult<List<LiveClientConnect>> clientConnectList(@RequestParam("hostName") String hostName) {
        return CommonResult.success(liveClientService.getClientConnectList(hostName));
    }


    @GetMapping("/liveUserData")
    public CommonResult<LiveUserData.Response> liveUserData(@RequestParam("hostName") String hostName) {
        return CommonResult.success(liveClientService.getLiveUserData(hostName));
    }

    @GetMapping("/liveData")
    public CommonResult<LiveData.Response> liveUData(@RequestParam("roomId") String roomId) {
        return CommonResult.success(liveClientService.getLiveData(roomId));
    }
}
