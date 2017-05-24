package userInterface;

import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.jfoenix.controls.JFXTabPane;

import application.Main;
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
import javafx.geometry.Rectangle2D;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.Getter;
import lombok.Setter;
import model.Contents;
import model.User;

@Getter
@Setter
public class MainScene implements Initializable {

	private UserInterface ui = UserInterface.getIntance();

	@FXML private JFXTabPane tabPane;

	@FXML private TableView<User>  groupParticipantTable;
	@FXML private TableColumn<User, String> groupPartiNicknameColumn;

	@FXML private TableView<Contents> historyTable;
	@FXML private TableColumn<Contents, String> typeColumn, uploaderColumn;
	@FXML private TableColumn<Contents, Object> contentsColumn;

	@FXML private Button exitBtn, groupKeyCopyBtn, nicknameChangeBtn;
	@FXML private Text nicknameText, groupKeyText;

	private Endpoint endpoint = Endpoint.getIntance();

	private boolean initGroupParticipantFlag;
	private boolean addGroupParticipantFlag;
	private boolean addContentsInHistoryFlag;
	private boolean showStartingViewFlag;
	public static boolean clipboadChangeFlag;

	private ObservableList<User> groupParticipantList;
	private ContentsUpload contentsUpload;
	private DownloadData downloader;

	private ObservableList<Contents> historyList;

	private Label popOverContents = new Label();

	/*
	 * private Notification clipboanoti; private Notification uploadnoti;
	 * private Notification.Notifier clipboardNotifier; private
	 * Notification.Notifier uploadNotifier;
	 */

	private Notification.ClipboadNotifier clipboardNotifier;
	private Notification.UploadNotifier uploadNotifier;

	private hookManager.GlobalKeyboardHook hook;
	
	private Thread clipboardMonitorThread;

	// directory location for uploading and downloading file
	public static final String UPLOAD_TEMP_DIR_LOCATION = "C:\\Program Files\\ClipconUpload";
	public static final String DOWNLOAD_TEMP_DIR_LOCATION = "C:\\Program Files\\ClipconDownload";
	
	private File dirForUpload = new File(MainScene.UPLOAD_TEMP_DIR_LOCATION);
	private File dirForDownload = new File(MainScene.DOWNLOAD_TEMP_DIR_LOCATION);

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ui.setMainScene(this);
		
		// flag initialize
		initGroupParticipantFlag = false;
		addGroupParticipantFlag = false;
		addContentsInHistoryFlag = false;
		showStartingViewFlag = false;
		clipboadChangeFlag = false;
		
		// UI css setting
		tabPane.getStylesheets().add("/resources/mytab.css");
		groupParticipantTable.getStylesheets().add("/resources/myparticipanttable.css");
		historyTable.getStylesheets().add("/resources/myhistorytable.css");

		contentsUpload = new ContentsUpload();
		downloader = new DownloadData(Endpoint.user.getName(), Endpoint.user.getGroup().getPrimaryKey());
		startHookProcess();
		createDirectory();

		clipboardMonitorThread = new Thread(new Runnable() {
			@Override
			public void run() {
				ClipboardController.clipboardMonitor();
			}
		});
		clipboardMonitorThread.start();

		clipboardNotifier = NotifierBuilder.clipboardNotiBuild();
		clipboardNotifier.setNotificationOwner(Main.getPrimaryStage());
		uploadNotifier = NotifierBuilder.uploadNotibuild();
		uploadNotifier.setNotificationOwner(Main.getPrimaryStage());

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
						if (clipboadChangeFlag) {
							clipboadChangeFlag = false;
							showClipboardChangeNoti();
						}
						if (addContentsInHistoryFlag) {
							System.out.println("addContentsInHistoryFlag");
							addContentsInHistoryFlag = false;
							addContentsInHistory();
						}
						if (showStartingViewFlag) {
							showStartingViewFlag = false;
							showStartingView();
							ui.setMainScene(null);
							return;
						}
					}
				});
			}
		}, 50, 50, TimeUnit.MILLISECONDS);

		exitBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				GroupExitDialog.show("그룹에서 나가며, 히스토리가 모두 삭제됩니다. 계속하시겠습니까?");
			}
		});

		groupKeyCopyBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ClipboardController.writeClipboard(new StringSelection(Endpoint.user.getGroup().getPrimaryKey()));
			}
		});
		
		nicknameChangeBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// TODO : nickname 변경
			}
		});

		// Double click event about table column
		historyTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getClickCount() > 1) {
					if (historyTable.getSelectionModel().getSelectedItem() != null)
						getRecentlyContentsInClipboard(historyTable.getSelectionModel().getSelectedItem());
				}
			}
		});

		// Tooltip about table row on mouse hover
		historyTable.setRowFactory((tableView) -> {
			return new TooltipTableRow<Contents>((Contents contents) -> {
				String tooltipMsg = null;

				if (contents.getContentsType().equals(Contents.TYPE_STRING))
					tooltipMsg = contents.getContentsValue() + "\n\nadded : " + contents.getUploadTime();
				else
					tooltipMsg = contents.getContentsValue() + "\n\nsize : " + contents.getContentsConvertedSize()
							+ "\nadded : " + contents.getUploadTime();

				return tooltipMsg;
			});
		});

	}

	/** Initialize group list */
	public void initGroupParticipantList() {
		groupParticipantList.clear();

		nicknameText.setText(Endpoint.user.getName());
		groupKeyText.setText(Endpoint.user.getGroup().getPrimaryKey());
		
		for (int i = 0; i < Endpoint.user.getGroup().getUserList().size(); i++) {
			groupParticipantList.add(Endpoint.user.getGroup().getUserList().get(i));
		}

		addGroupParticipantList();
	}

	/** Add participant in group list */
	public void addGroupParticipantList() {

		groupParticipantTable.setItems(groupParticipantList);

		// Nickname column setting
		groupPartiNicknameColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
		groupPartiNicknameColumn.setCellFactory(new Callback<TableColumn<User, String>, TableCell<User, String>>() {
			@Override
			public TableCell<User, String> call(TableColumn<User, String> column) {
				TableCell<User, String> tc = new TableCell<User, String>() {
					@Override
					public void updateItem(String item, boolean empty) {
						if (item != null) {
							setText(item);
						}
					}
				};
				tc.setAlignment(Pos.CENTER);
				return tc;
			}
		});
	}

	public void showClipboardChangeNoti() {
		Notification clipboanoti = NotificationBuilder.create().build();

		if (clipboardNotifier.getIsShowing()) {
			clipboardNotifier.hidePopUp();
		}
		
		clipboardNotifier.notify(clipboanoti);
		clipboardNotifier.onNotificationPressedProperty();
		clipboardNotifier.setOnNotificationPressed(event -> contentsUpload.upload());
	}

	/** Add content in history list */
	public void addContentsInHistory() {

		historyTable.setItems(historyList);

		Contents content = historyList.get(0);

		// Contents column setting
		contentsColumn.setCellValueFactory(new ContentsValueFactory());
		contentsColumn.setCellFactory(new Callback<TableColumn<Contents, Object>, TableCell<Contents, Object>>() {
			@Override
			public TableCell<Contents, Object> call(TableColumn<Contents, Object> column) {
				return new ContentsValueCell();
			}
		});

		// Type column setting
		typeColumn.setCellValueFactory(cellData -> cellData.getValue().getTypeProperty());
		typeColumn.setCellFactory(new Callback<TableColumn<Contents, String>, TableCell<Contents, String>>() {
			@Override
			public TableCell<Contents, String> call(TableColumn<Contents, String> column) {
				TableCell<Contents, String> tc = new TableCell<Contents, String>() {
					@Override
					public void updateItem(String item, boolean empty) {
						if (item != null) {
							setText(item);
						}
					}
				};
				tc.setAlignment(Pos.CENTER);
				return tc;
			}
		});

		// Uploader column setting
		uploaderColumn.setCellValueFactory(cellData -> cellData.getValue().getUploaderProperty());
		uploaderColumn.setCellFactory(new Callback<TableColumn<Contents, String>, TableCell<Contents, String>>() {
			@Override
			public TableCell<Contents, String> call(TableColumn<Contents, String> column) {
				TableCell<Contents, String> tc = new TableCell<Contents, String>() {
					@Override
					public void updateItem(String item, boolean empty) {
						if (item != null) {
							setText(item);
						}
					}
				};
				tc.setAlignment(Pos.CENTER);
				return tc;
			}
		});

		// Upload notification setting
		
		if (!content.getUploadUserName().equals(Endpoint.user.getName())) {
			Notification uploadnoti;

			String notiTitle = null;
			String notiMsg = null;

			if (content.getContentsType().equals(Contents.TYPE_STRING)) {
				notiTitle = "String from " + content.getUploadUserName();
				if (content.getContentsValue().length() > 30) {
					notiMsg = content.getContentsValue().substring(0, 30);
				} else {
					notiMsg = content.getContentsValue();
				}
				uploadnoti = NotificationBuilder.create().title(notiTitle).message(notiMsg).build();
			} else if (content.getContentsType().equals(Contents.TYPE_IMAGE)) {
				notiTitle = "Image from " + content.getUploadUserName();
				Image resizeImg = content.getContentsImage();
				uploadnoti = NotificationBuilder.create().title(notiTitle).resizeImage(resizeImg).build();
			} else {
				notiTitle = "File from " + content.getUploadUserName();
				if (content.getContentsValue().length() > 30) {
					notiMsg = content.getContentsValue().substring(0, 30);
				} else {
					notiMsg = content.getContentsValue();
				}
				uploadnoti = NotificationBuilder.create().title(notiTitle).message(notiMsg).build();
			}

			uploadNotifier.notify(uploadnoti);
			uploadNotifier.onNotificationPressedProperty();
			uploadNotifier.setOnNotificationPressed(event -> getRecentlyContentsInClipboard(content));
		}
	}

	/** get Recently Contents In Clipboard */
	public void getRecentlyContentsInClipboard(Contents content) {
		String downloadDataPK = content.getContentsPKName(); // recently Contents PK
		try {
			downloader.requestDataDownload(downloadDataPK);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/** Show starting view */
	public void showStartingView() {
		try {
			// TODO : 초기화...
			//clipboardMonitorThread.interrupt();
			hook = null;
			clipboardMonitorThread = null;
			clipboardNotifier = null;
			uploadNotifier = null;
			contentsUpload = null;
			downloader = null;
			removeDirectory();
			
			Parent goBack = FXMLLoader.load(getClass().getResource("/view/StartingView.fxml"));
			Scene scene = new Scene(goBack);
			Stage backStage = Main.getPrimaryStage();

			backStage.setScene(scene);
			backStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Start key Hooking */
	public void startHookProcess() {
		int uploadVitrualKey = KeyEvent.VK_H;
		int downloadVitrualKey = KeyEvent.VK_J;
		boolean CTRL_Key = true;
		boolean ALT_Key = true;
		boolean SHIFT_Key = false;
		boolean WIN_Key = false;

		hook = new hookManager.GlobalKeyboardHook();
		hook.setHotKey(uploadVitrualKey, downloadVitrualKey, ALT_Key, CTRL_Key, SHIFT_Key, WIN_Key);
		hook.startHook();
		// waiting for the event
		hook.addGlobalKeyboardListener(new hookManager.GlobalKeyboardListener() {
			/* Upload HotKey */
			public void onGlobalUploadHotkeysPressed() {
				System.out.println("CTRL + ALT + H was pressed");
				contentsUpload.upload();
			}

			/* Download HotKey */
			public void onGlobalDownloadHotkeysPressed() {
				System.out.println("CTRL + ALT + J was pressed");

				if(historyList.size() > 0) {
					Contents content = historyList.get(0);
					getRecentlyContentsInClipboard(content);
				}
			}
		});
	}

	/** Define toolTip class */
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

	/** Define content column value class */
	public class ContentsValueFactory
			implements Callback<TableColumn.CellDataFeatures<Contents, Object>, ObservableValue<Object>> {
		@SuppressWarnings("unchecked")
		@Override
		public ObservableValue<Object> call(TableColumn.CellDataFeatures<Contents, Object> data) {
			Object value = null;
			if (data.getValue().getContentsType().equals(Contents.TYPE_IMAGE)) {
				value = data.getValue().getContentsImage();
			} else {
				if (data.getValue().getContentsValue().length() > 25) {
					value = data.getValue().getContentsValue().substring(0, 25) + " ...";
				} else {
					value = data.getValue().getContentsValue();
				}
			}
			return (value instanceof ObservableValue) ? (ObservableValue) value : new ReadOnlyObjectWrapper<>(value);
		}
	}

	/** Define content column cell class */
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
					
					double width = ((Image) item).getWidth();
					double height = ((Image) item).getHeight();
					double x = 0;
					double y = height/4;
					
					// define crop in image coordinates:
					Rectangle2D croppedPortion = new Rectangle2D(x, y, width, height/3);

					imageView.setViewport(croppedPortion);
					imageView.setFitWidth(180);
					imageView.setFitHeight(50);
					//imageView.setPreserveRatio(true);
					imageView.setSmooth(true);
					setGraphic(imageView);
				} else {
					setText(null);
					setGraphic(null);
				}
			}
		}
	}

	/**
	 * Create a temporary directory to save the imageFile, file when downloading
	 * from server to save Zip file when uploading multiple file
	 * 
	 * @param directoryName
	 *            The name of the directory you want to create
	 */
	private void createDirectory() {
		if (!dirForUpload.exists()) {
			dirForUpload.mkdir(); // Create Directory
			System.out.println("------------------------------------ create dir for Upload ");
		}
		if (!dirForDownload.exists()) {
			dirForDownload.mkdir(); // Create Directory
			System.out.println("------------------------------------ create dir for Download");
		}
	}
	
	private void removeDirectory() {
		dirForUpload.delete();
		dirForDownload.delete();
	}
}
