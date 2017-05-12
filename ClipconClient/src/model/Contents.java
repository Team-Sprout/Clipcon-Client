package model;

import java.text.DecimalFormat;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
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
	public final static String TYPE_STRING = "STRING";
	public final static String TYPE_IMAGE = "IMAGE";
	public final static String TYPE_FILE = "FILE";
	public final static String TYPE_MULTIPLE_FILE = "MULTIPLE_FILE";

	private String contentsType;
	private long contentsSize;
	private String contentsConvertedSize;

	// A unique key value that distinguishes each data in the group
	public String contentsPKName;

	private String uploadUserName;
	private String uploadTime;

	// String Type: String object value, File Type: FileOriginName
	private String contentsValue;
	private Image contentsImage;

	private StringProperty typeProperty;
	private StringProperty uploaderProperty;

	/** Constructor */
	public Contents(String contentsType, long contentsSize, String contentsPKName, String uploadUserName, String uploadTime, String contentsValue, Image contentsImage) {
		this.contentsType = contentsType;
		this.contentsSize = contentsSize;
		this.contentsConvertedSize = convertContentsSize(contentsSize);
		this.contentsPKName = contentsPKName;
		this.uploadUserName = uploadUserName;
		this.uploadTime = uploadTime;
		if (contentsType.equals(Contents.TYPE_IMAGE)) {
			this.contentsValue = "Image";
		} else {
			this.contentsValue = contentsValue;
		}
		this.contentsImage = contentsImage;

		this.typeProperty = new SimpleStringProperty(contentsType);
		this.uploaderProperty = new SimpleStringProperty(uploadUserName);
	}

	/** convert contents size format */
	public String convertContentsSize(long size) {
		String contentsConvertedSize;

		double b = size;
		double k = size / 1024.0;
		double m = ((size / 1024.0) / 1024.0);
		double g = (((size / 1024.0) / 1024.0) / 1024.0);
		double t = ((((size / 1024.0) / 1024.0) / 1024.0) / 1024.0);

		DecimalFormat dec = new DecimalFormat("0.00");

		if (t > 1) {
			contentsConvertedSize = dec.format(t).concat(" TB");
		} else if (g > 1) {
			contentsConvertedSize = dec.format(g).concat(" GB");
		} else if (m > 1) {
			contentsConvertedSize = dec.format(m).concat(" MB");
		} else if (k > 1) {
			contentsConvertedSize = dec.format(k).concat(" KB");
		} else {
			contentsConvertedSize = dec.format(b).concat(" Bytes");
		}

		return contentsConvertedSize;
	}
}
