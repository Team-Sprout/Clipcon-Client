package userInterface;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import application.Main;
import javafx.application.Platform;

public class TrayIconManager {

	private final SystemTray systemTray = SystemTray.getSystemTray(); // 시스템트레이 얻어옴

	private Image trayIconImage; // 트레이아이콘 이미지
	private PopupMenu trayIconMenu; // 트레이아이콘 우클릭 메뉴
	private MenuItem menuItem; // 트레이아이콘 우클릭 메뉴 항목
	private MouseListener mouseListener; // 트레이아이콘 마우스 리스너
	private TrayIcon trayIcon; // 트레이아이콘

	private ActionListener closeListener;
	private ActionListener showListener;

	public TrayIconManager() {
		trayIconImage = Toolkit.getDefaultToolkit().getImage("src/resources/trayIcon.png"); // 트레이아이콘 이미지
		trayIconMenu = new PopupMenu();
		trayIcon = new TrayIcon(trayIconImage, "ClipCon", trayIconMenu);
	}

	/** 트레이아이콘을 시스템트레이에 추가 */
	public void addTrayIconInSystemTray() {
		if (SystemTray.isSupported()) { // 시스템 트레이가 지원되면
			setEventListener();
			setMenu();

			try {
				trayIcon.setImageAutoSize(true); // 트레이 아이콘 크기 자동 조절
				trayIcon.addActionListener(showListener);
				trayIcon.addMouseListener(mouseListener);
				systemTray.add(trayIcon); // 시스템 트레이에 트레이 아이콘 추가
			} catch (AWTException e) {
				e.printStackTrace();
			}

		} else {
			System.err.println("Tray unavailable");
		}
	}

	/** 트레이아이콘 이벤트 설정 */
	public void setEventListener() {

		// create a action listener to listen for default action executed on the tray icon
		closeListener = new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				System.exit(0);
			}
		};

		showListener = new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						Main.getPrimaryStage().show();
					}
				});
			}
		};
		
		mouseListener = new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (MouseEvent.MOUSE_PRESSED == 2) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							Main.getPrimaryStage().show();
						}
					});
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				
			}

		};
		
		/* 트레이 아이콘 마우스 리스너 */
		// mouseListener = new MouseAdapter() {
		// public void mousePressed(MouseEvent e) {
		// if (e.getClickCount() == 2) { // 트레이 아이콘을 더블 클릭하면
		// stage.show(); // stage를 보여줌
		// }
		// }
		// };
	}

	/** 트레이아이콘 우클릭 메뉴 설정 */
	public void setMenu() {
		menuItem = new MenuItem("Close"); // 프로그램 종료
		menuItem.addActionListener(closeListener);

		trayIconMenu.add(menuItem);
		
		menuItem = new MenuItem("Show App"); // show program
		menuItem.addActionListener(showListener);
		
		trayIconMenu.add(menuItem);
	}
}
