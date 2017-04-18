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

	private static UserInterface uniqueUserInterface;

	public static UserInterface getIntance() {
		System.out.println("UI getIntance()");
		if (uniqueUserInterface == null) {
			uniqueUserInterface = new UserInterface();
		}

		return uniqueUserInterface;
	}

}
