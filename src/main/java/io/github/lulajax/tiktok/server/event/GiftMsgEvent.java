package io.github.lulajax.tiktok.server.event;

import io.github.lulajax.tiktok.server.data.GiftMsg;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class GiftMsgEvent extends ApplicationEvent {
    private final GiftMsg msg;

    public GiftMsgEvent(Object source, GiftMsg msg) {
        super(source);
        this.msg = msg;
    }

}
