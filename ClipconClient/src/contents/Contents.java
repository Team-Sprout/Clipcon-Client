package contents;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Contents {
	public final static int STRING_TYPE = 0;
	public final static int IMAGE_TYPE = 1;
	public final static int FILE_TYPE = 2;
	public final static int DIRECTORY_TYPE = 3;
	
	protected int type;
}
