package contents;

import java.io.File;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FileContents extends Contents {
	private File contents;
	private String contentsPath;
	private String contentsName;
	private long contentSize;
	//private boolean isDirectory;
	
	public FileContents(String contentsPath) {
		this.contents = new File(contentsPath);
		this.contentsPath = contentsPath;
		this.contentsName = contents.getName();
		if(contents.isFile()) {
			System.out.println("[ClipboardManager]전송 객체의 타입: 파일");
			//this.type = Contents.FILE_TYPE;
			this.contentSize = contents.length();
			//this.isDirectory = false;
		}
		else if(contents.isDirectory()) {
			System.out.println("[ClipboardManager]전송 객체의 타입: 디렉터리");
			//this.type = Contents.DIRECTORY_TYPE;
			getDirectorySize(this.contents);
			//this.isDirectory = true;
			this.compress(); // 압축
		}
	}
	
	public void getDirectorySize(File contents) {
		File[] fileList = contents.listFiles();
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isFile()) {
				this.contentSize += fileList[i].length();
			} else {
				getDirectorySize(fileList[i]);
			}
		}
	}
	
	public void compress() {
		try {
			long startTime = System.currentTimeMillis();
			
			DirectoryCompression.compress(contents);
			
			long endTime = System.currentTimeMillis();
			System.out.println("압축시간 : " + (endTime - startTime) + " ms");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
