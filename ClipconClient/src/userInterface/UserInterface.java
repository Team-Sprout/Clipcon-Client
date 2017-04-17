package userInterface;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInterface {
	private StartingScene startingScene;
	private SignupScene signupScene;
	private EntryScene entryScene;
	private MainScene mainScene;
	
	public UserInterface() {
		startingScene = new StartingScene();
		signupScene = new SignupScene();
		entryScene = new EntryScene();
		mainScene = new MainScene();
	}
}
