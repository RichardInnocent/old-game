package objects;

import java.util.ArrayList;

import javafx.scene.image.Image;

public class Portal extends GameObject {
	
	String destination;
	int [] destinationCoordinates = {0,0};
	
	public Portal(String _name, String _location, int _id, ArrayList<Image> _images) {
		name = _name;
		location = _location;
		colliding = false;
		gameObjectType = 'p';
		id = _id;
		images = _images;
		currentImage = images.get(0);
		width = (int) currentImage.getWidth();
		height = (int) currentImage.getHeight();
	}

	public Portal(String _name, String _location, String _destination, int [] _destinationCoordinates, int _id, ArrayList<Image> _images) {
		name = _name;
		location = _location;
		colliding = false;
		gameObjectType = 'p';
		destination = _destination;
		destinationCoordinates = _destinationCoordinates;
		id = _id;
		images = _images;
		currentImage = images.get(0);
		width = (int) currentImage.getWidth();
		height = (int) currentImage.getHeight();
	}

	// Prints all associated data
	@Override
	public void printData() {
		System.out.println("Type: Portal");
		System.out.println("Type specifier: " + gameObjectType);
		System.out.println("Name: " + name);
		System.out.println("Location: " + location);
		System.out.println("Teleporting to: " + destination);
		System.out.println("Image width: " + getWidth());
		System.out.println("Image height: " + getHeight());
		System.out.println("Colliding: " + colliding);
		System.out.println("Lower X collision boundary: " + collisionLowerX);
		System.out.println("Upper X collision boundary: " + collisionUpperX);
		System.out.println("Lower Y collision boundary: " + collisionLowerY);
		System.out.println("Upper Y collision boundary: " + collisionUpperY);
		System.out.println("Number of associated images: " + images.size());
		System.out.println();
	}
	
	// Sets the path to the destination map file
	public void setPortalDestination(String _destination) {
		destination = _destination;
	}
	
	// Gets the destination map file path
	public String getDestination() {
		return destination;
	}
	
	// Gets the coordinate position of the object after it has been teleported
	public void setDestinationCoordinates(int [] _destinationCoordinates) {
		destinationCoordinates = _destinationCoordinates;
	}
	
	// Sets the coordinate position of the object after it has been teleported
	public int [] setDestinationCoordinates() {
		return destinationCoordinates;
	}

}
