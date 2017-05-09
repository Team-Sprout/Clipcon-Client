package contentsTransfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MultipleFileCompress {
	private static final int COMPRESSION_LEVEL = 1;
	private static final int BUFFER_SIZE = 4096;
	private static final String ZIP_FILE_PATH = "C:\\Program Files\\TempCilpcon\\";
	private static int lastIndex = 0;

	/**
	 * 지정된 폴더를 Zip 파일로 압축한다.
	 * @param sourcePath - 압축 대상 디렉토리
	 * @param output - 저장 zip 파일 이름
	 * @throws Exception
	 * @return outputFileName - 압축 파일이 있는 전체 경로
	 */
	/* 같은 경로에 있는 복수개의 파일을 압축 */
	@SuppressWarnings("finally")
	public static String compress(ArrayList<String> fileFullPathList) throws Exception {
		File[] files = new File[fileFullPathList.size()];
		String outputFileFullPath = ZIP_FILE_PATH + "Default.zip";

		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		ZipOutputStream zos = null;

		fos = new FileOutputStream(outputFileFullPath); // FileOutputStream
		bos = new BufferedOutputStream(fos); // BufferedStream
		zos = new ZipOutputStream(bos); // ZipOutputStream
		zos.setLevel(COMPRESSION_LEVEL); // 압축 레벨 - 최대 압축률은 9, 디폴트 8

		System.out.println(fileFullPathList.size());

		try {
			for (int i = 0; i < fileFullPathList.size(); i++) {
				files[i] = new File(fileFullPathList.get(i));
				System.out.println("----------------------생성한 file의 path: " + files[i].getPath());

				lastIndex = files[i].getPath().lastIndexOf(File.separator);

				// 압축 대상이 디렉토리나 파일이 아니면 리턴한다.
				if (!files[i].isFile() && !files[i].isDirectory()) {
					throw new Exception("압축 대상의 파일을 찾을 수가 없습니다.");
				}
				zipEntry(files[i], files[i].getPath(), zos); // Zip 파일 생성
			}
		} finally {
			if (zos != null) {
				zos.closeEntry();
				zos.finish(); // ZipOutputStream finish
				zos.close();
			}
			if (bos != null) {
				bos.close();
			}
			if (fos != null) {
				fos.close();
			}
			return outputFileFullPath;
		}
	}

	/**
	 * 압축
	 * @param sourceFile
	 * @param sourcePath
	 * @param zos
	 * @throws Exception
	 */
	private static void zipEntry(File file, String filePath, ZipOutputStream zos) throws Exception {
		// sourceFile이 디렉토리인 경우 하위 파일 리스트 가져와 재귀호출
		if (file.isDirectory()) {
			// .metadata 디렉토리
			if (file.getName().equalsIgnoreCase(".metadata")) {
				return;
			}
			File[] fileArray = file.listFiles(); // sourceFile 의 하위 파일 리스

			for (int i = 0; i < fileArray.length; i++) {
				zipEntry(fileArray[i], fileArray[i].getPath(), zos); // 재귀 호출
			}
		}

		// sourceFile이 디렉토리가 아닌 경우
		else {
			BufferedInputStream bis = null;

			try {
				String sFilePath = file.getPath();
				String zipEntryName = sFilePath.substring(lastIndex + 1, sFilePath.length());
				System.out.println("zipEntry <<sFilePath>>: " + sFilePath);
				System.out.println("zipEntry <<zipEntryName>>: " + zipEntryName);
				// String zipEntryName = sFilePath.substring(filePath.length() + 1, sFilePath.length());

				bis = new BufferedInputStream(new FileInputStream(file));
				ZipEntry zentry = new ZipEntry(zipEntryName);
				zentry.setTime(file.lastModified());
				zos.putNextEntry(zentry);

				byte[] buffer = new byte[BUFFER_SIZE];
				int cnt = 0;

				while ((cnt = bis.read(buffer, 0, BUFFER_SIZE)) != -1) {
					zos.write(buffer, 0, cnt);
				}
			} finally {
				if (bis != null) {
					bis.close();
				}
			}
		}
	}
}
