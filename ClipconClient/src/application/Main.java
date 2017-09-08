package application;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import javax.websocket.EncodeException;

import controller.ClipboardController;
import controller.Endpoint;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.Message;
import userInterface.FailDialog;
import userInterface.TrayIconManager;

public class Main extends Application {
	private static Stage primaryStage;

	public static final String SERVER_PORT = "8080";
	// public static final String SERVER_ADDR = "113.198.84.53";
	public static final String SERVER_ADDR = "delf.gonetis.com";

	public static final String SERVER_URI_PART = SERVER_ADDR + ":" + SERVER_PORT + "/";
	
	public final String CLIPCON_VERSION = "1.1";

	public static final String LOCK_FILE_LOCATION = System.getProperty("user.dir") + File.separator + "ClipCon.lock";
	private File lockFile = new File(Main.LOCK_FILE_LOCATION);

	public static boolean isInMainScene = false;

	private Endpoint endpoint = Endpoint.getInstance();

	/* dll load */
	static {
		try {
			System.load(System.getProperty("user.dir") + File.separator + "keyHooking.dll");
		} catch (UnsatisfiedLinkError e) {
			FailDialog.show("dll load error");
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
//		Message confirmVersionMsg = new Message().setType(Message.REQUEST_CONFIRM_VERSION);
//		confirmVersionMsg.add(Message.CLIPCON_VERSION, CLIPCON_VERSION);
//		
//		try {
//			endpoint.sendMessage(confirmVersionMsg);
//		} catch (IOException | EncodeException e) {
//			e.printStackTrace();
//		}
		
		@SuppressWarnings("resource")
		FileChannel channel = new RandomAccessFile(lockFile, "rw").getChannel();

		// try lock
		FileLock lock = channel.tryLock();

		// already obtain lock in other JVM
		if (lock == null) {
			channel.close();
			System.exit(0);
		}

		setPrimaryStage(primaryStage);

		TrayIconManager tray = new TrayIconManager();
		tray.addTrayIconInSystemTray();

		primaryStage.setTitle("ClipCon");
		primaryStage.getIcons().add(new javafx.scene.image.Image("resources/Logo.png"));
		Parent root = FXMLLoader.load(getClass().getResource("/view/StartingView.fxml"));
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setAlwaysOnTop(false);
		primaryStage.setResizable(true);
		primaryStage.show();

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent t) {
				if (isInMainScene) {
					Platform.setImplicitExit(false);
				} else {
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

	public static void main(String[] args) {
		launch(args);
	}
}
