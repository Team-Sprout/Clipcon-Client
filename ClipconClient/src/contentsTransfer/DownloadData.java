package contentsTransfer;

import java.awt.Image;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import application.Main;
import controller.ClipboardController;
import controller.Endpoint;
import model.Contents;
import model.FileTransferable;
import model.History;
import model.ImageTransferable;
import userInterface.MainScene;
import userInterface.UserInterface;

public class DownloadData {

	public final static String SERVER_URL = "http://" + Main.SERVER_ADDR + ":8080/websocketServerModule";
	public final static String SERVER_SERVLET = "/DownloadServlet";

	private static final int CHUNKSIZE = 4096;
	private final String charset = "UTF-8";
	private HttpURLConnection httpConn;

	private String userName = null;
	private String groupPK = null;

	private Contents requestContents; // Contents Info to download
	
	public static boolean isDownloading = false;
	private UserInterface ui = UserInterface.getIntance();
	
	/** Constructor
	 * Setting userName and groupPK */
	public DownloadData(String userName, String groupPK) {
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
		long start = System.currentTimeMillis();
		
		isDownloading = true;
		ui.getMainScene().showProgressBar();
		
		History myhistory = Endpoint.user.getGroup().getHistory();
		// Retrieving Contents from My History
		requestContents = myhistory.getContentsByPK(downloadDataPK);

		String contentsType = requestContents.getContentsType();
		// Parameter to be sent by the GET method
		String parameters = "userName=" + userName + "&" + "groupPK=" + groupPK + "&" + "downloadDataPK=" + downloadDataPK;

		try {
			URL url = new URL(SERVER_URL + SERVER_SERVLET + "?" + parameters);

			httpConn = (HttpURLConnection) url.openConnection();

			httpConn.setRequestMethod("GET");
			httpConn.setUseCaches(false);
			httpConn.setDoOutput(false); // indicates GET method
			httpConn.setDoInput(true);

			// checks server's status code first
			int status = httpConn.getResponseCode();

			if (status == HttpURLConnection.HTTP_OK) {
				switch (contentsType) {
				case Contents.TYPE_STRING:
					// Get String Object in Response Body
					
					String stringData = downloadStringData(httpConn.getInputStream());

					StringSelection stringTransferable = new StringSelection(stringData);
					ClipboardController.writeClipboard(stringTransferable);
					
					ui.getProgressBarScene().completeProgress();
					break;

				case Contents.TYPE_IMAGE:
					// Get Image Object in Response Body
					Image imageData = downloadCapturedImageData(httpConn.getInputStream());

					ImageTransferable imageTransferable = new ImageTransferable(imageData);
					ClipboardController.writeClipboard(imageTransferable);
					
					ui.getProgressBarScene().completeProgress();
					break;

				case Contents.TYPE_FILE:
					String fileOriginName = requestContents.getContentsValue();
					// Save Real File(filename: fileOriginName) to Clipcon Folder Get Image Object in Response Body
					File fileData = downloadFileData(httpConn.getInputStream(), fileOriginName);

					ArrayList<File> fileList = new ArrayList<File>();
					fileList.add(fileData);

					FileTransferable fileTransferable = new FileTransferable(fileList);
					ClipboardController.writeClipboard(fileTransferable);
					long end = System.currentTimeMillis();
					System.out.println("걸린 시간  : " + (end-start));
					
					ui.getProgressBarScene().completeProgress();
					break;

				case Contents.TYPE_MULTIPLE_FILE:
					String multipleFileOriginName = requestContents.getContentsValue();
					// Save Real ZIP File(filename: fileOriginName) to Clipcon Folder
					File multipleFile = downloadFileData(httpConn.getInputStream(), multipleFileOriginName);

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
					long end2 = System.currentTimeMillis();
					System.out.println("걸린 시간  : " + (end2-start));
					
					ui.getProgressBarScene().completeProgress();
					break;

				default:
					break;
				}

			} else {
				throw new IOException("Server returned non-OK status: " + status);
			}
			
			isDownloading = false;
			httpConn.disconnect();

		} catch (IOException e) {
			e.printStackTrace();
		}
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

		String s = stringBuilder.toString();
		s = s.substring(0, s.length() - 2);  // \n\n 제거

		return s;
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
			byte[] buffer = new byte[CHUNKSIZE];
			//byte[] buffer = new byte[0xFFFF]; // 65536

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
	private File downloadFileData(InputStream inputStream, String fileName) throws FileNotFoundException {
		// opens input stream from the HTTP connection
		String saveFileFullPath = MainScene.DOWNLOAD_TEMP_DIR_LOCATION + File.separator + fileName;
		File fileData;

		try {
			// Delete child files that already exist before downloading
			deleteAllFiles(MainScene.DOWNLOAD_TEMP_DIR_LOCATION);

			// opens an output stream to save into file
			FileOutputStream fileOutputStream = new FileOutputStream(saveFileFullPath);

			int bytesRead = -1;
			byte[] buffer = new byte[CHUNKSIZE];
			//byte[] buffer = new byte[0xFFFF]; // 65536

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				fileOutputStream.write(buffer, 0, bytesRead);
			}
			fileOutputStream.flush();
			fileOutputStream.close();
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		fileData = new File(saveFileFullPath);
		return fileData;
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
