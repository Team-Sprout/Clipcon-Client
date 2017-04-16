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

import application.Main;
import javafx.scene.control.TextArea;
import model.Message;
import model.MessageDecoder;
import model.MessageEncoder;

@ClientEndpoint(decoders = { MessageDecoder.class }, encoders = { MessageEncoder.class })
public class Endpoint {
	private String uri = "ws://localhost:8080/WebSocketPrj03/ServerEndpoint";
	private Session session = null;
	// private TextArea textArea = null;

	public Endpoint(TextArea textArea) throws DeploymentException, IOException, URISyntaxException {
		URI uRI = new URI(uri);
		ContainerProvider.getWebSocketContainer().connectToServer(this, uRI);
		// this.textArea = textArea;
	}

	@OnOpen
	public void onOpen(Session session) {
		this.session = session;
	}

	@OnMessage
	public void onMessage(Message message) {
		switch (Message.TYPE) {
		case Message.REQUEST_SIGN_IN:
			break;

		case Message.REQUEST_SIGN_UP:
			break;

		case Message.REQUEST_CREATE_GROUP:
			break;

		case Message.REQUEST_JOIN_GROUP:
			break;

		default:
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