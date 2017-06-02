package model;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.json.JSONObject;

/**
 * Encode object to send to server as string */
public class MessageEncoder implements Encoder.Text<Message> {
	private JSONObject tmp;

	public void destroy() {
	}

	public void init(EndpointConfig arg0) {
		tmp = new JSONObject();
	}

	public String encode(Message message) throws EncodeException {
		return message.getJson().toString();
	}
}
