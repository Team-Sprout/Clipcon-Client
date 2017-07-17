package userInterface;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import contentsTransfer.DownloadData;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Text;

public class ProgressBarScene implements Initializable {
	
	private UserInterface ui = UserInterface.getInstance();

	@FXML private Text text;
	@FXML private ProgressBar progressBar ;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ui.setProgressBarScene(this);
		
		if(DownloadData.isDownloading) {
			text.setText("Downloading");
		}
		
		progressBar.setProgress(0);
	}
	
	public void setProgeress(double value, long uploaded, long fileLength) {
		double uploadedMB = ((uploaded / 1024.0) / 1024.0);
		double fileLengthMB = ((fileLength / 1024.0) / 1024.0);
		
		DecimalFormat dec = new DecimalFormat("0.0");
		
		Platform.runLater(() -> {
			text.setText("Uploading " + (int)value + "% (" + dec.format(uploadedMB) + " / " + dec.format(fileLengthMB) + " MB)");
			progressBar.setProgress(value*0.01);
        });
	}
	
	public void completeProgress() {
		Platform.runLater(() -> {
			text.setText("Complete!");
			progressBar.setProgress(1);
        });
	}
}
