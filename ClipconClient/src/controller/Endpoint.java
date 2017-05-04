package controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import model.Contents;
import model.Message;
import model.MessageDecoder;
import model.MessageEncoder;
import model.MessageParser;
import model.User;
import userInterface.UserInterface;

@ClientEndpoint(decoders = { MessageDecoder.class }, encoders = { MessageEncoder.class })
public class Endpoint {
	// private String uri = "ws://182.172.16.118:8080/websocketServerModule/ServerEndpoint";
	// private String uri = "ws://223.194.157.244:8080/websocketServerModule/ServerEndpoint";
	private String uri = "ws://223.194.152.19:8080/websocketServerModule/ServerEndpoint";
	private Session session = null;
	private static Endpoint uniqueEndpoint;
	private static UserInterface ui;

	public static User user;

	public static Endpoint getIntance() {
		try {
			if (uniqueEndpoint == null) {
				uniqueEndpoint = new Endpoint();
			}
		} catch (DeploymentException | IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		return uniqueEndpoint;
	}

	public Endpoint() throws DeploymentException, IOException, URISyntaxException {
		URI uRI = new URI(uri);
		ContainerProvider.getWebSocketContainer().connectToServer(this, uRI);
		ui = UserInterface.getIntance();
	}

	@OnOpen
	public void onOpen(Session session) {
		this.session = session;
	}

	@OnMessage
	public void onMessage(Message message) {
		System.out.println("message type: " + message.getType());
		switch (message.get(Message.TYPE)) {

		case Message.RESPONSE_CREATE_GROUP:

			switch (message.get(Message.RESULT)) {
			case Message.CONFIRM:
				System.out.println("create group confirm");

				ui.getStartingScene().setCreateGroupSuccessFlag(true); // MainView 蹂댁뿬以�
				user = MessageParser.getUserAndGroupByMessage(message); // �꽌踰꾩뿉�꽌 primaryKey, name 諛쏆븘 Group 媛앹껜 �깮�꽦 �썑 user�뿉 set

				while (true) {
					if (ui.getMainScene() != null) {
						break;
					}
				}

				System.out.println("洹몃９�궎 : " + user.getGroup().getPrimaryKey());
				ui.getMainScene().setInitGroupParticipantFlag(true); // UI list 珥덇린�솕

				break;
			case Message.REJECT:
				System.out.println("create group reject");
				break;
			}

			break;

		case Message.RESPONSE_JOIN_GROUP:

			switch (message.get(Message.RESULT)) {
			case Message.CONFIRM:
				System.out.println("join group confirm");

				ui.getGroupJoinScene().setJoinGroupSuccessFlag(true); // Group join close �븯怨� MainView 蹂댁뿬以�
				user = MessageParser.getUserAndGroupByMessage(message); // �꽌踰꾩뿉�꽌 primaryKey, name 諛쏆븘 Group 媛앹껜 �깮�꽦 �썑 user�뿉 set

				while (true) {
					if (ui.getMainScene() != null) {
						break;
					}
				}

				System.out.println("洹몃９�궎 : " + user.getGroup().getPrimaryKey());
				ui.getMainScene().setInitGroupParticipantFlag(true); // UI list 珥덇린�솕

				break;
			case Message.REJECT:
				System.out.println("join group reject");
				break;
			}

			break;

		case Message.RESPONSE_EXIT_GROUP:

			System.out.println("exit group");

			while (true) {
				if (ui.getMainScene() != null) {
					break;
				}
			}

			ui.getMainScene().setShowStartingViewFlag(true); // StartingView 蹂댁뿬以�

			break;

		case Message.NOTI_ADD_PARTICIPANT: // 洹몃９ �궡 �떎瑜� User �뱾�뼱�삱 �븣 留덈떎 Message 諛쏄퀬 UI 媛깆떊

			System.out.println("add participant noti");

			User newParticipant = new User(message.get(Message.PARTICIPANT_NAME));

			user.getGroup().getUserList().add(newParticipant);
			ui.getMainScene().getGroupParticipantList().add(newParticipant);
			ui.getMainScene().setAddGroupParticipantFlag(true); // UI list 異붽�

			break;

		case Message.NOTI_EXIT_PARTICIPANT:

			System.out.println("exit participant noti");

			int removeIndex = -1;
			for (int i = 0; i < user.getGroup().getUserList().size(); i++) {
				if (message.get(Message.PARTICIPANT_NAME).equals(user.getGroup().getUserList().get(i))) {
					removeIndex = i;
				}
			}

			user.getGroup().getUserList().remove(removeIndex);
			ui.getMainScene().setInitGroupParticipantFlag(true); // UI list �젣嫄�

			break;

		case Message.NOTI_UPLOAD_DATA:

			System.out.println("update date noti");

			Contents contents = MessageParser.getContentsbyMessage(message);
			
			user.getGroup().addContents(contents);

			// TODO[�룄�뿰]: �엳�뒪�넗由� �뾽�뜲�씠�듃 UI泥섎━
			System.out.println("-----<Endpoint> contentsValue �궡�슜-----");
			System.out.println(contents.getContentsValue());
      ui.getMainScene().getHistoryList().add(contents);
			ui.getMainScene().setAddContentsInHistoryFlag(true); // UI list 異붽�
			break;

		default:
			System.out.println("default");
			break;
		}

	}

	public void sendMessage(Message message) throws IOException, EncodeException {
		if (session == null) {
			System.out.println("debuger_delf: session is null");
		}
		session.getBasicRemote().sendObject(message);
	}

	@OnClose
	public void onClose() {
		// �꽭�뀡�씠 �걡寃쇱쓣 �븣 �뼱�뼸寃� �븷吏� 泥섎━
	}
}