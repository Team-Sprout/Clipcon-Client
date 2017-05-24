package userInterface;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.websocket.EncodeException;

import application.Main;
import controller.Endpoint;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import model.Message;

@Getter
@Setter
public class GroupJoinScene implements Initializable{

	private UserInterface ui = UserInterface.getIntance();

	@FXML private TextField groupKey;
	@FXML private Button confirmBtn, XBtn;
	
	private Stage stage;
	private Endpoint endpoint = Endpoint.getIntance();
	
	private boolean joinGroupSuccessFlag;
	private boolean joinGroupFailFlag;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ui.setGroupJoinScene(this);
		joinGroupSuccessFlag = false;
		joinGroupFailFlag = false;
		
		groupKey.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode().equals(KeyCode.ENTER)) {
					if(groupKey.getText().length() == 0) {
						notInputGroupKey();
					} else {
						sendGroupJoinMessage();
					}
				}
			}
		});
		
		confirmBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(groupKey.getText().length() == 0) {
					notInputGroupKey();
				} else {
					sendGroupJoinMessage();
				}
			}
		});
		
		XBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					Parent goBack = FXMLLoader.load(getClass().getResource("/view/StartingView.fxml"));
					Scene scene = new Scene(goBack);
					Stage backStage = Main.getPrimaryStage();

					backStage.setScene(scene);
					backStage.show();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void notInputGroupKey() {
		GroupJoinFailDialog.show("Group key 를 입력하세요.");
	}
	
	// send REQUEST_JOIN_GROUP Messgae to server
	public void sendGroupJoinMessage() {
		if (groupKey.getText().length() != 0) {
			stage = (Stage) confirmBtn.getScene().getWindow();
			
			Message signUpMsg = new Message().setType(Message.REQUEST_JOIN_GROUP);
			signUpMsg.add(Message.GROUP_PK, groupKey.getText());
			try {
				endpoint.sendMessage(signUpMsg);
			} catch (IOException | EncodeException e) {
				e.printStackTrace();
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
								ui.getStartingScene().showMainView();
								return;
							}
							if (joinGroupFailFlag) {
								joinGroupFailFlag = false;
								GroupJoinFailDialog.show("유효하지 않는 Group Key 입니다. 다시 입력하세요.");
								groupKey.setText("");
							}
						}
					});

				}
			}, 50, 50, TimeUnit.MILLISECONDS);
		}
	}

}
