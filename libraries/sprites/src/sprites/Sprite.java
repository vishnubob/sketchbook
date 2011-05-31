/*
  Part of the Sprites for Processing library 
  	http://sprites4processing.lagers.org.uk
	http://code.google.com/p/sprites4processing/svn/trunk

  Copyright (c) 2009 Peter Lager

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
 */

package sprites;

import java.awt.event.MouseEvent;
import java.lang.reflect.Method;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

/**
 * This class represents a sprite based upon a bitmap image file 
 * (eg  jpeg, gif, png)
 * <br>
 * It provides methods to set the sprite's position, velocity, acceleration,
 * scale and rotation.
 * <br>
 * Four types of collision detection is provided <br>
 * (1) Collision circles <br>
 * (2) Image border overlap (boxes) <br>
 * (3) Pixel level - collision based on overlapping non-transparent pixels <br>
 * (4) Overlap - similar to image border but there must be a user defined percentage overlap<br><br>
 * 
 * Methods 2, 3 & 4 will not work if the image has been rotated, attempting to use
 * these on a rotated image will cause method 1 to be used instead.<br>
 * Method 3 will not work with scaled images, attempting to use this method on a 
 * scaled image will cause method 2 to be used. <br>
 * In method 1 the collision detection radius is calculated as (width+height)/2 <br><br>
 * 
 * 
 * @author Peter Lager
 *
 */
public class Sprite implements Comparable<Object>, PConstants, SConstants {

	public PApplet app;

	// Various constants to handle rotation directions
	//	public static final int CLOCKWISE = +1;
	//	public static final int SHORTEST = 0;			// DEFAULT SETTING
	//	public static final int ANTICLOCKWISE = -1;

	/**
	 * Action sprite takes at domain edges
	 */
	public static final int HALT = 0;
	public static final int REBOUND = 1;

	public static int ALPHALEVEL = 20;

	/**
	 * INTERNAL USE ONLY
	 * This holds a reference to the GComponent that currently has the
	 * focus.
	 * A component looses focus when another component takes focus with the
	 * takeFocus() method. The takeFocus method should use focusIsWith.looseFocus()
	 * before setting its value to the new component 
	 */
	protected static Sprite focusIsWith; // READ ONLY

	/*
	 * INTERNAL USE ONLY
	 * Used to track mouse required by GButton, GCheckbox, GHorzSlider
	 * GVertSlider, GPanel classes
	 */
	protected int mdx = Integer.MAX_VALUE, mdy = Integer.MAX_VALUE;

	// Current XY positional factors
	protected float x, y;			// World position
	protected float vx, vy;			// Velocities
	protected float ax, ay;			// Acceleration
	protected float rot;			// Rotation (radians)
	protected float scale = 1.0f;

	protected Integer zOrder = 0;	// Z order for drawing

	protected float colRadius;		// Collision radius

	protected boolean dead = false;
	protected boolean visible = true;

	protected Domain domain = null;
	protected int domainAction = REBOUND;

	// interval in seconds between bitmap image changes
	protected float animInterval;	// time between each frame
	protected float animTime;		// time since last frame change
	protected int frameCurrent = 0;	// current frame to display
	protected int frameBegin = 0;   // start frame for anim sequence
	protected int frameEnd = 0;		// end frame for anim sequence
	protected int nbrRepeats = 0;
	protected PImage[] frames;
	protected PImage[] colFrames = null;
	protected ImageInfo info;
	/*
	 * These variables relate to the image
	 */
	protected float halfWidth, width;
	protected float halfHeight, height;

	protected int hit_x, hit_y;
	
	/** The object to handle the event */
	protected Object eventHandlerObject = null;
	/** The method in eventHandlerObject to execute */
	protected Method eventHandlerMethod = null;
	/** the name of the method to handle the event */ 
	protected String eventHandlerMethodName;

	// The event type use READ ONLY
	public int eventType = 0;

	@SuppressWarnings("unused")
	private boolean beingDragged;
	
	protected boolean draggable = false;
	
	/**
	 * Create a sprite based on an image file
	 * You can specify the order the sprites are drawn using zOrder - the
	 * higher the value the nearer the viewer.
	 * 
	 * @param theApplet
	 * @param imageFname
	 * @param zOrder
	 */
	public Sprite(PApplet theApplet, String imageFname, int zOrder){
		app = theApplet;
		info = S4P.getImageInfo(app, imageFname, 1, 1);
		ctorCore(imageFname, 1, 1, zOrder);
	}

	/**
	 * Create a sprite based on an image file and an alphaMask file. <br>
	 * You can specify the order the sprites are drawn using zOrder - the
	 * higher the value the nearer the viewer.
	 * 
	 * @param theApplet
	 * @param imageFname
	 * @param alphaFname
	 * @param zOrder
	 */
	public Sprite(PApplet theApplet, String imageFname, String alphaFname, int zOrder){
		app = theApplet;
		info = S4P.getImageInfo(app, imageFname, alphaFname, 1, 1);
		ctorCore(imageFname, 1, 1, zOrder);
	}

	/**
	 * Create a sprite based on an image file. <br>
	 * The actual image can be made up of a number of tiled pictures. <br>
	 * For animation purposes the images should be ordered left to 
	 * right, top to bottom.
	 * You can also specify the order the sprites are drawn using zOrder - the
	 * higher the value the nearer the viewer.
	 * 
	 * @param theApplet
	 * @param imageFname
	 * @param cols
	 * @param rows
	 * @param zOrder the higher the z value the nearer the viewer
	 */
	public Sprite(PApplet theApplet, String imageFname, int cols, int rows, int zOrder){
		app = theApplet;
		info = S4P.getImageInfo(app, imageFname, cols, rows);
		ctorCore(imageFname, cols, rows, zOrder);
	}

	/**
	 * Create a sprite based on an image file and an alphaMask file. <br>
	 * The actual image can be made up of a number of tiled pictures. <br>
	 * For animation purposes the images should be ordered left to 
	 * right, top to bottom.
	 * You can also specify the order the sprites are drawn using zOrder - the
	 * higher the value the nearer the viewer.
	 * 
	 * @param theApplet
	 * @param imageFname
	 * @param alphaFname
	 * @param cols
	 * @param rows
	 * @param zOrder
	 */
	public Sprite(PApplet theApplet, String imageFname, String alphaFname, int cols, int rows, int zOrder){
		app = theApplet;
		info = S4P.getImageInfo(app, imageFname, alphaFname, cols, rows);
		ctorCore(imageFname, cols, rows, zOrder);
	}

	/**
	 * INETRNAL USE ONLY
	 * Core coding for ctors
	 * 
	 * @param imageFname
	 * @param cols
	 * @param rows
	 * @param zOrder the higher the z value the nearer the viewer
	 */
	private void ctorCore(String imageFname, int cols, int rows, int zOrder){
		frames = S4P.getFrames(info);
		width = frames[0].width;
		height = frames[0].height;
		// Next are used in collision detection
		halfWidth = width/2;
		halfHeight = height/2;
		colRadius = (width + height)/4;
		this.zOrder = zOrder;
		S4P.registerSprite(this);
//		
	}

	protected void calcCollisionImage(){
		colFrames = new PImage[frames.length];
		for(int f = 0; f < colFrames.length; f++){
			colFrames[f] = new PImage((int)width, (int)height, ARGB);
			colFrames[f].loadPixels();
			frames[f].loadPixels();
			for(int p = 0; p < frames[f].pixels.length; p++){
				if( app.alpha(frames[f].pixels[p]) < ALPHALEVEL )
					colFrames[f].pixels[p] = 0;
				else
					colFrames[f].pixels[p] = S4P.colColor;
			}
			colFrames[f].updatePixels();
		}
	}

	/**
	 * If set to true it will look for a method with the format<br>
	 * <pre>void handleSpriteEvents(Sprite sprite) </pre><br>
	 * in your sketch to handle mouse events PRESSED, RELEASED,
	 * CLICKED and DRAGGED <br>
	 * Inside the handleSpriteEvents method you can test for event type: <br>
	 * <pre>if(sprite.eventType == Sprite.PRESSED) ...</pre><br>
	 * and so on. <br>
	 * 
	 * @param mouse_repond true or false
	 */
	public void respondToMouse(boolean mouse_repond){
		try{
			app.unregisterMouseEvent(this);
		}
		catch(Exception e){
		}
		if(mouse_repond){
			app.registerMouseEvent(this);
			if(this.eventHandlerObject == null){
				createEventHandler(app, "handleSpriteEvents", new Class[]{ Sprite.class });
			}
		}
	}
	/**
	 * Attempt to create the default event handler for the sprite class. 
	 * The default event handler is a method that returns void and has a single
	 * parameter of type sprite and a method called handleSpriteEvents.
	 * 
	 * @param handlerObj the object to handle the event
	 * @param methodName the method to execute in the object handler class
	 * @param parameters the parameter classes.
	 */
	@SuppressWarnings("unchecked")
	protected void createEventHandler(Object handlerObj, String methodName, Class[] parameters){
		try{
			eventHandlerMethod = handlerObj.getClass().getMethod(methodName, parameters );
			eventHandlerObject = handlerObj;
			eventHandlerMethodName = methodName;
		} catch (Exception e) {
			SMessenger.message(MISSING, this, new Object[] {methodName, parameters});
			eventHandlerObject = null;
		}
	}

	/**
	 * Attempt to create the mouse event handler for the sprite class. <br>
	 * The event handler is a method that returns void and has a single parameter
	 * of type Sprite and a method name. 
	 * 
	 * @param obj the object to handle the event
	 * @param methodName the method to execute in the object handler class
	 */
	public void addEventHandler(Object obj, String methodName){
		try{
			eventHandlerObject = obj;
			eventHandlerMethodName = methodName;
			eventHandlerMethod = obj.getClass().getMethod(methodName, new Class[] {this.getClass() } );
		} catch (Exception e) {
			SMessenger.message(NONEXISTANT, this, new Object[] {methodName, new Class[] { this.getClass() } } );
			eventHandlerObject = null;
			eventHandlerMethodName = "";
		}
	}

	/**
	 * Attempt to fire an event for this component.
	 * 
	 * The method called must have a single parameter which is the object 
	 * firing the event.
	 * If the method to be called is to have different parameters then it should
	 * be overridden in the child class
	 * The method 
	 */
	protected void fireEvent(){
		if(eventHandlerMethod != null){
			try {
				eventHandlerMethod.invoke(eventHandlerObject, new Object[] { this });
			} catch (Exception e) {
				SMessenger.message(EXCP_IN_HANDLER, eventHandlerObject, 
						new Object[] {eventHandlerMethodName, e } );
			}
		}		
	}

	/**
	 * Restore images back to their original state. Useful if
	 * you have used the bite() method.
	 */
	public void restoreImages(){
		frames = S4P.getFrames(info);	
	}

	/**
	 * Update the positions of all the sprites.
	 * 
	 * @param deltaTime the time in seconds since last called
	 */
	public void update(float deltaTime){
		updatePosition(deltaTime);
		updateImageAnimation(deltaTime);
	}

	/**
	 * Calculates if part or all of the sprite is in the visible
	 * portion of the world.
	 *  
	 * @return true if part or all of the sprite is on screen 
	 */
	public boolean isOnScreem(){
		if(x+width*scale < S4P.screenDomain.left 
				|| x-width*scale >S4P.screenDomain.right
				|| y+height*scale < S4P.screenDomain.top
				|| y-height*scale > S4P.screenDomain.bottom)
			return false;

		return true;
	}

	/**
	 * Updates the image to be displayed
	 * 
	 * @param deltaTime
	 */
	protected void updateImageAnimation(float deltaTime){
		if(animInterval > 0.0 && nbrRepeats > 0){
			animTime += deltaTime;
			while(animTime > animInterval){
				animTime -= animInterval;
				frameCurrent++;
				if(frameCurrent > frameEnd){
					frameCurrent = frameBegin;
					nbrRepeats--;
				}
			}
		}	
		if(nbrRepeats <= 0){
			nbrRepeats = 0;
			this.animInterval = 0.0f;
		}
	}

	/**
	 * Update the sprites position based on the time since last call
	 * 
	 * @param deltaTime
	 */
	protected void updatePosition(float deltaTime){
		vx += ax * deltaTime;
		vy += ay * deltaTime;
		x += vx * deltaTime;
		y += vy * deltaTime;
		if(domain != null){
			if(x-width*scale/2 < domain.left){
				switch(domainAction){
				case REBOUND:
					x = domain.left + width*scale/2;					
					vx = -vx;
					ax = -ax;
					break;
				case HALT:
					x = domain.left + width*scale/2;					
					vx = ax = 0;
					break;
				}
			}
			else if(x+width*scale/2 > domain.right){
				switch(domainAction){
				case REBOUND:
					x = domain.right - width*scale/2;
					vx = -vx;
					ax = -ax;
					break;
				case HALT:
					x = domain.right - width*scale/2;
					vx = ax = 0;
				}
			}
			if(y-height*scale/2 < domain.top){
				switch(domainAction){
				case REBOUND:
					y = domain.top + height*scale/2;
					vy = -vy;
					ay = -ay;
					break;
				case HALT:
					y = domain.top + height*scale/2;
					vy = ay = 0;
					break;
				}
			}
			else if(y+height*scale/2 > domain.bottom){
				switch(domainAction){
				case REBOUND:
					y = domain.bottom - height*scale/2;
					vy = -vy;
					ay = -ay;	
					break;
				case HALT:
					y = domain.bottom - height*scale/2;
					vy = ay = 0;
					break;
				}
			}
		}
	}

	/**
	 * Set the sprite's movement domain. Normally you would ensure that the sprite 
	 * starts <b>inside</b> the domain. The last attribute defines what happens 
	 * to the sprite when it reaches the domain boundary - at present there are 
	 * 2 options REBOUND and HALT. <br>
	 * The domain boundaries are specified by the top-left and bottom-right corner
	 * co-ordinates.
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @param action
	 */
	public void setDomain(float left, float top, float right, float bottom, int action){
		domain = new Domain(left,top,right,bottom);
		domainAction = action;
	}

	/**
	 * Set the sprite's movement domain. Normally you would ensure that the sprite 
	 * starts <b>inside</b> the domain. The last attribute defines what happens 
	 * to the sprite when it reaches the domain boundary - at present there are 
	 * 2 options REBOUND and HALT. <br>
	 * 
	 * @param domain
	 * @param action
	 */
	public void setDomain(Domain domain, int action){
		this.domain = domain;
		domainAction = action;
	}

	/**
	 * Get the sprite's movement domain.
	 * 
	 * @return the movement domain
	 */
	public Domain getDomain(){
		return domain;
	}

	/**
	 * Removes the current domain so the sprite's movement is no
	 * longer constrained.
	 */
	public void clearDomain(){
		domain = null;
		domainAction = REBOUND;
	}

	/**
	 * If you have said that this sprite is to respond to the mouse
	 * then this method will be called every loop.
	 * 
	 * @param event
	 */
	public void mouseEvent(MouseEvent event){
		if(!visible || dead)
			return;
		
		boolean mouseOver = isOver(app.mouseX, app.mouseY);
		switch(event.getID()){
		case MouseEvent.MOUSE_PRESSED:
			if(focusIsWith != this && mouseOver){
				mdx = app.mouseX;
				mdy = app.mouseY;
				focusIsWith = this;
				// May become true but will soon be set to false when
				// we loose focus
				eventType = PRESSED;
				fireEvent();
				beingDragged = true;
			}
			break;
		case MouseEvent.MOUSE_CLICKED:
			if(focusIsWith == this){
//				x = app.mouseX;
//				y = app.mouseY;					
				// This component does not keep the focus when clicked
				focusIsWith = null;
				mdx = mdy = Integer.MAX_VALUE;
				eventType = CLICKED;
				fireEvent();
			}
			break;
		case MouseEvent.MOUSE_RELEASED:
			if(focusIsWith == this){
				if(mouseHasMoved(app.mouseX, app.mouseY)){
					mdx = mdy = Integer.MAX_VALUE;
					focusIsWith = null;
					beingDragged = false;
					eventType = RELEASED;
					fireEvent();
				}
			}
			break;
		case MouseEvent.MOUSE_DRAGGED:
			if(focusIsWith == this && draggable){
				beingDragged = true;
				PointF2D p = S4P.pixel2world(app.mouseX, app.mouseY);
				x = p.x;
				y = p.y;
				eventType = DRAGGED;
				fireEvent();
			}
			break;
		}
	}
	
	/**
	 * Draw this sprite
	 * 
	 */
	public void draw(){
		if(visible && !dead){
			app.pushMatrix();
			app.translate(x, y);
			app.scale(scale);
			app.rotate(rot);
			app.image(frames[frameCurrent],0,0,width,height);
			if(S4P.collisionAreasVisible)
				drawCollisionArea();
			app.popMatrix();
		}
	}

	/**
	 * Draw the collision area that will be used for this area
	 * 
	 */
	protected void drawCollisionArea(){
		app.pushStyle();
		app.noStroke();
		app.fill(S4P.colColor);
		if(isOver(app.mouseX, app.mouseY)){
			app.tint(app.red(S4P.selColor),app.green(S4P.selColor),app.blue(S4P.selColor),0xff);
			app.fill(S4P.selColor);
		}

		if(rot != 0){
			app.ellipseMode(CENTER);
			app.ellipse(0, 0, colRadius*2, colRadius*2);
		}
		else if(scale != 1){
			app.rectMode(CENTER);
			app.rect(0, 0, width, height);
		}
		else {
			if(colFrames == null)
				calcCollisionImage();
			app.image(colFrames[frameCurrent],0,0,width,height);							
		}
		//		app.noTint();
		app.popStyle();
	}

	/**
	 * Used on the MOUSE_RELEASED event 
	 * @param x
	 * @param y
	 * @return
	 */
	protected boolean mouseHasMoved(int x, int y){
		return (mdx != x || mdy != y);
	}

	/**
	 * This method will test if a screen position is over a sprite. <br>
	 * @param mx
	 * @param my
	 * @return true if the position mx, my is over a sprite
	 */
	public boolean isOver(int mx, int my){
		// Calculate world position of mx, my
		PointF2D p = S4P.pixel2world(mx, my);
		if(rot != 0){
			return (p.x-x)*(p.x-x)+(p.y-y)*(p.y-y) < colRadius*colRadius*scale*scale;
		}
		else if(scale != 1){
			return(p.x>x-halfWidth*scale && p.x<x+halfWidth*scale 
					&& p.y>y-halfHeight*scale&& p.y<y+halfHeight*scale);
		}
		else if(Math.abs(p.x-x)<halfWidth && Math.abs(p.y-y)<halfHeight){
			PImage c = frames[frameCurrent];
			int xx = (int)(p.x-x+halfWidth);
			int yy = (int)(p.y-y+halfHeight);
			int pn = yy * c.width + xx;
			c.loadPixels();
			if(app.alpha(c.pixels[pn]) > ALPHALEVEL){
				return true;
			}
		}
		return false;
	}

	/**
	 * See if the sprite's collision circles overlap i.e. collision
	 * 
	 * @param spriteB
	 * @return true if sprite collision circles overlap (collision)
	 */
	public boolean cc_collision(Sprite spriteB) {
		if(!visible || !spriteB.visible) return false;

		float d;
		d = (x-spriteB.x)*(x-spriteB.x) + (y-spriteB.y)*(y-spriteB.y);
		d -= ((colRadius*scale+spriteB.colRadius*spriteB.scale)
				*(colRadius*scale+spriteB.colRadius*spriteB.scale));
		return (d < 0.0f);
	}

	/**
	 * See if the spites have a none transparent pixel that collide <br>
	 * A pixel is transparent if its alpha component < ALPHALEVEL
	 * @param spriteB
	 * @return true if the sprites collide at the pixel level
	 */
	public boolean pp_collision(Sprite spriteB) {
		int topA, botA, leftA, rightA;
		int topB, botB, leftB, rightB;
		int topO, botO, leftO, rightO;
		int ax, ay;
		int bx, by;
		int APx, APy, ASx, ASy;
		@SuppressWarnings("unused")
		int BPx, BPy, BSx, BSy;

		if(!visible || !spriteB.visible) return false;

		if(rot!=0 || spriteB.rot!=0 || scale!=1 || spriteB.scale!=1)
			return bb_collision(spriteB);

		topA   = (int) (y - halfHeight);
		botA   = (int) (y + halfHeight);
		leftA  = (int) (x - halfWidth);
		rightA = (int) (x + halfWidth);
		topB   = (int) (spriteB.y - spriteB.halfHeight);
		botB   = (int) (spriteB.y + spriteB.halfHeight);
		leftB  = (int) (spriteB.x - spriteB.halfWidth);
		rightB = (int) (spriteB.x + spriteB.halfWidth);

		if(botA <= topB  || botB <= topA || rightA <= leftB || rightB <= leftA)
			return false;

		// If we get here, we know that there is an overlap
		// So we work out where the sides of the ovelap are
		leftO = (leftA < leftB) ? leftB : leftA;
		rightO = (rightA > rightB) ? rightB : rightA;
		botO = (botA > botB) ? botB : botA;
		topO = (topA < topB) ? topB : topA;
		int widthO = rightO - leftO;

		// P is the top-left, S is the bottom-right of the overlap
		APx = leftO-leftA;   APy = topO-topA;
		ASx = rightO-leftA;  ASy = botO-topA-1;
		BPx = leftO-leftB;   BPy = topO-topB;
		BSx = rightO-leftB;  BSy = botO-topB-1;

		boolean foundCollision = false;

		// Images to test
		PImage imgA = frames[frameCurrent];
		PImage imgB = spriteB.frames[spriteB.frameCurrent];
		// loadPixels
		imgA.loadPixels();
		imgB.loadPixels();

		// These are widths in BYTES. They are used inside the loop
		//  to avoid the need to do the slow multiplications
		int surfaceWidthA = frames[frameCurrent].width;
		int surfaceWidthB = spriteB.frames[spriteB.frameCurrent].width;

		boolean pixelAtransparent = true;
		boolean pixelBtransparent = true;

		// Get start pixel positions
		int pA = (APy * surfaceWidthA) + APx;
		int pB = (BPy * surfaceWidthB) + BPx;

		ax = APx; ay = APy;
		bx = BPx; by = BPy;
		for(ay = APy; ay < ASy; ay++) {
			bx = BPx;
			for(ax = APx; ax < ASx; ax++) {
				pixelAtransparent = app.alpha(imgA.pixels[pA]) < ALPHALEVEL;
				pixelBtransparent = app.alpha(imgB.pixels[pB]) < ALPHALEVEL;

				if(!pixelAtransparent && !pixelBtransparent) {
					hit_x = ax;
					hit_y = ay;
					spriteB.setHitXY(bx, by);
					foundCollision = true;
					break;
				}
				pA ++;
				pB ++;
				bx++;
			}
			if(foundCollision) break;
			pA = pA + surfaceWidthA - widthO;
			pB = pB + surfaceWidthB - widthO;
			by++;
		}
		return foundCollision;
	}

	/**
	 * Collision detection based on the percentage overlap of THIS sprite
	 * caused by spriteB. <br>
	 * Note: <br>
	 * If we have 2 sprites with different sizes then <br>
	 * spriteA.oo_collision(spriteB, 40); <br>
	 * spriteB.oo_collision(spriteA, 40); <br>
	 * are <b>not</b> equivalent. The first returns true if >=40% of spriteA
	 * is covered by spriteB, the second statement returns true if >=40% of
	 * spriteB is covered by spriteA.
	 * 
	 * @param spriteB
	 * @param pcent
	 * @return true if we have sufficient overlap between sprites
	 */
	public boolean oo_collision(Sprite spriteB, float pcent){
		int topA, botA, leftA, rightA;
		int topB, botB, leftB, rightB;
		int topO, botO, leftO, rightO;

		if(!visible || !spriteB.visible) return false;

		// If either sprite is rotated use collision circles
		if(rot!=0 || spriteB.rot!=0)
			return cc_collision(spriteB);

		// No rotation so check for sprite overlap
		topA   = (int) (y - halfHeight*scale);
		botA   = (int) (y + halfHeight*scale);
		leftA  = (int) (x - halfWidth*scale);
		rightA = (int) (x + halfWidth*scale);
		topB   = (int) (spriteB.y - spriteB.halfHeight*spriteB.scale);
		botB   = (int) (spriteB.y + spriteB.halfHeight*spriteB.scale);
		leftB  = (int) (spriteB.x - spriteB.halfWidth*spriteB.scale);
		rightB = (int) (spriteB.x + spriteB.halfWidth*spriteB.scale);

		if(botA <= topB  || botB <= topA || rightA <= leftB || rightB <= leftA)
			return false;

		// If we get here, we know that there is an overlap
		// So we work out where the sides of the ovelap are
		leftO = (leftA < leftB) ? leftB : leftA;
		rightO = (rightA > rightB) ? rightB : rightA;
		botO = (botA > botB) ? botB : botA;
		topO = (topA < topB) ? topB : topA;

		float cover = (rightO - leftO)*(botO - topO)*100.0f/((rightA - leftA)*(botA-topA));
		return cover > pcent;
	}

	/**
	 * Determines whether the spriteB overlaps this sprite. It uses the position, width, height
	 * and scale to represent its boundaries.
	 * @param spriteB
	 * @return true if sprite boxes overlap (collision)
	 */
	public boolean bb_collision(Sprite spriteB) {
		int topA, botA, leftA, rightA;
		int topB, botB, leftB, rightB;

		if(!visible || !spriteB.visible) return false;

		// If either sprite is rotated use collision circles
		if(rot!=0 || spriteB.rot!=0)
			return cc_collision(spriteB);
		
		// No rotation so check for sprite overlap
		topA   = (int) (y - halfHeight*scale);
		botA   = (int) (y + halfHeight*scale);
		leftA  = (int) (x - halfWidth*scale);
		rightA = (int) (x + halfWidth*scale);
		topB   = (int) (spriteB.y - spriteB.halfHeight*spriteB.scale);
		botB   = (int) (spriteB.y + spriteB.halfHeight*spriteB.scale);
		leftB  = (int) (spriteB.x - spriteB.halfWidth*spriteB.scale);
		rightB = (int) (spriteB.x + spriteB.halfWidth*spriteB.scale);

		if(botA <= topB  || botB <= topA || rightA <= leftB || rightB <= leftA)
			return false;

		return true;
	}

	/**
	 * This method makes a circular area of pixels centered around hit_x/hit_y 
	 * transparent. This works fine with sprites using per-pixel transparency.
	 *
	 * @param biteRadius
	 */
	public void bite(int biteRadius) {
		bite(hit_x, hit_y, biteRadius);
	}

	/**
	 * This method makes a circular area of pixels centered around x/y 
	 * transparent. This works fine with sprites using per-pixel transparency.
	 *
	 * @param x
	 * @param y
	 * @param biteRadius
	 */
	public void bite(int x, int y, int biteRadius) {
		int x1 = x - biteRadius;
		int y1 = y - biteRadius;
		int x2 = x + biteRadius;
		int y2 = y + biteRadius;
		if(x1 < 0) x1 = 0;
		if(y1 < 0) y1 = 0;
		if(x2 >= width) x2 = (int) width;
		if(y2 >= height) y2 = (int) height;

		int br2 = biteRadius * biteRadius;

		int imgWidth = (int)width;

		for(int h = 0; h < frames.length; h++) {
			frames[h].loadPixels();
			for(int px = x1; px < x2; px++) {
				for (int py = y1; py < y2; py++) {
					if((px-x)*(px-x) + (py-y)*(py-y) <= br2) {
						frames[h].pixels[px + py * imgWidth] &= 0x00ffffff;
					}
				}
			}
			frames[h].updatePixels();
		}
	}

	/**
	 * INTERNAL USE ONLY
	 * Called by pp_collision() if a pixel level collision occured
	 * 
	 * @param x
	 * @param y
	 */
	public void setHitXY(int x, int y){
		hit_x = x;
		hit_y = y;
	}

	/**
	 * Get the x pixel coordinate in the image where the first
	 * pixel level collision occurred.
	 * @return the hit_x
	 */
	public int getHitX() {
		return hit_x;
	}

	/**
	 * Get the y pixel coordinate in the image where the first
	 * pixel level collision occurred.
	 * @return the hit_y
	 */
	public int getHitY() {
		return hit_y;
	}

	/**
	 * Get the x/y pixel coordinates in the image where the first
	 * pixel level collision occurred.
	 */
	public PointF2D getHitXY(){
		return new PointF2D(hit_x, hit_y);
	}

	/**
	 * When there are multiple frames then this can be used to animate through
	 * some or all of the images. Animation is repeated indefinitely.
	 * 
	 * @param firstFrame start with this frame
	 * @param lastFrame go back to firstFrame after this frame
	 * @param interval time in seconds between frames
	 */
	public void startImageAnim(int firstFrame, int lastFrame, float interval){
		startImageAnim(firstFrame, lastFrame, interval, Integer.MAX_VALUE);
	}

	/**
	 * When there are multiple frames then this can be used to animate through
	 * some or all of the images. Animation is repeated for the number of
	 * times specified.
	 * 
	 * @param firstFrame start with this frame
	 * @param lastFrame go back to firstFrame after this frame
	 * @param interval time in seconds between frames
	 * @param nrepeats how many times to repaet this
	 */
	public void startImageAnim(int firstFrame, int lastFrame, float interval, int nrepeats){
		if(interval > 0.0f){
			nbrRepeats = nrepeats;
			animInterval = interval;
			animTime = 0.0f;
			frameBegin = PApplet.constrain(firstFrame, 0, frames.length - 1);
			frameEnd = PApplet.constrain(lastFrame, 0, frames.length - 1);

			if(frameBegin > frameEnd){
				int temp = frameBegin;
				frameBegin = frameEnd;
				frameEnd = temp;
			}
			frameCurrent = frameBegin;
		}		
	}

	/**
	 * Stop the image animation at the current frame
	 */
	public void stopImageAnim(){
		animInterval = 0.0f;
	}

	/**
	 * Returns true if the image is currently being animated
	 */
	public boolean isImageAnimating(){
		return (animInterval > 0.0f);
	}

	/**
	 * Sets the frame to be displayed - will stop image animation
	 * @param frameNo
	 */
	public void setFrame(int frameNo){
		animInterval = 0.0f;
		frameCurrent = PApplet.constrain(frameNo, 0, frames.length);	
	}

	/**
	 * Get the frame number for the current frame.
	 */
	public int getFrame(){
		return frameCurrent;
	}
	
	/**
	 * Sets the speed of the sprite in its current direction. If it
	 * is not moving direction is set at the image rotation angle. 
	 * 
	 * @param speed
	 */
	public void setSpeed(float speed){
		float currentSpeed = (float) Math.sqrt(vx*vx+vy*vy);
		if(currentSpeed == 0.0f){
			setSpeed(speed, rot);
		}
		else {
			float changeFactor = speed/currentSpeed;
			vx *= changeFactor;
			vy *= changeFactor;
		}
	}

	/**
	 * Set the sprites speed and direction of travel.
	 * 
	 * @param speed
	 * @param angle radians
	 */
	public void setSpeed(float speed, float angle){
		vx =  speed * (float) Math.cos(angle);
		vy =  speed * (float) Math.sin(angle);
	}
	
	/**
	 * Get the speed based on x and y velocity components
	 * @return the speed scalar
	 */
	public float getSpeed(){
		return (float) Math.sqrt(vx*vx + vy*vy);
	}
	
	/**
	 * Sets the acceleration of the sprite in its current direction. If it
	 * is not moving direction is set at the image rotation angle. 
	 * 
	 * @param acceleration
	 */
	public void setAcceleration(float acceleration){
//		float currentAccel = (float) Math.sqrt(ax*ax+ay*ay);
		float currentSpeed = (float) Math.sqrt(vx*vx+vy*vy);
		// If the sprite is motionless then set the acceleration in diretcion of 
		// image rotation
		if(currentSpeed == 0.0f){
			setAcceleration(acceleration, rot);
		} 
		// we are moving so test if we have existing acceleration if not 
		// accelerate in same direction we are moving
		else {
			setAcceleration(acceleration, (float) Math.atan2(vy, vx));
		}
	}

	/**
	 * Set the sprite's acceleration rate and direction.
	 * 
	 * @param acceleration
	 * @param angle radians
	 */
	public void setAcceleration(float acceleration, float angle){
		ax =  acceleration * (float) Math.cos(angle);
		ay =  acceleration * (float) Math.sin(angle);
	}

	/**
	 * Get the acceleration based on x and y acceleration components
	 * @return the resolved acceleration
	 */
	public float getAcceleration(){
		return (float) Math.sqrt(ax*ax + ay*ay);
	}
	
	/**
	 * Changes the velocities so the sprite is travelling in the desired
	 * direction. If the sprite is stopped has no effect.
	 * 
	 * @param dir
	 */
	public void setDirection(float dir){
		float currentSpeed = (float) Math.sqrt(vx*vx+vy*vy);
		setSpeed(currentSpeed, dir);
		float currentAccel = (float) Math.sqrt(ax*ax+ay*ay);
		setAcceleration(currentAccel, dir);
	}
	
	/**
	 * Get the current direction based on x & y velocity vectors
	 * @return the angle in radians
	 */
	public float getDirection(){
		return (float) Math.atan2(vy,vx);
	}
	
	/**
	 * Rotate the image
	 * @param angle s
	 */
	public void setRot(float angle){
		this.rot = angle;
	}

	/**
	 * Get the sprite's rotation angle
	 * 
	 * @return rotation angle in radians
	 */
	public float getRot(){
		return rot;
	}

	/**
	 * Set the scale the image is to be displayed at.
	 * 
	 * @param scale
	 */
	public void setScale(float scale){
		this.scale = scale;
	}

	/**
	 * Get the scale used for sizing the sprite.
	 */
	public float getScale(){
		return scale;
	}

	/**
	 * Set the sprite's world x/y position
	 * 
	 * @param x
	 * @param y
	 */
	public void setXY(float x, float y){
		this.x = x;
		this.y = y;
	}

	/**
	 * Set the sprite's world x position
	 * @param x
	 */
	public void setX(float x){
		this.x = x;
	}

	/**
	 * Get the sprite's world x position
	 */
	public float getX(){
		return x;
	}

	/**
	 * Set the sprite's world y position
	 * @param y
	 */
	public void setY(float y){
		this.y = y;
	}

	/**
	 * Get the scaled height
	 */
	public float getHeight(){
		return height * scale;
	}

	/**
	 * Get the scaled width
	 */
	public float getWidth(){
		return width * scale;
	}

	/**
	 * Get the sprite's world y position
	 */
	public float getY(){
		return y;
	}

	/**
	 * Set the sprite's x/y velocity
	 * @param vx
	 * @param vy
	 */
	public void setVelXY(float vx, float vy){
		this.vx = vx;
		this.vy = vy;
	}

	/**
	 * Set the sprite's x velocity
	 * @param vx
	 */
	public void setVelX(float vx){
		this.vx = vx;
	}

	/**
	 * Get the sprite's x velocity
	 */
	public float getVelX(){
		return vx;
	}

	/**
	 * Set the sprite's y velocity
	 * @param vy
	 */
	public void setVelY(float vy){
		this.vy = vy;
	}

	/**
	 * Get the sprite's x velocity
	 */
	public float getVelY(){
		return vy;
	}

	/**
	 * Set the sprite's x/y acceleration
	 * @param ax
	 * @param ay
	 */
	public void setAccXY(float ax, float ay){
		this.ax = ax;
		this.ay = ay;
	}

	/**
	 * Set the sprite's x acceleration
	 * @param ax
	 */
	public void setAccX(float ax){
		this.ax = ax;
	}

	/**
	 * Get the sprite's x acceleration
	 */
	public float getAccX(){
		return ax;
	}

	/**
	 * Set the sprite's y acceleration
	 * @param ay
	 */
	public void setAccY(float ay){
		this.ay = ay;
	}

	/**
	 * Get the sprite's y acceleration
	 */
	public float getAccY(){
		return ay;
	}

	/**
	 * Set the collision circle radius
	 * 
	 * @param colRadius
	 */
	public void setCollisionRadius(float colRadius){
		this.colRadius = colRadius;
	}

	/**
	 * Get the collision circle radius
	 */
	public float getCollisionRadius(){
		return colRadius;
	}

	/**
	 * Set the sprite's visibility
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible){
		this.visible = visible;
	}

	/**
	 * Is this sprite visible?
	 */
	public boolean isVisible(){
		return visible;
	}

	/**
	 * Sets the order that sprites are drawn the low numbre are drawn before
	 * high numbers.
	 * 
	 * @param zOrder
	 */
	public void setZorder(int zOrder){
		this.zOrder = zOrder;
		S4P.sortZorder();
	}

	
	/**
	 * Can the shape be dragged by the mouse
	 * @return true if we can drag the sprite in GUI else false
	 */
	public boolean isDraggable() {
		return draggable;
	}

	/**
	 * @param draggable the draggable to set
	 */
	public void setDraggable(boolean draggable) {
		this.draggable = draggable;
	}

	/**
	 * When set to true this sprite will be removed from the list of sprites
	 * to be updated and displayed.
	 * 
	 * @param dead
	 */
	public void setDead(boolean dead){
		this.dead = dead;
	}

	/**
	 * See if the sprite is dead
	 * @return true if dead else false
	 */
	public boolean isDead(){
		return dead;
	}
	/**
	 * Uses the z order value to order the sprites
	 */
	public int compareTo(Object o) {
		return this.zOrder.compareTo(((Sprite)o).zOrder);
	}

}
