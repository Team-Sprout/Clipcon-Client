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
import model.Group;
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

				ui.getStartingScene().setCreateGroupSuccessFlag(true); // MainView 보여줌
				user = MessageParser.getUserAndGroupByMessage(message); // 서버에서  primaryKey, name 받아  Group 객체 생성 후 user에 set

				while (true) {
					if (ui.getMainScene() != null) {
						break;
					}
				}

				System.out.println("그룹키 : " + user.getGroup().getPrimaryKey());
				ui.getMainScene().setInitGroupParticipantFlag(true); // UI list 초기화

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

				ui.getGroupJoinScene().setJoinGroupSuccessFlag(true); // Group join close 하고 MainView 보여줌
				user = MessageParser.getUserAndGroupByMessage(message); // 서버에서 primaryKey, name 받아 Group 객체 생성 후 user에 set

				while (true) {
					if (ui.getMainScene() != null) {
						break;
					}
				}

				System.out.println("그룹키 : " + user.getGroup().getPrimaryKey());
				ui.getMainScene().setInitGroupParticipantFlag(true); // UI list 초기화

				break;
			case Message.REJECT:
				System.out.println("join group reject");
				break;
			}

			break;

		case Message.NOTI_ADD_PARTICIPANT: // 그룹 내 다른 User 들어올 때 마다 Message 받고 UI 갱신

			System.out.println("add participant confirm");

			user.getGroup().getUserList().add(new User(message.get(Message.PARTICIPANT_NAME)));
			ui.getMainScene().setAddGroupParticipantFlag(true); // UI list 추가

			break;

		case Message.NOTI_EXIT_PARTICIPANT:
			// TODO[도연]: 클라이언트 그룹 탈퇴 메시지 처리
			break;

		case Message.NOTI_UPLOAD_DATA:
			Contents contents = MessageParser.getContentsbyMessage(message);
			user.getGroup().addContents(contents);
			// TODO[도연]: 히스토리 업데이트 UI처리
			System.out.println("-----<Endpoint> contentsValue 내용-----");
			System.out.println(contents.getContentsValue());
			
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
		// 세션이 끊겼을 때 어떻게 할지 처리
	}
}