package userInterface;

import java.io.IOException;

import javax.websocket.EncodeException;

import application.Main;
import controller.Endpoint;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Message;

public class GroupExitDialog extends Stage {
	
	private Endpoint endpoint = Endpoint.getIntance();
 
    private static Label label;
    private static GroupExitDialog popup;
    private static int result;
 
    public static final int NO = 0;
    public static final int YES = 1;
 
    private GroupExitDialog() {
        setResizable(false);
        initStyle(StageStyle.TRANSPARENT);
 
        label = new Label();
        label.setWrapText(true);
        label.setGraphicTextGap(20);
 
        Button yesBtn = new Button("Yes");
        yesBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				result = YES;
                GroupExitDialog.this.close();
                
				// Send REQUEST_EXIT_GROUP Message To Server
				Message exitGroupMsg = new Message().setType(Message.REQUEST_EXIT_GROUP);
				exitGroupMsg.add(Message.GROUP_PK, Endpoint.user.getGroup().getPrimaryKey());
				exitGroupMsg.add(Message.NAME, Endpoint.user.getName());
				try {
					if (endpoint == null) {
						System.out.println("debuger_delf: endpoint is null");
					}
					endpoint = Endpoint.getIntance();
					endpoint.sendMessage(exitGroupMsg);
				} catch (IOException | EncodeException e) {
					e.printStackTrace();
				}
			}
		});
        
        yesBtn.getStyleClass().add("button");
        
        Button noBtn = new Button("No");
        noBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				result = NO;
                GroupExitDialog.this.close();
			}
		});
        
        noBtn.getStyleClass().add("button");
 
        BorderPane borderPane = new BorderPane();
 
        BorderPane dropShadowPane = new BorderPane();
        dropShadowPane.getStyleClass().add("content");
        dropShadowPane.setTop(label);
 
        HBox hbox = new HBox();
        hbox.setSpacing(15);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().add(yesBtn);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().add(noBtn);
 
        dropShadowPane.setBottom(hbox);
 
        borderPane.setCenter(dropShadowPane);
 
        Scene scene = new Scene(borderPane);                       
        scene.getStylesheets().add("resources/alert.css");
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }
 
    public static int show(String msg) {
        if (popup == null) {
            popup = new GroupExitDialog();
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
 
        return result;
    }
 
}