package contentsTransfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import userInterface.MainScene;

//import com.sun.jna.platform.FileUtils;
//import org.apache.commons.io.FileUtils;

public class MultipleFileCompress {
	private static final int COMPRESSION_LEVEL = 1;
	private static final int BUFFER_SIZE = 4096;
	private static final String ZIP_FILE_PATH = MainScene.UPLOAD_TEMP_DIR_LOCATION + File.separator;
	private static final String ZIP_FILE_NAME = "Default.zip";
	private static int lastIndex = 0;

	/**
	 * 吏��젙�맂 �뤃�뜑瑜� Zip �뙆�씪濡� �븬異뺥븳�떎.
	 * @param sourcePath - �븬異� ���긽 �뵒�젆�넗由�
	 * @param output - ���옣 zip �뙆�씪 �씠由�
	 * @throws Exception
	 * @return outputFileName - �븬異� �뙆�씪�씠 �엳�뒗 �쟾泥� 寃쎈줈
	 */
	/* 媛숈� 寃쎈줈�뿉 �엳�뒗 蹂듭닔媛쒖쓽 �뙆�씪�쓣 �븬異� */
	@SuppressWarnings("finally")
	public static String compress(ArrayList<String> fileFullPathList) throws Exception {
		File[] files = new File[fileFullPathList.size()];
		String outputFileFullPath = ZIP_FILE_PATH + ZIP_FILE_NAME;

		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		ZipOutputStream zos = null;

		fos = new FileOutputStream(outputFileFullPath); // FileOutputStream
		bos = new BufferedOutputStream(fos); // BufferedStream
		zos = new ZipOutputStream(bos); // ZipOutputStream
		zos.setLevel(COMPRESSION_LEVEL); // �븬異� �젅踰� - 理쒕� �븬異뺣쪧�� 9, �뵒�뤃�듃 8

		System.out.println(fileFullPathList.size());

		try {
			for (int i = 0; i < fileFullPathList.size(); i++) {
				files[i] = new File(fileFullPathList.get(i));
				System.out.println("----------------------�깮�꽦�븳 file�쓽 path: " + files[i].getPath());

				lastIndex = files[i].getPath().lastIndexOf(File.separator);

				// �븬異� ���긽�씠 �뵒�젆�넗由щ굹 �뙆�씪�씠 �븘�땲硫� 由ы꽩�븳�떎.
				if (!files[i].isFile() && !files[i].isDirectory()) {
					throw new Exception("�븬異� ���긽�쓽 �뙆�씪�쓣 李얠쓣 �닔媛� �뾾�뒿�땲�떎.");
				}
				zipEntry(files[i], files[i].getPath(), zos); // Zip �뙆�씪 �깮�꽦
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
	 * �븬異�
	 * @param sourceFile
	 * @param sourcePath
	 * @param zos
	 * @throws Exception
	 */
	private static void zipEntry(File file, String filePath, ZipOutputStream zos) throws Exception {
		// sourceFile�씠 �뵒�젆�넗由ъ씤 寃쎌슦 �븯�쐞 �뙆�씪 由ъ뒪�듃 媛��졇�� �옱洹��샇異�
		if (file.isDirectory()) {
			// .metadata �뵒�젆�넗由�
			if (file.getName().equalsIgnoreCase(".metadata")) {
				return;
			}
			File[] fileArray = file.listFiles(); // sourceFile �쓽 �븯�쐞 �뙆�씪 由ъ뒪�듃

			for (int i = 0; i < fileArray.length; i++) {
				zipEntry(fileArray[i], fileArray[i].getPath(), zos); // �옱洹� �샇異�
			}
		}

		/// sourceFile�씠 �뵒�젆�넗由ш� �븘�땶 寃쎌슦
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

	/**
	 * Zip 파일의 압축을 푼다.
	 *
	 * @param zipFile - 압축 풀 Zip 파일
	 * @param targetDir - 압축 푼 파일이 들어간 디렉토리
	 * @param fileNameToLowerCase - 파일명을 소문자로 바꿀지 여부
	 * @throws Exception
	 */
	public static void unzip(File zipFile, File targetDir, boolean fileNameToLowerCase) throws Exception {
		FileInputStream fis = null;
		ZipInputStream zis = null;
		ZipEntry zentry = null;

		try {
			fis = new FileInputStream(zipFile); // FileInputStream
			zis = new ZipInputStream(fis); // ZipInputStream

			while ((zentry = zis.getNextEntry()) != null) {
				String fileNameToUnzip = zentry.getName();
				System.out.println("====fileNameToUnzip: " + fileNameToUnzip);
				
				// fileName toLowerCase
				if (fileNameToLowerCase) {
					fileNameToUnzip = fileNameToUnzip.toLowerCase();
				}

				File targetFile = new File(targetDir, fileNameToUnzip);
				File targetFileDir = null;

				// case: Directory. make directory
				if (zentry.isDirectory()) {
					targetFileDir = new File(targetFile.getAbsolutePath());
					System.out.println("====targetFileDir(DIR): " + targetFile.getAbsolutePath());
					targetFileDir.mkdir();
					// FileUtils.forceMkdir(targetFile.getAbsolutePath());
				}
				// case: File. make parent directory
				else{
					targetFileDir = new File(targetFile.getParent());
					System.out.println("====targetFileDir(FILE): " + targetFile.getParent());
					targetFileDir.mkdir();
					// FileUtils.makeDir(targetFile.getParent());
					unzipEntry(zis, targetFile);
				}
			}
		} finally {
			if (zis != null) {
				zis.close();
			}
			if (fis != null) {
				fis.close();
			}
		}
	}

	/**
	 * Zip 파일의 한 개 엔트리의 압축을 푼다.
	 *
	 * @param zis - Zip Input Stream
	 * @param filePath - 압축 풀린 파일의 경로
	 * @return
	 * @throws Exception
	 */
	protected static File unzipEntry(ZipInputStream zis, File targetFile) throws Exception {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(targetFile);

			byte[] buffer = new byte[BUFFER_SIZE];
			int len = 0;
			while ((len = zis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
		return targetFile;
	}
}
