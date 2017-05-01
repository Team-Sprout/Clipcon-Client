package contentsTransfer;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class UploadData {
	   //public final static String SERVER_URL = "http://182.172.16.118:8080/websocketServerModule";
	   // public final static String SERVER_URL = "http://223.194.157.244:8080/websocketServerModule";
		public final static String SERVER_URL = "http://211.210.238.157:8080/websocketServerModule";
	   public final static String SERVER_SERVLET = "/UploadServlet";
	   private String charset = "UTF-8";
	   
	   private String userName = null;
	   private String groupPK = null;

	   /** 생성자 userEmail과 groupPK를 설정한다. */
	   public UploadData(String userName, String groupPK) {
	      this.userName = userName;
	      this.groupPK = groupPK;
	   }
	   
	   /** String Data를 업로드 */
	   public void uploadStringData(String stringData) {
	      try {
	         MultipartUtility multipart = new MultipartUtility(SERVER_URL + SERVER_SERVLET, charset);
	         setCommonParameter(multipart);
	         
	         multipart.addFormField("stringData", stringData);

	         List<String> response = multipart.finish();
	         System.out.println("SERVER REPLIED");
	         // responseMsgLog();

	         for (String line : response) {
	            System.out.println(line);
	         }
	      } catch (IOException ex) {
	         System.err.println(ex);
	      }
	   }

	   /** Clipboard에 있는 Captured Image Data를 업로드 */
	   public void uploadCapturedImageData(Image capturedImageData) {
	      try {
	         MultipartUtility multipart = new MultipartUtility(SERVER_URL + SERVER_SERVLET, charset);
	         setCommonParameter(multipart);

	         System.out.println("<uploadCapturedImageData> getWidth: " + capturedImageData.getWidth(null));
	         System.out.println("<uploadCapturedImageData> getHeight: " + capturedImageData.getHeight(null));
	         multipart.addImagePart("imageData", capturedImageData);

	         List<String> response = multipart.finish();
	         System.out.println("SERVER REPLIED");
	         // responseMsgLog();

	         for (String line : response) {
	            System.out.println(line);
	         }

	      } catch (IOException ex) {
	         System.err.println(ex);
	      }
	   }

	   /** 여러 File Data를 업로드
	    * 
	    * @param dir 업로드할 파일의 위치
	    * @param dir 업로드할 파일명
	    */
	   public void uploadMultipartData(ArrayList<String> fileFullPathList) {

	      try {
	         MultipartUtility multipart = new MultipartUtility(SERVER_URL + SERVER_SERVLET, charset);
	         setCommonParameter(multipart);

	         // Iterator 통한 전체 조회
	         Iterator iterator = fileFullPathList.iterator();

	         // 여러 파일을 순서대로 처리
	         while (iterator.hasNext()) {
	            String fileFullPath = (String) iterator.next();

	            System.out.println("fileFullPathList: " + fileFullPath);
	            System.out.println();

	            // 업로드할 파일 생성
	            File uploadFile = new File(fileFullPath);

	            /* uploadFilename is the name of the sequence input variable in the called project the value is the name that will be given to the file */
	            multipart.addFilePart("multipartFileData", uploadFile);
	         }

	         List<String> response = multipart.finish();
	         System.out.println("SERVER REPLIED");
	         // responseMsgLog();

	         for (String line : response) {
	            System.out.println(line);
	         }
	      } catch (IOException ex) {
	         System.err.println(ex);
	      }
	   } 
	   
	   /** 모든 Data에서 공통으로 설정해야하는 Parameter
	    * userEmail, groupPK, uploadTime */
	   public void setCommonParameter(MultipartUtility multipart){
	      multipart.addHeaderField("User-Agent", "Heeee");
	      multipart.addFormField("userName", userName);
	      multipart.addFormField("groupPK", groupPK);
	      multipart.addFormField("uploadTime", uploadTime());
	   }
	   
	   /** @return YYYY-MM-DD HH:MM:SS 형식의 현재 시간 */
	   public String uploadTime() {
	      Calendar cal = Calendar.getInstance();
	      String year = Integer.toString(cal.get(Calendar.YEAR));
	      String month = Integer.toString(cal.get(Calendar.MONTH)+1);
	      
	      String date = Integer.toString(cal.get(Calendar.DATE));
	      String hour = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
	      if(Integer.parseInt(hour) < 10) {
	         hour = "0" + hour;
	      }
	      if(Integer.parseInt(hour) > 12) {
	         hour = "PM " + Integer.toString(Integer.parseInt(hour)-12);
	      }
	      else {
	         hour = "AM " + hour;
	      }
	      
	      String minute = Integer.toString(cal.get(Calendar.MINUTE));
	      if(Integer.parseInt(minute) < 10) {
	         minute = "0" + minute;
	      }
	      String sec = Integer.toString(cal.get(Calendar.SECOND));
	      if(Integer.parseInt(sec) < 10) {
	         sec = "0" + sec;
	      }

	      return year + "-" + month + "-" + date + " " + hour + ":" + minute + ":" + sec;
	   }
	}