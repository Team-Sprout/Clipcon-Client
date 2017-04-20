package userInterface;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import model.User;

@Getter
@Setter
public class MainScene implements Initializable{
	
	private UserInterface ui = UserInterface.getIntance();
	
	@FXML private TableView<User> groupParticipantTable;
	@FXML private TableColumn<User, String> groupPartiNicknameColumn;
    @FXML private TableColumn<User, String> groupPartiEmailColumn;
	
	@FXML private Button exitBtn;
	
	@FXML private TextField groupNameTF;
	@FXML private TextField groupKeyTF;
	
	private ObservableList<User> groupParticipantList = FXCollections.observableArrayList();
		
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ui.setMainScene(this);
		System.out.println("MainScene initialize");
		
		exitBtn.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				
				System.out.println("그룹에서 나갑니다.");
				
				try {				
					Parent goBack = FXMLLoader.load(getClass().getResource("/view/StartingView.fxml"));
					Scene scene = new Scene(goBack);
					Stage backStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
					
					backStage.hide();
					backStage.setScene(scene);
					backStage.show();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
