package model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;

import javafx.scene.image.Image;

public class MessageParser {
	
	private static int cnt = 0;
	
	/**
	 * @param message
	 *            �꽌踰꾩뿉�꽌 諛쏆� Message媛앹껜
	 * @return message 濡쒕��꽣 蹂��솚�맂 Group媛앹껜
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

	// Image�엫�떆
	public static Contents getContentsbyMessage(Message m) {
		/* [doy] debuger test code */
		Image img = null;
		
		 try {
		 img = new Image(new FileInputStream("C:\\Users\\Administrator\\Desktop\\1.png"));
		 System.out.println("Image 揶쏆빘猿� 占쎄문占쎄쉐");
		 } catch (FileNotFoundException e) {
		 e.printStackTrace();
		 }
		 
		 cnt++;
		 
		 if(cnt % 2 == 0) {
			 return new Contents(Contents.TYPE_IMAGE, m.getLong("contentsSize"), m.get("contentsPKName"), m.get("uploadUserName"), m.get("uploadTime"), "imgae", img);
		 }
		 /* [doy] debuger test code */
		 
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