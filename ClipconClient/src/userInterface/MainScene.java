package userInterface;

import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Function;

import com.jfoenix.controls.JFXTabPane;

import application.Main;
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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import lombok.Getter;
import model.Contents;
import model.User;
import retrofitContentsTransfer.ContentsUpload;
import retrofitContentsTransfer.RetrofitDownloadData;

public class MainScene implements Initializable {

	private UserInterface ui = UserInterface.getInstance();

	@FXML
	private JFXTabPane tabPane;

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
	private Button exitBtn, settingBtn, groupKeyCopyBtn, nicknameChangeBtn;
	@FXML
	private Text nicknameText, groupKeyText;
	@FXML
	private Hyperlink homepageHL, bugreportHL;

	private Stage BugReportStage;
	private Stage SettingStage;
	private Stage nicknameChangeStage;
	private Stage progressBarStage;
	@Getter
	private Stage [] progressBarStageArray = new Stage[10];

	@Getter
	private ObservableList<User> groupParticipantList;
	@Getter
	private ObservableList<Contents> historyList;
	
	private Dialog dialog;
	
	private ContentsUpload contentsUpload;
	private Thread uploadThread;
	private RetrofitDownloadData downloader;
	private Thread downloadThread;

	private Notification.ClipboadNotifier clipboardNotifier;
	private Notification.UploadNotifier uploadNotifier;

	private hookManager.GlobalKeyboardHook hook;

	// directory location for uploading and downloading file

	public static final String UPLOAD_TEMP_DIR_LOCATION = System.getProperty("user.dir") + File.separator + "ClipconUpload";
	public static final String DOWNLOAD_TEMP_DIR_LOCATION = System.getProperty("user.dir") + File.separator + "ClipconDownload";

	private File dirForUpload = new File(MainScene.UPLOAD_TEMP_DIR_LOCATION);
	private File dirForDownload = new File(MainScene.DOWNLOAD_TEMP_DIR_LOCATION);

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ui.setMainScene(this);

		Main.isInMainScene = true;

		// UI css setting
		tabPane.getStylesheets().add("/resources/mytab.css");
		groupParticipantTable.getStylesheets().add("/resources/myparticipanttable.css");
		historyTable.getStylesheets().add("/resources/myhistorytable.css");

		contentsUpload = new ContentsUpload();
		downloader = new RetrofitDownloadData(Endpoint.user.getName(), Endpoint.user.getGroup().getPrimaryKey());

		startHookProcess();
		createDirectory();

		clipboardNotifier = NotifierBuilder.clipboardNotiBuild();
		// clipboardNotifier.setNotificationOwner(Main.getPrimaryStage());
		uploadNotifier = NotifierBuilder.uploadNotibuild();
		// uploadNotifier.setNotificationOwner(Main.getPrimaryStage());

		groupParticipantList = FXCollections.observableArrayList();
		historyList = FXCollections.observableArrayList();

		exitBtn.setTooltip(new Tooltip("Exit"));
		exitBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				dialog = new GroupExitDialog("그룹에서 나가며, 히스토리가 모두 삭제됩니다. 계속하시겠습니까?");
				dialog.showAndWait();
			}
		});

		settingBtn.setTooltip(new Tooltip("Setting"));
		settingBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					Parent toSetting = FXMLLoader.load(getClass().getResource("/view/SettingView.fxml"));
					Scene scene = new Scene(toSetting);
					SettingStage = new Stage();

					SettingStage.setScene(scene);
					SettingStage.initStyle(StageStyle.TRANSPARENT);
					SettingStage.initOwner(Main.getPrimaryStage());
					SettingStage.initModality(Modality.WINDOW_MODAL);
					SettingStage.show();
					SettingStage.setX(Main.getPrimaryStage().getX() + Main.getPrimaryStage().getWidth() / 2 - SettingStage.getWidth() / 2);
					SettingStage.setY(Main.getPrimaryStage().getY() + Main.getPrimaryStage().getHeight() / 2 - SettingStage.getHeight() / 2);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		groupKeyCopyBtn.setTooltip(new Tooltip("Copy"));
		groupKeyCopyBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ClipboardController.writeClipboard(new StringSelection(Endpoint.user.getGroup().getPrimaryKey()));
			}
		});

		nicknameChangeBtn.setTooltip(new Tooltip("Change"));
		nicknameChangeBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					Parent toNicknameChange = FXMLLoader.load(getClass().getResource("/view/NicknameChangeView.fxml"));
					Scene scene = new Scene(toNicknameChange);
					nicknameChangeStage = new Stage();

					nicknameChangeStage.setScene(scene);
					nicknameChangeStage.initStyle(StageStyle.TRANSPARENT);
					nicknameChangeStage.initOwner(Main.getPrimaryStage());
					nicknameChangeStage.initModality(Modality.WINDOW_MODAL);
					nicknameChangeStage.show();
					nicknameChangeStage.setX(Main.getPrimaryStage().getX() + Main.getPrimaryStage().getWidth() / 2 - nicknameChangeStage.getWidth() / 2);
					nicknameChangeStage.setY(Main.getPrimaryStage().getY() + Main.getPrimaryStage().getHeight() / 2 - nicknameChangeStage.getHeight() / 2);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		homepageHL.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		        Main.getHostService().showDocument("http://113.198.84.53/globalclipboard/download");
		    }
		});
		
		bugreportHL.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		        Main.getHostService().showDocument("http://113.198.84.53/globalclipboard/bugreport");
		    }
		});

		// Double click event about table column
		historyTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getClickCount() > 1) {
					if (historyTable.getSelectionModel().getSelectedItem() != null)
						getContentsInClipboard(historyTable.getSelectionModel().getSelectedItem());
				}
			}
		});

		// Tooltip about table row on mouse hover
		historyTable.setRowFactory((tableView) -> {
			return new TooltipTableRow<Contents>((Contents contents) -> {
				String tooltipMsg = null;

				if (contents.getContentsType().equals(Contents.TYPE_STRING))
					tooltipMsg = contents.getContentsValue() + "\n\nadded : " + contents.getUploadTime();
				else if (contents.getContentsType().equals(Contents.TYPE_MULTIPLE_FILE))
					tooltipMsg = contents.getMultipleFileListInfo() + "\n\nsize : " + contents.getContentsConvertedSize() + "\nadded : " + contents.getUploadTime();
				else
					tooltipMsg = contents.getContentsValue() + "\n\nsize : " + contents.getContentsConvertedSize() + "\nadded : " + contents.getUploadTime();

				return tooltipMsg;
			});
		});

	}

	/** Initialize group list */
	public void initGroupParticipantList() {
		Platform.runLater(() -> {
			groupParticipantList.clear();

			nicknameText.setText(Endpoint.user.getName());
			groupKeyText.setText(Endpoint.user.getGroup().getPrimaryKey());

			for (int i = 0; i < Endpoint.user.getGroup().getUserList().size(); i++) {
				groupParticipantList.add(Endpoint.user.getGroup().getUserList().get(i));
			}

			addGroupParticipantList();
		});
	}

	/** Add participant in group list */
	public void addGroupParticipantList() {
		Platform.runLater(() -> {
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
		});
	}

	public void showClipboardChangeNoti() {
		if (SettingScene.clipboardMonitorNotiFlag) {
			Platform.runLater(() -> {
				Notification clipboanoti = NotificationBuilder.create().build();

				if (clipboardNotifier.getIsShowing()) {
					clipboardNotifier.hidePopUp();
				}

				clipboardNotifier.notify(clipboanoti);
				clipboardNotifier.onNotificationPressedProperty();
				clipboardNotifier.setOnNotificationPressed(event -> upload());
			});
		}
	}

	public void upload() {
		uploadThread = new Thread(new Runnable() {
			@Override
			public void run() {
				contentsUpload.upload();
			}
		});
		uploadThread.start();
	}

	/** Add content in history list */
	public void addContentsInHistory() {
		Platform.runLater(() -> {
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
			if (!content.getUploadUserName().equals(Endpoint.user.getName()) && SettingScene.uploadNotiFlag) {
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
				uploadNotifier.setOnNotificationPressed(event -> getContentsInClipboard(content));
			}
		});
	}

	/** get Selected or Recently Contents In Clipboard */
	public void getContentsInClipboard(Contents content) {
		String downloadDataPK = content.getContentsPKName(); // Selected or Recently Contents PK

		downloadThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					downloader.requestDataDownload(downloadDataPK);

				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		});
		downloadThread.start();

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
	public class ContentsValueFactory implements Callback<TableColumn.CellDataFeatures<Contents, Object>, ObservableValue<Object>> {
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
					double y = height / 4;

					// define crop in image coordinates:
					Rectangle2D croppedPortion = new Rectangle2D(x, y, width, height / 3);

					imageView.setViewport(croppedPortion);
					imageView.setFitWidth(180);
					imageView.setFitHeight(50);
					// imageView.setPreserveRatio(true);
					imageView.setSmooth(true);
					setGraphic(imageView);
				} else {
					setText(null);
					setGraphic(null);
				}
			}
		}
	}

	/** Start key Hooking */
	public void startHookProcess() {
		int uploadVitrualKey = KeyEvent.VK_C;
		int downloadVitrualKey = KeyEvent.VK_V;
		boolean CTRL_Key = true;
		boolean ALT_Key = false;
		boolean SHIFT_Key = true;
		boolean WIN_Key = false;

		hook = new hookManager.GlobalKeyboardHook();
		hook.setHotKey(uploadVitrualKey, downloadVitrualKey, ALT_Key, CTRL_Key, SHIFT_Key, WIN_Key);
		hook.startHook();
		// waiting for the event
		hook.addGlobalKeyboardListener(new hookManager.GlobalKeyboardListener() {
			/* Upload HotKey */
			public void onGlobalUploadHotkeysPressed() {
				if (clipboardNotifier.getIsShowing()) {
					Platform.runLater(() -> {
						clipboardNotifier.hidePopUp();
					});
				}
				upload();
			}

			/* Download HotKey */
			public void onGlobalDownloadHotkeysPressed() {
				if (historyList.size() > 0) {
					Contents content = historyList.get(0);
					getContentsInClipboard(content);
				}
			}
		});
	}

	public void showProgressBar() {
		Platform.runLater(() -> {
			try {
				Parent toProgressBar = FXMLLoader.load(getClass().getResource("/view/ProgressBar.fxml"));
				Scene scene = new Scene(toProgressBar);
				scene.getStylesheets().add("resources/myprogressbar.css");
				progressBarStage = new Stage();
				
				int progressBarIndex = ProgressBarScene.getIndex();

				progressBarStage.initStyle(StageStyle.TRANSPARENT);
				progressBarStage.setScene(scene);
				progressBarStage.getIcons().add(new javafx.scene.image.Image("resources/Logo.png"));
				progressBarStage.initModality(Modality.WINDOW_MODAL);
				progressBarStage.show();
				// progressBarStage.setX(Main.getPrimaryStage().getX() + Main.getPrimaryStage().getWidth()/2 - progressBarStage.getWidth()/2);
				// progressBarStage.setY(Main.getPrimaryStage().getY() + Main.getPrimaryStage().getHeight()/2 - progressBarStage.getHeight()/2);
				progressBarStage.setX(Screen.getPrimary().getBounds().getWidth() - progressBarStage.getWidth() - 10);
				progressBarStage.setY(Screen.getPrimary().getBounds().getHeight() - progressBarStage.getHeight() - 50 - progressBarIndex * 55);
				
				progressBarStageArray[progressBarIndex] = progressBarStage;
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/** Show starting view */
	public void showStartingView() {
		ui.setMainScene(null);
		Main.isInMainScene = false;

		hook.stopHook();
		clipboardNotifier = null;
		uploadNotifier = null;
		contentsUpload = null;
		uploadThread = null;
		downloader = null;
		downloadThread = null;
		removeDirectory();

		Platform.runLater(() -> {
			try {
				Parent goBack = FXMLLoader.load(getClass().getResource("/view/StartingView.fxml"));
				Scene scene = new Scene(goBack);
				Stage backStage = Main.getPrimaryStage();

				backStage.setScene(scene);
				backStage.show();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public void closeProgressBarStage(int index) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Platform.runLater(() -> {
			progressBarStageArray[index].close();
			
			ProgressBarScene.setNumber(ProgressBarScene.getNumber() - 1);
			if(ProgressBarScene.getIndex() == 0) {
				ProgressBarScene.setNumber(-1);
			}
			progressBarStageArray[index] = null;
		});
		
	}

	public void closeNicknameChangeStage() {
		Platform.runLater(() -> {
			nicknameChangeStage.close();
		});
	}
	
	public void closeBugReportStage() {
		Platform.runLater(() -> {
			BugReportStage.close();
		});
	}

	public void closeSettingStage() {
		Platform.runLater(() -> {
			SettingStage.close();
		});
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
		}
		if (!dirForDownload.exists()) {
			dirForDownload.mkdir(); // Create Directory
		}
	}

	private void removeDirectory() {
		dirForUpload.delete();
		dirForDownload.delete();
	}

}
