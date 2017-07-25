package userInterface;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Text;
import retrofitContentsTransfer.RetrofitDownloadData;

public class ProgressBarScene implements Initializable {
	
	private UserInterface ui = UserInterface.getInstance();

	@FXML private Text text;
	@FXML private ProgressBar progressBar ;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ui.setProgressBarScene(this);
		
		if(RetrofitDownloadData.isDownloading) {
			text.setText("Downloading");
		}
		
		progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
	}
	
	public void setProgeress(double value, long onGoing, long fileLength) {
		double onGoingMB = ((onGoing / 1024.0) / 1024.0);
		double fileLengthMB = ((fileLength / 1024.0) / 1024.0);
		
		DecimalFormat dec = new DecimalFormat("0.0");
		
		String progress = (int)value + "% (" + dec.format(onGoingMB) + " / " + dec.format(fileLengthMB) + " MB)";
		
		Platform.runLater(() -> {
			if(!RetrofitDownloadData.isDownloading)
				text.setText("Uploading " + progress);
			else
				text.setText("Downloading " + progress);
			
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
