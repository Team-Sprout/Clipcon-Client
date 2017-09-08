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
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import model.Contents;
import model.Message;
import model.MessageDecoder;
import model.MessageEncoder;
import model.MessageParser;
import model.User;
import userInterface.plainDialog;
import userInterface.UserInterface;

@ClientEndpoint(decoders = { MessageDecoder.class }, encoders = { MessageEncoder.class })
public class Endpoint {

	private final String PROTOCOL = "ws://";
	private final String CONTEXT_ROOT = "websocketServerModule/ServerEndpoint";
	private final String uri = PROTOCOL + Main.SERVER_URI_PART + CONTEXT_ROOT;

	private Session session = null;
	private static Endpoint uniqueEndpoint;
	private static UserInterface ui;

	public static User user;

	public static Endpoint getInstance() {
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
		ui = UserInterface.getInstance();
		new PingPong().start();
	}

	@OnOpen
	public void onOpen(Session session) {
		this.session = session;
	}

	@OnMessage
	public void onMessage(Message message) {
		System.out.println("message type: " + message.get(Message.TYPE));
		switch (message.get(Message.TYPE)) {
		case Message.RESPONSE_CONFIRM_VERSION:
			switch (message.get(Message.RESULT)) {
			case Message.REJECT:
				Platform.runLater(() -> {
					plainDialog.show("Download updated version!! http://113.198.84.53/websocketServerModule/download");
				});
				break;
			}
			break;

		case Message.RESPONSE_CREATE_GROUP:
			switch (message.get(Message.RESULT)) {
			case Message.CONFIRM:
				ui.getStartingScene().showMainView(); // Show MainView
				user = MessageParser.getUserAndGroupByMessage(message); // create Group Object using primaryKey, name(get from server) and set to user

				while (true) {
					if (ui.getMainScene() != null && user != null) {
						break;
					}
					System.out.print(""); // [TODO] UI refresh
				}

				ui.getMainScene().initGroupParticipantList(); // UI list initialization
				break;
			}
			break;

		case Message.RESPONSE_CHANGE_NAME:
			switch (message.get(Message.RESULT)) {
			case Message.CONFIRM:
				String changeName = message.get(Message.CHANGE_NAME);
				
				System.out.println("changeName : " + changeName);

				user.setName(changeName);
				user.getGroup().getUserList().get(0).setName(changeName);
				user.getGroup().getUserList().get(0).setNameProperty(new SimpleStringProperty(changeName));

				ui.getMainScene().closeNicknameChangeStage();
				ui.getMainScene().initGroupParticipantList(); // UI list initialization
				break;
			}
			break;

		case Message.RESPONSE_JOIN_GROUP:
			switch (message.get(Message.RESULT)) {
			case Message.CONFIRM:
				ui.getGroupJoinScene().showMainView(); // close group join and show MainView
				user = MessageParser.getUserAndGroupByMessage(message); // create Group Object using primaryKey, name(get from server) and set to user

				while (true) {
					if (ui.getMainScene() != null && user != null) {
						break;
					}
					System.out.print(""); // [TODO] UI refresh
				}

				ui.getMainScene().initGroupParticipantList(); // UI list initialization
				break;

			case Message.REJECT:
				ui.getGroupJoinScene().failGroupJoin(); // UI list initialization
				break;
			}
			break;

		case Message.RESPONSE_EXIT_GROUP:
			while (true) {
				if (ui.getMainScene() != null) {
					break;
				}
			}

			ui.getMainScene().showStartingView(); // show StartingView
			user = null;
			break;

		case Message.NOTI_ADD_PARTICIPANT: // receive a message when another user enters the group and updates the UI
			User newParticipant = new User(message.get(Message.PARTICIPANT_NAME));

			user.getGroup().getUserList().add(newParticipant);
			ui.getMainScene().getGroupParticipantList().add(newParticipant);
			ui.getMainScene().addGroupParticipantList(); // update UI list
			break;

		case Message.NOTI_CHANGE_NAME:
			String name = message.get(Message.NAME);
			String changeName = message.get(Message.CHANGE_NAME);

			for (int i = 0; i < user.getGroup().getUserList().size(); i++) {
				if (user.getGroup().getUserList().get(i).getName().equals(name)) {
					user.getGroup().getUserList().remove(i);
					user.getGroup().getUserList().add(i, new User(changeName));
				}
			}

			ui.getMainScene().initGroupParticipantList(); // UI list initialization
			break;

		case Message.NOTI_EXIT_PARTICIPANT:
			for (int i = 0; i < user.getGroup().getUserList().size(); i++) {
				if (message.get(Message.PARTICIPANT_NAME).equals(user.getGroup().getUserList().get(i).getName())) {
					int removeIndex = i;
					user.getGroup().getUserList().remove(removeIndex);
					break;
				}
			}

			ui.getMainScene().initGroupParticipantList(); // update UI list
			break;

		case Message.NOTI_UPLOAD_DATA:
			Contents contents = MessageParser.getContentsbyMessage(message);

			user.getGroup().addContents(contents);

			ui.getMainScene().getHistoryList().add(0, contents);
			ui.getMainScene().addContentsInHistory(); // update UI list

			break;
			
		case Message.PONG:
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
		System.out.println("[on Close]");
		// TODO [delf] How to handle when a session is lost
	}

	class PingPong extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(3 * 1000);
					sendMessage(new Message().setType(Message.PING));

				} catch (InterruptedException e) {
					System.out.println("[ERROR] Pingping thread - InterruptedException");
				} catch (IOException e) {
					System.out.println("[ERROR] Pingping thread - IOException");
				} catch (EncodeException e) {
					System.out.println("[ERROR] Pingping thread - EncodeException");
				}
			}
		}
	}
}