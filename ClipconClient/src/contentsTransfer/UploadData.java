package contentsTransfer;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * This program demonstrates a usage of the MultipartUtility class.
 */
public class UploadData {
	public final static String SERVER_URL = "http://182.172.16.118:8080/websocketServerModule";
	public final static String SERVER_SERVLET = "/UploadServlet";
	private String charset = "UTF-8";

	private String userName = null;
	private String groupPK = null;

	/** 생성자 userEmail과 groupPK를 설정한다. */
	public UploadData(String userName, String groupPK) {
		this.userName = userName;
		this.groupPK = groupPK;
	}

	public void test() {
		URL url;
		try {
			url = new URL("http://127.0.0.1:8080/websocketServerModule/UploadServlet");

			// 문자열로 URL 표현
			System.out.println("URL :" + url.toExternalForm());

			// HTTP Connection 구하기
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			// 요청 방식 설정 ( GET or POST or .. 별도로 설정하지않으면 GET 방식 )
			conn.setRequestMethod("POST");

			// 연결 타임아웃 설정
			conn.setConnectTimeout(3000); // 3초
			// 읽기 타임아웃 설정
			conn.setReadTimeout(3000); // 3초

			// 요청 방식 구하기
			System.out.println("getRequestMethod():" + conn.getRequestMethod());
			// 응답 콘텐츠 유형 구하기
			System.out.println("getContentType():" + conn.getContentType());
			// 응답 코드 구하기
			System.out.println("getResponseCode():" + conn.getResponseCode());
			// 응답 메시지 구하기
			System.out.println("getResponseMessage():" + conn.getResponseMessage());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** String Data를 업로드 */
	public void uploadStringData(String stringData) {
		try {
			MultipartUtility multipart = new MultipartUtility(SERVER_URL + SERVER_SERVLET, charset);
			setCommonParameter(multipart);

			multipart.addFormField("stringData", stringData);

			List<String> response = multipart.finish();
			System.out.println("SERVER REPLIED");
			// responseMsgLog();

			for (String line : response) {
				System.out.println(line);
			}
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	/** Clipboard에 있는 Captured Image Data를 업로드 */
	public void uploadCapturedImageData(Image capturedImageData) {
		try {
			MultipartUtility multipart = new MultipartUtility(SERVER_URL + SERVER_SERVLET, charset);
			setCommonParameter(multipart);

			System.out.println("<uploadCapturedImageData> getWidth: " + capturedImageData.getWidth(null));
			System.out.println("<uploadCapturedImageData> getHeight: " + capturedImageData.getHeight(null));
			multipart.addImagePart("imageData", capturedImageData);

			List<String> response = multipart.finish();
			System.out.println("SERVER REPLIED");
			// responseMsgLog();

			for (String line : response) {
				System.out.println(line);
			}

		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	/**
	 * 여러 File Data를 업로드
	 * 
	 * @param dir
	 *            업로드할 파일의 위치
	 * @param dir
	 *            업로드할 파일명
	 */
	public void uploadMultipartData(ArrayList<String> fileFullPathList) {

		try {
			MultipartUtility multipart = new MultipartUtility(SERVER_URL + SERVER_SERVLET, charset);
			setCommonParameter(multipart);

			// Iterator 통한 전체 조회
			Iterator iterator = fileFullPathList.iterator();

			// 여러 파일을 순서대로 처리
			while (iterator.hasNext()) {
				String fileFullPath = (String) iterator.next();

				System.out.println("fileFullPathList: " + fileFullPath);
				System.out.println();

				// 업로드할 파일 생성
				File uploadFile = new File(fileFullPath);

				/*
				 * uploadFilename is the name of the sequence input variable in
				 * the called project the value is the name that will be given
				 * to the file
				 */
				multipart.addFilePart("multipartFileData", uploadFile);
			}

			List<String> response = multipart.finish();
			System.out.println("SERVER REPLIED");
			// responseMsgLog();

			for (String line : response) {
				System.out.println(line);
			}
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	/**
	 * 모든 Data에서 공통으로 설정해야하는 Parameter userEmail, groupPK, uploadTime
	 */
	public void setCommonParameter(MultipartUtility multipart) {
		multipart.addHeaderField("User-Agent", "Heeee");
		multipart.addFormField("userName", userName);
		multipart.addFormField("groupPK", groupPK);
		multipart.addFormField("uploadTime", uploadTime());
	}

	/** @return YYYY-MM-DD HH:MM:SS 형식의 현재 시간 */
	public String uploadTime() {
		Calendar cal = Calendar.getInstance();
		String year = Integer.toString(cal.get(Calendar.YEAR));
		String month = Integer.toString(cal.get(Calendar.MONTH) + 1);

		String date = Integer.toString(cal.get(Calendar.DATE));
		String hour = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
		if (Integer.parseInt(hour) < 10) {
			hour = "0" + hour;
		}
		if (Integer.parseInt(hour) > 12) {
			hour = "오후 " + Integer.toString(Integer.parseInt(hour) - 12);
		} else {
			hour = "오전 " + hour;
		}

		String minute = Integer.toString(cal.get(Calendar.MINUTE));
		if (Integer.parseInt(minute) < 10) {
			minute = "0" + minute;
		}
		String sec = Integer.toString(cal.get(Calendar.SECOND));
		if (Integer.parseInt(sec) < 10) {
			sec = "0" + sec;
		}

		return year + "-" + month + "-" + date + " " + hour + ":" + minute + ":" + sec;
	}

	public static void main(String[] args) {
		new UploadData("123", "35").test();
	}
}