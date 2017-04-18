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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Popup;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import model.Message;

@Getter
@Setter
public class StartingScene implements Initializable {

	private UserInterface ui = UserInterface.getIntance();

	@FXML
	private Button loginBtn;
	@FXML
	private Button signupBtn;
	@FXML
	private TextField idTF;
	@FXML
	private PasswordField pwPF;

	private static ActionEvent event;

	private Endpoint endpoint = Endpoint.getIntance();
	
	private Stage entryStage;
	
	/**
	 * flag variable for checking it is initialize (success about login)
	 */
	private boolean flag;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ui.setStartingScene(this);
		flag = false;

		loginBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println("로그인 눌림");
				StartingScene.event = event;

				// 서버에 REQUEST_SIGN_IN Messgae 보냄
				Message signInMsg = new Message().setType(Message.REQUEST_SIGN_IN);
				signInMsg.add(Message.EMAIL, idTF.getText());
				signInMsg.add("password", pwPF.getText());

				try {
					endpoint.sendMessage(signInMsg);
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
									//checkTheNewUserRegisterSuccess();
									showEntryView();
									return;
								}
							}
						});

					}
				}, 50, 50, TimeUnit.MILLISECONDS);
				
			}
		});

		signupBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println("회원가입 눌림");

				showSignUpView();
			}
		});

	}

	public void showEntryView() {
		try {
			Parent entry = FXMLLoader.load(getClass().getResource("/view/EntryView.fxml"));
			Scene entryScene = new Scene(entry);
			entryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			entryStage.hide();
			entryStage.setScene(entryScene);
			entryStage.show();

			System.out.println("엔트리 화면 (만들기 or 참여) 으로 진입합니다.");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showSignUpView() {
		try {
			Parent signup = FXMLLoader.load(getClass().getResource("/view/SignupView.fxml"));
			Scene signupScene = new Scene(signup);
			Stage tempStage = new Stage();
			tempStage.setScene(signupScene);
			tempStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void showLoginFailPopup() {
		Platform.runLater(() -> handlePopup("이메일, 비밀번호 불일치"));
	}
	
//	public void checkTheNewUserRegisterSuccess() {
//		Platform.runLater(() -> handlePopup("로그인 성공"));
//	}
	
	public void handlePopup(String text) {
		Platform.runLater(() -> {
			try {
				Popup popup = new Popup();

				Parent parent;

				parent = FXMLLoader.load(getClass().getResource("/view/popup.fxml"));
				Label lblMessage = (Label) parent.lookup("#lblMessage");
				lblMessage.setText(text);
				lblMessage.setOnMouseClicked(event -> popup.hide());

				// set the popup window position
				popup.setX(entryStage.getX() + 200);
				popup.setY(entryStage.getY() + 370);

				popup.getContent().add(parent);
				popup.setAutoHide(true);
				popup.show(entryStage);
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		});
	}
}
