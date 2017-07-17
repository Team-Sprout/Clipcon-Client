package userInterface;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.websocket.EncodeException;

import application.Main;
import controller.Endpoint;
import javafx.application.Platform;
import model.Message;

public class TrayIconManager {

	private final SystemTray systemTray = SystemTray.getSystemTray(); // 시스템트레이 얻어옴

	private URL trayIconImageURL;
	private ImageIcon trayIconImageIcon;
	private Image trayIconImage; // 트레이아이콘 이미지
	private PopupMenu trayIconMenu; // 트레이아이콘 우클릭 메뉴
	private MenuItem menuItem; // 트레이아이콘 우클릭 메뉴 항목
	private MouseListener mouseListener; // 트레이아이콘 마우스 리스너
	private TrayIcon trayIcon; // 트레이아이콘

	private ActionListener closeListener;
	private ActionListener showListener;
	
	private Endpoint endpoint = Endpoint.getInstance();

	public TrayIconManager() {
		trayIconImageURL = Main.class.getResource("/resources/trayIcon.png");
		trayIconImageIcon = new ImageIcon(trayIconImageURL); // 트레이아이콘 이미지
		trayIconImage = trayIconImageIcon.getImage();
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
				Message exitProgramMsg = new Message().setType(Message.REQUEST_EXIT_PROGRAM);
				try {
					endpoint.sendMessage(exitProgramMsg);
				} catch (IOException | EncodeException e1) {
					e1.printStackTrace();
				}
				System.exit(0);
			}
		};

		showListener = new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Platform.runLater(() -> {
					Main.getPrimaryStage().show();
				});
			}
		};

		
		/* 트레이 아이콘 마우스 리스너 */
		mouseListener = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 2) { // 트레이 아이콘을 더블 클릭하면
					Platform.runLater(() -> {
						Main.getPrimaryStage().show();
					});
				}
			}
		};
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
