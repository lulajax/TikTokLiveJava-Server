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
package io.github.jwdeveloper.tiktok.server.service;

import cn.hutool.json.JSONUtil;
import com.sun.tools.jconsole.JConsoleContext;
import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.TikTokLiveHttpClient;
import io.github.jwdeveloper.tiktok.data.requests.GiftsData;
import io.github.jwdeveloper.tiktok.data.requests.LiveData;
import io.github.jwdeveloper.tiktok.data.requests.LiveUserData;
import io.github.jwdeveloper.tiktok.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.data.settings.ProxyClientSettings;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveOfflineHostException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import io.github.jwdeveloper.tiktok.http.HttpClientFactory;
import io.github.jwdeveloper.tiktok.http.LiveHttpClient;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.models.ConnectionState;
import io.github.jwdeveloper.tiktok.server.data.CommentMsg;
import io.github.jwdeveloper.tiktok.server.data.ConnectLog;
import io.github.jwdeveloper.tiktok.server.data.GiftMsg;
import io.github.jwdeveloper.tiktok.server.data.LiveClientConnect;
import io.github.jwdeveloper.tiktok.server.data.repository.CommentMsgRepository;
import io.github.jwdeveloper.tiktok.server.data.repository.ConnectLogRepository;
import io.github.jwdeveloper.tiktok.server.data.repository.GiftMsgRepository;
import io.github.jwdeveloper.tiktok.server.data.repository.LiveClientConnectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LiveClientService {
    public LiveHttpClient httpClient;

    private final LiveRoomService liveRoomService;
    private final LiveClientConnectRepository liveClientRepository;
    private final ConnectLogRepository connectLogRepository;
    private final GiftMsgRepository giftMsgRepository;
    private final CommentMsgRepository commentRepository;
    private final LiveRoomRankUserService liveRoomRankUserService;
    private final Map<String, LiveClient> liveClientPool;

    @Value("${proxy.enable:false}")
    private boolean isProxyEnabled;
    @Value("${proxy.type:SOCKS}")
    private String proxyType;
    @Value("${proxy.address:192.168.84.76}")
    private String proxyAddress;
    @Value("${proxy.port:8119}")
    private int proxyPort;


    public LiveClientService(LiveRoomService liveRoomService, LiveClientConnectRepository liveClientRepository, ConnectLogRepository connectLogRepository, GiftMsgRepository giftMsgRepository, CommentMsgRepository commentRepository, LiveRoomRankUserService liveRoomRankUserService) {
        this.liveRoomService = liveRoomService;
        this.liveClientRepository = liveClientRepository;
        this.connectLogRepository = connectLogRepository;
        this.giftMsgRepository = giftMsgRepository;
        this.commentRepository = commentRepository;
        this.liveClientPool = new HashMap<>();
        this.liveRoomRankUserService = liveRoomRankUserService;
    }

    public LiveHttpClient getHttpClient() {
        if (httpClient == null) {
            LiveClientSettings liveClientSettings = LiveClientSettings.createDefault();
            liveClientSettings.setOffline(false);
            if (isProxyEnabled) {
                ProxyClientSettings proxySettings = new ProxyClientSettings();
                proxySettings.setOnProxyUpdated(proxyData -> System.err.println("Next proxy: " + proxyData.toString()));
                proxySettings.setType(Proxy.Type.valueOf(proxyType));
                proxySettings.addProxy(proxyAddress, proxyPort);
                proxySettings.setEnabled(true);
                liveClientSettings.getHttpSettings().setProxyClientSettings(proxySettings);
            }

            httpClient = new TikTokLiveHttpClient(new HttpClientFactory(liveClientSettings));
        }

        return httpClient;
    }

    public LiveClientConnect createClientConnect(String hostName) {
        return createClientConnect(hostName, null);
    }
    public LiveClientConnect createClientConnect(String hostName, String roomId) {
        log.info("Creating new client for: " + hostName);
        LiveClient client = liveClientPool.get(hostName);
        if (client != null
                && ConnectionState.CONNECTED.equals(client.getRoomInfo().getConnectionState())
                && client.getRoomInfo().getRoomId().equals(roomId)) {
            log.info("Client is already connected");
            throw new TikTokLiveRequestException("Client is already connected");
        }
        client = TikTokLive.newClient(hostName)
                .configure(liveClientSettings -> {
                    liveClientSettings.setOffline(false);
                    liveClientSettings.setPrintToConsole(false);
                    liveClientSettings.setFetchGifts(false);
                    if (isProxyEnabled) {
                        liveClientSettings.getHttpSettings().configureProxy(proxySettings -> {
                            proxySettings.setOnProxyUpdated(proxyData -> System.err.println("Next proxy: " + proxyData.toString()));
                            proxySettings.setType(Proxy.Type.valueOf(proxyType));
                            proxySettings.addProxy(proxyAddress, proxyPort);
                        });
                    }
                })
                .onConnected((liveClient, event) -> {
                    log.info("{} Connected", liveClient.getRoomInfo().getHostName());
                    liveRoomService.liveStartUpdate(liveClient);
                    connectLogRepository.save(new ConnectLog(liveClient.getRoomInfo().getRoomId(), liveClient.getRoomInfo().getHost().getId(), liveClient.getRoomInfo().getHost().getName(), ConnectionState.CONNECTED.toString()));
                })
                .onDisconnected((liveClient, event) -> {
                    log.info("{} Disconnected", liveClient.getRoomInfo().getHostName());
                    if (liveClient.getRoomInfo() != null && liveClient.getRoomInfo().getHost() != null) {
                        var liveData = getLiveData(liveClient.getRoomInfo().getRoomId());
                        liveRoomService.liveUpdateByRoomId(liveData, liveClient.getRoomInfo().getRoomId());
                        disconnect(liveClient.getRoomInfo().getHostName());
                    }
                    connectLogRepository.save(new ConnectLog(liveClient.getRoomInfo().getRoomId(), liveClient.getRoomInfo().getHost().getId(), liveClient.getRoomInfo().getHost().getName(), ConnectionState.DISCONNECTED.toString()));
                })
                .onComment((liveClient, event) -> {
                    log.info(event.getUser().getName()+": "+event.getText());
                    CommentMsg commentMsg = new CommentMsg().buildFrom(liveClient, event);
                    commentRepository.save(commentMsg);
                })
                .onEmote((liveClient, event) -> {
                    log.info("{} New fake Emote: " + event.getEmotes(), liveClient.getRoomInfo().getHostName());
                    CommentMsg commentMsg = new CommentMsg().buildFrom(liveClient, event);
                    commentRepository.save(commentMsg);
                })
                .onGift((liveClient, event) -> {
                    log.info("{} New fake Gift: " + event.getGift(), liveClient.getRoomInfo().getHostName());
                    GiftMsg giftMsg = new GiftMsg().buildFrom(liveClient, event);
                    giftMsgRepository.save(giftMsg);
                })
                .onRoomInfo((liveClient, event) -> {
                    log.info("{} New Room Info: " + JSONUtil.toJsonStr(event.getRoomInfo()), liveClient.getRoomInfo().getHostName());
                    liveRoomRankUserService.updateRoomRankList(event.getRoomInfo());
                })
                .onError((liveClient, event) -> {
                    log.info("{} Error: " + event.getException(), liveClient.getRoomInfo().getHostName());
                    disconnect(liveClient.getRoomInfo().getHostName());
                })
                .build();

        try {
            client.connect();
            liveClientPool.put(hostName, client);

            LiveClientConnect newClient = new LiveClientConnect().buildFrom(client.getRoomInfo());
            if (!StringUtils.hasLength(newClient.getHostName()) || newClient.getHostId() == null){
                disconnect(hostName);
                throw new TikTokLiveRequestException("Host name is not valid");
            }

            LiveClientConnect clientInfo = liveClientRepository.findByHostId(newClient.getHostId());
            if (clientInfo == null) {
                return liveClientRepository.save(newClient);
            } else {
                clientInfo.buildFrom(client.getRoomInfo());
                return liveClientRepository.save(clientInfo);
            }
        } catch (TikTokLiveOfflineHostException e) {
            log.info("{} Host is offline", hostName);
            disconnect(hostName);
            throw e;
        }
    }

    public LiveClientConnect disconnect(String hostName) {
        log.info("Disconnecting client for: " + hostName);
        LiveClient client = liveClientPool.get(hostName);
        if (client != null && !ConnectionState.DISCONNECTED.equals(client.getRoomInfo().getConnectionState())) {
            client.disconnect();
            liveClientPool.put(hostName, client);
        }
        LiveClientConnect connect = liveClientRepository.findByHostName(hostName);
        if (connect == null) {
            return null;
        }
        connect.setConnectionState(JConsoleContext.ConnectionState.DISCONNECTED.name());
        return liveClientRepository.save(connect);
    }


    public LiveClientConnect create(String hostName) {
        LiveUserData.Response liveUserData = getLiveUserData(hostName);
        if (!liveUserData.isHostNameValid()) {
            throw new TikTokLiveRequestException("Host name is not valid");
        }

        if (liveUserData.isLiveOnline()) {
            return createClientConnect(hostName);
        } else {
            LiveClientConnect clientInfo = liveClientRepository.findByHostName(hostName);
            if (clientInfo == null) {
                clientInfo = new LiveClientConnect().buildFrom(liveUserData);
            } else {
                clientInfo.buildFrom(liveUserData);
            }
            return liveClientRepository.save(clientInfo);
        }
    }

    @Transactional
    public boolean remove(String hostName) {
        log.info("Remove client for: " + hostName);
        LiveClient client = liveClientPool.get(hostName);
        if (client != null) {
            client.disconnect();
            liveClientPool.remove(hostName, client);
        }
        try {
            LiveClientConnect connect = liveClientRepository.findByHostName(hostName);
            if (connect != null) {
                liveClientRepository.delete(connect);
                // 删除相关数据
                giftMsgRepository.deleteAllByHostId(connect.getHostId());
                commentRepository.deleteAllByHostId(connect.getHostId());
                liveRoomService.remove(connect.getHostId());
            }
        } catch (Exception e) {
            log.error("Error while removing client for: " + hostName, e);
            return false;
        }
        return true;
    }

    public List<LiveClientConnect> getClientConnectList(String hostName) {
        if (StringUtils.hasLength(hostName)) {
            LiveClientConnect clientInfo = liveClientRepository.findByHostName(hostName);
            if (clientInfo != null) {
                List<LiveClientConnect> list = new ArrayList<>();
                list.add(clientInfo);
                return list;
            }
            return null;
        } else {
            return liveClientRepository.findAll();
        }
    }

    public LiveUserData.Response getLiveUserData(String hostName) {
        return getHttpClient().fetchLiveUserData(hostName);
    }

    public LiveData.Response getLiveData(String roomId) {
        var liveDataRequest = new LiveData.Request(roomId);
        return getHttpClient().fetchLiveData(liveDataRequest);
    }

    public GiftsData.Response getRoomGifts(String roomId) {
        return getHttpClient().fetchRoomGiftsData(roomId);
    }
}
