package io.github.lulajax.tiktok.server.service;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONUtil;
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
import io.github.lulajax.tiktok.server.data.CommentMsg;
import io.github.lulajax.tiktok.server.data.ConnectLog;
import io.github.lulajax.tiktok.server.data.GiftMsg;
import io.github.lulajax.tiktok.server.data.LiveClientConnect;
import io.github.lulajax.tiktok.server.data.repository.CommentMsgRepository;
import io.github.lulajax.tiktok.server.data.repository.ConnectLogRepository;
import io.github.lulajax.tiktok.server.data.repository.GiftMsgRepository;
import io.github.lulajax.tiktok.server.data.repository.LiveClientConnectRepository;
import io.github.lulajax.tiktok.server.event.GiftMsgEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher applicationEventPublisher;

    private final Map<String, LiveClient> liveClientPool;

    @Value("${proxy.enable:false}")
    private boolean isProxyEnabled;
    @Value("${proxy.type:SOCKS}")
    private String proxyType;
    @Value("${proxy.address:192.168.84.76}")
    private String proxyAddress;
    @Value("${proxy.port:8119}")
    private int proxyPort;
    @Value("${eulerstream.api.key:MTliNDBhOTJhMTU3Mjcz}")
    private String apiKey;


    public LiveClientService(LiveRoomService liveRoomService,
                             LiveClientConnectRepository liveClientRepository,
                             ConnectLogRepository connectLogRepository,
                             GiftMsgRepository giftMsgRepository,
                             CommentMsgRepository commentRepository,
                             LiveRoomRankUserService liveRoomRankUserService,
                             ApplicationEventPublisher applicationEventPublisher) {
        this.liveRoomService = liveRoomService;
        this.liveClientRepository = liveClientRepository;
        this.connectLogRepository = connectLogRepository;
        this.giftMsgRepository = giftMsgRepository;
        this.commentRepository = commentRepository;
        this.liveClientPool = new HashMap<>();
        this.liveRoomRankUserService = liveRoomRankUserService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public LiveHttpClient getHttpClient() {
        if (httpClient == null) {
            LiveClientSettings liveClientSettings = LiveClientSettings.createDefault();
            liveClientSettings.setApiKey(apiKey);
            liveClientSettings.setOffline(false);
            liveClientSettings.setPrintToConsole(true);
            liveClientSettings.setFetchGifts(false);
//            liveClientSettings.setThrowOnAgeRestriction(true);
//            liveClientSettings.setRetryOnConnectionFailure(true);
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
        LiveClientConnect clientConnect = liveClientRepository.findByHostName(hostName);
        if (clientConnect != null && !StringUtils.hasLength(roomId)) {
            roomId = clientConnect.getRoomId();
        }

        log.info("Creating new client for: " + hostName);
        LiveClient client = liveClientPool.get(hostName);
        if (client != null) {
            if (client.getRoomInfo().getRoomId().equals(roomId)) {
                if (ConnectionState.CONNECTED.equals(client.getRoomInfo().getConnectionState())) {
                    log.info("{} Client is already connected", hostName);
                    throw new TikTokLiveRequestException("Client is already connected");
                } else if (ConnectionState.CONNECTING.equals(client.getRoomInfo().getConnectionState())) {
                    log.info("{} Client is connecting", hostName);
                    throw new TikTokLiveRequestException("Client is connecting");
                }
            } else {
                log.info("{} Client is not connected to the same room", hostName);
                disconnect(hostName, "房间变了,关闭旧的连接");
            }
        }


        client = TikTokLive.newClient(hostName)
                .configure(liveClientSettings -> {
                    liveClientSettings.setApiKey(apiKey);
                    liveClientSettings.setOffline(false);
                    liveClientSettings.setPrintToConsole(true);
                    liveClientSettings.setFetchGifts(false);
//                    liveClientSettings.setThrowOnAgeRestriction(true);
//                    liveClientSettings.setRetryOnConnectionFailure(true); // Reconnecting if TikTok user is offline
//                    liveClientSettings.setRetryConnectionTimeout(Duration.ofSeconds(1)); // Timeout before next reconnection
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
                    Long hostId = clientConnect != null && clientConnect.getHostId() != null ? clientConnect.getHostId() : liveClient.getRoomInfo().getHost().getId();
                    ThreadUtil.execAsync(() -> connectLogRepository.save(new ConnectLog(liveClient.getRoomInfo().getRoomId(), hostId, hostName, ConnectionState.CONNECTED.toString())));
                    liveRoomService.liveStartUpdate(liveClient);
                })
                .onDisconnected((liveClient, event) -> {
                    log.info("{} Disconnected {} ConnectionState:{}", liveClient.getRoomInfo().getHostName(), event.getReason(), liveClient.getRoomInfo().getConnectionState());
                    Long hostId = clientConnect != null && clientConnect.getHostId() != null ? clientConnect.getHostId() : liveClient.getRoomInfo().getHost().getId();
                    ThreadUtil.execAsync(() -> connectLogRepository.save(new ConnectLog(liveClient.getRoomInfo().getRoomId(), hostId, hostName, ConnectionState.DISCONNECTED.toString())));
                    if (liveClient.getRoomInfo() != null && liveClient.getRoomInfo().getHost() != null) {
                        var liveData = getLiveData(liveClient.getRoomInfo().getRoomId());
                        liveRoomService.liveUpdateByRoomId(liveData, liveClient.getRoomInfo().getRoomId());
                        updateDisconnectedState(hostName);
                    }
                })
                .onComment((liveClient, event) -> {
                    log.info(event.getUser().getName()+": "+event.getText());
                    try {
                        CommentMsg commentMsg = new CommentMsg().buildFrom(liveClient, event);
                        commentRepository.save(commentMsg);
                    } catch (Exception e) {
                        log.error("Error while saving comment", e);
                    }
                })
                .onEmote((liveClient, event) -> {
                    log.info("{} New fake Emote: " + event.getEmotes(), liveClient.getRoomInfo().getHostName());
                    try {
                        CommentMsg commentMsg = new CommentMsg().buildFrom(liveClient, event);
                        commentRepository.save(commentMsg);
                    } catch (Exception e) {
                        log.error("Error while saving Emote comment", e);
                    }
                })
                .onGift((liveClient, event) -> {
                    log.info("{} New fake Gift: " + event.getGift(), liveClient.getRoomInfo().getHostName());
                    try {
                        GiftMsg giftMsg = new GiftMsg().buildFrom(liveClient, event);
                        giftMsgRepository.save(giftMsg);
                        applicationEventPublisher.publishEvent(new GiftMsgEvent(this, giftMsg));
                    } catch (Exception e) {
                        log.error("Error while saving Gift", e);
                    }
                })
                .onRoomInfo((liveClient, event) -> {
                    log.info("{} New Room Info: " + JSONUtil.toJsonStr(event.getRoomInfo()), liveClient.getRoomInfo().getHostName());
                    liveRoomRankUserService.updateRoomRankList(event.getRoomInfo());
                })
                .onJoin((liveClient, event) -> {
                    log.info("{} New Join: {}", liveClient.getRoomInfo().getHostName(), event.getUser());
                })
                .onError((liveClient, event) -> {
                    log.error("{} Error: {}", liveClient.getRoomInfo().getHostName(), event.getException().getMessage());
                })
                .build();

        try {
            client.connect();
            log.info("Creating new client for: {}, roomInfo: {}", hostName, client.getRoomInfo());
            ThreadUtil.safeSleep(5000);
            liveClientPool.put(hostName, client);

            LiveClientConnect newClient = new LiveClientConnect().buildFrom(client.getRoomInfo());
            if (!StringUtils.hasLength(newClient.getHostName()) || newClient.getHostId() == null){
                disconnect(hostName, "Host name is not valid");
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
            disconnect(hostName, "Host is offline");
            throw e;
        }
    }

    public LiveClientConnect disconnect(String hostName, String reason) {
        log.info("disconnect client for: {}, reason: {}", hostName, reason);
        LiveClient client = liveClientPool.get(hostName);
        if (client != null && !ConnectionState.DISCONNECTED.equals(client.getRoomInfo().getConnectionState())) {
            log.info("disconnect client for: {}, reason: {}, ConnectionState: {}", hostName, reason, client.getRoomInfo().getConnectionState());
            client.disconnect();
        } else {
            log.info("disconnect client for: {}, is already disconnected", hostName);
        }
        return updateDisconnectedState(hostName);
    }

    private LiveClientConnect updateDisconnectedState(String hostName) {
        log.info("updateDisconnectedState for: {}", hostName);
        LiveClientConnect connect = liveClientRepository.findByHostName(hostName);
        if (connect == null) {
            return null;
        }
        connect.setConnectionState(ConnectionState.DISCONNECTED.name());
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

        LiveClientConnect connect = liveClientRepository.findByHostName(hostName);
        if (connect != null) {
            try {
                liveClientRepository.delete(connect);
                // 删除相关数据
                giftMsgRepository.deleteAllByHostId(connect.getHostId());
                commentRepository.deleteAllByHostId(connect.getHostId());
                liveRoomService.remove(connect.getHostId());
            } catch (Exception e) {
                log.error("Error while removing client for: " + hostName, e);
                return false;
            }
        }
        return true;
    }

    public void setUserStatus(String hostName, String status) {
        LiveClientConnect clientInfo = liveClientRepository.findByHostName(hostName);
        if (clientInfo != null) {
            clientInfo.setStatus(status);
            liveClientRepository.save(clientInfo);
        }
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

    public List<LiveClientConnect> getClientConnects(List<String> hostNames) {
        return liveClientRepository.findAllByHostNameIn(hostNames);
    }

    public List<ConnectLog> getConnectLogList(String roomId) {
        return connectLogRepository.findAllByRoomId(roomId);
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
