package controller;

import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ResourceBundle;

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

public class StartingController implements Initializable {
	
	@FXML private Button loginBtn;
	@FXML private Button signupBtn;	
	@FXML private TextField idTF;	
	@FXML private PasswordField pwPF;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		// TODO Auto-generated method stub
		loginBtn.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				System.out.println("로그인 눌림");
						
				if(idTF.getText().equals("test") && pwPF.getText().equals("12")){
					System.out.println("로그인 성공");
									
					try {
						Parent entry = FXMLLoader.load(getClass().getResource("/view/EntryView.fxml"));
						Scene entryScene = new Scene(entry);
						Stage entryStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
						
						entryStage.hide();
						entryStage.setScene(entryScene);
						entryStage.show();
						
						System.out.println("엔트리 화면 (만들기 or 참여) 으로 진입합니다.");
						
						startHookProcess();
						
						
					} catch (Exception e){
						e.printStackTrace();
					}
					
				}else{
					System.out.println("로그인 실패");
				}
			
			
			}
		});
		
		signupBtn.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				System.out.println("회원가입 눌림");	
				
				try {
					Parent signup = FXMLLoader.load(getClass().getResource("/view/SignupView.fxml"));
					Scene signupScene = new Scene(signup);
					Stage tempStage = new Stage();
					tempStage.setScene(signupScene);
					tempStage.show();
//					Stage signupStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
//					signupStage.setScene(signupScene);
//					signupStage.show();
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		});
		
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
