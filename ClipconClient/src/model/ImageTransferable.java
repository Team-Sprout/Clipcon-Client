package model;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import lombok.AllArgsConstructor;

/** 클립보드에 들어갈 수 있는 Image 객체 */

@AllArgsConstructor
public class ImageTransferable implements Transferable {
	private Image image;
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DataFlavor.imageFlavor };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return DataFlavor.imageFlavor.equals(flavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (isDataFlavorSupported(flavor)) {
			return image;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

}
