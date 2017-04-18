package userInterface;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.websocket.EncodeException;

import controller.ClipboardController;
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
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Message;

public class EntryScene implements Initializable {
	
	private UserInterface ui = UserInterface.getIntance();
	
	@FXML private Button createBtn;
	@FXML private Button joinBtn;
	@FXML private TextField groupNameTF;
	@FXML private TextField groupKeyTF;
	@FXML private Button backBtn;
	
	private static ActionEvent event;
	
	private Endpoint endpoint = Endpoint.getIntance();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		ui.setEntryScene(this);
		
		System.out.println("EntryScene initialize");
		
		createBtn.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				EntryScene.event = event;
				
				System.out.println("그룹생성");
				System.out.println("테스트");
				
				Message createGroupMsg = new Message(Message.REQUEST_CREATE_GROUP);
				createGroupMsg.add("groupName", groupNameTF.getText());
				try {
					endpoint.sendMessage(createGroupMsg);
				} catch (IOException | EncodeException e) {
					e.printStackTrace();
				}
			}
		});
		
		joinBtn.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				EntryScene.event = event;
				
				System.out.println("그룹참여");
				
				Message joinGroupMsg = new Message(Message.REQUEST_JOIN_GROUP);
				joinGroupMsg.add("groupPK", groupKeyTF.getText());
				try {
					endpoint.sendMessage(joinGroupMsg);
				} catch (IOException | EncodeException e) {
					e.printStackTrace();
				}
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
