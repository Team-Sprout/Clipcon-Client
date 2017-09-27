package server;

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
import model.User;
import model.message.Message;
import model.message.MessageDecoder;
import model.message.MessageEncoder;
import model.message.MessageParser;
import userInterface.UserInterface;
import userInterface.dialog.Dialog;
import userInterface.dialog.PlainDialog;

@ClientEndpoint(decoders = { MessageDecoder.class }, encoders = { MessageEncoder.class })
public class Endpoint {

	private final String PROTOCOL = "ws://";
	private final String CONTEXT_ROOT = "globalclipboard/ServerEndpoint";
	private final String uri = PROTOCOL + Main.SERVER_URI_PART + CONTEXT_ROOT;

	private Session session = null;
	private static Endpoint uniqueEndpoint;
	private static UserInterface ui;
	private Dialog dialog;

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
				// Show dialog
				Platform.runLater(() -> {
					dialog = new PlainDialog("You have to download update version http://113.198.84.53/globalclipboard/download", false);
					dialog.showAndWait();
				});
				break;
			}
			break;

		case Message.RESPONSE_CREATE_GROUP:
			switch (message.get(Message.RESULT)) {
			case Message.CONFIRM:
				ui.getStartingScene().showMainView(); // Show main view
				user = MessageParser.getUserAndGroupByMessage(message); // Create group object using primaryKey, name(get from server) and set to user
				break;
			}
			break;

		case Message.RESPONSE_CHANGE_NAME:
			switch (message.get(Message.RESULT)) {
			case Message.CONFIRM:
				String changeName = message.get(Message.CHANGE_NAME); // Get change name
				
				// Set change name
				user.setName(changeName);
				user.getGroup().getUserList().get(0).setName(changeName);
				user.getGroup().getUserList().get(0).setNameProperty(new SimpleStringProperty(changeName));

				ui.getMainScene().closeNicknameChangeStage(); // Close nickname change stage
				ui.getMainScene().initGroupParticipantList(); // Group participant list initialization
				break;
			}
			break;

		case Message.RESPONSE_JOIN_GROUP:
			switch (message.get(Message.RESULT)) {
			case Message.CONFIRM:
				ui.getGroupJoinScene().showMainView(); // close group join and show MainView
				user = MessageParser.getUserAndGroupByMessage(message); // create Group Object using primaryKey, name(get from server) and set to user
				break;

			case Message.REJECT:
				ui.getGroupJoinScene().failGroupJoin(); // Show fail dialog
				break;
			}
			break;

		case Message.RESPONSE_EXIT_GROUP:
			ui.getMainScene().showStartingView(); // show starting view
			user = null;
			break;

		// Receive a message when another user enters the group and updates the UI
		case Message.NOTI_ADD_PARTICIPANT:
			User newParticipant = new User(message.get(Message.PARTICIPANT_NAME)); // Get new participant nickname

			// Add new participant nickname in participant list
			user.getGroup().getUserList().add(newParticipant);
			ui.getMainScene().getGroupParticipantList().add(newParticipant);
			ui.getMainScene().addGroupParticipantList(); // Group participant list update
			break;

		case Message.NOTI_CHANGE_NAME:
			String name = message.get(Message.NAME); // Get the origin nickname of the participant
			String changeName = message.get(Message.CHANGE_NAME); // Get the changed nickname of the participant

			// Find original nickname and change it to a new nickname
			for (int i = 0; i < user.getGroup().getUserList().size(); i++) {
				if (user.getGroup().getUserList().get(i).getName().equals(name)) {
					user.getGroup().getUserList().remove(i);
					user.getGroup().getUserList().add(i, new User(changeName));
				}
			}

			ui.getMainScene().initGroupParticipantList(); // Group participant list initialization
			break;

		case Message.NOTI_EXIT_PARTICIPANT:
			// Find the nickname of the participant who left the group and remove it from the list
			for (int i = 0; i < user.getGroup().getUserList().size(); i++) {
				if (message.get(Message.PARTICIPANT_NAME).equals(user.getGroup().getUserList().get(i).getName())) {
					int removeIndex = i;
					user.getGroup().getUserList().remove(removeIndex);
					break;
				}
			}

			ui.getMainScene().initGroupParticipantList(); // Group participant list initialization
			break;

		case Message.NOTI_UPLOAD_DATA:
			Contents contents = MessageParser.getContentsbyMessage(message); // Get content

			// Add new content in history list
			user.getGroup().addContents(contents);
			ui.getMainScene().getHistoryList().add(0, contents);
			ui.getMainScene().addContentsInHistory(); // History list update

			break;
			
		case Message.PONG:
			break;
			
		case Message.SERVERMSG:
			String msg = message.get(Message.CONTENTS);
			Platform.runLater(() -> {
				Dialog plainDialog = new PlainDialog(msg, false);
				plainDialog.showAndWait();
			});
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
		// show dialog when the web socket is disconnected
		Platform.runLater(() -> {
			dialog = new PlainDialog("서버와의 연결이 끊겼습니다.", true);
			dialog.showAndWait();
		});
	}

	class PingPong extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(3 * 60 * 1000);
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