package uniandes.lym.robot.kernel;

import java.awt.Point;

/**
* Decorates world with more complex instructions
*  Adds error management. 
*  Adds a function that allows negative arguments horizontally or vertically.
*/

public class RobotWorldDec extends RobotWorld
{
   
	 
	
	public RobotWorldDec (int tam, Point p, int initGlobos, int initFichas){ 	
	 	super(tam,p, initGlobos, initFichas);	
	}


	/* Private  methods */
	
	/**
	 * @return spaces available to put chips.
	 */
	
	public int freeSpacesForChips(){
		Point myPos=getPosicion();
		int n=getN();
		int i;
	       
		for(i=myPos.y;i <= n  && !hayFicha(new Point(myPos.x,i));i++){
		}
		return i-myPos.y;
	}

	/**   
	 * @return number of chips on or above the current position. 
	 */
	
	public int chipsToPick(){
		Point myPos=getPosicion();
		int i;
	      
		for(i=myPos.y;i > 0 && hayFicha(new Point(myPos.x,i));i--){
		}
		return myPos.y-i;
	}

	
	/*  Public methods */

	
	/**
	* Used to move the robot horizontally on the board
	* @param  steps to move (if steps > 0 it moves to the right; if steps < 0 it moves to the left. 
	* @throws  Error if the robot ends up outside the board
	*/
	public void moveHorizontally(int pasos) throws Error {
	
		Point p =getPosicion();
		int tam = getN();
		int newX,i;
		
		newX = p.x+pasos;
		
		if (newX > tam) {
			throw new Error("Fell off  the right");
    	}
		else if (newX < 1) {
			throw new Error("Fell off the left");
		}
		else	{ 
			if (pasos >= 0)  {
				for (i=0;i<pasos;i++)	{
            		derecha();        
        		}
    		}
			else {
				for (i=0;i>pasos;i--) {
            		izquierda();
      			}
     		}
		}
	}

	/**
	* Used to move the robot vertically on the board
	* @param  steps to move (if steps > 0 it moves down; if steps < 0 it moves up. 
	* @throws  Error if the robot ends up outside the board
	*/
    
	public void moveVertically(int pasos) throws Error {
	
		Point p =getPosicion();
		int tam = getN();
		int newY,i;
		
		newY = p.y+pasos;
		
		if (newY > tam) {
			throw new Error("Fell off  the bottom");
		}
		else if (newY < 1) {
			throw new Error("Fell off the top");
		}
		else	{ 
			if (pasos >= 0)  {
				for (i=0;i<pasos;i++)	{
					abajo();        
				}
			}
			else {
				for (i=0;i>pasos;i--) {
					arriba();
				}
			}
		}
	}

	
	
	/**
	* Para recoger fichas
	* @param f fichas a recoger.
	* @throws Error Si hay menos fichas. 
	*/
	
	public void pickChips(int f) throws Error
	{
		int  i=0;
		 	
		if (f < 0)
			throw new Error ("Number of chips should be positive");
		else if (f>chipsToPick())
			throw new Error ("There are not enough chips");	
		else {
			for (i=0; i<f; i++) {
					recogerFicha();
			}	
		}
	}		
	
	/**
	* Method used for putting chips
	* @param f number of chips to put
	* @throws Error if:  f is  negative,  there is not enough room for the chips or if the robot does not have enough chips 
	*/
		 
	public void putChips(int f) throws Error
	{
		int  i=0;
		 
		if (f < 0)
			throw new Error ("Number of chips should be positive");
		else if (f>freeSpacesForChips())
			throw new Error ("Chips do not fit");	
		else if (getMisFichas()< f) 
			throw new Error("Robor does not have enough chips");
		else {
			for (i=0; i<f; i++) {
				ponerFicha();
			}
		}
	}
	
	/**
	* Method used for grabbing balloons that are on the same position as teh robot.
	* @param g Number of balloons to grab
	* @throws Error if g is negative or if there are not enough balloons
	*/
	
	public void grabBalloons(int g) throws Error
	{
		int  i=0;
		if (g < 0)
			throw new Error("Number of balloons should be positive");
		else if(contarGlobos(getPosicion()) < g) {
			throw new Error("There are less than "+g+" balloons!");
		}	
		for (i=0; i<g; i++) {
			recogerGlobo();
		}
	}		
	
	/**
	* Para poner globos.
	* @param g N&uacute;mero de globos a poner
	* @throws Error si no tiene suficientes fichas
	*/
	public void putBalloons(int g) throws Error
	{
		int  i=0;		 	
		
		if (g < 0)
			throw new Error("Number of balloons should be positive");
		else if (getMisGlobos()< g)
			throw new Error("Robot has less than "+g+" balloons!");
		else
			for (i=0; i<g; i++) {
				ponerGlobo();
			}
	}
	
	/*Nuevas instrucciones*/
	
	/**
	 * Used to move the robot forward
	 * @param steps number of steps to move in the direction it is facing.
	 * @throws  Error if the robot ends up outside the board
	*/	
	
	
	public void moveForward(int pasos) throws Error {
	
		int orient = getOrientacion();
		
		if(orient == NORTH){
			moveVertically(-pasos);
		}else if(orient == SOUTH) {
			moveVertically(pasos);
		}else if(orient == EAST){
			moveHorizontally(pasos);
		}else{
			moveHorizontally(-pasos);
		}
	}
}  


