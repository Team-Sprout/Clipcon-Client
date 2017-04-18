package model;

import org.json.JSONObject;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Message {
	private String type;
	private JSONObject json;

	public Message setJson(String string) {
		json = new JSONObject(string);
		type = json.getString(TYPE);
		return this;
	}

	public Message setJson(JSONObject json) {
		this.json = json;
		type = json.getString(TYPE);
		return this;
	}
	
	public Message setType(String type) {
		json = new JSONObject();
		this.type = type;
		json.put(TYPE, type);
		return this;
	}

	public Message(String type, String jsonString) {
		this.type = type;
		json = new JSONObject();
		json.put(TYPE, type);
		json.put(CONTENTS, jsonString);
		System.out.println(json.toString());
	}

	public void add(String key, String value) {
		json.put(key, value);
	}

	public String get(String key) {
		return json.get(key).toString();
	}
	
	public String toString() {
		return json.toString();
	}

	public final static String TYPE = "message type";

	public final static String REQUEST_SIGN_IN = "sign in";
	public final static String REQUEST_SIGN_UP = "sign up";
	public final static String REQUEST_CREATE_GROUP = "create group";
	public final static String REQUEST_JOIN_GROUP = "join group";
	public final static String REQUEST_TEST = "test";

	public final static String EMAIL = "email";
	public final static String NAME = "name";
	public final static String CONTENTS = "contents";
	public final static String RESPONSE_SIGN_IN = "sign in";
	public final static String ADDRESS_BOOK = "address book";
	public final static String LIST = "list";

	public final static String TEST_DEBUG_MODE = "debug";
	public final static String REQUEST_GET_ADDRESSBOOK = "address book";
}
