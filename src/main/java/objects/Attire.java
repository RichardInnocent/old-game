package objects;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;

public class Attire {
	
	int id;
	boolean imagesFound = false;
	List<Image> images_n = new ArrayList<Image>(1);
	List<Image> images_s = new ArrayList<Image>(1);
	List<Image> images_e = new ArrayList<Image>(1);
	List<Image> images_w = new ArrayList<Image>(1);
	Image current_image_n, current_image_s, current_image_e, current_image_w;
	String name;
	
	public int getId() {
		return id;
	}
	
	public boolean imagesFound() {
		return imagesFound;
	}
	
	public Image getImage_n() {
		return current_image_n;
	}
	
	public Image getImage_s() {
		return current_image_s;
	}
	
	public Image getImage_e() {
		return current_image_e;
	}
	
	public Image getImage_w() {
		return current_image_w;
	}
	
	public void onTick() {}
	
}
