package retrofitContentsTransfer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import controller.ClipboardController;
import controller.Endpoint;
import javafx.application.Platform;
import model.Contents;
import model.FileTransferable;
import model.History;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import userInterface.MainScene;
import userInterface.UserInterface;

public class RetrofitDownloadTest {

	// private String charset = "UTF-8";

	private String userName = null;
	private String groupPK = null;
	private Contents requestContents; // Contents Info to download

	private UserInterface ui = UserInterface.getIntance();

	public static boolean isDownloading = false;

	/** Constructor
	 * setting userName and groupPK. */
	public RetrofitDownloadTest(String userName, String groupPK) {
		this.userName = userName;
		this.groupPK = groupPK;
	}

	/**
	 * Send request the data you want to download
	 * 
	 * @param downloadDataPK
	 *            The primary key of the content to download
	 * @param myhistory
	 *            History of my group
	 */
	public void requestDataDownload(String downloadDataPK) throws MalformedURLException {
		Platform.runLater(() -> {
			isDownloading = true;
		});
		ui.getMainScene().showProgressBar();

		History myhistory = Endpoint.user.getGroup().getHistory();
		// Retrieving Contents from My History
		requestContents = myhistory.getContentsByPK(downloadDataPK);
		String contentsType = requestContents.getContentsType();

		// Parameter to be sent by the GET method
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("userName", userName);
		parameters.put("groupPK", groupPK);
		parameters.put("downloadDataPK", downloadDataPK);

		// create Retrofit instance
		Retrofit.Builder builder = new Retrofit.Builder().baseUrl(RetrofitInterface.BASE_URL).addConverterFactory(GsonConverterFactory.create());
		// Retrofit.Builder builder = new Retrofit.Builder().baseUrl(RetrofitInterface.BASE_URL);
		Retrofit retrofit = builder.build();

		// todo get client & call object for the request
		RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
		Call<ResponseBody> call = retrofitInterface.requestDataDownload2(parameters);
		call.enqueue(new Callback<ResponseBody>() {

			@Override
			public void onResponse(Call<ResponseBody> call, final retrofit2.Response<ResponseBody> response) {

				if (response.isSuccessful()) {
					switch (contentsType) {
					case Contents.TYPE_STRING:
						// Get String Object in Response Body

						System.out.println("TYPE_STRING");
						// String stringData = downloadStringData(httpConn.getInputStream());
						//
						// StringSelection stringTransferable = new StringSelection(stringData);
						// ClipboardController.writeClipboard(stringTransferable);
						break;

					case Contents.TYPE_IMAGE:
						// Get Image Object in Response Body

						System.out.println("TYPE_IMAGE");
						//
						// Image imageData = downloadCapturedImageData(httpConn.getInputStream());
						//
						// ImageTransferable imageTransferable = new ImageTransferable(imageData);
						// ClipboardController.writeClipboard(imageTransferable);
						break;

					case Contents.TYPE_FILE:
						String fileOriginName = requestContents.getContentsValue();
						// Save Real File(filename: fileOriginName) to Clipcon Folder Get Image Object in Response Body

						File fileData = saveFileDataToDisk(response.body(), fileOriginName);

						ArrayList<File> fileList = new ArrayList<File>();
						fileList.add(fileData);

						FileTransferable fileTransferable = new FileTransferable(fileList);
						ClipboardController.writeClipboard(fileTransferable);
						break;

					case Contents.TYPE_MULTIPLE_FILE:

						System.out.println("TYPE_MULTIPLE_FILE");

						// String multipleFileOriginName = requestContents.getContentsValue();
						// // Save Real ZIP File(filename: fileOriginName) to Clipcon Folder
						// File multipleFile = downloadFileData(httpConn.getInputStream(), multipleFileOriginName);
						//
						// File outputUnZipFile = new File(MainScene.DOWNLOAD_TEMP_DIR_LOCATION);
						// ArrayList<File> multipleFileList = new ArrayList<File>();
						// File[] multipleFiles = null;
						//
						// try {
						// MultipleFileCompress.unzip(multipleFile, outputUnZipFile, false);
						// multipleFile.delete(); // Delete Real ZIP File
						// multipleFiles = outputUnZipFile.listFiles();
						//
						// for (int j = 0; j < multipleFiles.length; j++) {
						// multipleFileList.add(multipleFiles[j]);
						// }
						// } catch (Exception e) {
						// e.printStackTrace();
						// }
						// FileTransferable multipleFileTransferable = new FileTransferable(multipleFileList);
						// ClipboardController.writeClipboard(multipleFileTransferable);
						break;

					default:
						break;
					}

					ui.getProgressBarScene().completeProgress();
				}
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable arg1) {
				// TODO Auto-generated method stub
				System.out.println("Download onFailure");
			}

		});
	}

	/** Download File Data to Temporary folder
	 * @return File object */
	private File saveFileDataToDisk(ResponseBody body, String fileName) {
		try {
			String saveFileFullPath = MainScene.DOWNLOAD_TEMP_DIR_LOCATION + File.separator + fileName;
			File fileData;

			InputStream inputStream = null;

			try {
				// Delete child files that already exist before downloading
				deleteAllFiles(MainScene.DOWNLOAD_TEMP_DIR_LOCATION);

				// opens an output stream to save into file
				FileOutputStream fileOutputStream = new FileOutputStream(saveFileFullPath);

				int bytesRead = -1;
				// byte[] buffer = new byte[CHUNKSIZE];
				byte[] buffer = new byte[0xFFFF]; // 65536

				/* progress when saving file to disk */
				long fileSize = body.contentLength();
				long fileSizeDownloaded = 0;

				inputStream = body.byteStream();

				while ((bytesRead = inputStream.read(buffer)) != -1) {
					fileOutputStream.write(buffer, 0, bytesRead);
					fileSizeDownloaded += bytesRead;

					System.out.println((int) (100 * fileSizeDownloaded / fileSize));

					if (bytesRead == -1) {
						break;
					}
				}

				fileOutputStream.flush();
				fileOutputStream.close();

				fileData = new File(saveFileFullPath);
				return fileData;

			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/** Delete all files in directory */
	public void deleteAllFiles(String parentDirPath) {
		// Get the files in the folder into an array.
		File file = new File(parentDirPath);
		File[] tempFile = file.listFiles();

		if (tempFile.length > 0) {
			for (int i = 0; i < tempFile.length; i++) {
				if (tempFile[i].isFile()) {
					tempFile[i].delete();
				} else { // Recursive function
					deleteAllFiles(tempFile[i].getPath());
				}
				tempFile[i].delete();
			}
		}
	}
}
