package io.github.lulajax.tiktok.server.service.pk;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import io.github.lulajax.tiktok.server.data.GiftMsg;
import io.github.lulajax.tiktok.server.data.pk.PkRound;
import io.github.lulajax.tiktok.server.data.pk.PkRoundScores;
import io.github.lulajax.tiktok.server.data.pk.PkTargetGift;
import io.github.lulajax.tiktok.server.data.pk.repository.PkRoundRepository;
import io.github.lulajax.tiktok.server.data.pk.repository.PkRoundScoresRepository;
import io.github.lulajax.tiktok.server.data.pk.repository.PkTargetGiftRepository;
import io.github.lulajax.tiktok.server.event.GiftMsgEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GiftMsgListener {
    private final PkRoundRepository pkRoundRepository;
    private final PkRoundScoresRepository pkRoundScoresRepository;
    private final PkTargetGiftRepository pkTargetGiftRepository;

    public GiftMsgListener(PkRoundRepository pkRoundRepository,
                           PkRoundScoresRepository pkRoundScoresRepository,
                           PkTargetGiftRepository pkTargetGiftRepository) {
        this.pkRoundRepository = pkRoundRepository;
        this.pkRoundScoresRepository = pkRoundScoresRepository;
        this.pkTargetGiftRepository = pkTargetGiftRepository;
    }

    @EventListener
    public void handleGiftMsgEvent(GiftMsgEvent giftMsgEvent) {
        log.info("handleGiftMsgEvent Received GiftMsgEvent: {}", JSONUtil.toJsonStr(giftMsgEvent.getMsg()));
        PkRound pkRound = isPKing(giftMsgEvent.getMsg().getHostName());
        if (pkRound == null) {
            return;
        }
        savePkRoundScores(pkRound, giftMsgEvent.getMsg());
    }

    private void savePkRoundScores(PkRound pkRound, GiftMsg msg) {
        Map<Integer, PkTargetGift> pkTargetGifts = getPkTargetGifts(pkRound.getConfigId()).stream().collect(Collectors.toMap(PkTargetGift::getGiftId, y->y, (q, p) -> q));
        if(!pkTargetGifts.containsKey(msg.getGiftId())) {
            log.info("Gift not found in msgGiftId: {}, pkTargetGifts: {}", msg.getGiftId(), pkTargetGifts.keySet());
            return;
        }
        log.info("Gift found in msgGiftId: {}, pkTargetGifts: {}", msg.getGiftId(), pkTargetGifts.keySet());
        PkTargetGift pkTargetGift = pkTargetGifts.get(msg.getGiftId());

        PkRoundScores pkRoundScores = buildPkRoundScores(pkRound, msg, pkTargetGift);
        if (isBetween(pkRoundScores.getTimeStamp(), pkRound.getStartTime(), pkRound.getEndTime())) {
            pkRoundScoresRepository.save(pkRoundScores);
        } else {
            // 不在回合内时间内送礼
            String formattedGiftTime = DateUtil.date(msg.getTimeStamp()).toString(DatePattern.ISO8601_PATTERN);
            String formattedStartTime = DateUtil.date(msg.getTimeStamp()).toString(DatePattern.ISO8601_PATTERN);
            String formattedEndTime = DateUtil.date(msg.getTimeStamp()).toString(DatePattern.ISO8601_PATTERN);
            log.info("Gift not in pkRound time, msgGiftId: {} msgTimestamp:{}, pkRound time between {} and {}",
                    msg.getGiftId(), formattedGiftTime, formattedStartTime, formattedEndTime);
        }
    }

    public static <T extends Comparable<T>> boolean isBetween(T value, T start, T end) {
        return value.compareTo(start) >= 0 && value.compareTo(end) <= 0;
    }

    private PkRoundScores buildPkRoundScores(PkRound pkRound, GiftMsg msg, PkTargetGift pkTargetGift) {
        return PkRoundScores.builder()
                .roundId(pkRound.getId())
                .player(pkTargetGift.getPlayer())
                .roomId(msg.getRoomId())
                .giftId(msg.getGiftId())
                .giftName(msg.getGiftName())
                .diamondCost(msg.getDiamondCost())
                .giftPictureLink(msg.getGiftPictureLink())
                .userId(msg.getUserId())
                .userName(msg.getUserName())
                .userProfileName(msg.getUserProfileName())
                .userPictureLink(msg.getUserPictureLink())
                .combo(msg.getCombo())
                .messageId(msg.getMessageId())
                .timeStamp(msg.getTimeStamp()).build();
    }

    private List<PkTargetGift> getPkTargetGifts(int configId) {
        return pkTargetGiftRepository.findAllByConfigId(configId);
    }

    private PkRound isPKing(String hostName) {
        List<PkRound> pkRounds = pkRoundRepository.findAllPkingRoundList();
        if (CollectionUtil.isEmpty(pkRounds)) {
            return null;
        }
        // pkRounds 是否存在roomId
       return pkRounds.stream().filter(x -> x.getHostName().equals(hostName)).findFirst().orElse(null);
    }
}
