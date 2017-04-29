package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Contents {
	public static String TYPE_STRING = "STRING";
	public static String TYPE_IMAGE = "IMAGE";
	public static String TYPE_FILE = "FILE";

	private String contentsType;
	private long contentsSize;

	// 그룹내의 각 Data를 구분하는 고유키값
	public String contentsPKName;

	private String uploadUserName;
	private String uploadTime;
	// XXX[희정]: uploadTime을 클라이언트에서 관리하는 이유를 묻고 parser 수정

	// String Type: String값, File Type: FileOriginName
	private String contentsValue;

	/**
	 * @author delf 임시 생성자
	 */
	public Contents(String contentsType, long contentsSize, String contentsPKName, String uploadUserName,
			String uploadTime) {
		this(contentsType, contentsSize, contentsPKName, uploadUserName, uploadTime, null);
	}
}
