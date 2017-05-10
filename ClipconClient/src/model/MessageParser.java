package model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;

import javafx.scene.image.Image;

public class MessageParser {

	/**
	 * @param message
	 *            서버에서 받은 Message객체
	 * @return user message 로부터 변환된 User객체
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

	public static InputStream testDecodeMethod(String imageString) {
	      BufferedImage image = null;
	      byte[] imageByte = Base64.decodeBase64(imageString);
	      ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
	      return bis;
	   }

	// Image임시
	public static Contents getContentsbyMessage(Message m) {
		/* [doy] debuger test code */
//		try {
//			img = new Image(new FileInputStream("C:\\Users\\Administrator\\Desktop\\1.png"));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//
//		cnt++;
//
//		if (cnt % 2 == 0) {
//			return new Contents(Contents.TYPE_IMAGE, m.getLong("contentsSize"), m.get("contentsPKName"),
//					m.get("uploadUserName"), m.get("uploadTime"), "image", img);
//		}
		/* [doy] debuger test code */
		
		String imageString = m.get("imageString");
	    Image image = new Image(testDecodeMethod(imageString));

		return new Contents(m.get("contentsType"), m.getLong("contentsSize"), m.get("contentsPKName"),
				m.get("uploadUserName"), m.get("uploadTime"), m.get("contentsValue"), image);
	}

	/**
	 * @author delf client code
	 */
	// public static Image getImagebyMessage(Message message) {
	// String imageString = message.get("imageString");
	// byte[] imageBytes = Base64.getDecoder().decode(imageString);
	// BufferedImage imag;
	// try {
	// imag = ImageIO.read(new ByteArrayInputStream(imageBytes));
	// return imag;
	// } catch (IOException e) {
	// e.printStackTrace();
	// return null;
	// }
	// }
}
