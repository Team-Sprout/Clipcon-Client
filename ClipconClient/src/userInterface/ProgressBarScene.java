package userInterface;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.Setter;
import retrofitContentsTransfer.RetrofitDownloadData;

public class ProgressBarScene implements Initializable {
	
	private UserInterface ui = UserInterface.getInstance();

	@FXML private Text text;
	@FXML private ProgressBar progressBar ;
	
	private static Text [] textArray = new Text [10];
	private static ProgressBar [] progressBarArray = new ProgressBar[10];
	@Getter
	private static int index = -1;
	@Getter
	@Setter
	private static int number = 0;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ui.setProgressBarScene(this);
		
		for(int i=0; i<progressBarArray.length; i++) {
			if(ui.getMainScene().getProgressBarStageArray()[i] == null) {
				index = i;
				break;
			}
		}
		
		number++;
		
		textArray[index] = text;
		if(RetrofitDownloadData.isDownloading) {
			textArray[index].setText("Downloading");
		}
		
		progressBarArray[index] = progressBar;
		progressBarArray[index].setProgress(ProgressBar.INDETERMINATE_PROGRESS);
	}
	
	public void setProgeress(int index, double value, long onGoing, long fileLength, boolean isDownloading) {
		double onGoingMB = ((onGoing / 1024.0) / 1024.0);
		double fileLengthMB = ((fileLength / 1024.0) / 1024.0);
		
		DecimalFormat dec = new DecimalFormat("0.0");
		
		String progress = (int)value + "% (" + dec.format(onGoingMB) + " / " + dec.format(fileLengthMB) + " MB)";
		
		Platform.runLater(() -> {
			if(!isDownloading)
				textArray[index].setText("Uploading " + progress);
			else
				textArray[index].setText("Downloading " + progress);
			
			progressBarArray[index].setProgress(value*0.01);
		});
	}
	
	public void completeProgress(int index) {
		Platform.runLater(() -> {
			textArray[index].setText("Complete!");
			progressBarArray[index].setProgress(1);
        });
	}
}
