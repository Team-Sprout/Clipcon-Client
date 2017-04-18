package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {

		Parent root = FXMLLoader.load(getClass().getResource("/view/StartingView.fxml"));
		System.out.println("root");
		Scene scene = new Scene(root);
		System.out.println("new scene");
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		System.out.println("css");
		primaryStage.setScene(scene);
		System.out.println("setScene");
		primaryStage.show();
		System.out.println("show");
	}

	public static void main(String[] args) {
		launch(args);
	}

}
