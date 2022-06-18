public class MainMenuController {
	
	SceneRootController sceneRootController;
	
	// Constructor
	public MainMenuController() {
		sceneRootController = SceneRootController.getInstance();
	}
	
	// Changing scene to the game
	public void newGameButtonPressed() {
		sceneRootController.setState(GameState.GAME);
	}
	
	// Changing scene to the load game screen
	public void loadGameButtonPressed() {
		sceneRootController.setState(GameState.LOAD_GAME);
	}
	
	// Changing scene to the map editor screen
	public void mapEditorButtonPressed() {
		sceneRootController.setState(GameState.MAP_EDITOR);
	}
	
	// Changing scene to the character creation screen
	public void characterCreationButtonPressed() {
		sceneRootController.setState(GameState.CHARACTER_CREATION);
	}

}
