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
	 * 			서버에서 받은 Message객체
	 * @return user 
	 * 			message 로부터 변환된 User객체
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

	/**
	 * @param m
	 * 			서버에서 받은 Message객체
	 * @return Contents 
	 * 			message 로부터 변환된 Contents객체
	 */
	public static Contents getContentsbyMessage(Message m) {

		Image image = null;
		
		if (m.get("contentsType").equals(Contents.TYPE_IMAGE)) {
			String imageString = m.get("imageString");
			image = new Image(testDecodeMethod(imageString));
		}

		return new Contents(m.get("contentsType"), m.getLong("contentsSize"), m.get("contentsPKName"),
				m.get("uploadUserName"), m.get("uploadTime"), m.get("contentsValue"), image);
	}
	
	
	
	/**
	 * @param imageString
	 * 			서버에서 받은 Image를 변환한 String
	 * @return InputStream
	 * 			Javafx Image 객체를 생성하기 위한 InputStream
	 */
	public static InputStream testDecodeMethod(String imageString) {
		byte[] imageByte = Base64.decodeBase64(imageString);
		ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
		return bis;
	}
}
