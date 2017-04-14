package contents;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DirectoryCompression {
	private static final int COMPRESSION_LEVEL = 1;
    private static final int BUFFER_SIZE = 1024 * 2;
    
    /**
     * 지정된 폴더를 Zip 파일로 압축한다.
     * @param sourcePath - 압축 대상 디렉토리
     * @param output - 저장 zip 파일 이름
     * @throws Exception
     */
    public static void compress(File file) throws Exception {

    	String filePath = file.getPath();
    	String outputFileName = filePath + ".zip";
    	System.out.println("outputFileName : " + outputFileName);
    	
        // 압축 대상이 디렉토리나 파일이 아니면 리턴한다.
        if (!file.isFile() && !file.isDirectory()) {
            throw new Exception("압축 대상의 파일을 찾을 수가 없습니다.");
        }

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ZipOutputStream zos = null;

        try {
            fos = new FileOutputStream(outputFileName); // FileOutputStream
            bos = new BufferedOutputStream(fos); // BufferedStream
            zos = new ZipOutputStream(bos); // ZipOutputStream
            zos.setLevel(COMPRESSION_LEVEL); // 압축 레벨 - 최대 압축률은 9, 디폴트 8
            zipEntry(file, filePath, zos); // Zip 파일 생성
            zos.finish(); // ZipOutputStream finish
        } finally {
            if (zos != null) {
                zos.close();
            }
            if (bos != null) {
                bos.close();
            }
            if (fos != null) {
                fos.close();
            }
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
        // sourceFile 이 디렉토리인 경우 하위 파일 리스트 가져와 재귀호출
        if (file.isDirectory()) {
            if (file.getName().equalsIgnoreCase(".metadata")) { // .metadata 디렉토리 return
                return;
            }
            File[] fileArray = file.listFiles(); // sourceFile 의 하위 파일 리스트
            for (int i = 0; i < fileArray.length; i++) {
                zipEntry(fileArray[i], filePath, zos); // 재귀 호출
            }
        } else { // sourcehFile 이 디렉토리가 아닌 경우
            BufferedInputStream bis = null;
            try {
                String sFilePath = file.getPath();
                String zipEntryName = sFilePath.substring(filePath.length() + 1, sFilePath.length());

                bis = new BufferedInputStream(new FileInputStream(file));
                ZipEntry zentry = new ZipEntry(zipEntryName);
                zentry.setTime(file.lastModified());
                zos.putNextEntry(zentry);

                byte[] buffer = new byte[BUFFER_SIZE];
                int cnt = 0;
                while ((cnt = bis.read(buffer, 0, BUFFER_SIZE)) != -1) {
                    zos.write(buffer, 0, cnt);
                }
                zos.closeEntry();
            } finally {
                if (bis != null) {
                    bis.close();
                }
            }
        }
    }

}
