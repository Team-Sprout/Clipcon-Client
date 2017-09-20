package transfer;

import java.awt.Image;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import ClipboardManager.ClipboardController;
import model.Contents;
import model.History;
import model.trasferabel.FileTransferable;
import model.trasferabel.ImageTransferable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import server.Endpoint;
import userInterface.UserInterface;
import userInterface.scene.MainScene;
import userInterface.scene.ProgressBarScene;

public class RetrofitDownloadData {

	private UserInterface ui = UserInterface.getInstance();

	private String userName = null;
	private String groupPK = null;
	private Contents requestContents; // Contents Info to download

	private final int CHUNKSIZE = 0xFFFF; // 65536
	private String charset = "UTF-8";

	public static boolean isDownloading = false;

	/** Constructor
	 * setting userName and groupPK. */
	public RetrofitDownloadData(String userName, String groupPK) {
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
		isDownloading = true;
		ui.getMainScene().showProgressBar();

		// retrieving Contents from My History
		History myhistory = Endpoint.user.getGroup().getHistory();
		requestContents = myhistory.getContentsByPK(downloadDataPK);
		String contentsType = requestContents.getContentsType();

		// Parameter to be sent by the GET method
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("userName", userName);
		parameters.put("groupPK", groupPK);
		parameters.put("downloadDataPK", downloadDataPK);

		// create Retrofit instance
		Retrofit.Builder builder = new Retrofit.Builder().baseUrl(RetrofitInterface.BASE_URL).addConverterFactory(GsonConverterFactory.create());
		Retrofit retrofit = builder.build();

		// call object for the request
		RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
		Call<ResponseBody> call = retrofitInterface.requestFileDataDownload(parameters);

		call.enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
				
				int progressBarIndex = ProgressBarScene.getIndex();
				
				if (response.isSuccessful()) {
					switch (contentsType) {
					case Contents.TYPE_STRING:
						// Get String Object in Response Body
						String stringData = downloadStringData(response.body().byteStream());

						StringSelection stringTransferable = new StringSelection(stringData);
						ClipboardController.writeClipboard(stringTransferable);
						break;

					case Contents.TYPE_IMAGE:
						// Get Image Object in Response Body
						Image imageData = downloadCapturedImageData(response.body().byteStream());

						ImageTransferable imageTransferable = new ImageTransferable(imageData);
						ClipboardController.writeClipboard(imageTransferable);
						break;

					case Contents.TYPE_FILE:
						String fileOriginName = requestContents.getContentsValue();
						// Save Real File(filename: fileOriginName) to Clipcon Folder Get Image Object in Response Body
						File fileData = saveFileDataToDisk(response.body(), fileOriginName, progressBarIndex);

						ArrayList<File> fileList = new ArrayList<File>();
						fileList.add(fileData);

						FileTransferable fileTransferable = new FileTransferable(fileList);
						ClipboardController.writeClipboard(fileTransferable);

						break;

					case Contents.TYPE_MULTIPLE_FILE:
						String multipleFileOriginName = requestContents.getContentsValue();
						// Save Real ZIP File(filename: fileOriginName) to Clipcon Folder
						File multipleFile = saveFileDataToDisk(response.body(), multipleFileOriginName, progressBarIndex);

						File outputUnZipFile = new File(MainScene.DOWNLOAD_TEMP_DIR_LOCATION);
						ArrayList<File> multipleFileList = new ArrayList<File>();
						File[] multipleFiles = null;

						try {
							MultipleFileCompress.unzip(multipleFile, outputUnZipFile, false);
							multipleFile.delete(); // Delete Real ZIP File
							multipleFiles = outputUnZipFile.listFiles();

							for (int j = 0; j < multipleFiles.length; j++) {
								multipleFileList.add(multipleFiles[j]);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						FileTransferable multipleFileTransferable = new FileTransferable(multipleFileList);
						ClipboardController.writeClipboard(multipleFileTransferable);

						break;

					default:
						break;
					}
				}
				
				ui.getProgressBarScene().completeProgress(progressBarIndex);
				ui.getMainScene().closeProgressBarStage(progressBarIndex);
				isDownloading = false;
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable arg1) {
				// TODO Auto-generated method stub
				System.out.println("Download onFailure");
			}

		});
	}

	/** Download String Data */
	private String downloadStringData(InputStream inputStream) {
		BufferedReader bufferedReader;
		StringBuilder stringBuilder = null;

		try {
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset));

			stringBuilder = new StringBuilder();
			String line = null;

			try {
				while ((line = bufferedReader.readLine()) != null) {
					stringBuilder.append(line + "\n");
				}
				inputStream.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		String deleteNewLineString = stringBuilder.toString();
		deleteNewLineString = deleteNewLineString.substring(0, deleteNewLineString.length() - 2); // \n\n remove

		return deleteNewLineString;
	}

	/**
	 * Download Captured Image Data
	 * Change to Image object from file form of Image data
	 */
	private Image downloadCapturedImageData(InputStream inputStream) {
		byte[] imageInByte = null;
		BufferedImage bImageFromConvert = null;

		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			int bytesRead = -1;
			byte[] buffer = new byte[CHUNKSIZE]; // 65536

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				byteArrayOutputStream.write(buffer, 0, bytesRead);
			}

			byteArrayOutputStream.flush();
			imageInByte = byteArrayOutputStream.toByteArray();

			inputStream.close();

			// convert byte array back to BufferedImage
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageInByte);
			bImageFromConvert = ImageIO.read(byteArrayInputStream);

		} catch (IOException e) {
			e.printStackTrace();
		}
		Image ImageData = (Image) bImageFromConvert;

		return ImageData;
	}

	/** Download File Data to Temporary folder
	 * @return File object */
	private File saveFileDataToDisk(ResponseBody body, String fileName, int progressBarIndex) {
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
				byte[] buffer = new byte[CHUNKSIZE]; // 65536

				// progress when saving file to disk
				long fileSize = body.contentLength();
				long fileSizeDownloaded = 0;

				inputStream = body.byteStream();

				while ((bytesRead = inputStream.read(buffer)) != -1) {
					fileOutputStream.write(buffer, 0, bytesRead);
					fileSizeDownloaded += bytesRead;

					double progressValue = (100 * fileSizeDownloaded / fileSize);
					// System.out.println((int) progressValue);
					ui.getProgressBarScene().setProgeress(progressBarIndex, progressValue, fileSizeDownloaded, fileSize, true);

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
