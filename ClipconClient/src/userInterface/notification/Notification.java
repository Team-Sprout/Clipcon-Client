package userInterface.notification;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class Notification {
	protected String title;
	protected Image resizeImage;
	protected String message;
	
	protected static double width;
	protected static double height;
	protected static double offsetX;
	protected static double offsetY;
	protected static double spacingY = 5;
	protected static Pos popupLocation = Pos.BOTTOM_RIGHT;
	protected static Stage stageRef = null;
	protected Duration popupLifetime;
	protected Duration popupAnimationTime = Duration.millis(500);
	protected Scene scene;
	protected Popup popup;
	protected Timeline timeline;
	protected boolean isShowing = false;
	
	protected KeyValue fadeOutBegin;
	protected KeyValue fadeOutEnd;
	protected KeyFrame kfBegin;
	protected KeyFrame kfEnd;
	
	public Notification(String title, String message) {
		this.title = title;
		this.resizeImage = null;
		this.message = message;
	}

	public Notification(String title, Image resizeImage) {
		this.title = title;
		this.resizeImage = resizeImage;
		this.message = null;
	}
	
	protected abstract void init();
	
	protected void initGraphics(Stage stage) {
		scene = new Scene(new Region());
		scene.setFill(null);
		
		stage.setMaxWidth(1);
		stage.setMaxHeight(1);
		stage.setWidth(1);
		stage.setHeight(1);
		stage.getIcons().add(new javafx.scene.image.Image("resources/Logo.png"));
		
		stage.setResizable(false);
		stage.setAlwaysOnTop(true);
	}
	
	public void setNotificationOwner(Stage stage, Stage owner) {
		stage.initOwner(owner);
	}
	
	public boolean getIsShowing() {
		return isShowing;
	}

//	public void stop() {
//		popups.clear();
//	}
	
	public void startNotify() {
		preOrder();
		showPopup();
	}
	
	protected abstract void preOrder();

	protected abstract void showPopup();
	
	public void hidePopUp() {
		popup.hide();
        timeline.stop();
        isShowing = false;
    }
	
	public double getX() {
		if (null == stageRef)
			return calcX(0.0, Screen.getPrimary().getBounds().getWidth());
		return calcX(stageRef.getX(), stageRef.getWidth());
	}
	
	public double getY() {
		if (null == stageRef)
			return calcY(0.0, Screen.getPrimary().getBounds().getHeight());
		return calcY(stageRef.getY(), stageRef.getHeight());
	}

	public double calcX(double left, double totalWidth) {
		switch (popupLocation) {
		case TOP_LEFT:
		case CENTER_LEFT:
		case BOTTOM_LEFT:
			return left + offsetX;
		case TOP_CENTER:
		case CENTER:
		case BOTTOM_CENTER:
			return left + (totalWidth - width) * 0.5 - offsetX;
		case TOP_RIGHT:
		case CENTER_RIGHT:
		case BOTTOM_RIGHT:
			return left + totalWidth - width - offsetX;
		default:
			return 0.0;
		}
	}

	public double calcY(final double top, final double totalHeight) {
		switch (popupLocation) {
		case TOP_LEFT:
		case TOP_CENTER:
		case TOP_RIGHT:
			return top + offsetY;
		case CENTER_LEFT:
		case CENTER:
		case CENTER_RIGHT:
			return top + (totalHeight - height) / 2 - offsetY;
		case BOTTOM_LEFT:
		case BOTTOM_CENTER:
		case BOTTOM_RIGHT:
			return top + totalHeight - height - offsetY;
		default:
			return 0.0;
		}
	}
	
	public ObjectProperty<EventHandler<NotificationEvent>> onNotificationPressedProperty() {
		return onNotificationPressed;
	}

	public void setOnNotificationPressed(EventHandler<NotificationEvent> value) {
		onNotificationPressedProperty().set(value);
	}

	public EventHandler<NotificationEvent> getOnNotificationPressed() {
		return onNotificationPressedProperty().get();
	}

	private ObjectProperty<EventHandler<NotificationEvent>> onNotificationPressed = new ObjectPropertyBase<EventHandler<NotificationEvent>>() {
		@Override
		public Object getBean() {
			return this;
		}

		@Override
		public String getName() {
			return "onNotificationPressed";
		}
	};

	public ObjectProperty<EventHandler<NotificationEvent>> onShowNotificationProperty() {
		return onShowNotification;
	}

	public void setOnShowNotification(EventHandler<NotificationEvent> value) {
		onShowNotificationProperty().set(value);
	}

	public EventHandler<NotificationEvent> getOnShowNotification() {
		return onShowNotificationProperty().get();
	}

	private ObjectProperty<EventHandler<NotificationEvent>> onShowNotification = new ObjectPropertyBase<EventHandler<NotificationEvent>>() {
		@Override
		public Object getBean() {
			return this;
		}

		@Override
		public String getName() {
			return "onShowNotification";
		}
	};

	public void fireNotificationEvent(final NotificationEvent event) {
		EventType<?> TYPE = event.getEventType();
		EventHandler<NotificationEvent> handelr;
		if (NotificationEvent.NOTIFICATION_PRESSED == TYPE) {
			handelr = getOnNotificationPressed();
		} else if (NotificationEvent.SHOW_NOTIFICATION == TYPE) {
			handelr = getOnShowNotification();
		} else {
			handelr = null;
		}
		if (null == handelr)
			return;
		handelr.handle(event);
	}

}
