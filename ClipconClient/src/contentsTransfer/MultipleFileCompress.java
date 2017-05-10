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
	private static final String ZIP_FILE_PATH = MainScene.CLIPCON_DIR_LOCATION + File.separator;
	private static final String ZIP_FILE_NAME = "Default.zip";
	private static int lastIndex = 0;

	/**
	 * ì§€ì •ëœ í´ë”ë¥¼ Zip íŒŒì¼ë¡œ ì••ì¶•í•œë‹¤.
	 * @param sourcePath - ì••ì¶• ëŒ€ìƒ ë””ë ‰í† ë¦¬
	 * @param output - ì €ì¥ zip íŒŒì¼ ì´ë¦„
	 * @throws Exception
	 * @return outputFileName - ì••ì¶• íŒŒì¼ì´ ìˆëŠ” ì „ì²´ ê²½ë¡œ
	 */
	/* ê°™ì€ ê²½ë¡œì— ìˆëŠ” ë³µìˆ˜ê°œì˜ íŒŒì¼ì„ ì••ì¶• */
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
		zos.setLevel(COMPRESSION_LEVEL); // ì••ì¶• ë ˆë²¨ - ìµœëŒ€ ì••ì¶•ë¥ ì€ 9, ë””í´íŠ¸ 8

		System.out.println(fileFullPathList.size());

		try {
			for (int i = 0; i < fileFullPathList.size(); i++) {
				files[i] = new File(fileFullPathList.get(i));
				System.out.println("----------------------ìƒì„±í•œ fileì˜ path: " + files[i].getPath());

				lastIndex = files[i].getPath().lastIndexOf(File.separator);

				// ì••ì¶• ëŒ€ìƒì´ ë””ë ‰í† ë¦¬ë‚˜ íŒŒì¼ì´ ì•„ë‹ˆë©´ ë¦¬í„´í•œë‹¤.
				if (!files[i].isFile() && !files[i].isDirectory()) {
					throw new Exception("ì••ì¶• ëŒ€ìƒì˜ íŒŒì¼ì„ ì°¾ì„ ìˆ˜ê°€ ì—†ìŠµë‹ˆë‹¤.");
				}
				zipEntry(files[i], files[i].getPath(), zos); // Zip íŒŒì¼ ìƒì„±
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
	 * ì••ì¶•
	 * @param sourceFile
	 * @param sourcePath
	 * @param zos
	 * @throws Exception
	 */
	private static void zipEntry(File file, String filePath, ZipOutputStream zos) throws Exception {
		// sourceFileì´ ë””ë ‰í† ë¦¬ì¸ ê²½ìš° í•˜ìœ„ íŒŒì¼ ë¦¬ìŠ¤íŠ¸ ê°€ì ¸ì™€ ì¬ê·€í˜¸ì¶œ
		if (file.isDirectory()) {
			// .metadata ë””ë ‰í† ë¦¬
			if (file.getName().equalsIgnoreCase(".metadata")) {
				return;
			}
			File[] fileArray = file.listFiles(); // sourceFile ì˜ í•˜ìœ„ íŒŒì¼ ë¦¬ìŠ¤íŠ¸

			for (int i = 0; i < fileArray.length; i++) {
				zipEntry(fileArray[i], fileArray[i].getPath(), zos); // ì¬ê·€ í˜¸ì¶œ
			}
		}

		/// sourceFileì´ ë””ë ‰í† ë¦¬ê°€ ì•„ë‹Œ ê²½ìš°
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
	 * Zip ÆÄÀÏÀÇ ¾ĞÃàÀ» Ç¬´Ù.
	 *
	 * @param zipFile - ¾ĞÃà Ç® Zip ÆÄÀÏ
	 * @param targetDir - ¾ĞÃà Ç¬ ÆÄÀÏÀÌ µé¾î°£ µğ·ºÅä¸®
	 * @param fileNameToLowerCase - ÆÄÀÏ¸íÀ» ¼Ò¹®ÀÚ·Î ¹Ù²ÜÁö ¿©ºÎ
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
				// fileName toLowerCase
				if (fileNameToLowerCase) {
					fileNameToUnzip = fileNameToUnzip.toLowerCase();
				}

				File targetFile = new File(targetDir, fileNameToUnzip);
				File targetFileDir = null;

				// case: Directory. make directory
				if (zentry.isDirectory()) {
					targetFileDir = new File(targetFile.getAbsolutePath());
					targetFileDir.mkdir();
					// FileUtils.forceMkdir(targetFile.getAbsolutePath());
				}
				// case: File. make parent directory
				else {
					targetFileDir = new File(targetFile.getParent());
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
	 * Zip ÆÄÀÏÀÇ ÇÑ °³ ¿£Æ®¸®ÀÇ ¾ĞÃàÀ» Ç¬´Ù.
	 *
	 * @param zis - Zip Input Stream
	 * @param filePath - ¾ĞÃà Ç®¸° ÆÄÀÏÀÇ °æ·Î
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
