package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.websocket.EncodeException;

import authority.Elevator;
import controller.ClipboardController;
import controller.Endpoint;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.Message;
import userInterface.Dialog;
import userInterface.PlainDialog;
import userInterface.TrayIconManager;

public class Main extends Application {
	private static Stage primaryStage;

	public static final String SERVER_PORT = "80";
	public static final String SERVER_ADDR = "113.198.84.53";
	// public static final String SERVER_ADDR = "delf.gonetis.com";
	// public static final String SERVER_ADDR = "223.194.156.74";

	public static final String SERVER_URI_PART = SERVER_ADDR + ":" + SERVER_PORT + "/";

	public final String CLIPCON_VERSION = "1.1";

	public static final String LOCK_FILE_LOCATION = System.getProperty("user.dir") + File.separator + "ClipCon.lock";
	private static File lockFile;

	public static boolean isInMainScene = false;

	private Endpoint endpoint = Endpoint.getInstance();

	private static HostServices hostService;

	@Override
	public void start(Stage primaryStage) throws Exception {

		hostService = getHostServices();

		// version check
		Message confirmVersionMsg = new Message().setType(Message.REQUEST_CONFIRM_VERSION);
		confirmVersionMsg.add(Message.CLIPCON_VERSION, CLIPCON_VERSION);

		try {
			endpoint.sendMessage(confirmVersionMsg);
		} catch (IOException | EncodeException e) {
			e.printStackTrace();
		}
		try {
			System.load(System.getProperty("user.dir") + File.separator + "keyHooking.dll");
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			Platform.runLater(() -> {
//				PlainDialog.show("dll load error");
				Dialog plainDialog = new PlainDialog("dll load error", true);
				plainDialog.showAndWait();
			});
		}

		setPrimaryStage(primaryStage);

		TrayIconManager tray = new TrayIconManager();
		tray.addTrayIconInSystemTray();

		primaryStage.setTitle("ClipCon");
		primaryStage.getIcons().add(new javafx.scene.image.Image("resources/Logo.png"));
		Parent root = FXMLLoader.load(getClass().getResource("/view/StartingView.fxml"));
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		// primaryStage.setAlwaysOnTop(false);
		primaryStage.setResizable(true);
		primaryStage.show();

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent t) {
				if (isInMainScene) {
					Platform.setImplicitExit(false);
				}
				else {
					Message exitProgramMsg = new Message().setType(Message.REQUEST_EXIT_PROGRAM);
					try {
						endpoint.sendMessage(exitProgramMsg);
					} catch (IOException | EncodeException e) {
						e.printStackTrace();
					}
					System.exit(0);
				}
			}
		});

		Thread clipboardMonitorThread = new Thread(new Runnable() {
			@Override
			public void run() {
				ClipboardController.clipboardMonitor();
			}
		});
		clipboardMonitorThread.start();
	}

	private void setPrimaryStage(Stage stage) {
		Main.primaryStage = stage;
	}

	static public Stage getPrimaryStage() {
		return Main.primaryStage;
	}

	static public HostServices getHostService() {
		return Main.hostService;
	}

	@SuppressWarnings("resource")
	public static void fileLock() throws FileNotFoundException {
		lockFile = new File(Main.LOCK_FILE_LOCATION);

		FileChannel channel;
		channel = new RandomAccessFile(lockFile, "rw").getChannel();

		// try lock
		FileLock lock;
		try {
			lock = channel.tryLock();
			// already obtain lock in other JVM
			if (lock == null) {
				channel.close();
				System.exit(0);
			}
		} catch (IOException e) {
			// error handle
		}
	}

	public static void main(String[] args) {
		try {
			fileLock();
			launch(args);
		} catch (FileNotFoundException e) {
			System.out.println(System.getProperty("user.dir") + File.separator + "ClipCon.exe");
			runCommandAsAdmin("\"" + System.getProperty("user.dir") + File.separator + "ClipCon.exe" + "\""); // 이러면되나
			System.exit(0);
		}
	}

	/**
	 * for test 17.09.13
	 * 
	 * @return Current Time YYYY-MM-DD HH:MM:SS
	 */
	public static String getTime() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd a hh:mm:ss");

		return sdf.format(date).toString();
	}

	/** run a program at CMD */
	public static void runCommandAsAdmin(String command) {
		Elevator.executeAsAdmin("c:\\windows\\system32\\cmd.exe", "/C " + command);
	}
}
