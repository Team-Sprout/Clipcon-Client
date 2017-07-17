package retrofitContentsTransfer;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import userInterface.UserInterface;

public class RetrofitUploadTest {
	// private String charset = "UTF-8";

	private String userName = null;
	private String groupPK = null;

	private UserInterface ui = UserInterface.getInstance();

	public static String multipleFileListInfo = "";

	/** Constructor
	 * setting userName and groupPK. */
	public RetrofitUploadTest(String userName, String groupPK) {
		this.userName = userName;
		this.groupPK = groupPK;

//		ui.getMainScene().showProgressBar();
	}

	// public void uploadFile(URI fileUri)
	public void uploadFile(ArrayList<String> fileFullPathList) {

		// creates a unique boundary based on time stamp
		// String boundary = "===" + System.currentTimeMillis() + "===";
		// RequestBody descriptionPart = RequestBody.create(MultipartBody.FORM, boundary);
		// System.out.println("boundary: " + boundary);

		// create uploading file
		File originalFile = new File(fileFullPathList.get(0));
		// create RequestBody instance from file
		// RequestBody filePart = RequestBody.create(MediaType.parse("multipart/form-data"), originalFile);
		// MultipartBody.Part is used to send also the actual file name
		// MultipartBody.Part file = MultipartBody.Part.createFormData("fileData", originalFile.getName(), filePart);

		
		
		/* test */
		// ProgressRequestBody progressFilePart = ProgressRequestBody.create(MediaType.parse("multipart/form-data"), originalFile);
		ProgressRequestBody progressFilePart = new ProgressRequestBody(originalFile);
		MultipartBody.Part file = MultipartBody.Part.createFormData("fileData", originalFile.getName(), progressFilePart);

		// add another part within the multipart request
		RequestBody username = RequestBody.create(MediaType.parse("text/plain"), userName);
		RequestBody grouppk = RequestBody.create(MediaType.parse("text/plain"), groupPK);

		// create Retrofit instance
		Retrofit.Builder builder = new Retrofit.Builder().baseUrl(RetrofitInterface.BASE_URL).addConverterFactory(GsonConverterFactory.create());
		Retrofit retrofit = builder.build();

		// get client & call object for the request
		RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);

		// finally, execute the request
		Call<ResponseBody> call = retrofitInterface.uploadFile(username, grouppk, file);
		call.enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
				// TODO Auto-generated method stub
				System.out.println("----- response toString -----\n" + response.toString());

				Headers headers = response.headers();
				Iterator<String> itr = headers.names().iterator();

				System.out.println("----- response headers -----");
				while (itr.hasNext()) {
					String s = itr.next();
					System.out.println("header: " + s + ", value: " + headers.values(s));
				}
				System.out.println("----------------------------");
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable arg1) {
				// TODO Auto-generated method stub
				System.out.println("Upload onFailure");
			}
		});
	}

}
