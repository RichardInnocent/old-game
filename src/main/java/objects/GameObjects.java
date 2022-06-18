package objects;

import java.io.FileInputStream;
import java.util.List;

import javafx.scene.image.Image;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class GameObjects {

	String mapTileLocation = "assets/mapTiles", mapOverlayLocation = "assets/mapOverlays", portalLocation = "assets/portals",
			clothingLocation = "assets/clothing", characterLocation = "assets/characters";
	static GameObjects gameObjects;
	List<MapTile> mapTiles = new ArrayList<MapTile>();
	List<MapOverlay> mapOverlays = new ArrayList<MapOverlay>();
	List<Portal> portals = new ArrayList<Portal>();
	List<GameCharacter> characters = new ArrayList<GameCharacter>();
	List<Clothing> headware = new ArrayList<Clothing>();
	List<Clothing> upperArmware = new ArrayList<Clothing>();
	List<Clothing> lowerArmware = new ArrayList<Clothing>();
	List<Clothing> bodyware = new ArrayList<Clothing>();
	List<Clothing> legware = new ArrayList<Clothing>();
	String location = ""; // Holds the location where the images were found
	
	private GameObjects() {}
	
	// Returns all map tiles
	public List<MapTile> getMapTiles() {
		return mapTiles;
	}
	
	// Returns all map overlays
	public List<MapOverlay> getMapOverlays() {
		return mapOverlays;
	}
	
	// Returns all portals
	public List<Portal> getPortals() {
		return portals;
	}
	
	// Returns all characters
	public List<GameCharacter> getCharacters() {
		return characters;
	}
	
	// Returns all clothing for a character's head
	public List<Clothing> getHeadware() {
		return headware;
	}
	
	// Returns all clothing for a character's arms
	public List<Clothing> getUpperArmware() {
		return upperArmware;
	}
	
	// Returns all clothing for a character's lower arms
	public List<Clothing> getLowerArmware() {
		return lowerArmware;
	}
	
	// Returns all clothing for a character's body
	public List<Clothing> getBodyware() {
		return bodyware;
	}
	
	// Returns all clothing for a character's legs
	public List<Clothing> getLegware() {
		return legware;
	}
	
	// Returns the location of the map tiles
	public String getMapTileLocation() {
		return mapTileLocation;
	}
	
	// Returns the location of the map overlays
	public String getMapOverlayLocation() {
		return mapOverlayLocation;
	}
	
	// Returns the location of the portals
	public String getPortalLocation() {
		return portalLocation;
	}
	
	// Returns location of clothing
	public String getClothingLocation() {
		return clothingLocation;
	}
	
	// Allowing only one instance of GameObjects to be created
	public static GameObjects getInstance() {
		if (gameObjects == null)
			gameObjects = new GameObjects();
		return gameObjects;
	}
	
	// Prints the data for the map tile with the corresponding ID
	public void printMapTileData(int id) {
		boolean mapTileFound = false;
		for (MapTile mapTile : mapTiles) {
			if (mapTile.getId() == id) {
				mapTileFound = true;
				mapTile.printData();
			}
		}
		if (mapTileFound == false) {
			System.out.println("Map tile ID " + id + " not found.");
		}
	}
	
	// Prints the data for the map overlay with the corresponding ID
	public void printMapOverlayData(int id) {
		boolean mapOverlayFound = false;
		for (MapOverlay mapOverlay : mapOverlays) {
			if (mapOverlay.getId() == id) {
				mapOverlayFound = true;
				mapOverlay.printData();
			}
		}
		if (mapOverlayFound == false) {
			System.out.println("Map tile ID " + id + " not found.");
		}
	}
	
	// Prints all map tiles and their IDs
	public void printAllMapTileIDs() {
		for (MapTile mapTile : mapTiles) {
			System.out.println(mapTile.getId() + "\t" + mapTile.getName());
		}
	}
	
	// Prints the map overlay data with the corresponding data
	public void printAllMapOverlayIDs() {
		for (MapOverlay mapOverlay : mapOverlays) {
			System.out.println(mapOverlay.getId() + "\t" + mapOverlay.getName());
		}
	}
	
	// Retrieving all map tiles
	public void getAllMapTiles() {
		// Finding the directory
		File mapTileDirectory = new File(mapTileLocation);
		if (!mapTileDirectory.isDirectory()) {
			System.out.println("Map tile directory not found.");
			System.exit(1);
		}
		
		try {
			// Finding the data file
			BufferedReader reader = new BufferedReader(new FileReader(mapTileLocation + "/mapTileData.dat"));
			
			String inputLine, name;
			String [] components;
			int id, collisionLowerX, collisionUpperX, collisionLowerY, collisionUpperY;
			boolean colliding;
			ArrayList<Image> images;
			// Format of data file:
			// 	For colliding objects: 
			//		imageName locationFromSrc 1
			// 		imageName locationFromSrc 1 collisionLowerX collisionUpperX collisionLowerY collisionUpperY
			//	For non-colliding objects:
			//		imageName locationFromSrc 0
			while ((inputLine = reader.readLine()) != null) {
				components = inputLine.split(" ");
				
				// Getting name and trimming
				name = components[0];
				name = name.substring(0, name.length());
				
				// Hashing string for ID:
				id = name.hashCode();
				
				// Determining collision:
				colliding = (components[2].equals("1")) ? true : false;
				
				// Collecting images
				images = getImages(name, components[1]);
				
				// Only does the remaining if the images exist:
				if (images != null) {
					
					// If colliding, get collision boundaries:
					if (colliding) {
						if (components.length == 7) {
							collisionLowerX = Integer.parseInt(components[3]);
							collisionUpperX = Integer.parseInt(components[4]);
							collisionLowerY = Integer.parseInt(components[5]);
							collisionUpperY = Integer.parseInt(components[6]);
						} else {
							collisionLowerX = 0;
							collisionUpperX = 64;
							collisionLowerY = 0;
							collisionUpperY = 64;
						}
						
						// Adding map tile to database
						mapTiles.add(new MapTile(name, location, id, colliding, collisionLowerX, collisionUpperX, collisionLowerY, collisionUpperY, images));
					} else {
						// Adding map tile to database
						mapTiles.add(new MapTile(name, location, id, images));
					}
				}
			}
			
			// Closing resource
			reader.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("Map tile data file not found.");
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Map tile data file could not be read.");
			System.exit(0);
		}
	}
	
	// Retrieving all map tiles
	public void getAllMapOverlays() {
		try {
			// Finding the data file
			BufferedReader reader = new BufferedReader(new FileReader(mapOverlayLocation + "/mapOverlayData.dat"));
			
			String inputLine, name;
			String [] components;
			int id, collisionLowerX, collisionUpperX, collisionLowerY, collisionUpperY;
			boolean colliding, tileCollisionOverride;
			ArrayList<Image> images = new ArrayList<Image>();
			
			// Format of data file:
			// 	For colliding objects: 
			//		imageName locationFromSrc 1
			// 		imageName locationFromSrc 1 collisionLowerX collisionUpperX collisionLowerY collisionUpperY
			//	For non-colliding objects:
			//		imageName locationFromSrc 0
			while ((inputLine = reader.readLine()) != null) {
				components = inputLine.split(" ");
				
				// Getting name and trimming
				name = components[0];
				name = name.substring(0, name.length());
				
				// Hashing string for ID:
				id = name.hashCode();
				
				// Determining collision and whether to override the underneath tile's true collision value (useful for a bridge or door)
				switch (components[2]) {
				
				case "0":
					colliding = false;
					tileCollisionOverride = false;
					break;
					
				case "1":
					colliding = true;
					tileCollisionOverride = false;
					break;
					
				case "2":
					colliding = false;
					tileCollisionOverride = true;
					break;
					
				default:
					colliding = true;
					tileCollisionOverride = false;
				
				}
				
				// Collecting images
				images = getImages(name, components[1]);
				
				// Only does the remaining if the images exist:
				if (images != null) {
					
					// If colliding, get collision boundaries:
					if (colliding) {
						if (components.length == 7) {
							collisionLowerX = Integer.parseInt(components[3]);
							collisionUpperX = Integer.parseInt(components[4]);
							collisionLowerY = Integer.parseInt(components[5]);
							collisionUpperY = Integer.parseInt(components[6]);
						} else {
							collisionLowerX = 0;
							collisionUpperX = 64;
							collisionLowerY = 0;
							collisionUpperY = 64;
						}
						// Adding map tile to database
						mapOverlays.add(new MapOverlay(name, location, id, colliding, tileCollisionOverride, collisionLowerX, collisionUpperX, collisionLowerY, collisionUpperY, images));
					} else {
						// Adding map tile to database
						mapOverlays.add(new MapOverlay(name, location, id, tileCollisionOverride, images));
					}
				}
				
			}
			
			// Closing resource
			reader.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("Map overlay data file not found.");
			System.exit(1);
		} catch (IOException e) {
			System.out.println("Map overlay data file could not be read.");
			System.exit(1);
		}
		
	}
	
	public void getAllPortals() {
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(portalLocation + "/portalData.dat"));

			String inputLine, name;
			int id;
			String [] components;
			ArrayList<Image> images = new ArrayList<Image>();
			
			// Format of data file:
			//	imageName locationFromSrc
			while ((inputLine = reader.readLine()) != null) {
				components = inputLine.split(" ");
				
				// Getting name and trimming
				name = components[0];
				name = name.substring(0, name.length());
				
				// Hashing string for ID:
				id = name.hashCode();
				
				// Collecting images
				images = getImages(name, components[1]);
				
				// Only does the remaining if the images exist:
				if (images != null) {
					portals.add(new Portal(name, location, id, images));
				}
			}
			
			reader.close();
		} catch (FileNotFoundException e) {
			System.out.println("Map overlay data file not found.");
			System.exit(1);
		} catch (IOException e) {
			System.out.println("Map overlay data file could not be read.");
			System.exit(1);
		}
		
	}
	
	// Gets all of the attire
	public void getAllAttire() {
		// Finding the directory
		File attireDirectory = new File(clothingLocation);
		if (!attireDirectory.isDirectory()) {
			System.out.println("Attire directory not found.");
			System.exit(1);
		}
		
		// Getting all clothing items:
		try {
			// Getting all of the headware items:
			BufferedReader reader = new BufferedReader(new FileReader(clothingLocation + "/head/headwareData.dat"));
			String inputLine = reader.readLine();
			int numberOfInputs = Integer.parseInt(inputLine);
			for (int i = 0; i < numberOfInputs; i++) {
				inputLine = reader.readLine();
				headware.add(new Clothing(inputLine, clothingLocation + "/head"));
			}
			reader.close();
			
			// Trying to get all upper arm items:
			reader = new BufferedReader(new FileReader(clothingLocation + "/upperArms/upperArmData.dat"));
			inputLine = reader.readLine();
			numberOfInputs = Integer.parseInt(inputLine);
			for (int i = 0; i < numberOfInputs; i++) {
				inputLine = reader.readLine();
				upperArmware.add(new Clothing(inputLine, clothingLocation + "/upperArms"));
			}
			reader.close();
			
			// Trying to get all lower arm items:
			reader = new BufferedReader(new FileReader(clothingLocation + "/lowerArms/lowerArmData.dat"));
			inputLine = reader.readLine();
			numberOfInputs = Integer.parseInt(inputLine);
			for (int i = 0; i < numberOfInputs; i++) {
				inputLine = reader.readLine();
				lowerArmware.add(new Clothing(inputLine, clothingLocation + "/lowerArms"));
			}
			reader.close();
			
			// Getting all bodyware items:
			reader = new BufferedReader(new FileReader(clothingLocation + "/body/bodywareData.dat"));
			inputLine = reader.readLine();
			numberOfInputs = Integer.parseInt(inputLine);
			for (int i = 0; i < numberOfInputs; i++) {
				inputLine = reader.readLine();
				bodyware.add(new Clothing(inputLine, clothingLocation + "/body"));
			}
			reader.close();
			
			// Getting all legware items:
			reader = new BufferedReader(new FileReader(clothingLocation + "/legs/legwareData.dat"));
			inputLine = reader.readLine();
			numberOfInputs = Integer.parseInt(inputLine);
			for (int i = 0; i < numberOfInputs; i++) {
				inputLine = reader.readLine();
				legware.add(new Clothing(inputLine, clothingLocation + "/legs"));
			}
			reader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
	
	public void getAllCharacters() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(characterLocation + "/characters.dat"));
			String inputLine = reader.readLine();
			int numberOfCharacters = Integer.parseInt(inputLine);
			
			for (int i = 0; i < numberOfCharacters; i++) {
				inputLine = reader.readLine();
				characters.add(new GameCharacter(inputLine));
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Gets all images for a particular object name in a specified directory and returns them in order
	private ArrayList<Image> getImages(String name, String location) throws IOException {
		
		// Storing images:
		ArrayList<Image> images = new ArrayList<>();
		
		// Checks to see if directory exists
		File directory = new File("assets/" + location);
		if (!directory.isDirectory()) {
			return null;
		}
		
		// Gets all files in directory
		File [] filesInDirectory = directory.listFiles();
		boolean fileFound = false;
		Image currentImage;
		
		// Looping through directory to find right images
		for (File currentFile : filesInDirectory) {
			if (currentFile.getName().startsWith(name + "_vr")) {
				fileFound = true;
				this.location = (directory + "/" + currentFile.getName());
				currentImage = new Image(new FileInputStream(this.location));
				images.add(currentImage);
			}
		}
		
		// Returning images
		if (fileFound) return images;
		else return null;
		
	}
	
}
