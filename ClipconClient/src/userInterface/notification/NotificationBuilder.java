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

package userInterface.notification;

import javafx.beans.property.*;
import javafx.scene.image.Image;

import java.util.HashMap;


/**
 * User: hansolo
 * Date: 29.04.14
 * Time: 08:53
 */
public class NotificationBuilder<B extends NotificationBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected NotificationBuilder() {
    }


    // ******************** Methods *******************************************
    public final static NotificationBuilder create() {
        return new NotificationBuilder();
    }

    public final B title(final String TITLE) {
        properties.put("title", new SimpleStringProperty(TITLE));
        return (B) this;
    }
    
    public final B resizeImage(final Image IMAGE) {
        properties.put("resizeImage", new SimpleObjectProperty<>(IMAGE));
        return (B) this;
    }

    public final B message(final String MESSAGE) {
        properties.put("message", new SimpleStringProperty(MESSAGE));
        return (B) this;
    }

    public final B image(final Image IMAGE) {
        properties.put("image", new SimpleObjectProperty<>(IMAGE));
        return (B) this;
    }
    
    public final Notification build() {
        final Notification NOTIFICATION;     
        if (properties.keySet().contains("title") && properties.keySet().contains("resizeImage")) {
            NOTIFICATION = new Notification(((StringProperty) properties.get("title")).get(), 
            								((ObjectProperty<Image>) properties.get("resizeImage")).get());
        }
        else if (properties.keySet().contains("title") && properties.keySet().contains("message")) {
            NOTIFICATION = new Notification(((StringProperty) properties.get("title")).get(), 
                                            ((StringProperty) properties.get("message")).get());
        }
        else {
        	NOTIFICATION = new Notification();
        }               
        
        return NOTIFICATION;
    }
}
