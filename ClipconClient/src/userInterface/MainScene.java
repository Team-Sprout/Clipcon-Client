package userInterface;

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
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class MainScene implements Initializable{
	
	private UserInterface ui = UserInterface.getIntance();
	
	@FXML private Button exitBtn;
	@FXML private Button addBtn;
	@FXML private Button deleteBtn;
	@FXML private Button searchBtn;
	@FXML private Button inviteBtn;
	
	@FXML private TextField groupNameTF;
	@FXML private TextField groupKeyTF;
	@FXML private TextField searchTF; 
	
//	controller.EntryControl entryControl = new controller.EntryControl();
		
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ui.setMainScene(this);
		System.out.println("MainScene initialize");
		
//		String groupName = entryControl.getGroupName();
//		groupNameTF.setText(groupName);
		
		addBtn.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				
				System.out.println("주소록에서 친구 추가를 진행합니다.");
				
//				try {				
//					Parent newGroup = FXMLLoader.load(getClass().getResource("/view/EntryView.fxml"));
//					Scene newScene = new Scene(newGroup);
//					Stage newStage = new Stage();
//					newStage.setScene(newScene);
//					newStage.show();
//					
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
			}
		});
		
			
		deleteBtn.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				
				System.out.println("주소록에서 친구 삭제를 진행합니다.");
				

			}
		});
		
		inviteBtn.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				
				System.out.println("주소록에서 친구 초대를 진행합니다.");
				

			}
		});
		
		searchBtn.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				
				String searchTarget = searchTF.getText();
				System.out.println("주소록에서 친구 검색을 진행합니다. 검색 대상은 < "+searchTarget+" > 입니다.");
				

			}
		});
		
		exitBtn.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				
				System.out.println("그룹에서 나갑니다.");
				
				try {				
					Parent goBack = FXMLLoader.load(getClass().getResource("/view/EntryView.fxml"));
					Scene scene = new Scene(goBack);
					Stage backStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
					
					backStage.hide();
					backStage.setScene(scene);
					backStage.show();
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
}
