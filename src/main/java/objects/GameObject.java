package objects;

import java.util.ArrayList;

import javafx.scene.image.Image;

public abstract class GameObject {
	
	String name;
	int id = 0, width = 64, height = 64;
	boolean colliding = false;
	int collisionLowerX = 0, collisionUpperX = 64, collisionLowerY = 0, collisionUpperY = 64;
	ArrayList<Image> images;
	Image currentImage;
	String location;
	char gameObjectType;
	
	// Decides what the object should do on a game click
	public void onGameTick() {}
	
	// Returns the name of the object
	public String getName() {
		return name;
	}
	
	// Sets the name of the object
	public void setName(String _name) {
		name = _name;
	}
	
	// Returns the ID of the object
	public int getId() {
		return id;
	}
	
	// Sets the ID of the object
	public void setID(int _id) {
		id = _id;
	}
	
	// Returns the type of game object
	public char getGameObjectType() {
		return gameObjectType;
	}
	
	// Sets the location of the images
	public void setLocation(String _location) {
		location = _location;
	}
	
	// Returns the location of the images
	public String getLocation() {
		return location;
	}
	
	// Returns the height of the object image
	public int getHeight() {
		return height;
	}
	
	// Sets that height of the object image
	public void setHeight(int _height) {
		height = _height;
	}
	
	// Returns the width of the object image
	public int getWidth() {
		return width;
	}
	
	// Sets the width of the object image
	public void setWidth(int _width) {
		width = _width;
	}
	
	// Returns whether the object is colliding or not
	public boolean isColliding() {
		return colliding;
	}
	
	// Sets the object to be colliding or non-colliding
	public void setColliding(boolean _colliding) {
		colliding = _colliding;
	}
	
	// Returns the collision bounds of the object (lowest x, largest x, lowest y, largest y)
	public int [] getCollisionBounds() {
		int [] collisionBounds = {collisionLowerX, collisionUpperX, collisionLowerY, collisionUpperY};
		return collisionBounds;
	}
	
	// Sets the boundaries of the collision field
	public void setCollisionBounds(int [] collisionBounds) {
		if (collisionBounds.length == 4) {
			collisionLowerX = collisionBounds[0];
			collisionUpperX = collisionBounds[1];
			collisionLowerY = collisionBounds[2];
			collisionUpperY = collisionBounds[3];
		} else {
			System.out.println("Error: number of elements in collision bounds array must be 4.");
		}
	}
	
	// Returns the current image of the object
	public Image getCurrentImage() {
		return currentImage;
	}
	
	public abstract void printData();

}
