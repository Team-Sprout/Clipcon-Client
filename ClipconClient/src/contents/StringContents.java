package contents;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StringContents extends Contents {
	private String contents;
	private int contentsLength;
	
	public StringContents(String contents) {
		//this.type = Contents.STRING_TYPE;
		this.contents = contents;
		contentsLength = contents.length();
	}
}
