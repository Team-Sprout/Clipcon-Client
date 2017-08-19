/*
 * Copyright (c) 2015 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package userInterface;

import java.util.stream.IntStream;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.event.WeakEventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Created by User: hansolo Date: 01.07.13 Time: 07:10
 */
public class Notification {
	public final String TITLE;
	public final Image RESIZEIMAGE;
	public final String MESSAGE;

	// ******************** Constructors **************************************
	public Notification() {
		this.TITLE = null;
		this.RESIZEIMAGE = null;
		this.MESSAGE = null;
	}
	
	public Notification(final String TITLE, final String MESSAGE) {
		this.TITLE = TITLE;
		this.RESIZEIMAGE = null;
		this.MESSAGE = MESSAGE;
	}

	public Notification(final String TITLE, final Image RESIZEIMAGE) {
		this.TITLE = TITLE;
		this.RESIZEIMAGE = RESIZEIMAGE;
		this.MESSAGE = null;
	}
	
	// ******************** Inner Classes *************************************
		public enum ClipboadNotifier {
			INSTANCE;

			private static double width = 80;
			private static double height = 80;
			private static double offsetX = 10;
			private static double offsetY = 50;
			private static double spacingY = 5;
			private static Pos popupLocation = Pos.BOTTOM_RIGHT;
			private static Stage stageRef = null;
			private Duration popupLifetime;
			private Duration popupAnimationTime;
			private Stage stage;
			private Scene scene;
			private Popup POPUP;
			private Timeline timeline;
			private ObservableList<Popup> popups;
			private boolean isShowing;

			// ******************** Constructor
			// ***************************************
			private ClipboadNotifier() {
				init();
				initGraphics();
			}

			// ******************** Initialization
			// ************************************
			private void init() {
				popupLifetime = Duration.millis(2000);
				popupAnimationTime = Duration.millis(500);
				popups = FXCollections.observableArrayList();
				isShowing = false;
			}

			private void initGraphics() {
				scene = new Scene(new Region());
				scene.setFill(null);
				scene.getStylesheets().add("/resources/myclipboardnoti.css");

				stage = new Stage();
				stage.setMaxWidth(1);
				stage.setMaxHeight(1);
				stage.setWidth(1);
				stage.setHeight(1);

				stage.setResizable(false);
				stage.initStyle(StageStyle.TRANSPARENT);
				stage.setScene(scene);
				stage.setAlwaysOnTop(true);
			}

			// ******************** Methods
			// *******************************************
			
			/**
			 * Sets the Notification's owner stage so that when the owner stage is
			 * closed Notifications will be shut down as well.<br>
			 * This is only needed if <code>setPopupLocation</code> is called
			 * <u>without</u> a stage reference.
			 * 
			 * @param OWNER
			 */
			public static void setNotificationOwner(final Stage OWNER) {
				INSTANCE.stage.initOwner(OWNER);
			}
			
			public boolean getIsShowing() {
				return INSTANCE.isShowing;
			}

			public void stop() {
				popups.clear();
			}

			/**
			 * Show the given Notification on the screen
			 * 
			 * @param NOTIFICATION
			 */
			public void notify(final Notification NOTIFICATION) {
				preOrder();
				showPopup(NOTIFICATION);
			}

			/**
			 * Reorder the popup Notifications on screen so that the latest
			 * Notification will stay on top
			 */
			private void preOrder() {
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

			/**
	         * Creates and shows a popup with the data from the given Notification object
	         * @param NOTIFICATION
	         */
	        private void showPopup(final Notification NOTIFICATION) {
	        	isShowing = true;
	        	
	            VBox popupLayout = new VBox();
	            popupLayout.setSpacing(10);
	            popupLayout.setPadding(new Insets(10, 10, 10, 10));

	            StackPane popupContent = new StackPane();
	            popupContent.setPrefSize(width, height);
	            popupContent.getStyleClass().add("notification");
	            popupContent.getChildren().addAll(popupLayout);

	            POPUP = new Popup();
	            POPUP.setX(getX());
	            POPUP.setY(getY());
	            POPUP.getContent().add(popupContent);
	            POPUP.addEventHandler(MouseEvent.MOUSE_PRESSED, new WeakEventHandler<>(event -> {
	                fireNotificationEvent(new NotificationEvent(NOTIFICATION, ClipboadNotifier.this, POPUP, NotificationEvent.NOTIFICATION_PRESSED));
	                hidePopUp();
	            }));            
	            popups.add(POPUP);

	            // Add a timeline for popup fade out
	            KeyValue fadeOutBegin = new KeyValue(POPUP.opacityProperty(), 1.0);            
	            KeyValue fadeOutEnd   = new KeyValue(POPUP.opacityProperty(), 0.0);

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

	            POPUP.show(stage);
	            fireNotificationEvent(new NotificationEvent(NOTIFICATION, ClipboadNotifier.this, POPUP, NotificationEvent.SHOW_NOTIFICATION));
	            timeline.play();
	        }
	        
	        public void hidePopUp() {
	        	POPUP.hide();
                popups.remove(POPUP);
                timeline.stop();
                stage.close();
                isShowing = false;
	        }

			private double getX() {
				if (null == stageRef)
					return calcX(0.0, Screen.getPrimary().getBounds().getWidth());

				return calcX(stageRef.getX(), stageRef.getWidth());
			}
			
			private double getY() {
				if (null == stageRef)
					return calcY(0.0, Screen.getPrimary().getBounds().getHeight());
				return calcY(stageRef.getY(), stageRef.getHeight());
			}

			private double calcX(final double LEFT, final double TOTAL_WIDTH) {
				switch (popupLocation) {
				case TOP_LEFT:
				case CENTER_LEFT:
				case BOTTOM_LEFT:
					return LEFT + offsetX;
				case TOP_CENTER:
				case CENTER:
				case BOTTOM_CENTER:
					return LEFT + (TOTAL_WIDTH - width) * 0.5 - offsetX;
				case TOP_RIGHT:
				case CENTER_RIGHT:
				case BOTTOM_RIGHT:
					return LEFT + TOTAL_WIDTH - width - offsetX;
				default:
					return 0.0;
				}
			}

			private double calcY(final double TOP, final double TOTAL_HEIGHT) {
				switch (popupLocation) {
				case TOP_LEFT:
				case TOP_CENTER:
				case TOP_RIGHT:
					return TOP + offsetY;
				case CENTER_LEFT:
				case CENTER:
				case CENTER_RIGHT:
					return TOP + (TOTAL_HEIGHT - height) / 2 - offsetY;
				case BOTTOM_LEFT:
				case BOTTOM_CENTER:
				case BOTTOM_RIGHT:
					return TOP + TOTAL_HEIGHT - height - offsetY;
				default:
					return 0.0;
				}
			}

			// ******************** Event handling ********************************
			public final ObjectProperty<EventHandler<NotificationEvent>> onNotificationPressedProperty() {
				return onNotificationPressed;
			}

			public final void setOnNotificationPressed(EventHandler<NotificationEvent> value) {
				onNotificationPressedProperty().set(value);
			}

			public final EventHandler<NotificationEvent> getOnNotificationPressed() {
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

			public final ObjectProperty<EventHandler<NotificationEvent>> onShowNotificationProperty() {
				return onShowNotification;
			}

			public final void setOnShowNotification(EventHandler<NotificationEvent> value) {
				onShowNotificationProperty().set(value);
			}

			public final EventHandler<NotificationEvent> getOnShowNotification() {
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

			public void fireNotificationEvent(final NotificationEvent EVENT) {
				final EventType<?> TYPE = EVENT.getEventType();
				final EventHandler<NotificationEvent> HANDLER;
				if (NotificationEvent.NOTIFICATION_PRESSED == TYPE) {
					HANDLER = getOnNotificationPressed();
				} else if (NotificationEvent.SHOW_NOTIFICATION == TYPE) {
					HANDLER = getOnShowNotification();
				} else {
					HANDLER = null;
				}
				if (null == HANDLER)
					return;
				HANDLER.handle(EVENT);
			}

		}
		
		// ******************** Inner Classes *************************************
		public enum UploadNotifier {
			INSTANCE;
			
			private static double width = 270;
			private static double height = 90;
			private static double offsetX = 0;
			private static double offsetY = 35;
			private static double spacingY = 5;
			private static Pos popupLocation = Pos.BOTTOM_RIGHT;
			private static Stage stageRef = null;
			private Duration popupLifetime;
			private Duration popupAnimationTime;
			private Stage stage;
			private Scene scene;
			private ObservableList<Popup> popups;
			
			// ******************** Constructor
			// ***************************************
			private UploadNotifier() {
				init();
				initGraphics();
			}
			
			// ******************** Initialization
			// ************************************
			private void init() {
				popupLifetime = Duration.millis(5000);
				popupAnimationTime = Duration.millis(500);
				popups = FXCollections.observableArrayList();
			}
			
			private void initGraphics() {
				scene = new Scene(new Region());
				scene.setFill(null);
				scene.getStylesheets().add("/resources/myuploadnoti.css");
				
				stage = new Stage();
				stage.setMaxWidth(1);
				stage.setMaxHeight(1);
				stage.setWidth(1);
				stage.setHeight(1);
				
				stage.setResizable(false);
				stage.initStyle(StageStyle.TRANSPARENT);
				stage.setScene(scene);
				stage.setAlwaysOnTop(true);
			}
			
			// ******************** Methods
			// *******************************************
			
			/**
			 * Sets the Notification's owner stage so that when the owner stage is
			 * closed Notifications will be shut down as well.<br>
			 * This is only needed if <code>setPopupLocation</code> is called
			 * <u>without</u> a stage reference.
			 * 
			 * @param OWNER
			 */
			public static void setNotificationOwner(final Stage OWNER) {
				INSTANCE.stage.initOwner(OWNER);
			}
			
			public void stop() {
				popups.clear();
			}
			
			/**
			 * Show the given Notification on the screen
			 * 
			 * @param NOTIFICATION
			 */
			public void notify(final Notification NOTIFICATION) {
				preOrder();
				showPopup(NOTIFICATION);
			}
			
			/**
			 * Reorder the popup Notifications on screen so that the latest
			 * Notification will stay on top
			 */
			private void preOrder() {
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
			
			/**
			 * Creates and shows a popup with the data from the given Notification object
			 * @param NOTIFICATION
			 */
			private void showPopup(final Notification NOTIFICATION) {
				
				Label title = new Label(NOTIFICATION.TITLE);
				title.getStyleClass().add("title");
				
				Label message = null;
				ImageView img = null;
				
				if(NOTIFICATION.RESIZEIMAGE != null) {
					img = new ImageView(NOTIFICATION.RESIZEIMAGE);
					img.getStyleClass().add("image");
					
					double width = NOTIFICATION.RESIZEIMAGE.getWidth();
					double height = NOTIFICATION.RESIZEIMAGE.getHeight();
					double x = 0;
					double y = height/4;
					
					Rectangle2D croppedPortion = new Rectangle2D(x, y, width, height/4);
					img.setViewport(croppedPortion);
					img.setFitWidth(180);
					img.setFitHeight(40);
					img.setSmooth(true);
				}
				else {
					message = new Label(NOTIFICATION.MESSAGE);
					message.getStyleClass().add("message");
				}
				
				VBox popupLayout = new VBox();
				popupLayout.setPadding(new Insets(15, 10, 10, 10));
				
				if(NOTIFICATION.RESIZEIMAGE != null) {
					popupLayout.setSpacing(3);
					popupLayout.setPadding(new Insets(15, 10, 10, 10));
					popupLayout.getChildren().addAll(title, img);
				} else {
					popupLayout.setSpacing(6);
					popupLayout.getChildren().addAll(title, message);
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
					fireNotificationEvent(new NotificationEvent(NOTIFICATION, UploadNotifier.this, POPUP, NotificationEvent.NOTIFICATION_PRESSED));
					POPUP.hide();
	                popups.remove(POPUP);
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
	                if(popups.size() == 1) {
	                	stage.close();
	                }
				}));
				
				if (stage.isShowing()) {
					stage.toFront();
				} else {
					stage.show();
				}
				
				POPUP.show(stage);
				fireNotificationEvent(new NotificationEvent(NOTIFICATION, UploadNotifier.this, POPUP, NotificationEvent.SHOW_NOTIFICATION));
				timeline.play();
			}
			
			private double getX() {
				if (null == stageRef)
					return calcX(0.0, Screen.getPrimary().getBounds().getWidth());
				
				return calcX(stageRef.getX(), stageRef.getWidth());
			}
			
			private double getY() {
				if (null == stageRef)
					return calcY(0.0, Screen.getPrimary().getBounds().getHeight());
				return calcY(stageRef.getY(), stageRef.getHeight());
			}
			
			private double calcX(final double LEFT, final double TOTAL_WIDTH) {
				switch (popupLocation) {
				case TOP_LEFT:
				case CENTER_LEFT:
				case BOTTOM_LEFT:
					return LEFT + offsetX;
				case TOP_CENTER:
				case CENTER:
				case BOTTOM_CENTER:
					return LEFT + (TOTAL_WIDTH - width) * 0.5 - offsetX;
				case TOP_RIGHT:
				case CENTER_RIGHT:
				case BOTTOM_RIGHT:
					return LEFT + TOTAL_WIDTH - width - offsetX;
				default:
					return 0.0;
				}
			}
			
			private double calcY(final double TOP, final double TOTAL_HEIGHT) {
				switch (popupLocation) {
				case TOP_LEFT:
				case TOP_CENTER:
				case TOP_RIGHT:
					return TOP + offsetY;
				case CENTER_LEFT:
				case CENTER:
				case CENTER_RIGHT:
					return TOP + (TOTAL_HEIGHT - height) / 2 - offsetY;
				case BOTTOM_LEFT:
				case BOTTOM_CENTER:
				case BOTTOM_RIGHT:
					return TOP + TOTAL_HEIGHT - height - offsetY;
				default:
					return 0.0;
				}
			}
			
			// ******************** Event handling ********************************
			public final ObjectProperty<EventHandler<NotificationEvent>> onNotificationPressedProperty() {
				return onNotificationPressed;
			}
			
			public final void setOnNotificationPressed(EventHandler<NotificationEvent> value) {
				onNotificationPressedProperty().set(value);
			}
			
			public final EventHandler<NotificationEvent> getOnNotificationPressed() {
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
			
			public final ObjectProperty<EventHandler<NotificationEvent>> onShowNotificationProperty() {
				return onShowNotification;
			}
			
			public final void setOnShowNotification(EventHandler<NotificationEvent> value) {
				onShowNotificationProperty().set(value);
			}
			
			public final EventHandler<NotificationEvent> getOnShowNotification() {
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
			
			public void fireNotificationEvent(final NotificationEvent EVENT) {
				final EventType<?> TYPE = EVENT.getEventType();
				final EventHandler<NotificationEvent> HANDLER;
				if (NotificationEvent.NOTIFICATION_PRESSED == TYPE) {
					HANDLER = getOnNotificationPressed();
				} else if (NotificationEvent.SHOW_NOTIFICATION == TYPE) {
					HANDLER = getOnShowNotification();
				} else {
					HANDLER = null;
				}
				if (null == HANDLER)
					return;
				HANDLER.handle(EVENT);
			}
			
		}

}
