package userInterface.notification;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class NotificationEvent extends Event {
    public static EventType<NotificationEvent> NOTIFICATION_PRESSED = new EventType(ANY, "NOTIFICATION_PRESSED");
    public static EventType<NotificationEvent> SHOW_NOTIFICATION    = new EventType(ANY, "SHOW_NOTIFICATION");

    public NotificationEvent(Object source, EventTarget target, EventType<NotificationEvent> type) {
        super(source, target, type);
    }
}
