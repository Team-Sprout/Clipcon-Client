package model;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends RecursiveTreeObject<User>{
	private String name;
	private Group group;
	
    private StringProperty nameProperty;
	
	public User(String name) {
		this.name = name;
		group = null;
		this.nameProperty = new SimpleStringProperty(name);
	}
}
