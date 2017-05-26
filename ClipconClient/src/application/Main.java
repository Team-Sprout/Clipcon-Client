package application;

import controller.ClipboardController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import userInterface.TrayIconManager;

public class Main extends Application {
	private static Stage primaryStage;
//	public static final String SERVER_ADDR = "delf.gonetis.com";
	public static final String SERVER_ADDR = "223.194.159.172";
	public static boolean isInMainScene = false;

	@Override
	public void start(Stage primaryStage) throws Exception {
		setPrimaryStage(primaryStage);
		
		TrayIconManager tray = new TrayIconManager();
		tray.addTrayIconInSystemTray();
		
		Main.primaryStage.setTitle("ClipCon");
		Main.primaryStage.getIcons().add(new javafx.scene.image.Image("resources/Logo.png"));
		Parent root = FXMLLoader.load(getClass().getResource("/view/StartingView.fxml"));
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent t) {
				System.out.println("Stage is closing");
				if (isInMainScene) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							Platform.setImplicitExit(false);
						}
					});
				} else {
					System.exit(0);
				}
			}
		});
		
		System.loadLibrary("KeyHooking");
		System.out.println("KeyHooking.dll file loaded");
		
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
