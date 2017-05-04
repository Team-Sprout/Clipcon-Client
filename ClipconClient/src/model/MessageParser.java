package model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.json.JSONArray;

import javafx.scene.image.Image;

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

	// Image임시
	public static Contents getContentsbyMessage(Message m) {

		Image img = null;

		/*도연이 코드*/
		// try {
		// img = new Image(new FileInputStream("C:\\Users\\Administrator\\Desktop\\1.png"));
		// System.out.println("Image 媛앹껜 �깮�꽦");
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// }

		return new Contents(m.get("contentsType"), m.getLong("contentsSize"), m.get("contentsPKName"), m.get("uploadUserName"), m.get("uploadTime"), m.get("contentsValue"), img);

	}
	
	/** @author delf
	 * client code */
//	public static Image getImagebyMessage(Message message) {
//		String imageString = message.get("imageString");
//		byte[] imageBytes = Base64.getDecoder().decode(imageString);
//		BufferedImage imag;
//		try {
//			imag = ImageIO.read(new ByteArrayInputStream(imageBytes));
//			return imag;
//		} catch (IOException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
}