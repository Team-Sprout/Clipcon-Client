package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
	private static Stage primaryStage;

	@Override
	public void start(Stage primaryStage) throws Exception {
		setPrimaryStage(primaryStage);
		Main.primaryStage.setTitle("ClipCon");
		Main.primaryStage.getIcons().add(new Image("resources/Logo.png"));
		Parent root = FXMLLoader.load(getClass().getResource("/view/StartingView.fxml"));
		Scene scene = new Scene(root);
//		primaryStage.setMinWidth(340);
//		primaryStage.setMinHeight(480);
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
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
