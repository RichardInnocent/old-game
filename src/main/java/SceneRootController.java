import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneRootController {
	
	Stage stage;
	Parent mainMenuRoot;
	Parent loadGameRoot;
	Parent mapEditorRoot;
	Parent characterCreationRoot;
	Parent gameRoot;
	static SceneRootController sceneRootController;
	GameState gameState;
	
	public static SceneRootController getInstance() {
		return sceneRootController;
	}
	
	public SceneRootController(Stage primaryStage) {
		
		sceneRootController = this;
		gameState = GameState.MAIN_MENU; // Initialising game state
		
		// Loading FXMLs:
//		try {
//			mainMenuRoot = FXMLLoader.load(getClass().getResource("MainMenuScreen.fxml"));
//			loadGameRoot = FXMLLoader.load(getClass().getResource("LoadGameScreen.fxml"));
//			mapEditorRoot = FXMLLoader.load(getClass().getResource("MapEditorScreen.fxml"));
//			characterCreationRoot = FXMLLoader.load(getClass().getResource("CharacterCreationScreen.fxml"));
//			gameRoot = FXMLLoader.load(getClass().getResource("GameScreen.fxml"));
//		} catch (IOException e) {
//			e.printStackTrace();
//			System.exit(0);
//		}
		
		// Setting stage and title
		stage = primaryStage;
		stage.setTitle("The Game");
		
		// Displaying window:
		try {
			stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("MainMenuScreen.fxml"))));
			stage.setResizable(false);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	// Gets the current state of the game
	public GameState getState() {
		return gameState;
	}
	
	// Sets the current state of the game 
	public void setState(GameState newState) {
		gameState = newState;
		try {
			if (gameState == GameState.MAIN_MENU) {
				stage.getScene().setRoot(FXMLLoader.load(getClass().getResource("MainMenuScreen.fxml")));
			} else if (gameState == GameState.LOAD_GAME) {
				stage.getScene().setRoot(FXMLLoader.load(getClass().getResource("LoadGameScreen.fxml")));
			} else if (gameState == GameState.MAP_EDITOR) {
				stage.getScene().setRoot(FXMLLoader.load(getClass().getResource("MapEditorScreen.fxml")));
			} else if (gameState == GameState.CHARACTER_CREATION) {
				stage.getScene().setRoot(FXMLLoader.load(getClass().getResource("CharacterCreationScreen.fxml")));
			} else if (gameState == GameState.GAME) {
				stage.getScene().setRoot(FXMLLoader.load(getClass().getResource("GameScreen.fxml")));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
