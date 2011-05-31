import sprites.*;
import guicomponents.*;

//S4P stuff
Sprite[] sprite = new Sprite[3];
// G4P stuff
GHorzSlider sdrScale, sdrRotation, sdrSpeed;
GHorzSlider sdrAcceleration, sdrDirection, sdrWorldScale;
GLabel lblScale, lblRotation, lblSpeed, lblAcceleration;
GLabel lblDirection, lblWorldScale;
GCheckbox cbxColAreaOn;
GButton btnResetScale, btnResetSpeed, btnResetAccel;
GButton btnResetWorld;

Domain domain;	
// Sprite that has been clicked on
Sprite selSprite;

final int MAX_SPEED = 200;
boolean canPan = false;

String instruction;

void setup(){
  size(700,560);
  registerPre(this);
  registerMouseEvent(this);		

  // Create the GUI to control this sketch using G4P
  int dy = 16; // used to separate the componets vertically		
  lblScale = new GLabel(this,"Scale",10,10,200,dy);
  sdrScale = new GHorzSlider(this, 10,10+dy,180,dy);
  sdrScale.setLimits(100,50,150);
  btnResetScale = new GButton(this,"1",194,10+dy,16,dy);

  lblRotation = new GLabel(this,"Rotation",10,10+2*dy,200,dy);
  sdrRotation = new GHorzSlider(this, 10,10+3*dy,200,dy);
  sdrRotation.setLimits(0,0,360);

  lblSpeed = new GLabel(this,"Speed",10,10+4*dy,200,dy);
  sdrSpeed = new GHorzSlider(this, 10,10+5*dy,180,dy);
  sdrSpeed.setLimits(0,0,MAX_SPEED);
  btnResetSpeed = new GButton(this,"0",194,10+5*dy,16,dy);

  lblAcceleration = new GLabel(this,"Acceleration",10,10+6*dy,200,dy);
  sdrAcceleration = new GHorzSlider(this, 10,10+7*dy,180,dy);
  sdrAcceleration.setLimits(0,0,50);
  btnResetAccel = new GButton(this,"0",194,10+7*dy,16,dy);

  lblDirection = new GLabel(this,"Direction",10,10+8*dy,200,dy);
  sdrDirection = new GHorzSlider(this, 10,10+9*dy,200,dy);
  sdrDirection.setLimits(0,-180,180);

  cbxColAreaOn = new GCheckbox(this, "Show Collision Areas",10,20+10*dy,200);


  // Create all the sprite stuff 
  // Do not show collision areas
  S4P.collisionAreasVisible = false;
  // Constrain sprites to small portion of the world
  Domain domain = new Domain(220,0,width,height);

  this.lblWorldScale = new GLabel(this, "World Scale",10,10+12*dy,200);
  sdrWorldScale = new GHorzSlider(this, 10,10+13*dy,200,dy);
  sdrWorldScale.setLimits(30,10,50);
  btnResetWorld = new GButton(this, "Reset World Display",10,10+15*dy,200,20);

  // Create the sprites
  sprite[0] = new Sprite(this, "ct_balloon.png", 10);
  sprite[0].setVelXY(13.8f, 14.9f);
  sprite[0].setXY(320, 100);
  sprite[0].setDomain(domain, Sprite.REBOUND);
  sprite[0].respondToMouse(true);
  sprite[0].setZorder(20);

  sprite[1] = new Sprite(this, "ct_chopper.png", 10);
  sprite[1].setVelXY(-12.8f, 18.9f);
  sprite[1].setXY(width-120, 100);
  sprite[1].setDomain(domain, Sprite.REBOUND);
  sprite[1].respondToMouse(true);
  sprite[1].setZorder(5);

  sprite[2] = new Sprite(this, "ct_plane.png", 10);
  sprite[2].setVelXY(-8.8f, - 10.9f);
  sprite[2].setXY(width-120, height-100);
  sprite[2].setDomain(domain, Sprite.REBOUND);
  sprite[2].respondToMouse(true);
  sprite[2].setZorder(10);

  selSprite = sprite[0];
  updateGUI();

  instruction = "You can select a sprite to adjust by clicking on it.\n";
  instruction += "In this demo you can change which part of the world to ";
  instruction+= " display by clicking and dragging in the pane on the ";
  instruction += "right. \n Notice that the sprites are still ";
  instruction += "constrained by the original domain (screen)\n.";
  instruction += "The sprites collision area depends on rotation ";
  instruction += "and scale (see reference for more info).\n";
  instruction += "Sprite to sprite collision detection is ";
  instruction += "available not used in this demo."; 
}

/*
 * Method provided by Processing and is called every 
 * loop before the draw method. It has to be activated
 * with the following statement in setup() <br>
 * <pre>registerPre(this);</pre>
 */
void pre(){
  // Calculate time since last called and update 
  // sprite's state
  S4P.updateTime();
  S4P.updateSprites();
  // Bit of a fix to cap velocities due to 
  // acceleration otherwise my be difficult
  // to select sprite with mouse.
  for(int i = 0; i < 3; i++){
    if(sprite[i].getAcceleration() > 0){
      if(sprite[i].getSpeed() > MAX_SPEED){
        sprite[i].setSpeed(MAX_SPEED);
        sprite[i].setAcceleration(0);
      }
    }
  }
}

void draw(){
  background(color(192,192,255));
  updateGUI();
  S4P.drawSprites();

  // Draw instructions & GUI after sprites
  noStroke();
  fill(color(128,128,255));
  rect(0,0,220,height);
  fill(0);
  text(instruction, 10,290,200,300);
  G4P.draw();
}

/*
 * Method provided by Processing and is called every 
 * loop It has to be activated with the following statement
 * in setup()
 * registerMouseEvent(this);
 */
void mouseEvent(MouseEvent event){
  switch(event.getID()){
  case MouseEvent.MOUSE_PRESSED:
    if(mouseX > 220)
      canPan = true;
    break;
  case MouseEvent.MOUSE_RELEASED:
    canPan = false;
    break;
  case MouseEvent.MOUSE_DRAGGED:
    if(canPan && pmouseX > 220 && mouseX > 220){
      PointF2D old = S4P.pixel2world(pmouseX, pmouseY);
      PointF2D curr = S4P.pixel2world(mouseX, mouseY);
      S4P.moveWorldBy(old.x-curr.x, old.y-curr.y);
    }
    break;
  }
}

/*
 * S4P Eventhandler called mouse is PRESSED, RELEASED
 * or CLICKED over the sprite.
 * 
 * @param sprite
 */
void handleSpriteEvents(Sprite sprite) { 
  if(sprite.eventType == Sprite.CLICKED){
    selSprite = sprite;
    updateGUI();
  }	
}

/*
 * Update the GUI components based on selected sprite
 */
void updateGUI() {
  // The second parameter in setValue prevents events being created
  // and get in with a fight with handleSliderEvents
  sdrScale.setValue((int) (selSprite.getScale()*100), true);
  sdrSpeed.setValue((int) selSprite.getSpeed(), true);
  sdrAcceleration.setValue((int) selSprite.getAcceleration(),true);
  sdrRotation.setValue((int) (PApplet.degrees(selSprite.getRot())),true);
  sdrDirection.setValue((int) (PApplet.degrees(selSprite.getDirection())),true);
}

/*
 * G4P Eventhandler
 * @param checkbox
 */
void handleCheckboxEvents(GCheckbox checkbox) {
  if(checkbox == cbxColAreaOn)
    S4P.collisionAreasVisible = checkbox.isSelected();
}

/*
 * G4P Eventhandler
 * @param slider
 */
void handleSliderEvents(GSlider slider) { 
  if(slider == sdrScale)
    selSprite.setScale(slider.getValue()/100.0f);
  else if(slider == sdrSpeed)
    selSprite.setSpeed(slider.getValue());
  else if(slider == sdrAcceleration)
    selSprite.setAcceleration(slider.getValue());
  else if(slider == sdrRotation)
    selSprite.setRot(radians(sdrRotation.getValue()));
  else if(slider == sdrDirection)
    selSprite.setDirection(radians(sdrDirection.getValue()));
  else if(slider == sdrWorldScale)
    S4P.resizeWorld(30.0f/slider.getValue());
}

/*
 * G4P Eventhandler
 * @param button
 */
void handleButtonEvents(GButton button) {
  if(button == btnResetScale){
    sdrScale.setValue(100);
    selSprite.setScale(1.0f);
  }
  else if(button == btnResetSpeed){
    sdrSpeed.setValue(0);
    selSprite.setSpeed(0);
  }
  else if(button == btnResetAccel){
    sdrAcceleration.setValue(0);
    selSprite.setAcceleration(0);
  }
  else if(button == btnResetWorld){
    S4P.resetWorld();
    sdrWorldScale.setValue(30, true);
  }
}

