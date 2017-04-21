package userInterface;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import contents.Contents;
import controller.ClipboardController;
import controller.Endpoint;
import javafx.application.Platform;
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
import javafx.scene.text.Text;
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
	
	@FXML private Button exitBtn;
	@FXML private Button groupKeyCopyBtn;
	
	//@FXML private TextField groupNameTF;
	//@FXML private TextField groupKeyTF;
	
	@FXML private Text groupKeyText;
	
	private static ActionEvent event;
	
	private ObservableList<User> groupParticipantList;
	
	/**
	 * flag variable for checking it is initialize (success about login)
	 */
	private boolean initGroupParticipantFlag;
	private boolean addGroupParticipantFlag;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ui.setMainScene(this);
		initGroupParticipantFlag = false;
		addGroupParticipantFlag = false;
		
		System.out.println("MainScene initialize");
		
		groupParticipantList = FXCollections.observableArrayList();
		
		// run scheduler for checking
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						// if flag turn on then client login game
						if (initGroupParticipantFlag) {
							initGroupParticipantFlag = false;
							initGroupParticipantList();
						}
						if (addGroupParticipantFlag) {
							addGroupParticipantFlag = false;
							addGroupParticipantList();
						}
					}
				});

			}
		}, 50, 50, TimeUnit.MILLISECONDS);
		
		exitBtn.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				MainScene.event = event;
				
				System.out.println("그룹에서 나갑니다.");
				
				showStartingView();
			}
		});
		
		groupKeyCopyBtn.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				ClipboardController.writeClipboard(Endpoint.user.getGroup().getPrimaryKey(), Contents.STRING_TYPE);
			}
		});
	}
	
	public void initGroupParticipantList() {
		groupKeyText.setText(Endpoint.user.getGroup().getPrimaryKey());
		
		for(int i=0; i<Endpoint.user.getGroup().getUserList().size(); i++) {
			groupParticipantList.add(Endpoint.user.getGroup().getUserList().get(i));
		}
		
		groupParticipantTable.setItems(groupParticipantList);
		groupPartiNicknameColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
	}
	
	public void addGroupParticipantList() {
		int index = Endpoint.user.getGroup().getUserList().size() - 1;
		User addedParticipantUser = Endpoint.user.getGroup().getUserList().get(index);
		groupParticipantList.add(addedParticipantUser);
		
		groupParticipantTable.setItems(groupParticipantList);
		groupPartiNicknameColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
	}
	
	public void showStartingView() {
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
}
