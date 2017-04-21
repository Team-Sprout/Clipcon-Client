package model;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.util.ArrayList;

import lombok.AllArgsConstructor;

/** 클립보드에 들어갈 수 있는 File 객체 */

@AllArgsConstructor
public class FileTransferable implements Transferable {
	private ArrayList<File> listOfFiles;

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DataFlavor.javaFileListFlavor };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return DataFlavor.javaFileListFlavor.equals(flavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (isDataFlavorSupported(flavor)) {
			return listOfFiles;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}
}