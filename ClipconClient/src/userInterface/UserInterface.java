package userInterface;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInterface {
	private StartingScene startingScene;
	private GroupJoinScene groupJoinScene;
	private MainScene mainScene;

	private static UserInterface uniqueUserInterface;

	public static UserInterface getIntance() {
		if (uniqueUserInterface == null) {
			uniqueUserInterface = new UserInterface();
		}

		return uniqueUserInterface;
	}

}
