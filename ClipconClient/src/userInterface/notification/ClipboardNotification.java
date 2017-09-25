package userInterface.notification;

import java.util.stream.IntStream;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.WeakEventHandler;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class ClipboardNotification extends Notification {
	
	static protected ObservableList<Popup> popups = FXCollections.observableArrayList();
	static protected Stage stage = new Stage();
	
	public ClipboardNotification() {
		super();
		
		initGraphics();
	}

	@Override
	protected void init() {
		width = 80;
		height = 80;
		offsetX = 10;
		offsetY = 50;
		popupLifetime = Duration.millis(2000);
	}

	protected void initGraphics() {
		super.initGraphics(stage);
		scene.getStylesheets().add("/resources/myclipboardnoti.css");
		stage.setScene(scene);
		stage.initStyle(StageStyle.TRANSPARENT);
	}
	
	@Override
	protected void preOrder() {
		if (popups.isEmpty())
			return;
		IntStream.range(0, popups.size()).parallel().forEachOrdered(i -> {
			switch (popupLocation) {
			case TOP_LEFT:
			case TOP_CENTER:
			case TOP_RIGHT:
				popups.get(i).setY(popups.get(i).getY() + height + spacingY);
				break;

			case BOTTOM_LEFT:
			case BOTTOM_CENTER:
			case BOTTOM_RIGHT:
				popups.get(i).setY(popups.get(i).getY() - height - spacingY);
				break;

			default:
				popups.get(i).setY(popups.get(i).getY() - height - spacingY);
				break;
			}
		});
	}

	protected void showPopup() {
		init();
		
		isShowing = true;
    	
        VBox popupLayout = new VBox();
        popupLayout.setSpacing(10);
        popupLayout.setPadding(new Insets(10, 10, 10, 10));

        StackPane popupContent = new StackPane();
        popupContent.setPrefSize(width, height);
        popupContent.getStyleClass().add("notification");
        popupContent.getChildren().addAll(popupLayout);

        popup = new Popup();
        popup.setX(getX());
        popup.setY(getY());
        popup.getContent().add(popupContent);
        popup.addEventHandler(MouseEvent.MOUSE_PRESSED, new WeakEventHandler<>(event -> {
            fireNotificationEvent(new NotificationEvent(this, popup, NotificationEvent.NOTIFICATION_PRESSED));
            hidePopUp();
        }));            
        popups.add(popup);

        // Add a timeline for popup fade out
        KeyValue fadeOutBegin = new KeyValue(popup.opacityProperty(), 1.0);            
        KeyValue fadeOutEnd   = new KeyValue(popup.opacityProperty(), 0.0);

        KeyFrame kfBegin = new KeyFrame(Duration.ZERO, fadeOutBegin);
        KeyFrame kfEnd   = new KeyFrame(popupAnimationTime, fadeOutEnd);

        timeline = new Timeline(kfBegin, kfEnd);
        timeline.setDelay(popupLifetime);
        timeline.setOnFinished(actionEvent -> Platform.runLater(() -> {
        	hidePopUp();
        }));
        
        if (stage.isShowing()) {
            stage.toFront();
        } else {
            stage.show();
        }

        popup.show(stage);
        fireNotificationEvent(new NotificationEvent(this, popup, NotificationEvent.SHOW_NOTIFICATION));
        timeline.play();
	}
	
	public void hidePopUp() {
		super.hidePopUp();
        popups.remove(popup);
        stage.close();
    }
}
