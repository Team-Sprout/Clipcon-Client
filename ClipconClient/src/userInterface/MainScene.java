package userInterface;

import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.websocket.EncodeException;

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
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.Getter;
import lombok.Setter;
import model.Contents;
import model.Message;
import model.Notification;
import model.NotificationBuilder;
import model.NotifierBuilder;
import model.User;

@Getter
@Setter
public class MainScene implements Initializable {

	private UserInterface ui = UserInterface.getIntance();

	@FXML
	private TableView<User> groupParticipantTable;
	@FXML
	private TableColumn<User, String> groupPartiNicknameColumn;

	@FXML
	private TableView<Contents> historyTable;
	@FXML
	private TableColumn<Contents, String> typeColumn, uploaderColumn;
	@FXML
	private TableColumn<Contents, Object> contentsColumn;

	@FXML
	private Button exitBtn, groupKeyCopyBtn;
	@FXML
	private Text groupKeyText;

	private static ActionEvent event;
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

	private Thread clipboardMonitorThread;

	// directory location for uploading and downloading file
	public static final String UPLOAD_TEMP_DIR_LOCATION = "C:\\Program Files\\ClipconUpload";
	public static final String DOWNLOAD_TEMP_DIR_LOCATION = "C:\\Program Files\\ClipconDownload";

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ui.setMainScene(this);
		initGroupParticipantFlag = false;
		addGroupParticipantFlag = false;
		addContentsInHistoryFlag = false;
		showStartingViewFlag = false;
		clipboadChangeFlag = false;

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

				// Send REQUEST_EXIT_GROUP Message To Server
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

		Contents content = historyList.get(historyList.size() - 1);

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

		Notification uploadnoti;

		String notiMsg = null;

		if (content.getContentsType().equals(Contents.TYPE_IMAGE)) {
			notiMsg = content.getContentsType() + " Content Upload";
			Image resizeImg = content.getContentsImage();
			uploadnoti = NotificationBuilder.create().title("Content Upload Notification").resizeImage(resizeImg)
					.message(notiMsg).image(Notification.INFO_ICON).build();
		} else {
			if (content.getContentsValue().length() > 10) {
				notiMsg = content.getContentsType() + " Content Upload" + " : "
						+ content.getContentsValue().substring(0, 10);
			} else {
				notiMsg = content.getContentsType() + " Content Upload" + " : " + content.getContentsValue();
			}
			uploadnoti = NotificationBuilder.create().title("Content Upload Notification").message(notiMsg)
					.image(Notification.INFO_ICON).build();
		}

		uploadNotifier.notify(uploadnoti);
		uploadNotifier.onNotificationPressedProperty();
		uploadNotifier.setOnNotificationPressed(event -> getRecentlyContentsInClipboard(content));
	}

	/** get Recently Contents In Clipboard */
	public void getRecentlyContentsInClipboard(Contents content) {
		String downloadDataPK = content.getContentsPKName(); // recently
																// Contents PK
		try {
			downloader.requestDataDownload(downloadDataPK);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/** Show starting view */
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

	/** Start key Hooking */
	public void startHookProcess() {
		hookManager.GlobalKeyboardHook hook = new hookManager.GlobalKeyboardHook();
		int uploadVitrualKey = KeyEvent.VK_H;
		int downloadVitrualKey = KeyEvent.VK_J;
		boolean CTRL_Key = true;
		boolean ALT_Key = true;
		boolean SHIFT_Key = false;
		boolean WIN_Key = false;

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

				Contents content = historyList.get(historyList.size() - 1);
				getRecentlyContentsInClipboard(content);
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

	/**
	 * Create a temporary directory to save the imageFile, file when downloading
	 * from server to save Zip file when uploading multiple file
	 * 
	 * @param directoryName
	 *            The name of the directory you want to create
	 */
	private void createDirectory() {
		File dirForUpload = new File(MainScene.UPLOAD_TEMP_DIR_LOCATION);
		File dirForDownload = new File(MainScene.DOWNLOAD_TEMP_DIR_LOCATION);

		if (!dirForUpload.exists()) {
			dirForUpload.mkdir(); // Create Directory
			System.out.println("------------------------------------ create dir for Upload ");
		}
		if (!dirForDownload.exists()) {
			dirForDownload.mkdir(); // Create Directory
			System.out.println("------------------------------------ create dir for Download");
		}
	}
}
