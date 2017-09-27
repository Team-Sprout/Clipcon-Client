package userInterface.scene;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.websocket.EncodeException;

import application.Main;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import model.message.Message;
import server.Endpoint;
import userInterface.UserInterface;

public class StartingScene implements Initializable {

	private UserInterface ui = UserInterface.getInstance();

	@FXML private Button createBtn, joinBtn;

	private Endpoint endpoint = Endpoint.getInstance();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ui.setStartingScene(this);

		// create button event handling
		createBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// send create group message to server
				Message createGroupMsg = new Message().setType(Message.REQUEST_CREATE_GROUP);
				try {
					endpoint.sendMessage(createGroupMsg);
				} catch (IOException | EncodeException e) {
					e.printStackTrace();
				}
			}
		});

		// join button event handling
		joinBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showGroupJoinView();
			}
		});
	}

	/** Show main view */
	public void showMainView() {
		Platform.runLater(() -> {
			try {
				Parent toMain = FXMLLoader.load(getClass().getResource("/view/MainView.fxml"));
				Scene mainScene = new Scene(toMain);
				Stage primaryStage = Main.getPrimaryStage();
				
				primaryStage.setScene(mainScene);
				primaryStage.show();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/** Show group join view */
	public void showGroupJoinView() {
		try {
			Parent toGroupJoin = FXMLLoader.load(getClass().getResource("/view/GroupJoinView.fxml"));
			Scene groupJoinScene = new Scene(toGroupJoin);
			Stage primaryStage = Main.getPrimaryStage();
			
			primaryStage.setScene(groupJoinScene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
