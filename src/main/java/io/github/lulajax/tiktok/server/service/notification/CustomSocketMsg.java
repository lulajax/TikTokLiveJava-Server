package io.github.lulajax.tiktok.server.service.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CustomSocketMsg {
	@JsonProperty("msg")
	private String msg;
	@JsonProperty("type")
	private CustomMsgType msgType;

	@JsonCreator
	public CustomSocketMsg(@JsonProperty("msg") String msg, @JsonProperty("type") CustomMsgType msgType) {
		this.msg = msg;
		this.msgType = msgType;
	}
}
