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

import model.Message;
import model.MessageDecoder;
import model.MessageEncoder;
import userInterface.UserInterface;

@ClientEndpoint(decoders = { MessageDecoder.class }, encoders = { MessageEncoder.class })
public class Endpoint {
	private String uri = "ws://182.172.16.118:8080/websocketServerModule/ServerEndpoint";
	private Session session = null;

	private static Endpoint uniqueEndpoint;
	private static UserInterface ui;

	public static Endpoint getIntance() {
		System.out.println("getIntance()");
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
		System.out.println("Endpoint 생성자");
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
		case Message.REQUEST_SIGN_IN: // 로그인 요청에 대한 응답
			switch (message.get("result")) {
			case "ok":
				System.out.println("sign in ok");
				System.out.println("Endpoint Tostring() : " + ui.getStartingScene().toString());
				ui.getStartingScene().showEntryView();
				//StartingScene.showEntryView(); // EntryView 보여줌
				// 서버에서 이메일, 닉네임, 주소록 받아 User 객체 생성
				break;
			case "NOT OK":
				System.out.println("이메일, 비밀번호 불일치");
				break;
			}
			
			break;

		case Message.REQUEST_SIGN_UP:
			
			switch (message.get("response")) {
			case "OK":
				//userInterface.getSignupScene().closeSignUpView(); // signUpView 닫음
				System.out.println("회원가입 완료");
				break;
			case "NOT OK":
				System.out.println("이메일/닉네임 중복");
				break;
			}
			
			break;

		case Message.REQUEST_CREATE_GROUP:
			
			switch (message.get("response")) {
			case "OK":
				//userInterface.getEntryScene().showMainView(); // MainView 보여줌
				// 서버에서 primaryKey, (name) 받아 Group 객체 생성 후 User에 할당
				// 이후에 다른 User 들어올 때 마다 respond 받고 UI 갱신
				break;
			case "NOT OK":
				break;
			}
			
			break;

		case Message.REQUEST_JOIN_GROUP:
			
			switch (message.get("response")) {
			case "OK":
				//userInterface.getEntryScene().showMainView(); // MainView 보여줌
				// 서버에서 (primaryKey), name, 참여자 명단, 히스토리 받아 Group 객체 생성 후 User에 할당
				// 이후에 다른 User 들어올 때 마다 respond 받고 UI 갱신
				break;
			case "NOT OK":
				break;
			}
			
			break;

		default:
			System.out.println("default");
			break;
		}
	}

	public void sendMessage(Message message) throws IOException, EncodeException {
		session.getBasicRemote().sendObject(message);
	}

	@OnClose
	public void onClose() {
		// 세션이 끊겼을 때 어떻게 할지 처리
	}
}