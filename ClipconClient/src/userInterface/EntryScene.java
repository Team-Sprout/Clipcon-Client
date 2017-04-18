package userInterface;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.websocket.EncodeException;

import controller.ClipboardController;
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
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import model.Message;

@Getter
@Setter
public class EntryScene implements Initializable {
	
	private UserInterface ui = UserInterface.getIntance();
	
	@FXML private Button createBtn;
	@FXML private Button joinBtn;
	@FXML private TextField groupNameTF;
	@FXML private TextField groupKeyTF;
	@FXML private Button backBtn;
	
	private static ActionEvent event;
	
	private Endpoint endpoint = Endpoint.getIntance();
	
	/**
	 * flag variable for checking it is initialize (success about login)
	 */
	private boolean flag;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ui.setEntryScene(this);
		flag = false;
		
		createBtn.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				EntryScene.event = event;
				
				System.out.println("그룹생성");
				
				// 서버에 REQUEST_REQUEST_CREATE_GROUP Messgae 보냄
				Message createGroupMsg = new Message().setType(Message.REQUEST_CREATE_GROUP);
				createGroupMsg.add(Message.GROUP_NAME, groupNameTF.getText());
				try {
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
								if (flag) {
									flag = false;
									showMainView();
									return;
								}
							}
						});

					}
				}, 50, 50, TimeUnit.MILLISECONDS);
			}
		});
		
		joinBtn.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				EntryScene.event = event;
				
				System.out.println("그룹참여");
				
				// 서버에 REQUEST_REQUEST_JOIN_GROUP Messgae 보냄
				Message joinGroupMsg = new Message().setType(Message.REQUEST_JOIN_GROUP);
				joinGroupMsg.add(Message.GROUP_PK, groupKeyTF.getText());
				try {
					endpoint.sendMessage(joinGroupMsg);
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
									showMainView();
									return;
								}
							}
						});

					}
				}, 50, 50, TimeUnit.MILLISECONDS);
			}
		});
		
		backBtn.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				EntryScene.event = event;
				
				System.out.println("초기화면으로");
				
				showStartingView();
			}
		});
	}
	
	public void showMainView() {
		try {				
			Parent toMain = FXMLLoader.load(getClass().getResource("/view/MainView.fxml"));
			Scene mainScene = new Scene(toMain);
			Stage mainStage = (Stage) ((Node) EntryScene.event.getSource()).getScene().getWindow();
			
			mainStage.hide();
			mainStage.setScene(mainScene);
			mainStage.show();
			
			startHookProcess(); // 키보드 후킹 시작
			
			System.out.println("만들어진 그룹명은 "+ groupNameTF.getText() +" 입니다.");
					
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void showStartingView() {
		try {				
			Parent toMain = FXMLLoader.load(getClass().getResource("/view/StartingView.fxml"));
			Scene mainScene = new Scene(toMain);
			Stage mainStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			
			mainStage.hide();
			mainStage.setScene(mainScene);
			mainStage.show();					
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void startHookProcess() {
		hookManager.GlobalKeyboardHook hook = new hookManager.GlobalKeyboardHook();
		int vitrualKey = KeyEvent.VK_H;
        boolean CTRL_Key = true;
        boolean ALT_Key = true;
        boolean SHIFT_Key = false;
        boolean WIN_Key = false;
		
        hook.setHotKey(vitrualKey, ALT_Key, CTRL_Key, SHIFT_Key, WIN_Key);
        hook.startHook();
        // waiting for the event
        hook.addGlobalKeyboardListener(new hookManager.GlobalKeyboardListener() {
            public void onGlobalHotkeysPressed() {
                System.out.println("CTRL + ALT + H was pressed");
                System.out.println(ClipboardController.readClipboard());
            }
        });
	}
}
