package contents;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DirectoryCompression {
	/**
	 * 압축 메소드
	 *
	 * @param path
	 *            경로
	 * @param outputFileName
	 *            출력파일명
	 */
	public static void compress(File file) throws Throwable {
		//File file = new File(path);
		String outputFileName = file.getName() + ".zip";
		int pos = outputFileName.lastIndexOf(".");
		if (!outputFileName.substring(pos).equalsIgnoreCase(".zip")) {
			outputFileName += ".zip";
		}
		// 압축 경로 체크
		if (!file.exists()) {
			throw new Exception("Not File!");
		}
		
		FileOutputStream fos = null; // 출력 스트림
		ZipOutputStream zos = null; // 압축 스트림
		
		try {
			fos = new FileOutputStream(new File(outputFileName));
			zos = new ZipOutputStream(fos);
			// 디렉토리 검색
			searchDirectory(file, file.getPath(), zos);
		} catch (Throwable e) {
			throw e;
		} finally {
			if (zos != null)
				zos.close();
			if (fos != null)
				fos.close();
		}
	}

	/**
	 * 디렉토리 탐색
	 *
	 * @param file
	 *            현재 파일
	 * @param root
	 *            루트 경로
	 * @param zos
	 *            압축 스트림
	 */
	private static void searchDirectory(File file, String root, ZipOutputStream zos) throws Exception {
		if (file.isDirectory()) { // 지정된 파일이 디렉토리인지 파일인지 검색
			// 디렉토리일 경우 재탐색(재귀)
			File[] files = file.listFiles();
			for (File f : files) {
				searchDirectory(f, root, zos);
			}
		} else {
			// 파일일 경우 압축을 한다.
			compressZip(file, root, zos);
		}
	}

	/**
	 * 압축 메소드
	 *
	 * @param file
	 * @param root
	 * @param zos
	 * @throws Exception
	 */

	private static void compressZip(File file, String root, ZipOutputStream zos) throws Exception {
		FileInputStream fis = null;
		try {
			String zipName = file.getPath().replace(root + "\\", "");
			fis = new FileInputStream(file); // 파일을 읽어드림
			ZipEntry zipentry = new ZipEntry(zipName); // Zip엔트리 생성(한글 깨짐 버그)
			zos.putNextEntry(zipentry); // 스트림에 밀어넣기(자동 오픈)
			
			int length = (int) file.length();
			byte[] buffer = new byte[length];
			
			fis.read(buffer, 0, length); // 스트림 읽어드리기
			zos.write(buffer, 0, length); // 스트림 작성
			zos.closeEntry(); // 스트림 닫기

		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			if (fis != null)
				fis.close();
		}
	}
}
