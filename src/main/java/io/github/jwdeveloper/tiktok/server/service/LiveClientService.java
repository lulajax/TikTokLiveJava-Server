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

import com.sun.tools.jconsole.JConsoleContext;
import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.TikTokLiveHttpClient;
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
import io.github.jwdeveloper.tiktok.server.data.GiftMsg;
import io.github.jwdeveloper.tiktok.server.data.LiveClientConnect;
import io.github.jwdeveloper.tiktok.server.data.repository.CommentMsgRepository;
import io.github.jwdeveloper.tiktok.server.data.repository.GiftMsgRepository;
import io.github.jwdeveloper.tiktok.server.data.repository.LiveClientConnectRepository;
import lombok.extern.slf4j.Slf4j;
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
    private final GiftMsgRepository giftMsgRepository;
    private final CommentMsgRepository commentRepository;
    private final Map<String, LiveClient> liveClientPool;


    public LiveClientService(LiveRoomService liveRoomService, LiveClientConnectRepository liveClientRepository, GiftMsgRepository giftMsgRepository, CommentMsgRepository commentRepository) {
        this.liveRoomService = liveRoomService;
        this.liveClientRepository = liveClientRepository;
        this.giftMsgRepository = giftMsgRepository;
        this.commentRepository = commentRepository;
        this.liveClientPool = new HashMap<>();
    }

    public LiveHttpClient getHttpClient() {
        if (httpClient == null) {
            ProxyClientSettings proxySettings = new ProxyClientSettings();
            proxySettings.setOnProxyUpdated(proxyData -> System.err.println("Next proxy: " + proxyData.toString()));
            proxySettings.setType(Proxy.Type.SOCKS);
            proxySettings.addProxy("192.168.84.76", 8119);
            proxySettings.setEnabled(true);

            LiveClientSettings liveClientSettings = LiveClientSettings.createDefault();
            liveClientSettings.setOffline(false);
            liveClientSettings.getHttpSettings().setProxyClientSettings(proxySettings);

            httpClient = new TikTokLiveHttpClient(new HttpClientFactory(liveClientSettings));
        }

        return httpClient;
    }

    public LiveClientConnect createClientConnect(String hostName) {
        log.info("Creating new client for: " + hostName);
        LiveClient client = liveClientPool.get(hostName);
        if (client != null && ConnectionState.CONNECTED.equals(client.getRoomInfo().getConnectionState())) {
            log.info("Client is already connected");
            throw new TikTokLiveRequestException("Client is already connected");
        }
        client = TikTokLive.newClient(hostName)
                .configure(liveClientSettings -> {
                    liveClientSettings.setOffline(false);
                    liveClientSettings.setPrintToConsole(true);
                    liveClientSettings.setFetchGifts(false);
                    liveClientSettings.getHttpSettings().configureProxy(proxySettings -> {
                        proxySettings.setOnProxyUpdated(proxyData -> System.err.println("Next proxy: " + proxyData.toString()));
                        proxySettings.setType(Proxy.Type.SOCKS);
                        proxySettings.addProxy("192.168.84.76", 8119);
                    });
                })
                .onConnected((liveClient, event) -> {
                    liveClient.getLogger().info("Connected");
                    liveRoomService.liveUpdate(liveClient);
                })
                .onDisconnected((liveClient, event) -> {
                    liveClient.getLogger().info("Disconnected");
                    if (liveClient.getRoomInfo() != null) {
                        liveRoomService.liveUpdate(liveClient);
                    }
                })
                .onComment((liveClient, event) -> {
                    liveClient.getLogger().info(event.getUser().getName()+": "+event.getText());
                    CommentMsg commentMsg = new CommentMsg().buildFrom(liveClient, event);
                    commentRepository.save(commentMsg);
                })
                .onGift((liveClient, event) -> {
                    liveClient.getLogger().info("New fake Gift: " + event.getGift());
                    GiftMsg giftMsg = new GiftMsg().buildFrom(liveClient, event);
                    giftMsgRepository.save(giftMsg);
                })
                .build();

        try {
            client.connect();
            liveClientPool.put(hostName, client);

            LiveClientConnect newClient = new LiveClientConnect().buildFrom(client.getRoomInfo());
            LiveClientConnect clientInfo = liveClientRepository.findByHostName(newClient.getHostName());
            if (clientInfo == null) {
                return liveClientRepository.save(newClient);
            } else {
                clientInfo.buildFrom(client.getRoomInfo());
                return liveClientRepository.save(clientInfo);
            }
        } catch (TikTokLiveOfflineHostException e) {
            log.info("Host is offline: " + hostName);
            disconnect(hostName);
            throw e;
        }
    }

    public LiveClientConnect disconnect(String hostName) {
        log.info("Disconnecting client for: " + hostName);
        LiveClient client = liveClientPool.get(hostName);
        if (client != null) {
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
        LiveClientConnect clientInfo = liveClientRepository.findByHostName(hostName);
        if (clientInfo == null) {
            clientInfo = new LiveClientConnect().buildFrom(liveUserData);
        } else {
            clientInfo.buildFrom(liveUserData);
        }
        return liveClientRepository.save(clientInfo);
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

    public boolean isLiveOnline(String hostName) {
        LiveUserData.Response liveUserData = getLiveUserData(hostName);
        if (liveUserData == null || liveUserData.getUserStatus() == null) {
            return false;
        }
        return liveUserData.isLiveOnline();
    }
}
