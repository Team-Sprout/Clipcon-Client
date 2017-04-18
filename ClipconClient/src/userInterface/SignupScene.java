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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import model.Message;

@Getter
@Setter
public class SignupScene implements Initializable{
	
	private UserInterface ui = UserInterface.getIntance();
	
	@FXML private TextField email;
	@FXML private PasswordField password1;
	@FXML private PasswordField password2;
	@FXML private TextField nickname;
	@FXML private Button confirmBtn;
	
	private Endpoint endpoint = Endpoint.getIntance();
	
	/**
	 * flag variable for checking it is initialize (success about login)
	 */
	private boolean flag;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ui.setSignupScene(this);
		flag = false;
		
		confirmBtn.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				if (email.getText().length()==0 || password1.getText().length()==0 || password2.getText().length()==0
						|| nickname.getText().length()==0) {
					System.out.println("모든 정보 입력");
				} 
				else {
					if (password1.getText().equals(password2.getText())) {
						
						// 서버에 REQUEST_SIGN_UP Messgae 보냄
						Message signUpMsg = new Message().setType(Message.REQUEST_SIGN_UP);
						signUpMsg.add(Message.EMAIL, email.getText());
						signUpMsg.add("password", password1.getText());
						signUpMsg.add(Message.NAME, nickname.getText());
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
										if (flag) {
											flag = false;
											closeSignUpView();
											return;
										}
									}
								});

							}
						}, 50, 50, TimeUnit.MILLISECONDS);
						
					} else {
						System.out.println("비밀번호 확인");
					}
				}
			}

		});
	}

	public void closeSignUpView() {
		Stage stage = (Stage) confirmBtn.getScene().getWindow();
	    stage.close();
	}

}
