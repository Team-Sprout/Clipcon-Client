package userInterface;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgressBarScene implements Initializable {
	
	private UserInterface ui = UserInterface.getIntance();

	@FXML private Text text;
	@FXML private ProgressBar progressBar ;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
		
		//text.setText("uploading...");
		//text.setText("downloading...");
	}

}
