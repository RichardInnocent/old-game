package objects;

import java.util.ArrayList;

import javafx.scene.image.Image;

public class MapTile extends GameObject {
	
	int currentImageIndex = 0;

	@Override
	public void onGameTick() {
		if (images.size() > 1) {
			if (++currentImageIndex == images.size())
				currentImageIndex = 0;
			currentImage = images.get(currentImageIndex);
		}
	}
	
	// Sets the size of the object to that of the size of the map tiles
	public MapTile () {
		width = height = 64;
	}
	
	// Creating colliding map tile object
	public MapTile(String _name, String _location, int _id, boolean _colliding, int _collisionLowerX, int _collisionUpperX, int _collisionLowerY, int _collisionUpperY, ArrayList<Image> _images) {
		name = _name;
		id = _id;
		location = _location;
		colliding = _colliding;
		gameObjectType = 't';
		collisionLowerX = _collisionLowerX;
		collisionUpperX = _collisionUpperX;
		collisionLowerY = _collisionLowerY;
		collisionUpperY = _collisionUpperY;
		images = _images;
		currentImage = images.get(0);
	}
	
	// Creating non-colliding map tile object
	public MapTile(String _name, String _location, int _id, ArrayList<Image> _images) {
		name = _name;
		id = _id;
		location = _location;
		colliding = false;
		gameObjectType = 't';
		images = _images;
		currentImage = images.get(0);
	}

	@Override
	public void printData() {
		System.out.println("Type: Map tile");
		System.out.println("Name: " + name);
		System.out.println("Location: " + location);
		System.out.println("Colliding: " + colliding);
		System.out.println("Lower X collision boundary: " + collisionLowerX);
		System.out.println("Upper X collision boundary: " + collisionUpperX);
		System.out.println("Lower Y collision boundary: " + collisionLowerY);
		System.out.println("Upper Y collision boundary: " + collisionUpperY);
		System.out.println("Number of associated images: " + images.size());
		System.out.println();
	}

}
