package userInterface;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofitContentsTransfer.RetrofitInterface;

public class BugReportScene implements Initializable {
	
	private UserInterface ui = UserInterface.getInstance();
	
	@FXML private TextArea bugMessageTA;
	@FXML private Button OkBtn, XBtn;
	
	// create Retrofit instance
	public Retrofit.Builder builder = new Retrofit.Builder().baseUrl(RetrofitInterface.BASE_URL).addConverterFactory(GsonConverterFactory.create());
	public Retrofit retrofit = builder.build();

	// get client & call object for the request
	public RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
	
	public Call<ResponseBody> call = null;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		OkBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(bugMessageTA.getText().length() == 0) {
					notInputBugMessage();
				} else {
					sendBugMessage(bugMessageTA.getText());
				}
			}
		});
		
		XBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ui.getMainScene().closeBugReportStage();
			}
		});
	}
	
	public void notInputBugMessage() {
		PlainDialog.show("Bug Message를 입력하세요.");
	}
	
	public void sendBugMessage(String bugMessage) {
		// add another part within the multipart request
		RequestBody bugmessage = RequestBody.create(MediaType.parse("text/plain"), bugMessage);
		
		call = retrofitInterface.sendBugMessage(bugmessage);
		
		callResult(call);
	}
	
	/** show dialog method- check for response */
	public void callResult(Call<ResponseBody> call) {
		call.enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
				ui.getMainScene().closeBugReportStage();
				Platform.runLater(() -> {
					PlainDialog.show("Bug Message 전송 성공");
				});
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable arg1) {
				Platform.runLater(() -> {
					PlainDialog.show("Bug Message 전송 실패");
				});
			}
		});
	}
}
