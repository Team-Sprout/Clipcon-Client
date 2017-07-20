package retrofitContentsTransfer;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import contentsTransfer.MultipleFileCompress;
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
import userInterface.MainScene;
import userInterface.UserInterface;

public class RetrofitUploadTest {

	private UserInterface ui = UserInterface.getIntance();

	private String userName = null;
	private String groupPK = null;

	// private String charset = "UTF-8";

	private UserInterface ui = UserInterface.getInstance();

	public static String multipleFileListInfo = "";

	// create Retrofit instance
	public Retrofit.Builder builder = new Retrofit.Builder().baseUrl(RetrofitInterface.BASE_URL).addConverterFactory(GsonConverterFactory.create());
	public Retrofit retrofit = builder.build();

	// get client & call object for the request
	public RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);

	public Call<ResponseBody> call = null;

	/** Constructor
	 * setting userName and groupPK. */
	public RetrofitUploadTest(String userName, String groupPK) {
		this.userName = userName;
		this.groupPK = groupPK;

//		ui.getMainScene().showProgressBar();
	}

	/** Upload String Data */
	public void uploadStringData(String stringData) {

		// add another part within the multipart request
		RequestBody username = RequestBody.create(MediaType.parse("text/plain"), userName);
		RequestBody grouppk = RequestBody.create(MediaType.parse("text/plain"), groupPK);
		RequestBody stringdata = RequestBody.create(MediaType.parse("text/plain"), stringData);

		call = retrofitInterface.requestStringDataUpload(username, grouppk, stringdata);
		callResult(call);
	}

	// /** Upload Captured Image Data in Clipboard */
	// public void uploadCapturedImageData(Image capturedImageData) {
	// try {
	// MultipartUtility multipart = new MultipartUtility(SERVER_URL + SERVER_SERVLET, charset);
	// setCommonParameter(multipart);
	//
	// multipart.addImagePart("imageData", capturedImageData);
	// multipart.finish();
	//
	// } catch (IOException ex) {
	// System.err.println(ex);
	// }
	// }

	/** Upload File Data
	 * 
	 * @param fileFullPathLis - file path from clipboard
	 */
	public void uploadMultipartData(ArrayList<String> fileFullPathList) {

		// create uploading file
		File originalFile = new File(fileFullPathList.get(0));

		// add another part within the multipart request
		RequestBody username = RequestBody.create(MediaType.parse("text/plain"), userName);
		RequestBody grouppk = RequestBody.create(MediaType.parse("text/plain"), groupPK);

		// create RequestBody instance (from file)
		ProgressRequestBody progressFilePart = new ProgressRequestBody();

		/* case: Single file data(not a folder) */
		if (fileFullPathList.size() == 1 && originalFile.isFile()) {
			progressFilePart.setMFile(originalFile);
			MultipartBody.Part file = MultipartBody.Part.createFormData("fileData", originalFile.getName(), progressFilePart);

			// finally, execute the request
			call = retrofitInterface.requestFileDataUpload(username, grouppk, file);
		}
		/* case: Multiple file data, One or more folders */
		else {
			try {
				File uploadRootDir = new File(MainScene.UPLOAD_TEMP_DIR_LOCATION);

				// Delete child files that already exist after uploading
				if (uploadRootDir.listFiles().length != 0) {
					for (int i = 0; i < uploadRootDir.listFiles().length; i++)
						uploadRootDir.listFiles()[i].delete();
				}

				multipleFileListInfo = "";

				String zipFileFillPath = MultipleFileCompress.compress(fileFullPathList);
				File uploadZipFile = new File(zipFileFillPath);

				progressFilePart.setMFile(uploadZipFile);
				MultipartBody.Part multipartFile = MultipartBody.Part.createFormData("multipartFileData", uploadZipFile.getName(), progressFilePart);

				// finally, execute the request
				RequestBody multiplefileListInfo = RequestBody.create(MediaType.parse("text/plain"), multipleFileListInfo);
				call = retrofitInterface.requestFileDataUpload(username, grouppk, multiplefileListInfo, multipartFile);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		callResult(call);
	}

	/** logging method- check for a successful response */
	public void callResult(Call<ResponseBody> call) {
		call.enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
				/* logging code */
				System.out.println("Upload onResponse");
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
				System.out.println("Upload onFailure");
			}
		});
	}

}
