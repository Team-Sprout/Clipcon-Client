package transfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import userInterface.scene.MainScene;

public class MultipleFileCompress {
	private static final int COMPRESSION_LEVEL = 1;
	private static final int BUFFER_SIZE = 4096;
	private static final String ZIP_FILE_PATH = MainScene.UPLOAD_TEMP_DIR_LOCATION + File.separator;
	private static final String ZIP_FILE_NAME = "ClipCon.zip";
	private static int lastIndex = 0;

	/**
	 * Compress the specified folder into a Zip file.
	 * @param sourcePath - Target directory to compress
	 * @param output - Name of zip file to save
	 * @throws Exception
	 * @return outputFileName - Full path with compressed files
	 */
	/* Compress multiple files in the same path */
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
		zos.setLevel(COMPRESSION_LEVEL); // Compression level - maximum compression ratio is 9, default 8

		try {
			for (int i = 0; i < fileFullPathList.size(); i++) {
				files[i] = new File(fileFullPathList.get(i));

				lastIndex = files[i].getPath().lastIndexOf(File.separator);

				// Returns if the target of compression is not a directory or file.
				if (!files[i].isFile() && !files[i].isDirectory()) {
					throw new Exception("I can not find the file to compress.");
				}
				zipEntry(files[i], files[i].getPath(), zos); // create Zip file
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
	 * compression
	 * @param sourceFile
	 * @param sourcePath
	 * @param zos
	 * @throws Exception
	 */
	private static void zipEntry(File file, String filePath, ZipOutputStream zos) throws Exception {
		// case: SourceFile is a directory, get a list of child files and recursively call
		if (file.isDirectory()) {
			// .metadata directory
			if (file.getName().equalsIgnoreCase(".metadata")) {
				return;
			}
			File[] fileArray = file.listFiles(); // sourceFile's child file list

			for (int i = 0; i < fileArray.length; i++) {
				zipEntry(fileArray[i], fileArray[i].getPath(), zos); // recursively call
			}
		}

		/// case: SourceFile is not a directory
		else {
			BufferedInputStream bis = null;

			try {
				String sFilePath = file.getPath();
				String zipEntryName = sFilePath.substring(lastIndex + 1, sFilePath.length());
				RetrofitUploadData.multipleFileListInfo += zipEntryName + "\n";
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
	 * Unzip the file.
	 *
	 * @param zipFile - Zip file you want to unzip
	 * @param targetDir - The directory containing the unpacked files
	 * @param fileNameToLowerCase - Whether filenames are lowercase
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
	 * Unzip one entry in the file.
	 *
	 * @param zis - Zip Input Stream
	 * @param filePath - Path to unpacked file
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
