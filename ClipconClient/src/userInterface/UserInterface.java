package userInterface;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInterface {
	private StartingScene startingScene;
	private GroupJoinScene groupJoinScene;
	private MainScene mainScene;

	private static UserInterface uniqueUserInterface;

	public static UserInterface getIntance() {
		if (uniqueUserInterface == null) {
			uniqueUserInterface = new UserInterface();
		}

		return uniqueUserInterface;
	}
	
	public void handlePopup(Stage stage, String text) {
		Platform.runLater(() -> {
			try {
				Popup popup = new Popup();

				Parent parent;

				parent = FXMLLoader.load(getClass().getResource("/view/popup.fxml"));
				Label lblMessage = (Label) parent.lookup("#lblMessage");
				lblMessage.setText(text);
				lblMessage.setOnMouseClicked(event -> popup.hide());

				// set the popup window position
				popup.setX(stage.getX() + 240);
				popup.setY(stage.getY() + 370);

				popup.getContent().add(parent);
				popup.setAutoHide(true);
				popup.show(stage);
				
				popup.hide();
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		});
	}

}
