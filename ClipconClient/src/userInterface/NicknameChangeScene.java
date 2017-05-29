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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.Getter;
import lombok.Setter;
import model.Message;

@Getter
@Setter
public class NicknameChangeScene implements Initializable {
	
	private UserInterface ui = UserInterface.getIntance();

	@FXML private TextField nicknameTF;
	@FXML private Button OkBtn, XBtn;
	
	private Endpoint endpoint = Endpoint.getIntance();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ui.setNicknameChangeScene(this);
		
		nicknameTF.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode().equals(KeyCode.ENTER)) {
					if(nicknameTF.getText().length() == 0) {
						notInputNickname();
					} else {
						sendNicknameChangeMessage(nicknameTF.getText());
					}
				}
			}
		});
		
		OkBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(nicknameTF.getText().length() == 0) {
					notInputNickname();
				} else {
					sendNicknameChangeMessage(nicknameTF.getText());
				}
			}
		});
		
		XBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ui.getMainScene().closeNicknameChangeStage();
			}
		});
		
	}
	
	public void notInputNickname() {
		FailDialog.show("변경할 Nickname 을 입력하세요.");
	}
	
	public void sendNicknameChangeMessage(String nickname) {
		Message changeNicknameMsg = new Message().setType(Message.REQUEST_CHANGE_NAME);
		changeNicknameMsg.add(Message.CHANGE_NAME, nickname);
		
		try {
			if (endpoint == null) {
				System.out.println("debuger_delf: endpoint is null");
			}

			endpoint = Endpoint.getIntance();
			endpoint.sendMessage(changeNicknameMsg);
		} catch (IOException | EncodeException e) {
			e.printStackTrace();
		}
	}
}
