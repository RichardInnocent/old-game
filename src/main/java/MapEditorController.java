import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import objects.GameObject;
import objects.GameObjects;
import objects.MapOverlay;
import objects.MapTile;
import objects.Portal;

public class MapEditorController implements Initializable {
	
	SceneRootController sceneRootController;
	GameObjects gameObjects;
	List<MapTile> mapTiles;
	List<MapOverlay> mapOverlays;
	List<Portal> portals;
	File mapTileDirectory;
	File mapOverlayDirectory;
	File portalDirectory;
	List<TreeItem<String>> treeItems = new ArrayList<TreeItem<String>>();
	double canvasZoom = 1;
	int maximumCanvasSize = 1000; // Maximum dimensions of map
	double mapMovementSpeed = 8; // How fast the camera moves on WASD press
	boolean wPressed = false, sPressed = false, aPressed = false, dPressed = false;
	String fileName;
	boolean deleting = false;
	boolean snapToGrid = false;
	boolean showGrid = false;
	TreeItem<String> treeViewRoot;
	GameObject currentGameObject;
	GraphicsContext gc;
	double [] mapEditorTopLeft = {0,0}; // Contains the location of the top left of the canvas (in pixels)
	int [][] mapTileIds = new int[maximumCanvasSize][maximumCanvasSize]; // Contains corresponding IDs for all map tiles
	boolean [][] mapTileDragLocations = new boolean[maximumCanvasSize][maximumCanvasSize];
	List<int[]> mapOverlayLocations = new ArrayList<int[]>(); // ID, xcoord, ycoord, width, height
	List<int []> portalLocations = new ArrayList<int []>(); // ID, xcoord, ycoord, xDestination, yDestination
	List<String> portalLocationMaps = new ArrayList<String>(); // Contains the location of the map file that this teleports to
	int [] mousePressedAt = {0,0};
	int currentLoop = 0, numberOfLoopsBeforeTick = 18;
	
	// ------------- FXML items --------------------
	@FXML Canvas canvas;
	@FXML TreeView<String> treeView;
	@FXML MenuBar menuBar;
	@FXML MenuItem FileSaveMap;
	@FXML MenuItem FileLoadMap;
	@FXML MenuItem FileExitEditor;
	@FXML Button snapToGridButton;
	@FXML Button deleteCreateButton;
	@FXML Button toggleGridButton;
	
	// On start:
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		sceneRootController = SceneRootController.getInstance(); // Setting up scene controller
		gameObjects = GameObjects.getInstance();
		mapTiles = gameObjects.getMapTiles();
		mapOverlays = gameObjects.getMapOverlays();
		portals = gameObjects.getPortals();
		currentGameObject = mapTiles.get(0);
		gc = canvas.getGraphicsContext2D();
		
		// Getting directories for images
		mapTileDirectory = new File(gameObjects.getMapTileLocation());
		mapOverlayDirectory = new File(gameObjects.getMapOverlayLocation());
		portalDirectory = new File(gameObjects.getPortalLocation());
		createTree(); // Creating all branches of tree
		
		for (int i = 0; i < maximumCanvasSize; i++) {
			for (int j = 0; j < maximumCanvasSize; j++) {
				mapTileIds[i][j] = 0;
			}
		}
		
		// Listens to a change in selected nodes on the tree
		treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
			@Override
			public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue, TreeItem<String> newValue) {
				
				if (newValue.isLeaf()) {
					for (int i = 0; i < mapTiles.size(); i++) {
						if (mapTiles.get(i).getName().equals(newValue.getValue())) {
							currentGameObject = mapTiles.get(i);
							return;
						}
					}
					
					for (int i = 0; i < mapOverlays.size(); i++) {
						if (mapOverlays.get(i).getName().equals(newValue.getValue())) {
							currentGameObject = mapOverlays.get(i);
							return;
						}
					}
					
					for (int i = 0; i < portals.size(); i++) {
						if (portals.get(i).getName().equals(newValue.getValue())) {
							currentGameObject = portals.get(i);
							return;
						}
					}
				}
			}
		});
		
		// Setting up canvas mouse click listener:
		canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				
				if (event.isPrimaryButtonDown()) { // Left click
					if (event.isControlDown()) {
						// Deleting portal
						int index = getPortalLocationIndexAt((int) event.getX(), (int) event.getY());
						if (index != -1) {
							portalLocations.remove(index);
							portalLocationMaps.remove(index);
							for (int i = 0; i < portalLocationMaps.size(); i++) {
								System.out.println(portalLocationMaps.get(i));
							}
							System.out.println();
							return;
						}
						// Deleting map overlay
						index = getMapOverlayLocationIndexAt((int) event.getX(), (int) event.getY());
						if (index != -1) {
							mapOverlayLocations.remove(index);
							return;
						}
					}
					
					if (!deleting) {
						char objectType = currentGameObject.getGameObjectType();
						
						int xCoord, yCoord;
						
						switch (objectType) {
						
						case 't':
							mousePressedAt[0] = (int) (((event.getX()/canvasZoom) + mapEditorTopLeft[0]) / 64);
							mousePressedAt[1] = (int) (((event.getY()/canvasZoom) + mapEditorTopLeft[1]) / 64);
							break;
							
						case 'o':
							if (!snapToGrid) {
								xCoord = (int) (event.getX()/canvasZoom + mapEditorTopLeft[0] - (currentGameObject.getWidth()/2));
								yCoord = (int) (event.getY()/canvasZoom + mapEditorTopLeft[1] - (currentGameObject.getHeight()));
							} else {
								xCoord = (int) (event.getX()/canvasZoom + mapEditorTopLeft[0])/64;
								xCoord *= 64;
								// TODO: Fix this:
								yCoord = (int) ((event.getY()+64)/canvasZoom + mapEditorTopLeft[1] - (currentGameObject.getHeight()))/64;
								yCoord *= 64;
							}
							int [] mapOverlayData = {currentGameObject.getId(), xCoord, yCoord, currentGameObject.getWidth(), currentGameObject.getHeight()};
							mapOverlayLocations.add(mapOverlayData);
							sortOverlayLocations();
							break;
							
						case 'p':
							if (!snapToGrid) {
								xCoord = (int) (event.getX()/canvasZoom + mapEditorTopLeft[0] - (currentGameObject.getWidth()/2));
								yCoord = (int) (event.getY()/canvasZoom + mapEditorTopLeft[1] - (currentGameObject.getHeight()));
							} else {
								xCoord = (int) (event.getX()/canvasZoom + mapEditorTopLeft[0])/64;
								xCoord *= 64;
								yCoord = (int) (event.getY()/canvasZoom + mapEditorTopLeft[1] - (currentGameObject.getHeight()))/64;
								yCoord *= 64;
							}
							int [] portalData = {currentGameObject.getId(), xCoord, yCoord, currentGameObject.getWidth(), currentGameObject.getHeight(), 0, 0};
							portalLocations.add(portalData);
							portalLocationMaps.add("undef");
							break;
						}
					} else {
						int deleteIndex = getPortalLocationIndexAt((int) event.getX(), (int) event.getY());
						if (deleteIndex != -1) {
							portalLocations.remove(deleteIndex);
							portalLocationMaps.remove(deleteIndex);
						} else {
							deleteIndex = getMapOverlayLocationIndexAt((int) event.getX(), (int) event.getY());
							if (deleteIndex != -1) {
								mapOverlayLocations.remove(deleteIndex);
							}
						}
					}
				} else { // On right click
					int portalIndex = getPortalLocationIndexAt((int) event.getX(), (int) event.getY()); // Get portal clicked on
					if (portalIndex != -1) { // If portal was found
						TextInputDialog dialog = new TextInputDialog("");
						dialog.setTitle("Select destination");
						dialog.setHeaderText("Which map should this teleport you to?");
						dialog.setGraphic(null);
						
						// Showing dialog:
						Optional<String> nameRegion = dialog.showAndWait();
						if (nameRegion.isPresent()) {
							String name = nameRegion.get(); // Get name of map
							if (name != null) {
								portalLocationMaps.set(portalIndex, name);
								TextInputDialog dialog2 = new TextInputDialog("");
								dialog2.setTitle("Select destination");
								dialog2.setHeaderText("Enter the x y coordinates of the teleport destination separated by a space.");
								nameRegion = dialog2.showAndWait();
								if (nameRegion.isPresent()) {
									String coordinates = nameRegion.get(); // Getting coordinates
									if (coordinates != null) {
										try {
											String [] coords = coordinates.split(" ");
											int newX = Integer.parseInt(coords[0]); // Parsing coordinates
											int newY = Integer.parseInt(coords[1]);
											if (newX >= 0 && newY >= 0) { // Checking coordinates are reasonable
												int [] line = portalLocations.get(portalIndex);
												line[5] = newX;
												line[6] = newY;
												portalLocations.set(portalIndex, line);
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}
							}
						}
					}
				}
			}
		});
		
		// Showing selected area on map tile drags
		canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				
				if (!deleting && !event.isControlDown()) {
					char objectType = currentGameObject.getGameObjectType();
					int xCoord, yCoord;
					
					switch (objectType) {
					
					case 't':
						// Getting right tile
						xCoord = (int) (((event.getX()/canvasZoom) + mapEditorTopLeft[0]) / 64);
						yCoord = (int) (((event.getY()/canvasZoom) + mapEditorTopLeft[1]) / 64);
						int xDirection = (mousePressedAt[0] > xCoord) ? -1 : 1;
						int yDirection = (mousePressedAt[1] > yCoord) ? -1 : 1;
						
						for (int x = 0; x < maximumCanvasSize; x++) {
							for (int y = 0; y < maximumCanvasSize; y++) {
								mapTileDragLocations[x][y] = false;
							}
						}
						
						for (int x = mousePressedAt[0]; x*xDirection <= xCoord*xDirection; x += xDirection) {
							for (int y = mousePressedAt[1]; y*yDirection <= yCoord*yDirection; y += yDirection) {
								mapTileDragLocations[x][y] = true;
							}
						}
						break;
					case 'o':
						break;
					case 'p':
						break;
					}
				}
			}
		});
		
		canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				
				if (!deleting && !event.isControlDown()) {
					char objectType = currentGameObject.getGameObjectType();
					int xCoord, yCoord;
					
					switch (objectType) {
					
					case 't':
						// Getting right tile
						xCoord = (int) (((event.getX()/canvasZoom) + mapEditorTopLeft[0]) / 64);
						yCoord = (int) (((event.getY()/canvasZoom) + mapEditorTopLeft[1]) / 64);
						int xDirection = (mousePressedAt[0] > xCoord) ? -1 : 1;
						int yDirection = (mousePressedAt[1] > yCoord) ? -1 : 1;
						
						for (int x = mousePressedAt[0]; x*xDirection <= xCoord*xDirection; x += xDirection) {
							for (int y = mousePressedAt[1]; y*yDirection <= yCoord*yDirection; y += yDirection) {
								mapTileIds[x][y] = currentGameObject.getId();
							}
						}
						
						for (int x = 0; x < maximumCanvasSize; x++) {
							for (int y = 0; y < maximumCanvasSize; y++) {
								mapTileDragLocations[x][y] = false;
							}
						}
						
						break;
						
					case 'o':
						break;
						
					}
				}
				
			}
		});
		
		canvas.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				double newZoom = canvasZoom + event.getDeltaY()/400;
				if (newZoom >= 0.4 && newZoom <= 2) {
					canvasZoom = newZoom;
					mapEditorTopLeft[0] += event.getDeltaY()/200;
					mapEditorTopLeft[1] += event.getDeltaY()/200;
					mapMovementSpeed = 8/canvasZoom;
				}
			}
		});
		
		(new AnimationTimer() { // Map controller thread
			@Override
			public void handle(long arg0) {
				  GameState currentState = sceneRootController.getState();
				  System.out.print("");
				  if (currentState == GameState.MAP_EDITOR) {
					  try {
						  if (KeyListener.W_PRESSED) {
							  if (mapEditorTopLeft[1] - mapMovementSpeed >= 0) {
								  mapEditorTopLeft[1] -= mapMovementSpeed;
							  }
						  }
						  if (KeyListener.S_PRESSED) {
							  if (KeyListener.CTRL_PRESSED) {
								  fileSave();
							  } else if (mapEditorTopLeft[1] + mapMovementSpeed <= maximumCanvasSize*(64*canvasZoom)) {
								  mapEditorTopLeft[1] += mapMovementSpeed;
							  }
						  }
						  if (KeyListener.A_PRESSED) {
							  if (mapEditorTopLeft[0] - mapMovementSpeed >= 0) {
								  mapEditorTopLeft[0] -= mapMovementSpeed;
							  }
						  }
						  if (KeyListener.D_PRESSED) {
							  if (mapEditorTopLeft[0] + mapMovementSpeed <= maximumCanvasSize*(64*canvasZoom)) {
								  mapEditorTopLeft[0] += mapMovementSpeed;
							  }
						  }
						  Thread.sleep(20);
					  } catch (InterruptedException e) {
						  e.printStackTrace();
					  }
					  
					  // Handling game clicks
					  currentLoop++;
					  if (currentLoop == numberOfLoopsBeforeTick) {
						  gameTick();
						  currentLoop = -1;
					  }
					  redraw();
				  }
			}
			 }).start();
		
	}
	
	// Handling the game click
	private void gameTick() {
		for (int i = 0; i < mapTiles.size(); i++) {
			mapTiles.get(i).onGameTick();
		}
		for (int i = 0; i < mapOverlays.size(); i++) {
			mapOverlays.get(i).onGameTick();
		}
	}
	
	// Gets the map overlay clicked on
	private int getMapOverlayLocationIndexAt(int x, int y) {
		x = (int) (x/canvasZoom + mapEditorTopLeft[0]);
		y = (int) (y/canvasZoom + mapEditorTopLeft[1]);
		
		for (int i = mapOverlayLocations.size() - 1; i >= 0; i--) {
			int [] overlayData = mapOverlayLocations.get(i);
			if (x > overlayData[1] && x < overlayData[1] + overlayData[3] && y > overlayData[2] && y < overlayData[2] + overlayData[4]) {
				return i;
			}
		}
		return -1;
	}
	
	// Gets the portal clicked on
	private int getPortalLocationIndexAt(int x, int y) {
		x = (int) (x/canvasZoom + mapEditorTopLeft[0]);
		y = (int) (y/canvasZoom + mapEditorTopLeft[1]);
		
		for (int i = portalLocations.size() - 1; i >= 0; i--) {
			int [] portalData = portalLocations.get(i);
			if (x > portalData[1] && x < portalData[1] + portalData[3] && y > portalData[2] && y < portalData[2] + portalData[4]) {
				return i;
			}
		}
		return -1;
	}
	
	// Sorts the overlays so that ones at the top are always at the back
	private void sortOverlayLocations() {
		Collections.sort(mapOverlayLocations, new Comparator<int[]>() {
			
			@Override
			public int compare(int[] a, int[] b) {
				return Integer.compare(a[2]+a[4], b[2]+b[4]);
			}
		});
	}
	
	// Creates the treeView
	public void createTree() {
		treeViewRoot = new TreeItem<String>("Root");
		treeView.setRoot(treeViewRoot);
		treeView.setShowRoot(false);
		findTreeItems(mapTileDirectory, makeBranch("Map Tiles", treeView.getRoot()));
		findTreeItems(mapOverlayDirectory, makeBranch("Map Overlays", treeView.getRoot()));
		findTreeItems(portalDirectory, makeBranch("Portals", treeView.getRoot()));
		
		// Finding all map tiles:
		for (int i = 0; i < mapTiles.size(); i++) { // Looping through all map tiles
			String [] directoryComponents = mapTiles.get(i).getLocation().replace("\\", "/").split("/"); // Breaking down directories
			String lineFolder = directoryComponents[directoryComponents.length-2]; // Getting folder images contained in
			for (int j = 0; j < treeItems.size(); j++) { // Looping through all tree items
				if (treeItems.get(j).getValue().equals(lineFolder)) { // If tree item has same name as containing folder...
					makeBranch(mapTiles.get(i).getName(), treeItems.get(j)); // Create branch with that tile's name in that tree item
					break;
				}
			}
		}
		
		// Finding all map overlays:
		for (int i = 0; i < mapOverlays.size(); i++) { // Looping through all map overlays
			String [] directoryComponents = mapOverlays.get(i).getLocation().replace("\\", "/").split("/"); // Breaking down directories
			String lineFolder = directoryComponents[directoryComponents.length-2]; // Getting folder images contained in
			for (int j = 0; j < treeItems.size(); j++) { // Looping through all tree items
				if (treeItems.get(j).getValue().equals(lineFolder)) { // If tree item has same name as containing folder...
					makeBranch(mapOverlays.get(i).getName(), treeItems.get(j)); // Create branch with overlay's name in that tree item
					break;
				}
			}
		}
		
		// Finding all portals
		for (int i = 0; i < portals.size(); i++) { // Looping through all portals
			String [] directoryComponents = portals.get(i).getLocation().replace("\\", "/").split("/"); // Breaking down directories
			String lineFolder = directoryComponents[directoryComponents.length-2]; // Getting folder images contained in
			for (int j = 0; j < treeItems.size(); j++) { // Looping through all tree items
				if (treeItems.get(j).getValue().equals(lineFolder)) { // If tree item has same name as containing folder...
					makeBranch(portals.get(i).getName(), treeItems.get(j)); // Create branch with overlay's name in that tree item
					break;
				}
			}
		}
		
	}
	
	// Makes a branch on the tree item with the given name
	private TreeItem<String> makeBranch(String name, TreeItem<String> root) {
		TreeItem<String> item = new TreeItem<String>(name);
		root.getChildren().add(item);
		treeItems.add(item);
		return item;
	}
	
	// Iterates through directories and files in the mapTextureDirectory location, adding them all to the given TreeItem.
	private void findTreeItems(File file, TreeItem<String> treeItem) {
		if (file.isDirectory()) {
			File [] filesInDirectory = file.listFiles();
			for (File currentFile : filesInDirectory) {
				if (currentFile.isDirectory()) {
					TreeItem<String> newTreeItem = makeBranch(currentFile.getName(), treeItem);
					findTreeItems(currentFile, newTreeItem);
				}
			}
		}
	}
	
	// Redraws all images on canvas
	private void redraw() {
		
		// Clearing canvas:
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		// Getting x and y coordinate boundaries:
		int xLowerLimit = (int) (mapEditorTopLeft[0] / 64);
		int xUpperLimit = (int) (2 + xLowerLimit + canvas.getWidth()/(64*canvasZoom));
		int yLowerLimit = (int) (mapEditorTopLeft[1] / 64);
		int yUpperLimit = (int) (2 + yLowerLimit + canvas.getHeight()/(64*canvasZoom));
		
		// Map tiles:
		for (int x = xLowerLimit; x < xUpperLimit; x++) {
			for (int y = yLowerLimit; y < yUpperLimit; y++) {
				if (mapTileIds[x][y] != 0) {// if map tile has been assigned:
					for (int i = 0; i < mapTiles.size(); i++) {
						if (mapTiles.get(i).getId() == mapTileIds[x][y]) {
							drawImageAt(mapTiles.get(i).getCurrentImage(), x*64, y*64);
						}
					}
				}
			}
		}
		
		// Map tiles dragged:
		for (int x = xLowerLimit; x < xUpperLimit; x++) {
			for (int y = yLowerLimit; y < yUpperLimit; y++) {
				if (mapTileDragLocations[x][y]) {// if map tile has been assigned:
					drawImageAt(currentGameObject.getCurrentImage(), x*64, y*64);
				}
			}
		}
		
		// Map overlays
		for (int i = 0; i < mapOverlayLocations.size(); i++) {
			for (int j = 0; j < mapOverlays.size(); j++) {
				MapOverlay currentMapOverlay = mapOverlays.get(j);
				if (mapOverlayLocations.get(i)[0] == currentMapOverlay.getId()) { // Checking if IDs are the same
					// If it's on the canvas:
					if (mapOverlayLocations.get(i)[1] + currentMapOverlay.getWidth() >= mapEditorTopLeft[0] && // if it's in from the left side of the canvas
							mapOverlayLocations.get(i)[1] < mapEditorTopLeft[0]+(canvas.getWidth()/canvasZoom) && // if it's in from the right side of the canvas
							mapOverlayLocations.get(i)[2] + currentMapOverlay.getHeight() >= mapEditorTopLeft[1] && // if it's in from the top of the canvas
							mapOverlayLocations.get(i)[2] < mapEditorTopLeft[1]+(canvas.getHeight()/canvasZoom)) { // if it's in from the bottom of the canvas
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
					if (portalLocations.get(i)[1] + currentPortal.getWidth() >= mapEditorTopLeft[0] && // if it's in from the left side of the canvas
							portalLocations.get(i)[1] < mapEditorTopLeft[0]+(canvas.getWidth()/canvasZoom) && // if it's in from the right side of the canvas
							portalLocations.get(i)[2] + currentPortal.getHeight() >= mapEditorTopLeft[1] && // if it's in from the top of the canvas
									portalLocations.get(i)[2] < mapEditorTopLeft[1]+(canvas.getHeight()/canvasZoom)) { // if it's in from the bottom of the canvas
						drawImageAt(portals.get(j).getCurrentImage(), portalLocations.get(i)[1], portalLocations.get(i)[2]);
						break;
					}
				}
			}
		}
		
		// Drawing grid lines
		if (showGrid) { // If grid lines are to be shown
			gc.setStroke(Color.WHITE);
			// Drawing horizontal grid lines
			for (double x = (64 - (mapEditorTopLeft[0] % 64))*canvasZoom; x <= canvas.getWidth(); x += 64*canvasZoom) {
				gc.strokeLine(x, 0, x, canvas.getHeight());
			}
			
			// Drawing vertical grid lines
			for (double y = (64 - (mapEditorTopLeft[1] % 64))*canvasZoom; y <= canvas.getHeight(); y += 64*canvasZoom) {
				gc.strokeLine(0, y, canvas.getWidth(), y);
			}
		}
		
	}
	
	
	// Draws image at specified pixel coordinates
	private void drawImageAt(Image image, int xCoord, int yCoord) {
		gc.drawImage(image, (xCoord - mapEditorTopLeft[0])*canvasZoom, (yCoord - mapEditorTopLeft[1])*canvasZoom, image.getWidth()*canvasZoom, image.getHeight()*canvasZoom);
	}
	
	// ------------- Button functionality -----------------
	
	// Returns to main menu
	public void fileExitEditor() {
		sceneRootController.setState(GameState.MAIN_MENU);
	}
	
	// Saves the files for the map
	public void fileSave() {
		if (fileName == null) {
			fileSaveAs();
			return;
		}
		
		File directory = new File("assets/maps/" + fileName);
		if (!directory.isDirectory()) { // If directory has been removed or cannot be found
			fileSaveAs();
		} else { // If directory can be found, delete all content in that directory
			File [] allFiles = directory.listFiles();
			for (int i = 0; i < allFiles.length; i++)
				allFiles[i].delete(); // Deleting all files in directory
		}
		
		saveMap(directory);
		
	}
	
	// Saves the files for the map in a separate location
	public void fileSaveAs() {

		// Setting up dialog:
		TextInputDialog dialog = new TextInputDialog("");
		dialog.setTitle("Name your map");
		dialog.setHeaderText("Enter a name for your map.");
		dialog.setGraphic(null);
		
		// Showing dialog:
		Optional<String> nameRegion = dialog.showAndWait();
		if (nameRegion.isPresent()) {
			String name = nameRegion.get();
			// Checking there are no illegal modifiers in the name
			if (name.contains("#") || name.contains("%") || name.contains("&") || name.contains("{") || name.contains("}") || name.contains("\\") ||
					name.contains("<") ||name.contains(">") || name.contains("*") || name.contains("?") || name.contains("/") || name.contains("$") ||
					name.contains("!") || name.contains( "\'") || name.contains("\"") || name.contains(":") || name.contains("@") ||
					name.contains("+") || name.contains("`") || name.contains("|") || name.contains("=")) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText("Map could not be saved");
				alert.setContentText("Map name contains illegal modifiers");
				alert.showAndWait();
				return;
			} else {
				fileName = name;
			}
		}
		
		if (fileName != null) {
			File directory = new File("assets/maps/" + fileName);
			
			if (!directory.isDirectory()) { // Checking there is not already another map by this name
				if (!directory.mkdir()) { // Checking if directory could be created
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText("Map could not be saved");
					alert.setContentText("Map directory could not be created");
					alert.showAndWait();
					return;
				};
			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText("Map could not be saved");
				alert.setContentText("Map directory already exists. Please choose an alternative name.");
				alert.showAndWait();
				return;
			}
			saveMap(directory);
		}
		
	}
	
	// Saves the map
	private void saveMap(File directory) {
		saveMapTiles(directory);
		saveMapOverlays(directory);
		savePortals(directory);
	}
	
	// Saves the map tiles to a file
	private void saveMapTiles(File directory) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(directory + "/tiles.dat"));
			
			writer.write(Integer.toString(maximumCanvasSize));
			writer.write("\r\n");
			for (int y = 0; y < maximumCanvasSize; y++) {
				for (int x = 0; x < maximumCanvasSize; x++) {
					writer.write(Integer.toString(mapTileIds[x][y]));
					writer.write(" ");
				}
				writer.write("\r\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Saves the map overlays to a file
	private void saveMapOverlays(File directory) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(directory + "/overlays.dat"));
			writer.write(Integer.toString(mapOverlayLocations.size()));
			for (int i = 0; i < mapOverlayLocations.size(); i++) {
				int [] data = mapOverlayLocations.get(i);
				writer.write("\r\n" + Integer.toString(data[0]) + " " + Integer.toString(data[1]) + " " + Integer.toString(data[2]) + " " + Integer.toString(data[3]) +
						" " + Integer.toString(data[4]));
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Saves the portals to a file
	private void savePortals(File directory) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(directory + "/portals.dat"));
			writer.write(Integer.toString(portalLocations.size()));
			for (int i = 0; i < portalLocations.size(); i++) {
				int [] data = portalLocations.get(i);
				writer.write("\r\n" + Integer.toString(data[0]) + " " + Integer.toString(data[1]) + " " + Integer.toString(data[2]) + " " + Integer.toString(data[3]) +
						" " + Integer.toString(data[4]) + " " + Integer.toString(data[5]) + " " + Integer.toString(data[6]));
			}
			writer.close();
			if (portalLocationMaps.size() > 0) {
				writer = new BufferedWriter(new FileWriter(directory + "/portalDestinations.dat"));
				for (int i = 0; i < portalLocations.size()-1; i++) {
					writer.write(portalLocationMaps.get(i) + "\r\n");
				}
				writer.write(portalLocationMaps.get(portalLocations.size()-1));
				writer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	// Loads a specified map
	public void fileLoadMap() {
		
		try {
			String fileName = "testing";
			// Setting file name to that of the loaded map
			this.fileName = fileName;
			
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
				reader = new BufferedReader(new FileReader("assets/maps/" + fileName + "/portalDestinations.dat"));
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
	
	
	// Sets the delete flag when clicked
	public void deleteCreateButtonPressed() {
		if (deleting) {
			deleteCreateButton.setText("Delete items");
			deleteCreateButton.setTextFill(Color.RED);
			deleting = false;
		} else {
			deleteCreateButton.setText("Create items");
			deleteCreateButton.setTextFill(Color.BLACK);
			deleting = true;
		}
	}
	
	// Sets all map tiles on map to the selected tile
	public void editSetAllMapTilesToSelectedTile() {
		if (currentGameObject.getGameObjectType() == 't') {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Tile overwrite confirmation");
			alert.setHeaderText("Are you sure you wish to overwrite all map tiles?");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				for (int x = 0; x < maximumCanvasSize; x++) {
					for (int y = 0; y < maximumCanvasSize; y++) {
						mapTileIds[x][y] = currentGameObject.getId();
					}
				}
			}
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Overwrite Error");
			alert.setHeaderText("You must select a map tile before using this feature");
			alert.showAndWait();
		}
	}
	
	// Toggles whether non-tile object should be snapped to the grid or not
	public void toggleSnap() {
		if (snapToGrid) {
			snapToGrid = false;
			snapToGridButton.setText("Snap to Grid");
		} else {
			snapToGrid = true;
			snapToGridButton.setText("Freeform");
		}
	}
	
	// Toggles whether or not to show the map grid
	public void toggleGrid() {
		if (showGrid) {
			showGrid = false;
			toggleGridButton.setText("Show grid");
		} else {
			showGrid = true;
			toggleGridButton.setText("Hide grid");
		}
	}
	
}
