import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class KeyListener {
	
	public static boolean W_PRESSED = false, A_PRESSED = false, S_PRESSED = false, D_PRESSED = false,
			CTRL_PRESSED = false;
	
	public KeyListener(Stage scene) {
		scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				KeyCode keyCode = event.getCode();
				if (keyCode == KeyCode.W) {
					W_PRESSED = true;
				} else if (keyCode == KeyCode.A){
					A_PRESSED = true;
				} else if (keyCode == KeyCode.S) {
					S_PRESSED = true;
				} else if (keyCode == KeyCode.D) {
					D_PRESSED = true;
				} else if (keyCode == KeyCode.CONTROL) {
					CTRL_PRESSED = true;
				}
			}
		});
		scene.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				KeyCode keyCode = event.getCode();
				if (keyCode == KeyCode.W) {
					W_PRESSED = false;
				} else if (keyCode == KeyCode.A){
					A_PRESSED = false;
				} else if (keyCode == KeyCode.S) {
					S_PRESSED = false;
				} else if (keyCode == KeyCode.D) {
					D_PRESSED = false;
				} else if (keyCode == KeyCode.CONTROL) {
					CTRL_PRESSED = false;
				}
			}
		});
	}
	
	

}
