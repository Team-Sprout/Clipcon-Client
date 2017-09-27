package userInterface.dialog;

import java.io.IOException;

import javax.websocket.EncodeException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import model.message.Message;
import server.Endpoint;

public class GroupExitDialog extends Dialog {
	
	private Endpoint endpoint = Endpoint.getInstance();
 
    public GroupExitDialog(String msg) {
    	super(msg);
 
    	// Yes button event handling
        Button yesBtn = new Button("Yes");
        yesBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
                GroupExitDialog.this.close();
                
				// Send exit group message to server
				Message exitGroupMsg = new Message().setType(Message.REQUEST_EXIT_GROUP);
				try {
					endpoint.sendMessage(exitGroupMsg);
				} catch (IOException | EncodeException e) {
					e.printStackTrace();
				}
			}
		});
        
        yesBtn.getStyleClass().add("button");
        
        // No button event handling
        Button noBtn = new Button("No");
        noBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
                GroupExitDialog.this.close();
			}
		});
        
        noBtn.getStyleClass().add("button");
 
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().add(yesBtn);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().add(noBtn);
 
        dropShadowPane.setBottom(hbox);
 
        borderPane.setCenter(dropShadowPane);
 
        Scene scene = new Scene(borderPane);                       
        scene.getStylesheets().add("resources/myAlert.css");
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }
}