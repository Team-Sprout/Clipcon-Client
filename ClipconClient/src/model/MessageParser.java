package model;

import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class MessageParser {

   /**
    * @param message 서버에서 받은 Message객체
    * @return message 로부터 변환된 User객체 */
   public static User getUserByMessage(Message message) {
      User user = new User(); // 반환할 객체
      user.setEmail(message.get(Message.EMAIL));   // User객체에 email 삽입
      user.setName(message.get(Message.NAME));   // User객체에 name 삽입

      // user에 삽입할 AddressBook객체 생성
      AddressBook addressBook = new AddressBook();
      Map<String, String> users = addressBook.getAddressBook();

      // message에서 JSONObject추출
      JSONObject jsonMsg = message.getJson();
      JSONArray array = jsonMsg.getJSONArray(Message.LIST);

      Iterator<?> it = array.iterator();
      while (it.hasNext()) {
         JSONObject tmpJson = (JSONObject) it.next();
         String email = tmpJson.getString(Message.EMAIL);
         String name = tmpJson.getString(Message.NAME);
         users.put(email, name);
      }
      user.setAddressBook(addressBook);
      
      return user;
   }

   public static AddressBook getAddressBookByMessage(Message message) {
      AddressBook addressBook = new AddressBook();
      Map<String, String> users = addressBook.getAddressBook();
      JSONObject jsonMsg = message.getJson();
      JSONArray array = jsonMsg.getJSONArray(Message.LIST);

      Iterator<?> it = array.iterator();
      while (it.hasNext()) {
         JSONObject tmpJson = (JSONObject) it.next();
         String email = tmpJson.getString(Message.EMAIL);
         String name = tmpJson.getString(Message.NAME);
         // User tmpUser = new User(email, tmpJson.getString(Message.NAME));
         users.put(email, name);
      }

      for (String key : users.keySet()) {
         System.out.println(key + " " + users.get(key));
      }

      return addressBook;
   }

   public static Message getMeessageByAddressBook(AddressBook addressBook) {
      Map<String, String> users = addressBook.getAddressBook();
      Message message = new Message().setType(Message.ADDRESS_BOOK);

      JSONArray array = new JSONArray();
      for (String key : users.keySet()) {
         JSONObject tmp = new JSONObject();
         tmp.put(Message.EMAIL, users.get(key));
         tmp.put(Message.NAME, users.get(key));
         array.put(tmp);
      }
      message.getJson().put(Message.LIST, array);
      
      return message;
   }

   public static Message getMessageByUser(User user) {
      Message message = new Message().setType(Message.USER_INFO); // 반환할 객체, 타입은 '유저정보'

      message.add(Message.EMAIL, user.getEmail());   // email 삽입
      message.add(Message.NAME, user.getName());      // name 삽입

      // Json으로 변환할 주소록 Map
      Map<String, String> users = user.getAddressBook().getAddressBook();
      // 주소록 내용 담을 JsonArray
      JSONArray array = new JSONArray();
      // array에 주소록 내용 삽입
      for (String key : users.keySet()) {
         JSONObject tmp = new JSONObject();
         tmp.put(Message.EMAIL, users.get(key));
         tmp.put(Message.NAME, users.get(key));
         array.put(tmp);
      }
      // array를 message에 삽입
      message.getJson().put(Message.LIST, array);

      return message;
   }
   
   /**
    * @param message 서버에서 받은 Message객체
    * @return message 로부터 변환된 Group객체 */
   public static Group getGroupByMessage(Message message) {
		Group group = new Group();
		String key = message.get("groupkey");
		String name = message.get("groupname");
		group.setPrimaryKey(key);
		group.setName(name);

		group.setUserList((Map<String, String>)message.getObject("list"));
		return group;
	}

}