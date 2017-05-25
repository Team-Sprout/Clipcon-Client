package model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;

import javafx.scene.image.Image;

/**
 * @author Administrator
 *
 */
/**
 * @author Administrator
 *
 */
public class MessageParser {

	/**
	 * @param message
	 * 			Message object received from server
	 * @return user 
	 * 			User object converted from message
	 */
	public static User getUserAndGroupByMessage(Message message) {
		User user = new User(message.get(Message.NAME));
		Group group = new Group(message.get(Message.GROUP_PK));

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

	/**
	 * @param m
	 * 			Message object received from server
	 * @return Contents 
	 * 			Contents object converted from message
	 */
	public static Contents getContentsbyMessage(Message m) {

		Image image = null;

		if (m.get("contentsType").equals(Contents.TYPE_IMAGE)) {
			String imageString = m.get("imageString");
			image = new Image(testDecodeMethod(imageString));
		}

		return new Contents(m.get("contentsType"), m.getLong("contentsSize"), m.get("contentsPKName"), m.get("uploadUserName"), m.get("uploadTime"), m.get("contentsValue"), image);
	}

	/**
	 * @param imageString
	 * 			The String that transformed the Image received from the server
	 * @return InputStream
	 * 			An InputStream for creating Javafx Image objects
	 */
	public static InputStream testDecodeMethod(String imageString) {
		byte[] imageByte = Base64.decodeBase64(imageString);
		ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
		return bis;
	}
}
