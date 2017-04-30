package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;

public class MessageParser {

	/**
	 * @param message
	 *            서버에서 받은 Message객체
	 * @return message 로부터 변환된 Group객체
	 */
	public static User getUserAndGroupByMessage(Message message) {
		User user = new User(message.get(Message.NAME));
		Group group = new Group(message.get(Message.GROUP_PK));

		// group.setName(name);

		List<String> userStringList = new ArrayList<String>();
		JSONArray tmpArray = message.getJson().getJSONArray(Message.LIST);
		Iterator<?> it = tmpArray.iterator();
		while (it.hasNext()) {
			String tmpString = (String) it.next();
			userStringList.add(tmpString);
		}

		List<User> userList = new ArrayList<User>();
		for (String userName : userStringList) {
			userList.add(new User(userName));
		}

		group.setUserList(userList);
		user.setGroup(group);

		return user;
	}

	public static Contents getContentsbyMessage(Message m) {
		return new Contents(m.get("contentsType"), m.getLong("contentsSize"), m.get("contentsPKName"),
				m.get("uploadUserName"), m.get("uploadTime"));
	}
}