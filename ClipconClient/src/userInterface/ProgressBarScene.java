package userInterface;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
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
	
	public static boolean completeFlag;
	
	// run scheduler for checking
	ScheduledExecutorService scheduler;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ui.setProgressBarScene(this);
		
		progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
		
		scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (completeFlag) {
							completeFlag = false;
							text.setText("Complete!");
							progressBar.setProgress(1);
							return;
						}

					}
				});

			}
		}, 50, 50, TimeUnit.MILLISECONDS);
		
	}
}
