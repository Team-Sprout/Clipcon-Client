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
import java.util.function.Function;

import javax.websocket.EncodeException;

import org.controlsfx.control.PopOver;

import contentsTransfer.ContentsUpload;
import contentsTransfer.DownloadData;
import controller.ClipboardController;
import controller.Endpoint;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.Getter;
import lombok.Setter;
import model.Contents;
import model.History;
import model.Message;
import model.Notification;
import model.Notification.Notifier;
import model.NotificationBuilder;
import model.NotifierBuilder;
import model.User;

@Getter
@Setter
public class MainScene implements Initializable {

	private UserInterface ui = UserInterface.getIntance();

	@FXML private TableView<User> groupParticipantTable;
	@FXML private TableColumn<User, String> groupPartiNicknameColumn;
	
	@FXML private TableView<Contents> historyTable;
	@FXML private TableColumn<Contents, String> typeColumn, uploaderColumn;
	@FXML private TableColumn<Contents, Object> contentsColumn;
	
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
	
	private PopOver popOver = new PopOver();
	private Label popOverContents = new Label();

	private Notification noti;
	private Notification.Notifier notifier;
	
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
		
		historyTable.getStylesheets().add("style.css");
		
		//popOver.setTitle("Contents Vlaue");
        popOver.setAutoHide(true);
        popOver.setAutoFix(true);
        popOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_RIGHT);
        popOver.setHeaderAlwaysVisible(true);
        popOver.setDetachable(true);
        popOver.setDetached(true);
        popOver.setCornerRadius(4);
        
        Notifier.INSTANCE.setAlwaysOnTop(false);
		notifier = NotifierBuilder.create().popupLocation(Pos.BOTTOM_RIGHT).styleSheet("/resource/mynotification.css").build();

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
		
//		historyTable.setOnMouseClicked((MouseEvent event) -> {
//	        if(event.getButton().equals(MouseButton.PRIMARY)){
//	            popOverContents.setText("\n  " + historyTable.getSelectionModel().getSelectedItem().getContentsValue() +
//	            		"\n\n  size : " + historyTable.getSelectionModel().getSelectedItem().getContentsSize() +
//	            		"\n  added : " + historyTable.getSelectionModel().getSelectedItem().getUploadTime() + "  \n ");
//	            popOver.setContentNode(popOverContents);
//	            popOver.show(historyTable);
//    	        //((Parent) popOver.getSkin().getNode()).getStylesheets().add(getClass().getResource("PopOver.css").toExternalForm());
//	        }
//	    });
        
        historyTable.setRowFactory((tableView) -> {
            return new TooltipTableRow<Contents>((Contents contents) -> {
            	return contents.getContentsValue() + "\n\nsize : " + contents.getContentsSize() + "\nadded : " + contents.getUploadTime();
            });
      });
		

	}

	public void initGroupParticipantList() {

		groupKeyText.setText(Endpoint.user.getGroup().getPrimaryKey());

		for (int i = 0; i < Endpoint.user.getGroup().getUserList().size(); i++) {
			groupParticipantList.add(Endpoint.user.getGroup().getUserList().get(i));
		}

		groupParticipantTable.setItems(groupParticipantList);
		groupPartiNicknameColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
		groupPartiNicknameColumn.setCellFactory(new Callback<TableColumn<User, String>, TableCell<User, String>>() {
			@Override
			public TableCell<User, String> call(TableColumn<User, String> column) {
				TableCell<User, String> tc = new TableCell<User, String>() {
					@Override
                    public void updateItem(String item, boolean empty) {
                        if (item != null){
                            setText(item);
                        }
                    }
				};
				tc.setAlignment(Pos.CENTER);
				return tc;
			}
		});
	}

	public void addGroupParticipantList() {

		groupParticipantTable.setItems(groupParticipantList);
		
		groupPartiNicknameColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
		groupPartiNicknameColumn.setCellFactory(new Callback<TableColumn<User, String>, TableCell<User, String>>() {
			@Override
			public TableCell<User, String> call(TableColumn<User, String> column) {
				TableCell<User, String> tc = new TableCell<User, String>() {
					@Override
                    public void updateItem(String item, boolean empty) {
                        if (item != null){
                            setText(item);
                        }
                    }
				};
				tc.setAlignment(Pos.CENTER);
				return tc;
			}
		});
	}
	
	public void addContentsInHistory() {
		historyTable.setItems(historyList);
		
		Contents content = historyList.get(historyList.size()-1);
		
		contentsColumn.setCellValueFactory(new ContentsValueFactory());
		contentsColumn.setCellFactory(new Callback<TableColumn<Contents, Object>, TableCell<Contents, Object>>() {
			@Override
			public TableCell<Contents, Object> call(TableColumn<Contents, Object> column) {
				return new ContentsValueCell();
			}
		});
		
		typeColumn.setCellValueFactory(cellData -> cellData.getValue().getTypeProperty());
		typeColumn.setCellFactory(new Callback<TableColumn<Contents, String>, TableCell<Contents, String>>() {
			@Override
			public TableCell<Contents, String> call(TableColumn<Contents, String> column) {
				TableCell<Contents, String> tc = new TableCell<Contents, String>() {
					@Override
                    public void updateItem(String item, boolean empty) {
                        if (item != null){
                            setText(item);
                        }
                    }
				};
				tc.setAlignment(Pos.CENTER);
				return tc;
			}
		});
		
		uploaderColumn.setCellValueFactory(cellData -> cellData.getValue().getUploaderProperty());
		uploaderColumn.setCellFactory(new Callback<TableColumn<Contents, String>, TableCell<Contents, String>>() {
			@Override
			public TableCell<Contents, String> call(TableColumn<Contents, String> column) {
				TableCell<Contents, String> tc = new TableCell<Contents, String>() {
					@Override
                    public void updateItem(String item, boolean empty) {
                        if (item != null){
                            setText(item);
                        }
                    }
				};
				tc.setAlignment(Pos.CENTER);
				return tc;
			}
		});
		
		String notiMsg = content.getContentsType() + " Content";
		
		noti = NotificationBuilder.create().title("Content Upload Notification").message(notiMsg).image(Notification.INFO_ICON).build();
		
		notifier.notify(noti);
		notifier.setOnNotificationPressed(event -> System.out.println("Notification pressed:"));
		// [TODO] noti evnet : download
		
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

	public class TooltipTableRow<T> extends TableRow<T> {

		private Function<T, String> toolTipStringFunction;

		public TooltipTableRow(Function<T, String> toolTipStringFunction) {
			this.toolTipStringFunction = toolTipStringFunction;
		}

		@Override
		protected void updateItem(T item, boolean empty) {
			super.updateItem(item, empty);
			if (item == null) {
				setTooltip(null);
			} else {
				Tooltip tooltip = new Tooltip(toolTipStringFunction.apply(item));
				setTooltip(tooltip);
			}
		}
	}
	
	public class ContentsValueFactory implements Callback<TableColumn.CellDataFeatures<Contents, Object>, ObservableValue<Object>> {
	    @SuppressWarnings("unchecked")
	    @Override
	    public ObservableValue<Object> call(TableColumn.CellDataFeatures<Contents, Object> data) {
	    	Object value = null;
	    	if(data.getValue().getContentsType().equals(Contents.TYPE_IMAGE)) {
	    		value = data.getValue().getContentsImage();
	    	}
	    	else {
	    		if(data.getValue().getContentsValue().length() > 25) {
	    			value = data.getValue().getContentsValue().substring(0, 25) + " ...";
	    		}
	    		else {
	    			value = data.getValue().getContentsValue();
	    		}
	    	}
	        return (value instanceof ObservableValue) ? (ObservableValue) value : new ReadOnlyObjectWrapper<>(value);
	    }
	}

	public class ContentsValueCell extends TableCell<Contents, Object> {
		@Override
		protected void updateItem(Object item, boolean empty) {
			super.updateItem(item, empty);
			
			this.setAlignment(Pos.CENTER);
			
			if (item != null) {
				if (item instanceof String) {
					setText((String) item);
					setGraphic(null);
				} else if (item instanceof Image) {
					setText(null);
					ImageView imageView = new ImageView((Image) item);
					imageView.setFitWidth(50);
					imageView.setPreserveRatio(true);
					imageView.setSmooth(true);
					setGraphic(imageView);
				} else {
					setText(null);
					setGraphic(null);
				}
			}
		}
	}
}
