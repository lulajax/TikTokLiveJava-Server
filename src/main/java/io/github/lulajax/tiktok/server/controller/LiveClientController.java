package io.github.lulajax.tiktok.server.controller;

import io.github.jwdeveloper.tiktok.data.requests.GiftsData;
import io.github.jwdeveloper.tiktok.data.requests.LiveData;
import io.github.jwdeveloper.tiktok.data.requests.LiveUserData;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.lulajax.tiktok.server.data.ConnectLog;
import io.github.lulajax.tiktok.server.data.LiveClientConnect;
import io.github.lulajax.tiktok.server.response.CommonResult;
import io.github.lulajax.tiktok.server.service.LiveClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tiktok-live")
public class LiveClientController {

    private final LiveClientService liveClientService;

    @GetMapping("/connect")
    public CommonResult<LiveClientConnect> createClientConnect(@RequestParam("hostName") String hostName, @RequestParam(value = "roomId", required = false) String roomId) {
        try {
            return CommonResult.success(liveClientService.createClientConnect(hostName, roomId));
        } catch (TikTokLiveException e) {
            return CommonResult.failed(e.getMessage());
        }
    }
    
    @GetMapping("/connect/bySchedule")
    public CommonResult<LiveClientConnect> createClientConnectBySchedule(@RequestParam("hostName") String hostName) {
        try {
            liveClientService.createClientConnectBySchedule(hostName);
            return CommonResult.success();
        } catch (TikTokLiveException e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @GetMapping("/disconnect")
    public CommonResult<LiveClientConnect> disconnect(@RequestParam("hostName") String hostName) {
        try {
            return CommonResult.success(liveClientService.disconnect(hostName, "调用接口主动断开"));
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
    public CommonResult<List<LiveClientConnect>> clientConnectList(@RequestParam(name = "hostName", required = false) String hostName) {
        return CommonResult.success(liveClientService.getClientConnectList(hostName));
    }

    @GetMapping("/connect/logs")
    public CommonResult<List<ConnectLog>> clientConnectLogs(@RequestParam("roomId") String roomId) {
        try {
            return CommonResult.success(liveClientService.getConnectLogList(roomId));
        } catch (TikTokLiveException e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @GetMapping("/liveUserData")
    public CommonResult<LiveUserData.Response> liveUserData(@RequestParam("hostName") String hostName) {
        return CommonResult.success(liveClientService.getLiveUserData(hostName));
    }

    @GetMapping("/liveData")
    public CommonResult<LiveData.Response> liveUData(@RequestParam("roomId") String roomId) {
        return CommonResult.success(liveClientService.getLiveData(roomId));
    }


    @GetMapping("/roomGifts")
    public CommonResult<List<Map<String, Object>>> roomGifts(@RequestParam("roomId") String roomId) {
        GiftsData.Response response = liveClientService.getRoomGifts(roomId);

        List<Map<String, Object>> result = new ArrayList<>();
        response.getGifts().forEach(gift -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", gift.getId());
            item.put("name", gift.getName());
            item.put("diamondCost", gift.getDiamondCost());
            item.put("picture", gift.getPicture());
            result.add(item);
        });
        return CommonResult.success(result);
    }
}
