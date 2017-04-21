package userInterface;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.websocket.EncodeException;

import controller.Endpoint;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import model.Message;

@Getter
@Setter
public class GroupJoinScene implements Initializable{

	private UserInterface ui = UserInterface.getIntance();

	@FXML private TextField groupKey;
	@FXML private Button confirmBtn;

	private Endpoint endpoint = Endpoint.getIntance();

	/**
	 * flag variable for checking it is initialize (success about login)
	 */
	private boolean joinGroupSuccessFlag;

	private Stage stage;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ui.setGroupJoinScene(this);
		joinGroupSuccessFlag = false;
		
		confirmBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				stage = (Stage) confirmBtn.getScene().getWindow();

				// ¼­¹ö¿¡ REQUEST_JOIN_GROUP Messgae º¸³¿
				if (groupKey.getText().length() != 0) {
					Message signUpMsg = new Message().setType(Message.REQUEST_JOIN_GROUP);
					signUpMsg.add(Message.GROUP_PK, groupKey.getText());
					try {
						endpoint.sendMessage(signUpMsg);
					} catch (IOException | EncodeException e) {
						e.printStackTrace();
					}
				}

				// run scheduler for checking
				final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

				scheduler.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								// if flag turn on then client login game
								if (joinGroupSuccessFlag) {
									joinGroupSuccessFlag = false;
									closeSignUpView();
									ui.getStartingScene().showMainView();
									return;
								}
							}
						});

					}
				}, 50, 50, TimeUnit.MILLISECONDS);
			}
		});
	}

	public void closeSignUpView() {
		stage.close();
	}

}
