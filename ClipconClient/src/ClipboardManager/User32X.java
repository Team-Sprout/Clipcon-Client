package ClipboardManager;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface User32X extends StdCallLibrary {

	User32X INSTANCE = (User32X) Native.loadLibrary("user32", User32X.class, W32APIOptions.UNICODE_OPTIONS);

	final int WM_DESTROY = 0x0002;
	final int WM_CHANGECBCHAIN = 0x030D;
	final int WM_DRAWCLIPBOARD = 0x0308;

	HWND SetClipboardViewer(HWND viewer);

	void SendMessage(HWND nextViewer, int uMsg, WPARAM wParam, LPARAM lParam);

	void ChangeClipboardChain(HWND viewer, HWND nextViewer);

	int SetWindowLong(HWND hWnd, int nIndex, WinUser.WindowProc callback);

	int MsgWaitForMultipleObjects(int length, HANDLE[] handles, boolean b, int infinite, int qsAllinput);

}