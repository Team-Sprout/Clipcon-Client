package userInterface;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

public class PlainDialog extends Dialog {
	
    public PlainDialog(String msg, boolean isExit) {
    	super(msg);
    	
    	Button btn;
 
    	if(!isExit) {
    		btn = new Button("OK");
    	}
    	else {
    		btn = new Button("종료");
    	}
        
        btn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(!isExit) {
					PlainDialog.this.close();
				}
				else {
					System.exit(0);
				}
			}
		});
        
        hbox.setAlignment(Pos.CENTER);
        btn.getStyleClass().add("button");

        hbox.getChildren().add(btn);
 
        dropShadowPane.setBottom(hbox);
 
        borderPane.setCenter(dropShadowPane);
 
        scene = new Scene(borderPane);                       
        scene.getStylesheets().add("resources/myAlert.css");
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }
}
