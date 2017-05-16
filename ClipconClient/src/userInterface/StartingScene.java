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
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import model.Message;

@Getter
@Setter
public class StartingScene implements Initializable {

	private UserInterface ui = UserInterface.getIntance();

	//@FXML private StackPane pane;
	@FXML private Button createBtn, joinBtn;

	private Endpoint endpoint = Endpoint.getIntance();

	private boolean createGroupSuccessFlag;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ui.setStartingScene(this);
		createGroupSuccessFlag = false;

		createBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				// send REQUEST_REQUEST_CREATE_GROUP Messgae to server
				Message createGroupMsg = new Message().setType(Message.REQUEST_CREATE_GROUP);
				try {
					if (endpoint == null) {
						System.out.println("debuger_delf: endpoint is null");
					}
					if (createGroupMsg == null) {
						System.out.println("debuger_delf: createGroupMsg is null");
					}
					endpoint = Endpoint.getIntance();
					endpoint.sendMessage(createGroupMsg);
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
								if (createGroupSuccessFlag) {
									createGroupSuccessFlag = false;
									showMainView();
									return;
								}
							}
						});

					}
				}, 50, 50, TimeUnit.MILLISECONDS);
			}
		});

		joinBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showGroupJoinView();
			}
		});
	}

	public void showMainView() {
		try {
			Parent toMain = FXMLLoader.load(getClass().getResource("/view/MainView.fxml"));
			Scene mainScene = new Scene(toMain);
			Stage primaryStage = Main.getPrimaryStage();
			
			primaryStage.setScene(mainScene);
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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
		
//		JFXDialogLayout content = new JFXDialogLayout();
//		content.setHeading(new Text("Join"));
//		content.setBody(new TextField(), new Text("\n\n\n\n\n\n"));
//		JFXDialog dialog = new JFXDialog(pane, content, JFXDialog.DialogTransition.BOTTOM);
//		JFXButton button = new JFXButton("okay");
//		button.setOnAction(new EventHandler<ActionEvent>() {
//			@Override
//			public void handle(ActionEvent event) {
//				dialog.close();
//			}
//		});
//		content.setActions(button);
//		dialog.show();
	}
}
