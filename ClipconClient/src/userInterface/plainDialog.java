package userInterface;

import application.Main;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class plainDialog extends Stage {
	
    private static Label label;
    private static plainDialog popup;
 
    private plainDialog() {
        setResizable(false);
        initStyle(StageStyle.TRANSPARENT);
        initOwner(Main.getPrimaryStage());
        initModality(Modality.WINDOW_MODAL);
 
        label = new Label();
        label.setWrapText(true);
        label.setGraphicTextGap(20);
        label.getStyleClass().add("label");
 
        Button okBtn = new Button("OK");
        okBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
                plainDialog.this.close();
			}
		});
        okBtn.getStyleClass().add("button");
 
        BorderPane borderPane = new BorderPane();
 
        BorderPane dropShadowPane = new BorderPane();
        dropShadowPane.getStyleClass().add("content");
        dropShadowPane.setTop(label);
 
        HBox hbox = new HBox();
        hbox.setSpacing(15);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().add(okBtn);
 
        dropShadowPane.setBottom(hbox);
 
        borderPane.setCenter(dropShadowPane);
 
        Scene scene = new Scene(borderPane);                       
        scene.getStylesheets().add("resources/myAlert.css");
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }
 
    public static void show(String msg) {
        if (popup == null) {
            popup = new plainDialog();
        }
 
        label.setText(msg);

        // calculate width of string
        final Text text = new Text(msg);
        text.snapshot(null, null);

        int width = 300;
        int height = 100;
 
        popup.setWidth(width);
        popup.setHeight(height);
 
        // make sure this stage is centered on top of its owner
        popup.setX(Main.getPrimaryStage().getX() + (Main.getPrimaryStage().getWidth() / 2 - popup.getWidth() / 2));
        popup.setY(Main.getPrimaryStage().getY() + (Main.getPrimaryStage().getHeight() / 2 - popup.getHeight() / 2));
 
        popup.showAndWait();
    }
}
