package userInterface;

import lombok.Getter;
import lombok.Setter;
import userInterface.scene.GroupJoinScene;
import userInterface.scene.MainScene;
import userInterface.scene.NicknameChangeScene;
import userInterface.scene.ProgressBarScene;
import userInterface.scene.SettingScene;
import userInterface.scene.StartingScene;

@Getter
@Setter
public class UserInterface {
	private StartingScene startingScene;
	private GroupJoinScene groupJoinScene;
	private MainScene mainScene;
	private ProgressBarScene progressBarScene;
	private NicknameChangeScene nicknameChangeScene;
	private SettingScene settingScene;

	private static UserInterface uniqueUserInterface;

	public static UserInterface getInstance() {
		if (uniqueUserInterface == null) {
			uniqueUserInterface = new UserInterface();
		}

		return uniqueUserInterface;
	}

}
