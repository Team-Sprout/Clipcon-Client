package contents;

import java.awt.Image;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ImageContents extends Contents {
	private Image contents;
	private int contentsWidth;
	private int contentsHeight;
	
	public ImageContents(Image contents) {
		//this.type = Contents.IMAGE_TYPE;
		this.contents = contents;
		this.contentsWidth = contents.getWidth(null);
		this.contentsHeight = contents.getHeight(null);
	}
}
