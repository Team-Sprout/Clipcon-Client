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

import java.util.HashMap;

import javafx.beans.property.Property;
import lombok.NoArgsConstructor;


/**
 * User: hansolo
 * Date: 29.04.14
 * Time: 08:32
 */

@NoArgsConstructor
public class NotifierBuilder<B extends NotifierBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();
    
    public final static Notification.ClipboadNotifier clipboardNotiBuild() {
    	new NotifierBuilder();
        final Notification.ClipboadNotifier NOTIFIER = Notification.ClipboadNotifier.INSTANCE;
        return NOTIFIER;
    }

    public final static Notification.UploadNotifier uploadNotibuild() {
    	new NotifierBuilder();
        final Notification.UploadNotifier NOTIFIER = Notification.UploadNotifier.INSTANCE;
        return NOTIFIER;
    }
}
