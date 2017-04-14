package hookManager;

import java.util.EventListener;

public interface GlobalKeyboardListener extends EventListener {
	void onGlobalHotkeysPressed();
}
