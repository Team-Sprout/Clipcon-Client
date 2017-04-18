package userInterface;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.websocket.EncodeException;

import controller.Endpoint;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Message;

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

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		ui.setStartingScene(this);

		System.out.println("StartingScene initialize");
		System.out.println("StartingScene Tostring() : " + this.toString());

		loginBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println(event); // XXX: delf
				StartingScene.event = event;
				System.out.println("StartingScene evnet : " + StartingScene.event);

				System.out.println("로그인 눌림");

				Message signInMsg = new Message().setType(Message.REQUEST_SIGN_IN);
				signInMsg.add(Message.EMAIL, idTF.getText());
				signInMsg.add("password", pwPF.getText());

				try {
					endpoint.sendMessage(signInMsg);
				} catch (IOException | EncodeException e) {
					e.printStackTrace();
				}
				
				showEntryView();
			}
		});

		signupBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println(event); // XXX: delf
				StartingScene.event = event;

				System.out.println("회원가입 눌림");

				showSignUpView();
			}
		});
		System.out.println("초기화 끝");
	}

	public void showEntryView() {
		try {
			Parent entry = FXMLLoader.load(getClass().getResource("/view/EntryView.fxml"));
			System.out.println("showEntryView: " + event);
			Scene entryScene = new Scene(entry);
			Stage entryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			// Stage entryStage = new Stage();
			// System.out.println("StartingScene showEntryView evnet : " + StartingScene.event);
			// System.out.println("StartingScene showEntryView entryStage : " + entryStage.toString());

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
			// Stage signupStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
			// signupStage.setScene(signupScene);
			// signupStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
