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
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UploadNotification extends Notification {
	
	static protected ObservableList<Popup> popups = FXCollections.observableArrayList();
	static protected Stage stage = new Stage();
	static private boolean isFirst = true;

	public UploadNotification(String title, String message) {
		super(title, message);
		
		initGraphics();
	}
	
	public UploadNotification(String title, Image resizeImage) {
		super(title, resizeImage);
		
		initGraphics();
	}

	@Override
	protected void init() {
		width = 270;
		height = 90;
		offsetX = 0;
		offsetY = 35;
		popupLifetime = Duration.millis(5000);
	}

	protected void initGraphics() {
		super.initGraphics(stage);
		scene.getStylesheets().add("/resources/myuploadnoti.css");
		stage.setScene(scene);
		if(isFirst) {
			stage.initStyle(StageStyle.TRANSPARENT);
			isFirst = false;
		}
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
		
		Label titleLb = new Label(title);
		titleLb.getStyleClass().add("title");
		
		Label messageLb = null;
		ImageView img = null;
		
		if(resizeImage != null) {
			img = new ImageView(resizeImage);
			img.getStyleClass().add("image");
			
			double width = resizeImage.getWidth();
			double height = resizeImage.getHeight();
			double x = 0;
			double y = height/4;
			
			Rectangle2D croppedPortion = new Rectangle2D(x, y, width, height/4);
			img.setViewport(croppedPortion);
			img.setFitWidth(180);
			img.setFitHeight(40);
			img.setSmooth(true);
		}
		else {
			messageLb = new Label(message);
			messageLb.getStyleClass().add("message");
		}
		
		VBox popupLayout = new VBox();
		popupLayout.setPadding(new Insets(15, 10, 10, 10));
		
		if(resizeImage != null) {
			popupLayout.setSpacing(3);
			popupLayout.setPadding(new Insets(15, 10, 10, 10));
			popupLayout.getChildren().addAll(titleLb, img);
		} else {
			popupLayout.setSpacing(6);
			popupLayout.getChildren().addAll(titleLb, messageLb);
		}
		
		StackPane popupContent = new StackPane();
		popupContent.setPrefSize(width, height);
		popupContent.getStyleClass().add("notification");
		popupContent.getChildren().addAll(popupLayout);

		Popup POPUP = new Popup();
		POPUP.setX(getX());
		POPUP.setY(getY());
		POPUP.getContent().add(popupContent);
		POPUP.addEventHandler(MouseEvent.MOUSE_PRESSED, new WeakEventHandler<>(event -> {
			fireNotificationEvent(new NotificationEvent(this, POPUP, NotificationEvent.NOTIFICATION_PRESSED));
			POPUP.hide();
            popups.remove(POPUP);
            if(popups.size() == 0) {
            	stage.close();
            }
		}));            
		popups.add(POPUP);
		
		// Add a timeline for popup fade out
		KeyValue fadeOutBegin = new KeyValue(POPUP.opacityProperty(), 1.0);            
		KeyValue fadeOutEnd   = new KeyValue(POPUP.opacityProperty(), 0.0);
		
		KeyFrame kfBegin = new KeyFrame(Duration.ZERO, fadeOutBegin);
		KeyFrame kfEnd   = new KeyFrame(popupAnimationTime, fadeOutEnd);

		Timeline timeline = new Timeline(kfBegin, kfEnd);
		timeline.setDelay(popupLifetime);
		timeline.setOnFinished(actionEvent -> Platform.runLater(() -> {
			POPUP.hide();
            popups.remove(POPUP);
            timeline.stop();
            if(popups.size() == 0) {
            	stage.close();
            }
		}));
		
		if (stage.isShowing()) {
			stage.toFront();
		} else {
			stage.show();
		}
		
		POPUP.show(stage);
		fireNotificationEvent(new NotificationEvent(this, POPUP, NotificationEvent.SHOW_NOTIFICATION));
		timeline.play();
	}
}
