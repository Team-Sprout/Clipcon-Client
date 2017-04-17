package userInterface;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInterface {
	private StartingScene startingController;
	private SignupScene signupController;
	private EntryScene entryController;
	private MainScene mainController;
	
	public UserInterface() {
		startingController = new StartingScene();
		signupController = new SignupScene();
		entryController = new EntryScene();
		mainController = new MainScene();
	}
}
