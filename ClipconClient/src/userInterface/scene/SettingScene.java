package userInterface.scene;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import userInterface.UserInterface;

public class SettingScene implements Initializable {
	
	private UserInterface ui = UserInterface.getInstance();
	
	@FXML private CheckBox clipboardMonitorNotiCB, uploadNotiCB;
	@FXML private Button XBtn;
	
	public static boolean clipboardMonitorNotiFlag = true;
	public static boolean uploadNotiFlag = true;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		if(clipboardMonitorNotiFlag)
			clipboardMonitorNotiCB.setSelected(false);
		else
			clipboardMonitorNotiCB.setSelected(true);
		
		if(uploadNotiFlag)
			uploadNotiCB.setSelected(false);
		else
			uploadNotiCB.setSelected(true);
		
		
		XBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ui.getMainScene().closeSettingStage();
			}
		});
		
	}
	
	public void checkEvent(ActionEvent event){
        if(clipboardMonitorNotiCB.isSelected()){
        	clipboardMonitorNotiFlag = false;
        } else {
        	clipboardMonitorNotiFlag = true;
        }
        
        if(uploadNotiCB.isSelected()){
        	uploadNotiFlag = false;
        } else {
        	uploadNotiFlag = true;
        }
    }
}
