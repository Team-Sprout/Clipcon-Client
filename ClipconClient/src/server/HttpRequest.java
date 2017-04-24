package server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import lombok.Getter;

@Getter
public class HttpRequest {

	// 서버의 기본 context root. 요청 종류에 따라 뒤에 값이 붙어 서로 다른 url을 통해 요청한다.
	public static final String SERVER_CONTEXT_ROOT = "http://localhost:8080/ClipconServerProject/";
	// 요청 종류 구분 key값
	public static final String TYPE_OF_REQUEST = "request_type";

	// 요청 유형 (향후 계속 추가 예정)
	public static final String REQUEST_TEST = "";				// 테스트용
	public static final String REQUEST_SIGN_IN = "signIn";		// 로그인 요청
	public static final String REQUEST_SIGN_UP = "signUp";		// 회원가입 요청
	// TODO: 클라이언트 요청 유형 추가하기

	private HttpURLConnection conn;
	private String requestType = null;				// 요청의 유형
	private JSONObject json = new JSONObject();

	public HttpRequest(String requestType) {
		this.requestType = requestType;
		json.put(TYPE_OF_REQUEST, requestType);
	}
	
	public int send() {
		try {
			/* request 송신 부 */
			URL url = new URL(SERVER_CONTEXT_ROOT + requestType); 	// url 생성
			conn = (HttpURLConnection) url.openConnection(); 		// 연결 준비
			/*
			 * TODO 설정 추가 및 수정 필요. 헤더 정보 조사 필요
			 */
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST"); // request to post
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Clipcon", "ture");
			// configuration ~
			conn.setConnectTimeout(1000);
			conn.setReadTimeout(1000); 	// ~ configuration
			conn.connect(); // 연결

			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write("msg=" + json.toString()); // send post message.
			wr.flush();
			wr.close();

			/* response 수신 부 */
			int HttpResult = conn.getResponseCode(); // 서버에서 받은 응답 코드

			// TODO: 클라이언트가 서버에게 받은 응답으로 무엇을 할지에 따라 이 부분의 구현이 달라짐
			String result = ""; // 서버에게 받을 응답 메시지
			if (HttpResult == HttpURLConnection.HTTP_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
				String line = null;
				while ((line = br.readLine()) != null) {
					result = result + line + "\n";
					System.out.println(line + "\n");
				}
				System.out.println("result: " + result);
				br.close();
			}
			return conn.getResponseCode(); // return response code.

		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public String getResponseMessage(String type) {
		try {
			return new JSONObject(conn.getResponseMessage()).get(type).toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void addProperty(String key, String value) {
		json.put(key, value);
	}

	public static void main(String[] args) {
	}
}