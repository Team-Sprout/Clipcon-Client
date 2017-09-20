package model.message;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.json.JSONObject;

/**
 * Decode string received from server as object (Message) */
public class MessageDecoder implements Decoder.Text<Message> {
	JSONObject tmp;

	public void destroy() {
	}

	public void init(EndpointConfig arg0) {
		tmp = new JSONObject();
	}

	public Message decode(String incommingMessage) throws DecodeException {
		Message message = new Message().setJson(incommingMessage);
		return message;
	}

	public boolean willDecode(String message) {
		boolean flag = true;
		try {
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
}
