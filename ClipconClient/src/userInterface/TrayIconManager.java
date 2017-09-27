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
import javafx.application.Platform;
import model.message.Message;
import server.Endpoint;

public class TrayIconManager {

	private final SystemTray systemTray = SystemTray.getSystemTray(); // get system tray

	private URL trayIconImageURL;
	private ImageIcon trayIconImageIcon;
	private Image trayIconImage;
	private PopupMenu trayIconMenu;
	private MenuItem menuItem;
	private MouseListener mouseListener;
	private TrayIcon trayIcon;

	private ActionListener closeListener;
	private ActionListener showListener;
	
	private Endpoint endpoint = Endpoint.getInstance();

	public TrayIconManager() {
		trayIconImageURL = Main.class.getResource("/resources/trayIcon.png");
		trayIconImageIcon = new ImageIcon(trayIconImageURL);
		trayIconImage = trayIconImageIcon.getImage();
		trayIconMenu = new PopupMenu();
		trayIcon = new TrayIcon(trayIconImage, "ClipCon", trayIconMenu);
	}

	/** Add tray icon to system tray */
	public void addTrayIconInSystemTray() {
		if (SystemTray.isSupported()) {
			setEventListener();
			setMenu();

			try {
				trayIcon.setImageAutoSize(true);
				trayIcon.addActionListener(showListener);
				trayIcon.addMouseListener(mouseListener);
				systemTray.add(trayIcon);
			} catch (AWTException e) {
				e.printStackTrace();
			}

		} else {
			System.err.println("Tray unavailable");
		}
	}

	/** Tray icon event handling */
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

		
		// Tray icon mouse listener */
		mouseListener = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 2) { // Double-click the tray icon
					Platform.runLater(() -> {
						Main.getPrimaryStage().show();
					});
				}
			}
		};
	}

	/** Tray icon right-click event handling */
	public void setMenu() {
		menuItem = new MenuItem("Close"); // program exit
		menuItem.addActionListener(closeListener);

		trayIconMenu.add(menuItem);
		
		menuItem = new MenuItem("Show App"); // show program
		menuItem.addActionListener(showListener);
		
		trayIconMenu.add(menuItem);
	}
}
