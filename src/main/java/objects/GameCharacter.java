package objects;

public class GameCharacter extends GameObject {
	
	int headId, bodyId, upperArmId, lowerArmId, legId;
	char orientation = 'n';
	CharacterPose pose = CharacterPose.STANDING;
	int xCoord = 250; // TODO data file for character positions
	int yCoord = 250;
	double headRotation = 0;
	double bodyRotation = 0;
	double upperLeftArmForwardsRotation = -45;
	double upperLeftArmSidewaysRotation = 90;
	double upperRightArmForwardsRotation = -135;
	double upperRightArmSidewaysRotation = 90;
	double lowerLeftArmForwardsRotation = 45;
	double lowerLeftArmSidewaysRotation = 90;
	double lowerRightArmForwardsRotation = -45;
	double lowerRightArmSidewaysRotation = 90;
	double leftLegForwardsRotation = -10;
	double leftLegSidewaysRotation = 0;
	double rightLegForwardsRotation = 30;
	double rightLegSidewaysRotation = 0;
	
	public GameCharacter() {
		setCollisionBounds();
	}
	
	public GameCharacter(String inputLine) {
		
		String [] components = inputLine.split(" ");
		
		// Name
		name = components[0];
		
		// Clothing
		headId = components[1].hashCode();
		bodyId = components[2].hashCode();
		upperArmId = components[3].hashCode();
		lowerArmId = components[4].hashCode();
		legId = components[5].hashCode();
		
	}
	
	public GameCharacter(int _headId, int _bodyId, int _upperArmId, int _lowerArmId, int _legId) {
		setCollisionBounds();
		headId = _headId;
		bodyId = _bodyId;
		upperArmId = _upperArmId;
		lowerArmId = _lowerArmId;
		legId = _legId;
	}
	
	public GameCharacter(int _headId, int _bodyId, int _upperArmId, int _lowerArmId, int _legId, String _name) {
		setCollisionBounds();
		headId = _headId;
		bodyId = _bodyId;
		upperArmId = _upperArmId;
		lowerArmId = _lowerArmId;
		legId = _legId;
		name = _name;
	}
	
	@Override
	public void onGameTick() {
		// TODO
	}
	
	// Returns the x coordinate of the middle of the character
	public int getXCoord() {
		return xCoord;
	}
	
	// Returns the y coordinate of the bottom of the character's legs
	public int getYCoord() {
		return yCoord;
	}
	
	// Sets the pose of the character
	public void setPose(CharacterPose _pose) {
		pose = _pose;
	}
	
	// Gets the pose of the character
	public CharacterPose getPose() {
		return pose;
	}
	
	// Sets collision boundaries and orientation
	private void setCollisionBounds() {
		collisionLowerX = 10;
		collisionUpperX = 54;
		collisionLowerY = 40;
		collisionUpperY = 0;
	}
	
	// Sets the orientation of the given player and changes images accordingly
	public void setOrientation(char _orientation) {
		orientation = _orientation;
	}
	
	// Gets the orientation of the player
	public char getOrientation() {
		return orientation;
	}
	
	public double getHeadRotation() {
		return headRotation;
	}
	
	public double getBodyRotation() {
		return bodyRotation;
	}
	
	public double getUpperLeftArmForwardRotation() {
//		if (upperLeftArmForwardsRotation >= 90) {
//			upperLeftArmForwardsRotation = 0;
//		} else {
//			upperLeftArmForwardsRotation++;
//		}
		return upperLeftArmForwardsRotation;
	}
	
	public double getUpperLeftArmSidewaysRotation() {
//		if (upperLeftArmSidewaysRotation >= 90) {
//			upperLeftArmSidewaysRotation = 0;
//		} else {
//			upperLeftArmForwardsRotation++;
//		}
		return upperLeftArmSidewaysRotation;
	}
	
	public double getUpperRightArmForwardsRotation() {
//		if (upperRightArmForwardsRotation >= 90) {
//			upperRightArmForwardsRotation = 0;
//		} else {
//			upperRightArmForwardsRotation++;
//		}
		return upperRightArmForwardsRotation;
	}
	
	public double getUpperRightArmSidewaysRotation() {
//		if (upperRightArmSidewaysRotation >= 90) {
//			upperRightArmSidewaysRotation = 0;
//		} else {
//			upperRightArmSidewaysRotation++;
//		}
		return upperRightArmSidewaysRotation;
	}
	
	public double getLowerLeftArmForwardsRotation() {
		return lowerLeftArmForwardsRotation;
	}
	
	public double getLowerLeftArmSidewaysRotation() {
		return lowerLeftArmSidewaysRotation;
	}
	
	public double getLowerRightArmForwardsRotation() {
		return lowerRightArmForwardsRotation;
	}
	
	public double getLowerRightArmSidewaysRotation() {
		return lowerRightArmSidewaysRotation;
	}
	
	public double getLeftLegForwardsRotation() {
		return leftLegForwardsRotation;
	}
	
	public double getLeftLegSidewaysRotation() {
		return leftLegSidewaysRotation;
	}
	
	public double getRightLegForwardsRotation() {
		return rightLegForwardsRotation;
	}
	
	public double getRightLegSidewaysRotation() {
		return rightLegSidewaysRotation;
	}
	
	// TODO: change to WEAR
	// Sets the ID of the headwear:
	public void setHeadwareId(int id) {
		headId = id;
	}
	
	// Returns the ID of the headwear:
	public int getHeadwareID() {
		return headId;
	}
	
	// Sets the ID of the bodywear:
	public void setBodywareId(int id) {
		bodyId = id;
	}
	
	// Returns the ID of the headwear:
	public int getBodywareId() {
		return bodyId;
	}
	
	// Sets the ID of the upper arm:
	public void setUpperArmId(int id) {
		upperArmId = id;
	}
	
	// Returns the ID of the upper arm:
	public int getUpperArmId() {
		return upperArmId;
	}
	
	// Sets the ID of the lower arm:
	public void setLowerArmId(int id) {
		lowerArmId = id;
	}
	
	// Returns the ID of the lower arm:
	public int getLowerArmId() {
		return lowerArmId;
	}
	
	// Sets the ID of the legwear:
	public void setLegID(int id) {
		legId = id;
	}
	
	// Returns the ID of the legwear:
	public int getLegId() {
		return legId;
	}
	
	// Prints the data for a character
	@Override
	public void printData() {
		System.out.println("Type: Character");
		System.out.println("Name: " + name);
		System.out.println("Location: " + location);
		System.out.println("Colliding: " + colliding);
		System.out.println("Lower X collision boundary: " + collisionLowerX);
		System.out.println("Upper X collision boundary: " + collisionUpperX);
		System.out.println("Lower Y collision boundary: " + collisionLowerY);
		System.out.println("Upper Y collision boundary: " + collisionUpperY);
		System.out.println("Headware ID: " + headId);
		System.out.println("Bodywear ID: " + bodyId);
		System.out.println("Upper armware ID: " + upperArmId);
		System.out.println("Lower armware ID: " + lowerArmId);
		System.out.println("Legware ID: " + legId);
	}

}
