package userInterface;

import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.websocket.EncodeException;

import contentsTransfer.ContentsUpload;
import contentsTransfer.DownloadData;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.Getter;
import lombok.Setter;
import model.Contents;
import model.History;
import model.Message;
import model.User;

@Getter
@Setter
public class MainScene implements Initializable {

	private UserInterface ui = UserInterface.getIntance();

	@FXML private TableView<User> groupParticipantTable;
	@FXML private TableColumn<User, String> groupPartiNicknameColumn;
	
	@FXML private TableView<Contents> historyTable;
	@FXML private TableColumn<Contents, String> typeColumn, uploaderColumn;
	//@FXML private TableColumn<Contents, ImageView> contentsColumn;
	@FXML private TableColumn contentsColumn;
	
	@FXML private Button exitBtn, groupKeyCopyBtn;
	@FXML private Text groupKeyText;

	private static ActionEvent event;
	private Endpoint endpoint = Endpoint.getIntance();

	private boolean initGroupParticipantFlag;
	private boolean addGroupParticipantFlag;
	private boolean addContentsInHistoryFlag;
	private boolean showStartingViewFlag;

	private ObservableList<User> groupParticipantList;
	private ContentsUpload contentsUpload;
	
	private ObservableList<Contents> historyList;

	// download test
	private DownloadData downloader = new DownloadData("gmlwjd9405@naver.com", "doyyyy");

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ui.setMainScene(this);
		initGroupParticipantFlag = false;
		addGroupParticipantFlag = false;

		contentsUpload = new ContentsUpload();
		startHookProcess();

		groupParticipantList = FXCollections.observableArrayList();
		historyList = FXCollections.observableArrayList();

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
						if (addContentsInHistoryFlag) {
							addContentsInHistoryFlag = false;
							addContentsInHistory();
						}
						if (showStartingViewFlag) {
							showStartingViewFlag = false;
							showStartingView();
							return;
						}
					}
				});

			}
		}, 50, 50, TimeUnit.MILLISECONDS);

		exitBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				MainScene.event = event;

				// test
				//testDownload();

				 //서버에 REQUEST_EXIT_GROUP Messgae 보냄
				Message exitGroupMsg = new Message().setType(Message.REQUEST_EXIT_GROUP);
				try {
					if (endpoint == null) {
						System.out.println("debuger_delf: endpoint is null");
					}
					endpoint = Endpoint.getIntance();
					endpoint.sendMessage(exitGroupMsg);
				} catch (IOException | EncodeException e) {
					e.printStackTrace();
				}
			}
		});

		groupKeyCopyBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ClipboardController.writeClipboard(new StringSelection(Endpoint.user.getGroup().getPrimaryKey()));
			}
		});
	}

	public void initGroupParticipantList() {

		groupKeyText.setText(Endpoint.user.getGroup().getPrimaryKey());

		for (int i = 0; i < Endpoint.user.getGroup().getUserList().size(); i++) {
			groupParticipantList.add(Endpoint.user.getGroup().getUserList().get(i));
		}

		groupParticipantTable.setItems(groupParticipantList);
		groupPartiNicknameColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
	}

	public void addGroupParticipantList() {

		groupParticipantTable.setItems(groupParticipantList);
		
		groupPartiNicknameColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
	}
	
	@SuppressWarnings("unchecked")
	public void addContentsInHistory() {
		historyTable.setItems(historyList);
		
		Contents c = historyList.get(historyList.size() - 1);
		//if(c.getContentsType().equals(Contents.TYPE_IMAGE)) {
		//	contentsColumn.setCellValueFactory(cellData -> cellData.getValue().getContentsImageProperty());
		//if(c.getContentsType().equals(Contents.TYPE_IMAGE)) \
		//	contentsColumn.setCellValueFactory(cellData -> cellData.getValue().getContentsImageProperty());
		//}
		//else {
			//contentsColumn.setCellValueFactory(cellData -> cellData.getValue().getContentsProperty());
		//}
		
		contentsColumn.setCellValueFactory(new Callback<TableColumn<Contents, Object>, TableCell<Contents, Object>>() {
			@Override
			public TableCell<Contents, Object> call(TableColumn<Contents, Object> param) {
				TableCell<Contents, Object> cell = new TableCell<Contents, Object>() {
					ImageView imageview = new ImageView();
					@Override
					public void updateItem(Object item, boolean empty) {
						if(item!=null){
							HBox box= new HBox();
							VBox vbox = new VBox();
							
							vbox.getChildren().add(new Label(((Contents)item).getContentsValue()));
							
							imageview.setFitHeight(40);
							imageview.setFitHeight(40);
							imageview.setImage(((Contents)item).getContentsImage());
							
							box.getChildren().addAll(imageview,vbox);
							
							setGraphic(box);
						}
					}
				};
				return cell;
			}
		});
		
		typeColumn.setCellValueFactory(cellData -> cellData.getValue().getTypeProperty());
		uploaderColumn.setCellValueFactory(cellData -> cellData.getValue().getUploaderProperty());
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
				contentsUpload.upload();
			}
		});
	}

	// download test
	public void testDownload() {
		/* test를 위한 setting (원래는 알림을 받았을 때 세팅) */
		Contents content1 = new Contents();
		content1.setContentsPKName("1");
		content1.setContentsSize(400);
		content1.setContentsType(Contents.TYPE_STRING);
		content1.setContentsValue("");
		content1.setUploadTime("");
		content1.setUploadUserName("testHee");

		// Contents content1 = new Contents("1");
		// content1.setContentsSize(10000);
		// content1.setContentsType(Contents.TYPE_IMAGE);
		// content1.setFileOriginName("");
		// content1.setUploadTime("");
		// content1.setUploadUserName("testHee");

		Contents content2 = new Contents();
		content2.setContentsPKName("2");
		content2.setContentsSize(80451275);
		content2.setContentsType(Contents.TYPE_FILE);
		content2.setContentsValue("taeyeon.mp3");
		content2.setUploadTime("");
		content2.setUploadUserName("testHee");

		Contents content3 = new Contents();
		content3.setContentsPKName("3");
		content3.setContentsSize(387);
		content3.setContentsType(Contents.TYPE_FILE);
		content3.setContentsValue("bbbb.jpeg");
		content3.setUploadTime("");
		content3.setUploadUserName("testHee");

		// test) 나의 History
		History myhistory = new History();
		myhistory.addContents(content1);
		myhistory.addContents(content2);
		myhistory.addContents(content3);

		// 요청할 데이터의 고유키 값
		String downloadDataPK = "1";

		try {
			downloader.requestDataDownload(downloadDataPK, myhistory);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}
