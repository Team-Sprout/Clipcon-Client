package controller;

import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

import model.Contents;

public class ClipboardController {
	private static Clipboard systemClipboard; // 자신의 시스템 클립보드
	//private static Contents contents;
	private static String type; // 데이터 타입
	
	/**
	 * 시스템 클립보드에서 Transferable 객체를 읽어와 데이터 타입을 알아내고 Contents 객체로 변환
	 * 
	 * @return settingObject 서버에 전송할 Contents 객체
	 */
	public static Object readClipboard() {
		Transferable t = getSystmeClipboardContets();
		setDataFlavor(t);
		Object clipboardData = extractDataFromTransferable(t);

		return clipboardData;
	}
	
	/**
	 * 시스템 클립보드의 Transferable 객체 리턴
	 * 
	 * @return 시스템 클립보드에 존재하는 Transferable 객체
	 */
	public static Transferable getSystmeClipboardContets() {
		systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		return systemClipboard.getContents(null);
	}
	
	/**
	 * 클립보드의 Transferable 객체가 어떤 타입인지 set하고 리턴
	 * 
	 * @param t
	 *            클립보드의 Transferable 객체
	 * @return t 의 DataFlavor의 종류
	 */
	public static DataFlavor setDataFlavor(Transferable t) {
		DataFlavor[] flavors = t.getTransferDataFlavors();

		for (int i = 0; i < flavors.length; i++) {

			if (flavors[i].equals(DataFlavor.stringFlavor)) {
				type = Contents.TYPE_STRING;
				return DataFlavor.stringFlavor;
			} else if (flavors[i].equals(DataFlavor.imageFlavor)) {
				type = Contents.TYPE_IMAGE;
				return DataFlavor.imageFlavor;
			} else if (flavors[i].equals(DataFlavor.javaFileListFlavor)) {
				type = Contents.TYPE_FILE;
				return DataFlavor.javaFileListFlavor;
			} else {
			}
		}
		return null;
	}
	
	/**
	 * 클립보드의 Transferable 객체를 전송스트링으로 바꿈
	 * 
	 * @param contents
	 *            클립보드의 Transferable 객체
	 * @return sendObject 실제 전송 객체(Contents 타입) 리턴
	 */
	private static Object extractDataFromTransferable(Transferable t) {
		try {
			Object extractData = null;

			// 클립보드의 내용을 추출
			if (type.equals(Contents.TYPE_STRING)) {
				System.out.println("[ClipboardManager]클립보드 내용 타입: 문자열");
				//contents = new StringContents((String) t.getTransferData(DataFlavor.stringFlavor));
				//extractString = contents.toString();
				extractData = (String) t.getTransferData(DataFlavor.stringFlavor);
				
			} else if (type.equals(Contents.TYPE_IMAGE)) {
				System.out.println("[ClipboardManager]클립보드 내용 타입: 이미지");
				//contents = new ImageContents((Image) t.getTransferData(DataFlavor.imageFlavor));
				//extractString = contents.toString();
				extractData = (Image) t.getTransferData(DataFlavor.imageFlavor);
				
			} else if (type.equals(Contents.TYPE_FILE)) {
				String [] filePath = getFilePathInSystemClipboard().split(", ");
				
				ArrayList<String> filePathList = new ArrayList<String>();
				
				for(int i=0; i<filePath.length; i++) {
					filePathList.add(filePath[i]);
				}
				
				extractData = filePathList;

//				if (filePathList.size() == 1) {
//					contents = new FileContents(filePath[0]);
//					extractString = contents.toString();
//				} else {
//					System.out.println("[ClipboardManager]클립보드 내용 타입: 여러개의 파일 또는 디렉터리");
//					extractString = "";
//
//					Contents [] contentsList = new FileContents [filePath.length];
//					
//					for (int i = 0; i < filePath.length; i++) {
//						contentsList[i] = new FileContents(filePath[i]);
//						extractString += contentsList[i].toString() + "\n";
//					}
//				}
			} else {
				System.out.println("[ClipboardManager]클립보드 내용 타입: 문자열, 이미지, 파일, 디렉터리가 아님");
			}

			return extractData;

		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/** @return 클립보드에 있는 파일의 경로명 */
	private static String getFilePathInSystemClipboard() {
		try {
			Transferable contents = getSystmeClipboardContets(); // 시스템 클립보드에서 내용을 추출
			String fileTotalPath = contents.getTransferData(DataFlavor.javaFileListFlavor).toString();
			return fileTotalPath.substring(1, fileTotalPath.length() - 1); // 경로명만 얻어오기 위해 양 끝의 []를 제거
		} catch (HeadlessException e) {
			e.printStackTrace();
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 전송받은 Contents 객체를 Transferable해서 클립보드에 삽입(문자열, 이미지인 경우)
	 *
	 * @param data
	 *            전송받은 데이터
	 * @param dataType
	 *            전송받은 데이터 타입
	 */
	
	public static void writeClipboard(Transferable transferable) {
		systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		systemClipboard.setContents(transferable, null); // 시스템 클립보드에 삽입
	}
}

