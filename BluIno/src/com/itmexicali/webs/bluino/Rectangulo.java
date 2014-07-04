package com.itmexicali.webs.bluino;

import android.graphics.Point;
import android.graphics.Rect;

public abstract class Rectangulo{

	protected Point origen;
	protected int ancho, alto;
	protected Rect rect;
	
	public Rectangulo(Point or, int ancho, int alto) {
		this.origen = or;
		this.ancho = ancho;
		this.alto = alto;
		updateRect();
	}
	
	public int getX() {
		return origen.x;
	}
	
	public int getY() {
		return origen.y;
	}
	
	public int Top(){
		return origen.y;
	}
	
	public int Bottom(){
		return origen.y+alto;
	}
	
	public int Left(){
		return origen.x;
	}
	
	public int Right(){
		return origen.x+ancho;
	}
	
	public int getAncho() {
		return ancho;
	}
 
	public void setX(int newX) {
		origen.x=newX;
		updateRect();
	}
	
	public void setY(int newY) {
		origen.y=(newY);
	}
	
	public int getAlto() {
		return alto;
	}
	
	private void updateRect(){
		rect =new Rect(Left(),Top(),Right(),Bottom());
	}
	
	//checks if the ball is able to move within the screen
	public boolean puedoMover(int x, int y, Rect screen) {
		 return screen.contains(Left() + x, Top() + y, Right() + x, Bottom() + y);
	}

	public Rect getRect() {
		return rect;
	}
}