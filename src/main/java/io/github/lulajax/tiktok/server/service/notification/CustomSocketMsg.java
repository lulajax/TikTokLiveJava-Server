package io.github.lulajax.tiktok.server.service.notification;

import lombok.Data;

@Data
public class CustomSocketMsg {
	private String msg;
	private Integer msgType;

	public CustomSocketMsg(String msg, Integer msgType) {
		this.msg = msg;
		this.msgType = msgType;
	}
}
