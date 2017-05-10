package model;


import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.json.JSONObject;

/**
 * 서버로 보낼 object(Message)를 string으로 encoding. */
public class MessageEncoder implements Encoder.Text<Message> {
	private JSONObject tmp;

	public void destroy() {
	}

	public void init(EndpointConfig arg0) {
		tmp = new JSONObject();
	}

	public String encode(Message message) throws EncodeException {
		System.out.println("서버로 보낼 string: " + message.getJson());
		return message.getJson().toString();
	}
}
