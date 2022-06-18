import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import objects.GameObjects;

public class Main extends Application {
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		GameObjects gameObjects = GameObjects.getInstance();
		gameObjects.getAllMapTiles();
		gameObjects.getAllMapOverlays();
		gameObjects.getAllPortals();
		gameObjects.getAllAttire();
		gameObjects.getAllCharacters();
		
		new KeyListener(primaryStage);
		new SceneRootController(primaryStage);
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent arg0) {
				System.exit(0);
			}
		});

	}
	
}
