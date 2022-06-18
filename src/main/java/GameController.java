import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import objects.Clothing;
import objects.GameCharacter;
import objects.GameObjects;
import objects.MapOverlay;
import objects.MapTile;
import objects.Portal;

public class GameController implements Initializable {
	
	GraphicsContext gc;
	SceneRootController sceneRootController;
	GameObjects gameObjects;
	List<MapTile> mapTiles;
	List<MapOverlay> mapOverlays;
	List<Portal> portals;
	List<Clothing> headware, bodyware, upperArmware, lowerArmware;
	List<Clothing> legware;
	File mapTileDirectory, mapOverlayDirectory, portalDirectory;
	String fileName;
	double canvasZoom = 1;
	int maximumCanvasSize = 1000; // Maximum dimensions of map
	double mapMovementSpeed = 2; // How fast the player moves on WASD press
	boolean wPressed = false, sPressed = false, aPressed = false, dPressed = false;
	double [] topLeft = {0,0}; // Contains the location of the top left of the canvas (in pixels)
	double [] playerPosition = new double[2];
	int [][] mapTileIds = new int[maximumCanvasSize][maximumCanvasSize]; // Contains corresponding IDs for all map tiles
	boolean [][] mapTileDragLocations = new boolean[maximumCanvasSize][maximumCanvasSize];
	List<int[]> mapOverlayLocations = new ArrayList<int[]>(); // ID, xcoord, ycoord, width, height
	List<int []> portalLocations = new ArrayList<int []>(); // ID, xcoord, ycoord, xDestination, yDestination
	List<String> portalLocationMaps = new ArrayList<String>(); // Contains the location of the map file that this teleports to
	List<GameCharacter> characters = new ArrayList<GameCharacter>(); // Contains all of the different characters
	Rotate defaultRotation = new Rotate(); 
	
	// FXML items
	@FXML Canvas canvas;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		sceneRootController = SceneRootController.getInstance(); // Setting up scene controller
		
		gameObjects = GameObjects.getInstance();
		mapTiles = gameObjects.getMapTiles();
		mapOverlays = gameObjects.getMapOverlays();
		portals = gameObjects.getPortals();
		headware = gameObjects.getHeadware();
		bodyware = gameObjects.getBodyware();
		upperArmware = gameObjects.getUpperArmware();
		lowerArmware = gameObjects.getLowerArmware();
		legware = gameObjects.getLegware();
		characters = gameObjects.getCharacters();
		gc = canvas.getGraphicsContext2D();
		loadMap();
		playerPosition[0] = topLeft[0] + canvas.getWidth()/2;
		playerPosition[1] = topLeft[1] + canvas.getHeight()/2;
		
		(new AnimationTimer() { // Animation thread
			@Override
			public void handle(long arg0) {
				  GameState currentState = sceneRootController.getState();
				  System.out.print("");
				  if (currentState == GameState.GAME) {
					  try {
						  if (KeyListener.W_PRESSED) {
							  if (playerPosition[1] - mapMovementSpeed >= 0) {
								  playerPosition[1] -= mapMovementSpeed;
							  }
						  }
						  if (KeyListener.S_PRESSED) {
							  if (playerPosition[1] + mapMovementSpeed <= maximumCanvasSize*(64*canvasZoom)) {
								  playerPosition[1] += mapMovementSpeed;
							  }
						  }
						  if (KeyListener.A_PRESSED) {
							  if (playerPosition[0] - mapMovementSpeed >= 0) {
								  playerPosition[0] -= mapMovementSpeed;
							  }
						  }
						  if (KeyListener.D_PRESSED) {
							  if (playerPosition[0] + mapMovementSpeed <= maximumCanvasSize*(64*canvasZoom)) {
								  playerPosition[0] += mapMovementSpeed;
							  }
						  }
						  Thread.sleep(20);
					  } catch (InterruptedException e) {
						  e.printStackTrace();
					  }
					  recalculateTopLeft();
					  redraw();
				  }
			}
		}).start();
	}
	
	// Recalculates the top left of the map
	private void recalculateTopLeft() {
		topLeft[0] = playerPosition[0] - canvas.getWidth()/2;
		topLeft[1] = playerPosition[1] - canvas.getHeight()/2;
	}
	
	private void redraw() {
		
		// Clearing canvas:
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		// Getting x and y coordinate boundaries:
		int xLowerLimit = (int) (topLeft[0] / 64);
		int xUpperLimit = (int) (2 + xLowerLimit + canvas.getWidth()/(64*canvasZoom));
		int yLowerLimit = (int) (topLeft[1] / 64);
		int yUpperLimit = (int) (2 + yLowerLimit + canvas.getHeight()/(64*canvasZoom));
		
		// Map tiles:
		for (int x = xLowerLimit; x < xUpperLimit; x++) {
			for (int y = yLowerLimit; y < yUpperLimit; y++) {
				if (x >= 0 && y >= 0) { // Making sure we're not trying to draw non-indexed map tiles
					if (mapTileIds[x][y] != 0) {// if map tile has been assigned:
						for (int i = 0; i < mapTiles.size(); i++) {
							if (mapTiles.get(i).getId() == mapTileIds[x][y]) {
								drawImageAt(mapTiles.get(i).getCurrentImage(), x*64, y*64);
							}
						}
					}
				}
			}
		}
		
		// Map overlays
		for (int i = 0; i < mapOverlayLocations.size(); i++) {
			for (int j = 0; j < mapOverlays.size(); j++) {
				MapOverlay currentMapOverlay = mapOverlays.get(j);
				if (mapOverlayLocations.get(i)[0] == currentMapOverlay.getId()) { // Checking if IDs are the same
					// If it's on the canvas:
					if (mapOverlayLocations.get(i)[1] + currentMapOverlay.getWidth() >= topLeft[0] && // if it's in from the left side of the canvas
							mapOverlayLocations.get(i)[1] < topLeft[0]+(canvas.getWidth()/canvasZoom) && // if it's in from the right side of the canvas
							mapOverlayLocations.get(i)[2] + currentMapOverlay.getHeight() >= topLeft[1] && // if it's in from the top of the canvas
							mapOverlayLocations.get(i)[2] < topLeft[1]+(canvas.getHeight()/canvasZoom)) { // if it's in from the bottom of the canvas
						drawImageAt(mapOverlays.get(j).getCurrentImage(), mapOverlayLocations.get(i)[1], mapOverlayLocations.get(i)[2]);
						break;
					}
				}
			}
		}
		
		// Portals
		for (int i = 0; i < portalLocations.size(); i++) {
			for (int j = 0; j < portals.size(); j++) {
				Portal currentPortal = portals.get(j);
				if (portalLocations.get(i)[0] == currentPortal.getId()) { // Checking if IDs are the same
					// If it's on the canvas:
					if (portalLocations.get(i)[1] + currentPortal.getWidth() >= topLeft[0] && // if it's in from the left side of the canvas
							portalLocations.get(i)[1] < topLeft[0]+(canvas.getWidth()/canvasZoom) && // if it's in from the right side of the canvas
							portalLocations.get(i)[2] + currentPortal.getHeight() >= topLeft[1] && // if it's in from the top of the canvas
									portalLocations.get(i)[2] < topLeft[1]+(canvas.getHeight()/canvasZoom)) { // if it's in from the bottom of the canvas
						drawImageAt(portals.get(j).getCurrentImage(), portalLocations.get(i)[1], portalLocations.get(i)[2]);
						break;
					}
				}
			}
		}
		
		// Characters
		GameCharacter character = characters.get(0);
		drawGameCharacter(character);
	}

	// Draws image at specified pixel coordinates
	private void drawImageAt(Image image, int xCoord, int yCoord) {
		gc.drawImage(image, (xCoord - topLeft[0])*canvasZoom, (yCoord - topLeft[1])*canvasZoom, image.getWidth()*canvasZoom, image.getHeight()*canvasZoom);
	}
	
	// Draws the image with the specified scaling
	private void drawImageAt(Image image, double xCoord, double yCoord, double scale, double xScale, double yScale, Rotate rotation) {
		gc.setTransform(rotation.getMxx(), rotation.getMyx(), rotation.getMxy(), rotation.getMyy(), rotation.getTx(), rotation.getTy());
		gc.drawImage(image, (xCoord - topLeft[0])*canvasZoom, (yCoord - topLeft[1])*canvasZoom,
				canvasZoom*scale*(image.getHeight() + xScale*(image.getWidth()-image.getHeight())), image.getHeight()*canvasZoom*scale*yScale);
		gc.setTransform(defaultRotation.getMxx(), defaultRotation.getMyx(), defaultRotation.getMxy(), defaultRotation.getMyy(), defaultRotation.getTx(), defaultRotation.getTy());
	}
	
	// Draws image with specified scaling horizontally flipped
	private void drawHorizontallyFlippedImageAt(Image image, double xCoord, double yCoord, double scale, double xScale, double yScale, Rotate rotation) {
		gc.setTransform(rotation.getMxx(), rotation.getMyx(), rotation.getMxy(), rotation.getMyy(), rotation.getTx(), rotation.getTy());
		gc.drawImage(image, (xCoord - topLeft[0])*canvasZoom, (yCoord - topLeft[1])*canvasZoom,
				-canvasZoom*scale*(image.getHeight() + xScale*(image.getWidth()-image.getHeight())), image.getHeight()*canvasZoom*scale*yScale);
		gc.setTransform(defaultRotation.getMxx(), defaultRotation.getMyx(), defaultRotation.getMxy(), defaultRotation.getMyy(), defaultRotation.getTx(), defaultRotation.getTy());
	}
	
	// Draws GameCharacter at specified pixel coordinates
	private void drawGameCharacter(GameCharacter character) {
		int xCoord = character.getXCoord();
		int yCoord = character.getYCoord();
		int headId = character.getHeadwareID();
		int bodyId = character.getBodywareId();
		int upperArmId = character.getUpperArmId();
		int lowerArmId = character.getLowerArmId();
		int legId = character.getLegId();
		
		Clothing head = headware.get(0), body = bodyware.get(0), upperArm = upperArmware.get(0), lowerArm = lowerArmware.get(0), leg = legware.get(0);
		Image headImage = headware.get(0).getImage_n(), bodyImage = bodyware.get(0).getImage_n(), upperArmImage = upperArmware.get(0).getImage_n(),
				lowerArmImage = lowerArmware.get(0).getImage_n(), legImage = legware.get(0).getImage_n();
		
		for (int i = 0; i < headware.size(); i++) {
			if (headware.get(i).getId() == headId) {
				head = headware.get(i);
				break;
			}
		}
		
		for (int i = 0; i < bodyware.size(); i++) {
			if (bodyware.get(i).getId() == bodyId) {
				body = bodyware.get(i);
				break;
			}
		}
		
		for (int i = 0; i < upperArmware.size(); i++) {
			if (upperArmware.get(i).getId() == upperArmId) {
				upperArm = upperArmware.get(i);
				break;
			}
		}
		
		for (int i = 0; i < lowerArmware.size(); i++) {
			if (lowerArmware.get(i).getId() == lowerArmId) {
				lowerArm = lowerArmware.get(i);
				break;
			}
		}
		
		for (int i = 0; i < legware.size(); i++) {
			if (legware.get(i).getId() == legId) {
				leg = legware.get(i);
				break;
			}
		}
		
		// Left leg
		double leftLegTopLeftX = 1;
		double leftLegTopLeftY = 1;
		double leftLegRotatePointX = 1;
		double leftLegRotatePointY = 1;
		double leftLegXScale = 1;
		double leftLegYScale = 1;
		
		// Right leg
		double rightLegTopLeftX = 1;
		double rightLegTopLeftY = 1;
		double rightLegRotatePointX = 1;
		double rightLegRotatePointY = 1;
		double rightLegXScale = 1;
		double rightLegYScale = 1;
		
		// Body
		double bodyImageTopLeftX = 1;
		double bodyImageTopLeftY = 1;
		double bodyImageRotatePointX = 1;
		double bodyImageRotatePointY = 1;
		
		// Head
		double headImageTopLeftX = 1;
		double headImageTopLeftY = 1;
		double headImageRotatePointX = 1;
		double headImageRotatePointY = 1;
		
		// Upper left arm
		double upperLeftArmImageTopLeftX = 1;
		double upperLeftArmImageTopLeftY = 1;
		double upperLeftArmImageRotatePointX = 1;
		double upperLeftArmImageRotatePointY = 1;
		double upperLeftArmXScale = 1;
		double upperLeftArmYScale = 1;
		
		// Upper right arm
		double upperRightArmImageTopLeftX = 1;
		double upperRightArmImageTopLeftY = 1;
		double upperRightArmImageRotatePointX = 1;
		double upperRightArmImageRotatePointY = 1;
		double upperRightArmXScale = 1;
		double upperRightArmYScale = 1;
		
		// Lower left arm
		double lowerLeftArmImageTopLeftX = 1;
		double lowerLeftArmImageTopLeftY = 1;
		double lowerLeftArmImageRotatePointX = 1;
		double lowerLeftArmImageRotatePointY = 1;
		double lowerLeftArmXScale = 1;
		double lowerLeftArmYScale = 1;
		
		// Lower right arm
		double lowerRightArmImageTopLeftX = 1;
		double lowerRightArmImageTopLeftY = 1;
		double lowerRightArmImageRotatePointX = 1;
		double lowerRightArmImageRotatePointY = 1;
		double lowerRightArmXScale = 1;
		double lowerRightArmYScale = 1;
		
		switch (character.getOrientation()) {
		
		case 'n': // TODO: Fix north case
			headImage = head.getImage_n();
			bodyImage = body.getImage_n();
			upperArmImage = upperArm.getImage_n();
			lowerArmImage = lowerArm.getImage_n();
			legImage = leg.getImage_n();
			
			// Left leg
			leftLegTopLeftX = xCoord - 1;
			leftLegTopLeftY = (yCoord - (legImage.getHeight() / 2));
			leftLegRotatePointX = leftLegTopLeftX - topLeft[0];
			leftLegRotatePointY = leftLegTopLeftY - topLeft[1];
			leftLegYScale = Math.abs(Math.cos(character.getLeftLegForwardsRotation()*Math.PI/180));
			
			// Right leg
			rightLegTopLeftX = xCoord + 1;
			rightLegTopLeftY = leftLegTopLeftY;
			rightLegRotatePointX = rightLegTopLeftX - topLeft[0];
			rightLegRotatePointY = leftLegRotatePointY;
			rightLegXScale = 1;
			rightLegYScale = Math.abs(Math.cos(character.getRightLegForwardsRotation()*Math.PI/180));
			
			// Body
			bodyImageTopLeftX = (xCoord-bodyImage.getWidth()/4);
			bodyImageTopLeftY = (leftLegTopLeftY - bodyImage.getHeight()/2);
			bodyImageRotatePointX = xCoord - topLeft[0];
			bodyImageRotatePointY = leftLegTopLeftY - bodyImage.getHeight()/4 - topLeft[1];
			
			// Head
			headImageTopLeftX = (xCoord-headImage.getWidth()/4);
			headImageTopLeftY = (bodyImageTopLeftY - headImage.getHeight()/2);
			headImageRotatePointX = xCoord - topLeft[0];
			headImageRotatePointY = bodyImageTopLeftY - headImage.getHeight()/4 - topLeft[1];
			
			// Upper left arm
			upperLeftArmImageTopLeftX = bodyImageTopLeftX;
			upperLeftArmImageTopLeftY = bodyImageTopLeftY;
			upperLeftArmImageRotatePointX = upperLeftArmImageTopLeftX - upperArmImage.getHeight()/4 - topLeft[0];
			upperLeftArmImageRotatePointY = upperLeftArmImageTopLeftY + upperArmImage.getHeight()/4 - topLeft[1];
			upperLeftArmXScale = Math.sin(-character.getUpperLeftArmForwardRotation()*Math.PI/180);
			upperLeftArmYScale = 1;
			
			// Upper right arm
			upperRightArmImageTopLeftX = (xCoord + bodyImage.getWidth()/4);
			upperRightArmImageTopLeftY = bodyImageTopLeftY;
			upperRightArmImageRotatePointX = upperRightArmImageTopLeftX + upperArmImage.getHeight()/4 - topLeft[0];
			upperRightArmImageRotatePointY = upperLeftArmImageRotatePointY;
			upperRightArmXScale = Math.sin(character.getUpperRightArmForwardsRotation()*Math.PI/180);
			upperRightArmYScale = 1;
			
			// Lower left arm
			lowerLeftArmImageRotatePointX = upperLeftArmImageRotatePointX -
					0.5 * upperLeftArmXScale * (upperArmImage.getWidth() - upperArmImage.getHeight()/2) * Math.cos(character.getUpperLeftArmSidewaysRotation()*Math.PI/180);
			lowerLeftArmImageRotatePointY = upperLeftArmImageRotatePointY +
					0.5 * upperLeftArmXScale * (upperArmImage.getWidth()-upperArmImage.getHeight()/2) * Math.sin(-character.getUpperLeftArmSidewaysRotation()*Math.PI/180);
			lowerLeftArmImageTopLeftX = lowerLeftArmImageRotatePointX + topLeft[0];
			lowerLeftArmImageTopLeftY = lowerLeftArmImageRotatePointY - lowerArmImage.getHeight()/4 + topLeft[1];
			lowerLeftArmXScale = Math.sin(-character.getLowerLeftArmForwardsRotation()*Math.PI/180);
			lowerLeftArmYScale = 1;
			
			// Lower right arm
			lowerRightArmImageRotatePointX = upperRightArmImageRotatePointX +
					0.5 * upperRightArmXScale * (upperArmImage.getWidth() - upperArmImage.getHeight()) * Math.cos(character.getUpperRightArmSidewaysRotation()*Math.PI/180);
			lowerRightArmImageRotatePointY = upperRightArmImageRotatePointY +
					0.5 * upperRightArmXScale * (upperArmImage.getWidth() - upperArmImage.getHeight()) * Math.sin(-character.getUpperRightArmSidewaysRotation()*Math.PI/180);
			lowerRightArmImageTopLeftX = lowerRightArmImageRotatePointX + topLeft[0];
			lowerRightArmImageTopLeftY = lowerRightArmImageRotatePointY - lowerArmImage.getHeight()/4 + topLeft[1];
			lowerRightArmXScale = Math.sin(character.getLowerRightArmForwardsRotation()*Math.PI/180);
			lowerRightArmYScale = 1;
			
			// Drawing images
			drawHorizontallyFlippedImageAt(legImage, leftLegTopLeftX, leftLegTopLeftY, 0.5, leftLegXScale, leftLegYScale,
					new Rotate(character.getLeftLegSidewaysRotation(), leftLegRotatePointX, leftLegRotatePointY));
			
			drawImageAt(legImage, rightLegTopLeftX, rightLegTopLeftY, 0.5, rightLegXScale, rightLegYScale,
					new Rotate(-character.getRightLegSidewaysRotation(), rightLegRotatePointX, rightLegRotatePointY));
			
			drawHorizontallyFlippedImageAt(lowerArmImage, lowerLeftArmImageTopLeftX, lowerLeftArmImageTopLeftY, 0.5, lowerLeftArmXScale, lowerLeftArmYScale,
					new Rotate(-character.getLowerLeftArmSidewaysRotation(), lowerLeftArmImageRotatePointX, lowerLeftArmImageRotatePointY));
			
			drawImageAt(lowerArmImage, lowerRightArmImageTopLeftX, lowerRightArmImageTopLeftY, 0.5, lowerRightArmXScale, lowerRightArmYScale,
					new Rotate(character.getLowerRightArmSidewaysRotation(), lowerRightArmImageRotatePointX, lowerRightArmImageRotatePointY));
			
			drawHorizontallyFlippedImageAt(upperArmImage, upperLeftArmImageTopLeftX, upperLeftArmImageTopLeftY, 0.5, upperLeftArmXScale, upperLeftArmYScale,
					new Rotate(-character.getUpperLeftArmSidewaysRotation(), upperLeftArmImageRotatePointX, upperLeftArmImageRotatePointY));
			
			drawImageAt(upperArmImage, upperRightArmImageTopLeftX, upperRightArmImageTopLeftY, 0.5, upperRightArmXScale, upperRightArmYScale,
					new Rotate(character.getUpperRightArmSidewaysRotation(), upperRightArmImageRotatePointX, upperRightArmImageRotatePointY));

			drawImageAt(bodyImage, bodyImageTopLeftX, bodyImageTopLeftY, 0.5, 1, 1,
					new Rotate(character.getBodyRotation(), bodyImageRotatePointX, bodyImageRotatePointY));
			
			drawImageAt(headImage, headImageTopLeftX, headImageTopLeftY, 0.5, 1, 1,
					new Rotate(character.getHeadRotation(), headImageRotatePointX, headImageRotatePointY));
			
			gc.setStroke(Color.BLACK);
			gc.strokeOval(lowerLeftArmImageTopLeftX, lowerLeftArmImageTopLeftY, 2, 2);
//			gc.strokeOval(upperLeftArmImageTopLeftX, upperLeftArmImageTopLeftY, 2, 2);
			gc.setStroke(Color.RED);
			gc.strokeOval(lowerLeftArmImageRotatePointX, lowerLeftArmImageRotatePointY, 2, 2);
//			gc.strokeOval(upperLeftArmImageRotatePointX, upperLeftArmImageRotatePointY, 2, 2);
			
			break;
			
		case 's':
			headImage = head.getImage_s();
			bodyImage = body.getImage_s();
			upperArmImage = upperArm.getImage_s();
			lowerArmImage = lowerArm.getImage_s();
			legImage = leg.getImage_s();
			
			// Left leg
			leftLegTopLeftX = xCoord + 1;
			leftLegTopLeftY = yCoord - (legImage.getHeight() / 2);
			leftLegRotatePointX = leftLegTopLeftX - topLeft[0];
			leftLegRotatePointY = leftLegTopLeftY - topLeft[1];
			leftLegXScale = 1;
			leftLegYScale = Math.abs(Math.cos(character.getLeftLegForwardsRotation()*Math.PI/180));
			
			// Right leg
			rightLegTopLeftX = xCoord - 1;
			rightLegTopLeftY = leftLegTopLeftY;
			rightLegRotatePointX = rightLegTopLeftX - topLeft[0];
			rightLegRotatePointY = leftLegRotatePointY;
			rightLegXScale = 1;
			rightLegYScale = Math.abs(Math.cos(character.getRightLegForwardsRotation()*Math.PI/180));
			
			// Body
			bodyImageTopLeftX = xCoord-bodyImage.getWidth()/4;
			bodyImageTopLeftY = leftLegTopLeftY - bodyImage.getHeight()/2;
			bodyImageRotatePointX = xCoord - topLeft[0];
			bodyImageRotatePointY = leftLegTopLeftY - bodyImage.getHeight()/4 - topLeft[1];
			
			// Head
			headImageTopLeftX = xCoord-headImage.getWidth()/4;
			headImageTopLeftY = (bodyImageTopLeftY - headImage.getHeight()/2);
			headImageRotatePointX = xCoord - topLeft[0];
			headImageRotatePointY = bodyImageTopLeftY - headImage.getHeight()/4 - topLeft[1];
			
			// Upper left arm
			upperLeftArmImageTopLeftX = xCoord + bodyImage.getWidth()/4;
			upperLeftArmImageTopLeftY = bodyImageTopLeftY;
			upperLeftArmImageRotatePointX = upperLeftArmImageTopLeftX + upperArmImage.getHeight()/4 - topLeft[0];
			upperLeftArmImageRotatePointY = upperLeftArmImageTopLeftY + upperArmImage.getHeight()/4 - topLeft[1];
			upperLeftArmXScale = Math.cos(character.getUpperLeftArmForwardRotation()*Math.PI/180);
			upperLeftArmYScale = 1;
			
			// Upper right arm
			upperRightArmImageTopLeftX = bodyImageTopLeftX;
			upperRightArmImageTopLeftY = bodyImageTopLeftY;
			upperRightArmImageRotatePointX = upperRightArmImageTopLeftX - upperArmImage.getHeight()/4 - topLeft[0];
			upperRightArmImageRotatePointY = upperLeftArmImageRotatePointY;
			upperRightArmXScale = Math.cos(character.getUpperRightArmForwardsRotation()*Math.PI/180);
			upperRightArmYScale = 1;
			
			// Lower left arm
			lowerLeftArmImageRotatePointX = upperLeftArmImageRotatePointX + 0.5 * (upperArmImage.getHeight() + upperLeftArmXScale *
					(upperArmImage.getWidth() - upperArmImage.getHeight())) * Math.cos(character.getUpperLeftArmSidewaysRotation()*Math.PI/180);
			lowerLeftArmImageRotatePointY = upperLeftArmImageRotatePointY + 0.5 * (upperArmImage.getHeight() + upperLeftArmXScale *
					(upperArmImage.getWidth() - upperArmImage.getHeight())) * Math.sin(-character.getUpperLeftArmSidewaysRotation()*Math.PI/180);
			lowerLeftArmImageTopLeftX = lowerLeftArmImageRotatePointX + topLeft[0];
			lowerLeftArmImageTopLeftY = lowerLeftArmImageRotatePointY - lowerArmImage.getHeight()/4 + topLeft[1];
			lowerLeftArmXScale = Math.cos(character.getLowerLeftArmForwardsRotation()*Math.PI/180);
			lowerLeftArmYScale = 1;
			
			// Lower right arm
			lowerRightArmImageRotatePointX = upperRightArmImageRotatePointX - 0.5 * (upperArmImage.getHeight() + upperRightArmXScale *
					(upperArmImage.getWidth() - upperArmImage.getHeight())) * Math.cos(character.getUpperRightArmSidewaysRotation()*Math.PI/180);
			lowerRightArmImageRotatePointY = upperRightArmImageRotatePointY + 0.5 * (upperArmImage.getHeight() + upperRightArmXScale *
					(upperArmImage.getWidth() - upperArmImage.getHeight())) * Math.sin(-character.getUpperRightArmSidewaysRotation()*Math.PI/180);
			lowerRightArmImageTopLeftX = lowerRightArmImageRotatePointX + topLeft[0];
			lowerRightArmImageTopLeftY = lowerRightArmImageRotatePointY - lowerArmImage.getHeight()/4 + topLeft[1];
			lowerRightArmXScale = Math.cos(character.getLowerRightArmForwardsRotation()*Math.PI/180);
			lowerRightArmYScale = 1;
			
			// Drawing images
			drawImageAt(legImage, leftLegTopLeftX, leftLegTopLeftY, 0.5, leftLegXScale, leftLegYScale,
					new Rotate(-character.getLeftLegSidewaysRotation(), leftLegRotatePointX, leftLegRotatePointY));
			
			drawHorizontallyFlippedImageAt(legImage, rightLegTopLeftX, rightLegTopLeftY, 0.5, rightLegXScale, rightLegYScale,
					new Rotate(character.getRightLegSidewaysRotation(), rightLegRotatePointX, rightLegRotatePointY));
			
			drawImageAt(bodyImage, bodyImageTopLeftX, bodyImageTopLeftY, 0.5, 1, 1,
					new Rotate(character.getBodyRotation(), bodyImageRotatePointX, bodyImageRotatePointY));
			
			drawImageAt(headImage, headImageTopLeftX, headImageTopLeftY, 0.5, 1, 1,
					new Rotate(character.getHeadRotation(), headImageRotatePointX, headImageRotatePointY));
			
			drawImageAt(lowerArmImage, lowerLeftArmImageTopLeftX, lowerLeftArmImageTopLeftY, 0.5, lowerLeftArmXScale, lowerLeftArmYScale,
					new Rotate(-character.getLowerLeftArmSidewaysRotation(), lowerLeftArmImageRotatePointX, lowerLeftArmImageRotatePointY));

			drawHorizontallyFlippedImageAt(lowerArmImage, lowerRightArmImageTopLeftX, lowerRightArmImageTopLeftY, 0.5, lowerRightArmXScale, lowerRightArmYScale,
					new Rotate(character.getLowerRightArmSidewaysRotation(), lowerRightArmImageRotatePointX, lowerRightArmImageRotatePointY));
			
			drawImageAt(upperArmImage, upperLeftArmImageTopLeftX, upperLeftArmImageTopLeftY, 0.5, upperLeftArmXScale, upperLeftArmYScale,
					new Rotate(-character.getUpperLeftArmSidewaysRotation(), upperLeftArmImageRotatePointX, upperLeftArmImageRotatePointY));
			
			drawHorizontallyFlippedImageAt(upperArmImage, upperRightArmImageTopLeftX, upperRightArmImageTopLeftY, 0.5, upperRightArmXScale, upperRightArmYScale,
					new Rotate(character.getUpperRightArmSidewaysRotation(), upperRightArmImageRotatePointX, upperRightArmImageRotatePointY));
			
			gc.setStroke(Color.BLACK);
			gc.strokeOval(upperLeftArmImageTopLeftX, upperLeftArmImageTopLeftY, 2, 2);
			gc.strokeOval(upperRightArmImageTopLeftX, upperLeftArmImageTopLeftY, 2, 2);
			gc.setStroke(Color.RED);
			gc.strokeOval(upperLeftArmImageRotatePointX, upperLeftArmImageRotatePointY, 2, 2);
			gc.strokeOval(upperRightArmImageRotatePointX, upperRightArmImageRotatePointY, 2, 2);
			
			break;
			
		case 'e':
			headImage = head.getImage_e();
			bodyImage = body.getImage_e();
			upperArmImage = upperArm.getImage_e();
			lowerArmImage = lowerArm.getImage_e();
			legImage = leg.getImage_e();
			
			// Left leg
			leftLegTopLeftX = xCoord - 6;
			leftLegTopLeftY = yCoord - legImage.getHeight()/2;
			leftLegRotatePointX = xCoord - topLeft[0];
			leftLegRotatePointY = leftLegTopLeftY - topLeft[1];
			leftLegXScale = 1;
			leftLegYScale = Math.abs(Math.cos(character.getLeftLegSidewaysRotation()*Math.PI/180));
			
			// Right leg
			rightLegTopLeftX = xCoord - 6;
			rightLegTopLeftY = yCoord - legImage.getHeight()/2;
			rightLegRotatePointX = xCoord - topLeft[0];
			rightLegRotatePointY = rightLegTopLeftY - topLeft[1];
			rightLegXScale = 1;
			rightLegYScale = Math.abs(Math.cos(character.getRightLegSidewaysRotation()*Math.PI/180));
			
			// Body
			bodyImageTopLeftX = xCoord - bodyImage.getWidth()/4;
			bodyImageTopLeftY = leftLegTopLeftY - bodyImage.getHeight()/2;
			bodyImageRotatePointX = xCoord - topLeft[0];
			bodyImageRotatePointY = rightLegRotatePointY - bodyImage.getHeight()/4;
			
			// Head
			headImageTopLeftX = xCoord - headImage.getWidth()/4;
			headImageTopLeftY = bodyImageTopLeftY - headImage.getHeight()/2;
			headImageRotatePointX = xCoord - topLeft[0];
			headImageRotatePointY = bodyImageTopLeftY - headImage.getHeight()/4 - topLeft[1];
			
			// Upper left arm
			upperLeftArmImageTopLeftX = xCoord - upperArmImage.getHeight()/4;
			upperLeftArmImageTopLeftY = bodyImageTopLeftY;
			upperLeftArmImageRotatePointX = xCoord - topLeft[0];
			upperLeftArmImageRotatePointY = upperLeftArmImageTopLeftY + upperArmImage.getHeight()/4 - topLeft[1];
			upperLeftArmXScale = Math.abs(Math.sin(character.getUpperLeftArmSidewaysRotation()*Math.PI/180));
			upperLeftArmYScale = 1;
			
			// Upper right arm
			upperRightArmImageTopLeftX = upperLeftArmImageTopLeftX;
			upperRightArmImageTopLeftY = upperLeftArmImageTopLeftY;
			upperRightArmImageRotatePointX = upperLeftArmImageRotatePointX;
			upperRightArmImageRotatePointY = upperLeftArmImageRotatePointY;
			upperRightArmXScale = Math.abs(Math.sin(character.getUpperRightArmSidewaysRotation()*Math.PI/180));
			upperRightArmYScale = 1;
			
			// Lower left arm
			lowerLeftArmImageRotatePointX = upperLeftArmImageRotatePointX + 0.5 * (upperArmImage.getHeight()/2 + upperLeftArmXScale *
					(upperArmImage.getWidth() - upperArmImage.getHeight())) * Math.cos(character.getUpperLeftArmForwardRotation()*Math.PI/180);
			lowerLeftArmImageRotatePointY = upperLeftArmImageRotatePointY + 0.5 * (upperArmImage.getHeight()/2 + upperLeftArmXScale *
					(upperArmImage.getWidth() - upperArmImage.getHeight())) * Math.sin(-character.getUpperLeftArmForwardRotation()*Math.PI/180);
			lowerLeftArmImageTopLeftX = lowerLeftArmImageRotatePointX + topLeft[0];
			lowerLeftArmImageTopLeftY = lowerLeftArmImageRotatePointY - lowerArmImage.getHeight()/4 + topLeft[1];
			lowerLeftArmXScale = Math.abs(Math.sin(character.getLowerLeftArmSidewaysRotation()*Math.PI/180));
			lowerLeftArmYScale = 1;
			
			// Lower right arm
			lowerRightArmImageRotatePointX = upperRightArmImageRotatePointX + 0.5 * (upperArmImage.getHeight()/2 + upperRightArmXScale *
					(upperArmImage.getWidth() - upperArmImage.getHeight())) * Math.cos(character.getUpperRightArmForwardsRotation()*Math.PI/180);
			lowerRightArmImageRotatePointY = upperRightArmImageRotatePointY + 0.5 * (upperArmImage.getHeight()/2 + upperRightArmXScale *
					(upperArmImage.getWidth() - upperArmImage.getHeight())) * Math.sin(-character.getUpperRightArmForwardsRotation()*Math.PI/180);
			lowerRightArmImageTopLeftX = lowerRightArmImageRotatePointX + topLeft[0];
			lowerRightArmImageTopLeftY = lowerRightArmImageRotatePointY - lowerArmImage.getHeight()/4 + topLeft[1];
			lowerRightArmXScale = Math.abs(Math.sin(character.getLowerRightArmSidewaysRotation()*Math.PI/180));
			lowerRightArmYScale = 1;
			
			// Drawing images
			drawImageAt(legImage, rightLegTopLeftX, rightLegTopLeftY, 0.5, rightLegXScale, rightLegYScale,
					new Rotate(-character.getRightLegForwardsRotation(), rightLegRotatePointX, rightLegRotatePointY));

			drawImageAt(legImage, leftLegTopLeftX, leftLegTopLeftY, 0.5, leftLegXScale, leftLegYScale,
					new Rotate(-character.getLeftLegForwardsRotation(), leftLegRotatePointX, leftLegRotatePointY));
			
			drawImageAt(lowerArmImage, lowerLeftArmImageTopLeftX, lowerLeftArmImageTopLeftY, 0.5, lowerLeftArmXScale, lowerLeftArmYScale,
					new Rotate(-character.getLowerLeftArmForwardsRotation(), lowerLeftArmImageRotatePointX, lowerLeftArmImageRotatePointY));

			drawImageAt(upperArmImage, upperLeftArmImageTopLeftX, upperLeftArmImageTopLeftY, 0.5, upperLeftArmXScale, upperLeftArmYScale,
					new Rotate(-character.getUpperLeftArmForwardRotation(), upperLeftArmImageRotatePointX, upperLeftArmImageRotatePointY));

			drawImageAt(bodyImage, bodyImageTopLeftX, bodyImageTopLeftY, 0.5, 1, 1,
					new Rotate(character.getBodyRotation(), bodyImageRotatePointX, bodyImageRotatePointY));
			
			drawImageAt(headImage, headImageTopLeftX, headImageTopLeftY, 0.5, 1, 1,
					new Rotate(character.getHeadRotation(), headImageRotatePointX, headImageRotatePointY));

			drawImageAt(lowerArmImage, lowerRightArmImageTopLeftX, lowerRightArmImageTopLeftY, 0.5, lowerRightArmXScale, lowerRightArmYScale,
					new Rotate(-character.getLowerRightArmForwardsRotation(), lowerRightArmImageRotatePointX, lowerRightArmImageRotatePointY));
			
			drawImageAt(upperArmImage, upperRightArmImageTopLeftX, upperRightArmImageTopLeftY, 0.5, upperRightArmXScale, upperRightArmYScale,
					new Rotate(-character.getUpperRightArmForwardsRotation(), upperRightArmImageRotatePointX, upperRightArmImageRotatePointY));
			
			gc.setStroke(Color.BLACK);
			gc.strokeOval(upperLeftArmImageTopLeftX, upperLeftArmImageTopLeftY, 2, 2);
//			gc.strokeOval(upperRightArmImageTopLeftX, upperLeftArmImageTopLeftY, 2, 2);
			gc.setStroke(Color.RED);
			gc.strokeOval(upperLeftArmImageRotatePointX, upperLeftArmImageRotatePointY, 2, 2);
//			gc.strokeOval(upperRightA
			
			break;
			
		case 'w':
			headImage = head.getImage_w();
			bodyImage = body.getImage_w();
			upperArmImage = upperArm.getImage_w();
			lowerArmImage = lowerArm.getImage_w();
			legImage = leg.getImage_w();
			
			// Left leg
			leftLegTopLeftX = xCoord + 6;
			leftLegTopLeftY = yCoord - legImage.getHeight()/2;
			leftLegRotatePointX = xCoord - topLeft[0];
			leftLegRotatePointY = leftLegTopLeftY - topLeft[1];
			leftLegXScale = 1;
			leftLegYScale = Math.cos(character.getLeftLegSidewaysRotation()*Math.PI/180);
			
			// Right leg
			rightLegTopLeftX = xCoord + 6;
			rightLegTopLeftY = yCoord - legImage.getHeight()/2;
			rightLegRotatePointX = xCoord - topLeft[0];
			rightLegRotatePointY = rightLegTopLeftY - topLeft[1];
			rightLegXScale = 1;
			rightLegYScale = Math.cos(character.getRightLegSidewaysRotation()*Math.PI/180);
			
			// Body
			bodyImageTopLeftX = xCoord - bodyImage.getWidth()/4;
			bodyImageTopLeftY = leftLegTopLeftY - bodyImage.getHeight()/2;
			bodyImageRotatePointX = xCoord - topLeft[0];
			bodyImageRotatePointY = rightLegRotatePointY - bodyImage.getHeight()/4;
			
			// Head
			headImageTopLeftX = xCoord - headImage.getWidth()/4;
			headImageTopLeftY = bodyImageTopLeftY - headImage.getHeight()/2;
			headImageRotatePointX = xCoord - topLeft[0];
			headImageRotatePointY = bodyImageTopLeftY - headImage.getHeight()/4 - topLeft[1];
			
			// Upper left arm
			upperLeftArmImageTopLeftX = xCoord + upperArmImage.getHeight()/4;
			upperLeftArmImageTopLeftY = bodyImageTopLeftY;
			upperLeftArmImageRotatePointX = xCoord - topLeft[0];
			upperLeftArmImageRotatePointY = upperLeftArmImageTopLeftY + upperArmImage.getHeight()/4 - topLeft[1];
			upperLeftArmXScale = Math.abs(Math.sin(character.getUpperLeftArmSidewaysRotation()*Math.PI/180));
			upperLeftArmYScale = 1;
			
			// Upper right arm
			upperRightArmImageTopLeftX = upperLeftArmImageTopLeftX;
			upperRightArmImageTopLeftY = upperLeftArmImageTopLeftY;
			upperRightArmImageRotatePointX = upperLeftArmImageRotatePointX;
			upperRightArmImageRotatePointY = upperLeftArmImageRotatePointY;
			upperRightArmXScale = Math.abs(Math.sin(character.getUpperRightArmSidewaysRotation()*Math.PI/180));
			upperRightArmYScale = 1;
			
			// Lower left arm
			lowerLeftArmImageRotatePointX = upperLeftArmImageRotatePointX - 0.5 * (upperArmImage.getHeight()/2 + upperLeftArmXScale *
					(upperArmImage.getWidth() - upperArmImage.getHeight())) * Math.cos(character.getUpperLeftArmForwardRotation()*Math.PI/180);
			lowerLeftArmImageRotatePointY = upperLeftArmImageRotatePointY + 0.5 * (upperArmImage.getHeight()/2 + upperLeftArmXScale *
					(upperArmImage.getWidth() - upperArmImage.getHeight())) * Math.sin(-character.getUpperLeftArmForwardRotation()*Math.PI/180);
			lowerLeftArmImageTopLeftX = lowerLeftArmImageRotatePointX + topLeft[0];
			lowerLeftArmImageTopLeftY = lowerLeftArmImageRotatePointY - lowerArmImage.getHeight()/4 + topLeft[1];
			lowerLeftArmXScale = Math.abs(Math.sin(character.getLowerLeftArmSidewaysRotation()*Math.PI/180));
			lowerLeftArmYScale = 1;
			
			// Lower right arm
			lowerRightArmImageRotatePointX = upperRightArmImageRotatePointX - 0.5 * (upperArmImage.getHeight()/2 + upperRightArmXScale *
					(upperArmImage.getWidth() - upperArmImage.getHeight())) * Math.cos(character.getUpperRightArmForwardsRotation()*Math.PI/180);
			lowerRightArmImageRotatePointY = upperRightArmImageRotatePointY + 0.5 * (upperArmImage.getHeight()/2 + upperRightArmXScale *
					(upperArmImage.getWidth() - upperArmImage.getHeight())) * Math.sin(-character.getUpperRightArmForwardsRotation()*Math.PI/180);
			lowerRightArmImageTopLeftX = lowerRightArmImageRotatePointX + topLeft[0];
			lowerRightArmImageTopLeftY = lowerRightArmImageRotatePointY - lowerArmImage.getHeight()/4 + topLeft[1];
			lowerRightArmXScale = Math.abs(Math.sin(character.getLowerRightArmSidewaysRotation()*Math.PI/180));
			lowerRightArmYScale = 1;
			
			// Drawing images
			drawHorizontallyFlippedImageAt(legImage, rightLegTopLeftX, rightLegTopLeftY, 0.5, rightLegXScale, rightLegYScale,
					new Rotate(character.getRightLegForwardsRotation(), rightLegRotatePointX, rightLegRotatePointY));

			drawHorizontallyFlippedImageAt(legImage, leftLegTopLeftX, leftLegTopLeftY, 0.5, leftLegXScale, leftLegYScale,
					new Rotate(character.getLeftLegForwardsRotation(), leftLegRotatePointX, leftLegRotatePointY));
			
			drawHorizontallyFlippedImageAt(lowerArmImage, lowerRightArmImageTopLeftX, lowerRightArmImageTopLeftY, 0.5, lowerRightArmXScale, lowerRightArmYScale,
					new Rotate(character.getLowerRightArmForwardsRotation(), lowerRightArmImageRotatePointX, lowerRightArmImageRotatePointY));
			
			drawHorizontallyFlippedImageAt(upperArmImage, upperRightArmImageTopLeftX, upperRightArmImageTopLeftY, 0.5, upperRightArmXScale, upperRightArmYScale,
					new Rotate(character.getUpperRightArmForwardsRotation(), upperRightArmImageRotatePointX, upperRightArmImageRotatePointY));

			drawImageAt(bodyImage, bodyImageTopLeftX, bodyImageTopLeftY, 0.5, 1, 1,
					new Rotate(character.getBodyRotation(), bodyImageRotatePointX, bodyImageRotatePointY));
			
			drawImageAt(headImage, headImageTopLeftX, headImageTopLeftY, 0.5, 1, 1,
					new Rotate(character.getHeadRotation(), headImageRotatePointX, headImageRotatePointY));

			drawHorizontallyFlippedImageAt(lowerArmImage, lowerLeftArmImageTopLeftX, lowerLeftArmImageTopLeftY, 0.5, lowerLeftArmXScale, lowerLeftArmYScale,
					new Rotate(character.getLowerLeftArmForwardsRotation(), lowerLeftArmImageRotatePointX, lowerLeftArmImageRotatePointY));
			
			drawHorizontallyFlippedImageAt(upperArmImage, upperLeftArmImageTopLeftX, upperLeftArmImageTopLeftY, 0.5, upperLeftArmXScale, upperLeftArmYScale,
					new Rotate(character.getUpperLeftArmForwardRotation(), upperLeftArmImageRotatePointX, upperLeftArmImageRotatePointY));
			
			gc.setStroke(Color.BLACK);
			gc.strokeOval(upperLeftArmImageTopLeftX, upperLeftArmImageTopLeftY, 2, 2);
//			gc.strokeOval(upperRightArmImageTopLeftX, upperLeftArmImageTopLeftY, 2, 2);
			gc.setStroke(Color.RED);
			gc.strokeOval(upperLeftArmImageRotatePointX, upperLeftArmImageRotatePointY, 2, 2);
//			gc.strokeOval(upperRightArmImageRotatePointX, upperRightArmImageRotatePointY, 2, 2);
			
			break;
		
		}
		
	}
	
	// Loads a specified map
	public void loadMap() {
		
		try {
			String fileName = "testing";
			this.fileName = fileName;
			// Setting file name to that of the loaded map

			BufferedReader reader = new BufferedReader(new FileReader("assets/maps/" + fileName + "/tiles.dat"));
		
			maximumCanvasSize = Integer.parseInt(reader.readLine());
			
			// Loading map tiles
			mapTileIds = new int[maximumCanvasSize][maximumCanvasSize];
			String [] currentLine = new String[1000];
			
			for (int y = 0; y < maximumCanvasSize; y++) {
				currentLine = reader.readLine().split(" ");
				for (int x = 0; x < maximumCanvasSize; x++) {
					mapTileIds[x][y] = Integer.parseInt(currentLine[x]);
				}
			}
			
			reader.close();
			
			// Loading map overlays
			reader = new BufferedReader(new FileReader("assets/maps/" + fileName + "/overlays.dat"));
			int numberOfOverlays = Integer.parseInt(reader.readLine());
			
			int [] lineSplit;
			mapOverlayLocations.clear();
			
			for (int i = 0; i < numberOfOverlays; i++) {
				currentLine = reader.readLine().split(" ");
				lineSplit = new int[5];
				for (int j = 0; j < 5; j++) {
					lineSplit[j] = Integer.parseInt(currentLine[j]);
				}
				mapOverlayLocations.add(lineSplit);
			}
			
			reader.close();
			
			// Loading portals
			reader = new BufferedReader(new FileReader("assets/maps/" + fileName + "/portals.dat"));
			int numberOfPortals = Integer.parseInt(reader.readLine()); 
			
			portalLocations.clear();
			
			for (int i = 0; i < numberOfPortals; i++) {
				currentLine = reader.readLine().split(" ");
				lineSplit = new int[7];
				for (int j = 0; j < 7; j++) {
					lineSplit[j] = Integer.parseInt(currentLine[j]);
				}
				portalLocations.add(lineSplit);
			}
			
			reader.close();
			
			if (numberOfPortals > 0) { // Read in portal destinations
				reader = new BufferedReader(new FileReader("src/maps/" + fileName + "/portalDestinations.dat"));
				for (int i = 0; i < numberOfPortals; i++) {
					portalLocationMaps.add(reader.readLine()); // Adding line to array
				}
				reader.close(); // Releasing resource
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
