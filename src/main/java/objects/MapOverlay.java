package objects;

import java.util.ArrayList;

import javafx.scene.image.Image;

public class MapOverlay extends GameObject {
	
	boolean tileCollisionOverride;
	int currentImageIndex = 0;

	@Override
	public void onGameTick() {
		if (images.size() > 1) {
			if (++currentImageIndex == images.size())
				currentImageIndex = 0;
			currentImage = images.get(currentImageIndex);
		}
	}

	// Creating colliding map tile object
	public MapOverlay(String _name, String _location, int _id, boolean _colliding, boolean _tileCollisionOverride, int _collisionLowerX,
			int _collisionUpperX, int _collisionLowerY, int _collisionUpperY, ArrayList<Image> _images) {
		name = _name;
		id = _id;
		location = _location;
		colliding = _colliding;
		tileCollisionOverride = _tileCollisionOverride;
		gameObjectType = 'o';
		collisionLowerX = _collisionLowerX;
		collisionUpperX = _collisionUpperX;
		collisionLowerY = _collisionLowerY;
		collisionUpperY = _collisionUpperY;
		images = _images;
		currentImage = images.get(0);
	}
	
	// Creating non-colliding map tile object
	public MapOverlay(String _name, String _location, int _id, boolean _tileCollisionOverride, ArrayList<Image> _images) {
		name = _name;
		id = _id;
		location = _location;
		gameObjectType = 'o';
		colliding = false;
		tileCollisionOverride = _tileCollisionOverride;
		images = _images;
		currentImage = images.get(0);
		collisionUpperX = (int) currentImage.getWidth();
		collisionUpperY = (int) currentImage.getHeight();
	}
	
	// Prints all data associated with the image
	@Override
	public void printData() {
		System.out.println("Type: MapOverlay");
		System.out.println("Type specifier: " + gameObjectType);
		System.out.println("Name: " + name);
		System.out.println("Location: " + location);
		System.out.println("Image width: " + getWidth());
		System.out.println("Image height: " + getHeight());
		System.out.println("Colliding: " + colliding);
		System.out.println("Tile collision override: " + tileCollisionOverride);
		System.out.println("Lower X collision boundary: " + collisionLowerX);
		System.out.println("Upper X collision boundary: " + collisionUpperX);
		System.out.println("Lower Y collision boundary: " + collisionLowerY);
		System.out.println("Upper Y collision boundary: " + collisionUpperY);
		System.out.println("Number of associated images: " + images.size());
		System.out.println();
	}
	
	// Gets the actual width of the current image
	@Override
	public int getWidth() {
		return (int) currentImage.getWidth();
	}
	
	// Gets the actual height of the current image
	@Override
	public int getHeight() {
		return (int) currentImage.getHeight();
	}
	
}
