package objects;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javafx.scene.image.Image;

public class Clothing extends Attire {
	
	double defence = 0;
	double weight = 0;

	public Clothing (String inputLine, String directory) {
		String [] components = inputLine.split(" ");
		
		// Name
		name = components[0];
		
		// ID
		id = name.hashCode();
		
		Image image_n;
		Image image_s;
		Image image_e;
		Image image_w;
		
		// Images
		int counter = 1;
		try { // Adding images to ArrayList if all images have been found
			image_n = new Image(new FileInputStream(directory + "/" + name + "/" + name + "_n_vr" + counter + ".png"));
			image_s = new Image(new FileInputStream(directory + "/" + name + "/" + name + "_s_vr" + counter + ".png"));
			image_e = new Image(new FileInputStream(directory + "/" + name + "/" + name + "_e_vr" + counter + ".png"));
			image_w = new Image(new FileInputStream(directory + "/" + name + "/" + name + "_w_vr" + counter + ".png"));
			
			images_n.add(image_n);
			images_s.add(image_s);
			images_e.add(image_e);
			images_w.add(image_w);
			
			if (counter == 1) {
				current_image_n = images_n.get(0);
				current_image_s = images_s.get(0);
				current_image_e = images_e.get(0);
				current_image_w = images_w.get(0);
			}
			
			imagesFound = true;
			
		} catch (IllegalArgumentException | FileNotFoundException e) {
			if (counter == 1) {
				e.printStackTrace();
			}
		}
		
		if (components.length > 2) {
			defence = Integer.parseInt(components[1]);
			weight = Integer.parseInt(components[2]);
		}
		
	}
	
}
