package contentsTransfer;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import userInterface.FailDialog;
import userInterface.UserInterface;

/**
 * This utility class provides an abstraction layer for sending multipart HTTP
 * POST requests to a web server.
 */
public class MultipartUtility {
	private static final int CHUNKSIZE = 4096;
	private static final String LINE_FEED = "\r\n";
	private final String boundary;
	private HttpURLConnection httpConn;
	private String charset;
	private OutputStream outputStream;
	private PrintWriter writer;
	private UserInterface ui;
	
	public static boolean outOfMemoryException = false;

	/**
	 * This constructor initializes a new HTTP POST request with content type is
	 * set to multipart/form-data
	 * 
	 * @param requestURL
	 * @param charset
	 * @throws IOException
	 */
	public MultipartUtility(String requestURL, String charset) throws IOException {
		this.charset = charset;
		ui = UserInterface.getIntance();

		// creates a unique boundary based on time stamp
		boundary = "===" + System.currentTimeMillis() + "===";

		URL url = new URL(requestURL);
		httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setUseCaches(false);
		httpConn.setDoOutput(true); // indicates POST method
		httpConn.setDoInput(true);
		httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		httpConn.setRequestProperty("User-Agent", "pcProgram");

		outputStream = httpConn.getOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
	}

	/**
	* Adds a header field to the request.
	*
	* @param name name of the header field
	* @param value value of the header field
	*/
	public void addHeaderField(String name, String value) {
		writer.append(name + ": " + value).append(LINE_FEED);
		writer.flush();
	}

	/**
	* Adds a form field to the request
	*
	* @param name field name
	* @param value field value
	*/
	public void addFormField(String name, String value) {
		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE_FEED);
		writer.append("Content-Type: text/plain; charset=" + charset).append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.append(value).append(LINE_FEED);
		writer.flush();
	}

	/**
	* Adds a upload file section to the request
	* 
	* @param fieldName name attribute in <input type="file" name="..." />
	* @param uploadFile a File to be uploaded
	* @throws IOException
	*/
	public void addImagePart(String fieldName, Image image) throws IOException {
		String imageName = "capturedImage";

		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + imageName + "\"").append(LINE_FEED);
		writer.append("Content-Type: image/png").append(LINE_FEED);
		writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.flush();

		BufferedImage originalImage = (BufferedImage) image;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(originalImage, "png", baos);
		baos.flush();
		byte[] imageInByte = baos.toByteArray();
		baos.close();

		outputStream.write(imageInByte);
		outputStream.flush();

		writer.append(LINE_FEED);
		writer.flush();
	}

	/**
	* Adds a upload file section to the request
	* 
	* @param fieldName name attribute in <input type="file" name="..." />
	* @param uploadFile a File to be uploaded
	* @throws IOException
	*/
	public void addFilePart(String fieldName, File uploadFile) throws IOException {
		outOfMemoryException = false;
		String fileName = uploadFile.getName();

		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
		writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
		writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.flush();

		FileInputStream inputStream = new FileInputStream(uploadFile);

		int bytesRead = -1;
		byte[] buffer = new byte[CHUNKSIZE];

		while ((bytesRead = inputStream.read(buffer)) != -1) {
			try {
				outputStream.write(buffer, 0, bytesRead);
			} catch (OutOfMemoryError e) {
				outOfMemoryException = true;
				Platform.runLater(() -> {
					FailDialog.show("전송 가능한 용량을 초과하였습니다.");
				});
				ui.getMainScene().closeProgressBarStage();
				return;
			}
		}
		outputStream.flush();
		inputStream.close();

		writer.append(LINE_FEED);
		writer.flush();
	}

	/**
	* Completes the request and receives response from the server.
	*
	* @return a list of Strings as response in case the server returned status OK, otherwise an exception is thrown.
	* @throws IOException
	*/
	public List<String> finish() throws IOException {
		List<String> response = new ArrayList<String>();

		writer.append(LINE_FEED).flush();
		writer.append("--" + boundary + "--").append(LINE_FEED);
		writer.close();

		// checks server's status code first
		int status = httpConn.getResponseCode();
		if (status == HttpURLConnection.HTTP_OK) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				response.add(line);
			}
			reader.close();
			httpConn.disconnect();
		} else {
			throw new IOException("Server returned non-OK status: " + status);
		}

		return response;
	}
}