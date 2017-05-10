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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONObject;

import controller.ClipboardController;
import controller.Endpoint;
import model.Contents;
import model.FileTransferable;
import model.History;
import model.ImageTransferable;
import userInterface.MainScene;

public class DownloadData {

	// 다운로드 파일을 임시로 저장할 위치
	private final String DOWNLOAD_LOCATION = "C:\\Program Files\\Clipcon";

	public final static String SERVER_URL = "http://delf.gonetis.com:8080:/websocketServerModule";
//	 public final static String SERVER_URL = "http://223.194.158.100:8080/websocketServerModule"; // delf's


	public final static String SERVER_SERVLET = "/DownloadServlet";

	private final String charset = "UTF-8";
	private HttpURLConnection httpConn;

	private String userName = null;
	private String groupPK = null;

	private Contents requestContents; // Contents Info to download
	// private String downloadDataPK; // Contents' Primary Key to download

	/** Constructor
	 * Setting userName and groupPK */
	public DownloadData(String userName, String groupPK) {
		this.userName = userName;
		this.groupPK = groupPK;
	}

	/**
	 * 다운로드하기 원하는 Data를 request 복수 선택은 File Data의 경우만 가능(추후 개선)
	 * 
	 * @param downloadDataPK
	 *            다운로드할 Data의 고유키
	 * @param myhistory
	 *            내가 속한 그룹의 History 정보
	 */
	public void requestDataDownload(String downloadDataPK) throws MalformedURLException {

		// 내가 속한 Group의 History를 가져온다. 수정 필요.
		History myhistory = Endpoint.user.getGroup().getHistory();

		// Retrieving Contents from My History
		requestContents = myhistory.getContentsByPK(downloadDataPK);
		// Type of data to download
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
					System.out.println("stringData Result: " + stringData);

					StringSelection stringTransferable = new StringSelection(stringData);
					ClipboardController.writeClipboard(stringTransferable);
					break;

				case Contents.TYPE_IMAGE:
					// Get Image Object in Response Body
					Image imageData = downloadCapturedImageData(httpConn.getInputStream());
					System.out.println("ImageData Result: " + imageData.toString());

					ImageTransferable imageTransferable = new ImageTransferable(imageData);
					ClipboardController.writeClipboard(imageTransferable);
					break;

				case Contents.TYPE_FILE:
					String fileOriginName = requestContents.getContentsValue();
					// Save Real File(filename: fileOriginName) to Clipcon Folder Get Image Object in Response Body
					File fileData = downloadFileData(httpConn.getInputStream(), fileOriginName);
					System.out.println("fileOriginName Result: " + fileData.getName());

					ArrayList<File> fileList = new ArrayList<File>();
					fileList.add(fileData);

					FileTransferable fileTransferable = new FileTransferable(fileList);
					ClipboardController.writeClipboard(fileTransferable);
					break;

				case Contents.TYPE_MULTIPLE_FILE:
					String multipleFileOriginName = requestContents.getContentsValue();
					// Save Real ZIP File(filename: fileOriginName) to Clipcon Folder
					File multipleFile = downloadFileData(httpConn.getInputStream(), multipleFileOriginName);
					System.out.println("multipleFileOriginName Result: " + multipleFile.getName());
					
					File outputUnZipFile = new File(MainScene.CLIPCON_DIR_LOCATION);
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
					System.out.println("어떤 형식에도 속하지 않음.");
				}
				System.out.println();

			} else {
				throw new IOException("Server returned non-OK status: " + status);
			}
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
		return stringBuilder.toString();
	}

	/**
	 * Download Captured Image Data
	 * Change to Image object from file form of Image data
	 */
	private Image downloadCapturedImageData(InputStream inputStream) {
		byte[] imageInByte = null;
		BufferedImage bImageFromConvert = null;

		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();) {
			byte[] buffer = new byte[0xFFFF]; // 65536

			for (int len; (len = inputStream.read(buffer)) != -1;)
				byteArrayOutputStream.write(buffer, 0, len);

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

	/** download File Data to Temporary folder
	 * @return File object */
	private File downloadFileData(InputStream inputStream, String fileName) throws FileNotFoundException {
		// opens input stream from the HTTP connection
		// InputStream inputStream = httpConn.getInputStream();
		String saveFileFullPath = MainScene.CLIPCON_DIR_LOCATION + File.separator + fileName;
		File outputUnZipFile = new File(MainScene.CLIPCON_DIR_LOCATION);
		File fileData;

		try {
			// Before Download 이미 존재하는 하위 Files 삭제
			if (outputUnZipFile.listFiles().length != 0) {
				for (int i = 0; i < outputUnZipFile.listFiles().length; i++)
					outputUnZipFile.listFiles()[i].delete();
			}

			// opens an output stream to save into file
			FileOutputStream fileOutputStream = new FileOutputStream(saveFileFullPath);

			int bytesRead = -1;
			byte[] buffer = new byte[0xFFFF];

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
}
