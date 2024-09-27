package io.github.lulajax.tiktok.server.service.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CustomMsgType {
	GIFT_MSG(1),
	ENTER_ROOM_MSG(2);

	private final int type;

	CustomMsgType(int type) {
		this.type = type;
	}

	@JsonValue
	public int getType() {
		return type;
	}

	@JsonCreator
	public static CustomMsgType getType(int type) {
		for (CustomMsgType msgType : CustomMsgType.values()) {
			if (msgType.getType() == type) {
				return msgType;
			}
		}
		return null;
	}
}
