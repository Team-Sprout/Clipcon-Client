package userInterface;

import application.Main;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public abstract class Dialog extends Stage {
	protected Label label;
	protected Text text;
	protected BorderPane borderPane;
	protected BorderPane dropShadowPane;
	protected HBox hbox;
	protected Scene scene;

	public Dialog(String msg) {
		setResizable(false);
        initStyle(StageStyle.TRANSPARENT);
        initOwner(Main.getPrimaryStage());
        initModality(Modality.WINDOW_MODAL);
 
        label = new Label(msg);
        label.setWrapText(true);
        label.setGraphicTextGap(20);
        label.getStyleClass().add("label");
        
        borderPane = new BorderPane();
        
        dropShadowPane = new BorderPane();
        dropShadowPane.getStyleClass().add("content");
        dropShadowPane.setTop(label);
 
        hbox = new HBox();
        hbox.setSpacing(15);
        
        // calculate width of string
        text = new Text(msg);
        text.snapshot(null, null);
        
        int width = 300;
        int height = 100;
 
        this.setWidth(width);
        this.setHeight(height);
 
        // make sure this stage is centered on top of its owner
        this.setX(Main.getPrimaryStage().getX() + (Main.getPrimaryStage().getWidth() / 2 - this.getWidth() / 2));
        this.setY(Main.getPrimaryStage().getY() + (Main.getPrimaryStage().getHeight() / 2 - this.getHeight() / 2));
	}
}
