package uniandes.lym.robot.kernel;



import java.awt.Point;
import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;




/**
 * Robot World is an square board with a robot that moves from cell to cell 
 * There are chips. There can only be one chip per cell. The robot starts with some chips and some balLoons.
 * The Robot can put and pickUp chips and balloons. When the robot places a chip, it falls
 * bottom row, or to the row above the last placed chip. 
 * Balloons float and there can be more than one balloon in a cell.
 * This class does not  implement visualization.
 * <b>Note :</b>Invalid operations cause unexpected behavior, no exceptions are raised.
 * This class communicates  with the interface using PropertyChangeSupport.
 *  This class used to extend Observable 
 */



public class RobotWorld //extends Observable
{

	//   Attributes and methods for  simulating deprecated Observable Interface
	
	PropertyChangeSupport pcs = new  PropertyChangeSupport(this);
	
	public void addObserver(PropertyChangeListener l) {
		pcs.addPropertyChangeListener("theProperty", l);
	}
	
	
	
	
/** Constants to model the direction that the robot is facing
 * 	
 */
	public static final int NORTH = 0;
	public static final int SOUTH = 1;
	public static final int EAST = 2;
	public static final int WEST = 3;
	
	/**
	 * The dimension of the board. Being a square, only one value suffices.
	*/
	private int n;
	
	/**  Cell where the robot is located */
	private Point posicion;
	
	/**
	 * Set of positions that contain chips
	 */
	
	private HashSet <Point> fichas;
	
	/**  Number of balloons held by the Robot*/
	
	private int misGlobos;
	/** Number of Chips held by the Robot*/
	
	private int misFichas;
	
	
	
	/**   Last change performed on the world */
	
	private Change cambio;
	
	/**  int table used to store the number of balloons in each cell */
	
	
	private int globos[][];
	
	/**  Orientation that the robot is facing */
	
	private int orientacion;
	
	/**
	 * RobotWorld constructor
	 * @param dim  World dimension
	 * @param pos Robot's Initial position
	 * @param initB initial number of balloons
	 * @param initC initial number of chips
	 */
	
	public RobotWorld(int dim, Point pos, int initB, int initC) {
		
		
		this.n = dim;
		this.posicion = pos;
		this.misFichas = initC;
		this.misGlobos = initB;
		
		// Initially there are no chips nor balloons 
		
		this.fichas = new HashSet <Point>();
		this.globos = new int[dim][dim];

		// No change
		this.cambio = new Change();
		
		//Initially facing north
		
		this.orientacion = NORTH;
	}

	
	/** 
	 * Default constructor:  size 8, at position (1,1) with 630 balloons and 64 chips 
	 */
	public RobotWorld(){
		this(8,new Point(1,1),640,64);
	}	

	//
	// modifiers
	//


	/**
	 * Reinitializes world attributes
	 * @param pos Robot's  position
	 * @param newB new number of balloons
	 * @param newC new number of chips
	 * @param clean:  if false then then the current balloons and chips are left on the board
	 *                 if true the cells are left without chips and without balloons.
	 */
	
	public void reinicializar(Point pos, int newB, int newC, boolean clean) {
		int i,j;
		
		posicion = pos;
		misGlobos = newB;
		misFichas = newC;
		if (clean) {
			for(i=0; i<n; i++) {
				for (j=0; j<n; j++)
				{
					globos[i][j]=0;
				}
			}
			fichas.clear();			
		}

		cambio.setEnd(new Point(n+1,n+1));
		cambio.setStart(new Point(n+1,n+1));
		//this.setChanged();
		//this.notifyObservers(this.cambio);
		pcs.firePropertyChange("theProperty", n, cambio);
}
	
	/**
	 * Move the robot left
	 * If the robot is at the far left, the behavior is not defined
	 */
	public void izquierda() {
		// En realidad no definido significa que la nueva posicion es un numero aleatorio
		// Moverse a la izquierda es avanzar -1 pasos en x
		// Translate en un metodo de point
		this.cambio.setStart(new Point(this.posicion));
		this.posicion.translate(-1,0);
		buscarErrores();
		informar();

	}

	/**
	 * Move the robot right
	 * If the robot is at the far left, the behavior is not defined
	 */
	public void derecha() {
		// Moverse a la derecha es avanzar 1 paso en x
		// Translate en un metodo de point
		this.cambio.setStart(new Point(this.posicion));
		this.posicion.translate(1,0);
		buscarErrores();
		informar();
	}
	/**
	 * Move the robot up
	 * If the robot is at the far left, the behavior is not defined
	 */
	public void arriba() {
		// Moverse hacia arriba es retroceder 1 paso en y
		// Translate en un metodo de point
		this.cambio.setStart(new Point(this.posicion));
		this.posicion.translate(0,-1);
		buscarErrores();
		informar();
	}
	/**
	 * Move the robot down
	 * If the robot is at the far left, the behavior is not defined
	 */
	public void abajo() {
		// Moverse hacia abajo es avanzar 1 paso en y
		// Translate en un metodo de point
		this.cambio.setStart(new Point(this.posicion));
		this.posicion.translate(0,1);
		buscarErrores();
		informar();
	}
	
	/**
	 *  Puts a balloon in its current position, incrementing the number of balloons in that position.
	 *  If the robot has no balloons, the behavior is nor defined
	 */
	
	public void ponerGlobo() {
		int x = (int)this.posicion.getX();
		int y = (int)this.posicion.getY();	
		this.globos[x-1][y-1]++;
		this.misGlobos--;
		buscarErrores();
		informar();
	}

	/**
	 * Picks a balloon from its position
	 * IF there are no balloons, the behavior is not defined.
	 */
	public void recogerGlobo() {
		int x = (int)this.posicion.getX();
		int y = (int)this.posicion.getY();	
		this.globos[x-1][y-1]--;
		this.misGlobos++;
		buscarErrores();
		informar();
	}
	/**
	 * Pone una ficha en la posicion donde esta el robot actualmente
	 * Si ya exisia una ficha en esa posicion hay un comportamiento no definido
	 * Las fichas no flotan por lo que es necesario averiguar si hay fichas en
	 * la misma columna y mas abajo para poner la nueva ficha en la posicion
	 * mas baja que se encuentre
	 */
	public void ponerFicha() {
		//De acuerdo al API de java.util.HashSet la funcion add devuelve un valor de verdad
		//que es cierto si se esta incertando por primera vez, falso en caso contrario
		//Si no se esta insertando por primera vez eso indica que ya habia ficha en la posicion en la que 
	
		int y = (int)this.posicion.getY();
		int b;
		boolean yaPuesta=false;
		Point p= new Point(this.posicion);
		//Debe analizar todos los posibles Y desde el n (que es el mas bajo)
		//hasta y mismo		
		for(b=this.n;b>=y;b--) {
			p.setLocation(p.getX(),b);
			if(!this.hayFicha(p)) {
				//No hay ficha, ya podemos poner la ficha
				yaPuesta=true;	
				this.cambio.setStart(new Point(p));				
				this.fichas.add(p);
				informar();
				//Sale del ciclo
				break;
			}
		//buscarErrores();
		//informar();
		}
		if(!yaPuesta) {
			this.posicion = new Point(1,1);
			comportamientoDeError("Can't put chip");
			informar();
		}
		else {
		  misFichas--;
		  informar();	
		}
	}
	/**
	 * Attempts to pickup a chip.
	 * If there is a chip, then the robot keeps it and all chips above it fall one position
	 * If there is no chip, the behavior is undefined.
	 */
		
	public void recogerFicha() {
		int y = (int)this.posicion.getY();
		int x = (int)this.posicion.getX();
		int b;
		//Point actual;
		Point anterior,siguiente;
		//actual = 	new Point(this.posicion);
		anterior = 	new Point(this.posicion);
		siguiente = new Point(this.posicion);
			
		if(!this.hayFicha(this.posicion)) {
			comportamientoDeError("No chip to put");
			informar();
		}
		else {
	    // hay dos casos: 
		//    1. Hay fichas encima
		//    2. No hay fichas encima
		misFichas++;

		  if (y != 0) {
			siguiente.setLocation(siguiente.getX(),y-1);
			if (!this.hayFicha(siguiente)){
			
				this.cambio.setStart(this.posicion);
				this.fichas.remove(this.posicion);
				
				informar();
			}
            else {
              if (y-2 < 0) {
				  anterior.setLocation(siguiente.getX(),siguiente.getY());
			  }
			  else {                    
				for(b=y-2;b >= 0 && this.hayFicha(siguiente);b--){
					anterior.setLocation(siguiente.getX(),siguiente.getY());
					siguiente.setLocation(siguiente.getX(),b);
			   	}
              }
			  this.cambio.setStart(anterior);
			  this.fichas.remove(anterior);
			  informar();
              this.posicion.setLocation(x,y);  
			  informar();
			}		
		  }
          else {
			this.cambio.setStart(this.posicion);
			this.fichas.remove(this.posicion);
			informar();
			}
		}
	}
	
	//
	// Metodos privados
	//
	
	/**
	 * Used to determine if there was an error:
	 *  Robot's position outside the board
	 *  Robot with a negative number of chips or balloons
	 *  Positions with a negative number of balloons
	 *  Errors due to placing chips are taken care of it the nethod putChips
	 *  If there is an error, errorBehavior is invoked 
	 *  */
	
	private void buscarErrores() {
		int x,y;
		//Hay que hacer casting porque getX y getY devuelven variables de tipo double
		x = (int)this.posicion.getX();
		y = (int)this.posicion.getY();		
		if(x < 1 || x > this.n) {
				comportamientoDeError("Illegal column: "+ x);
		}
		else if ( y<1 || y>this.n )  {
				comportamientoDeError("Illegal row: "+ y);
		}
		else if (this.misFichas < 0) {
			comportamientoDeError("Not enough chips o put:" + (-this.misFichas));
		}
		else if ( this.misGlobos < 0 ) {
			comportamientoDeError("Not enough balloons to put:" + (-this.misGlobos));
		}
		else if (this.globos[x-1][y-1]< 0){
			comportamientoDeError("Not enough balloons to pick" + (-this.misGlobos));
		}
	}

	/**
	* Error Behavior: world without chips, balloons, and robot
	*/
	private void comportamientoDeError(String message) throws Error  {
		
		this.posicion = new Point(1,1);			
		this.misFichas = 0;
		this.misGlobos = 0;
		
		cambio.setEnd(new Point(n+1,n+1));
		cambio.setStart(new Point(0,0));
		
		//this.setChanged();
		//this.notifyObservers(this.cambio);
		
		pcs.firePropertyChange("theProperty", n, cambio);
		throw new Error(message);
	}

	/**
	 * Informa a los observadores que este objeto cambio, 
	 * y les dice cual fue el cambio, supondiendo que se acaba en la posicion donde esta el robot
	 * @see java.util.Observer
	 */

	protected void informar() {
		this.cambio.setEnd(this.posicion);
		
		//Metodos heredados de observer
		//this.setChanged();
		//this.notifyObservers(this.cambio);
		
		pcs.firePropertyChange("theProperty", n, cambio);
	
	}
	
	
	//
	// Metodos analizadores
	//
	/**
	 * @return robot's current posiiotn
	 */
	public Point getPosicion() {
		return this.posicion;
	}
	/**
	 * @return board size
	 */
	public int getN() {
		return this.n;
	}
	/**
	* @return nuimber of balloons held by the robot
    */
	public int getMisGlobos() {
		return this.misGlobos;
	}
	/**
	* @return nuimber of chips held by the robot
    */
	public int getMisFichas() {
			return this.misFichas;
	}

	/**
	 * @return true if there is a chip in cell p; false otherwise 
	 * 
	 * */
	public boolean hayFicha(Point p) {
		return this.fichas.contains(p);
	}
	/**
	 * @return true if there is a chip in the robot's current position; false otherwise 
	 * 
	 * */
	public boolean hayFicha() {
		return this.fichas.contains(this.posicion);
	}
	/**
	 * Devuelve el numero de globos que hay en el punto dado.
	 * Si p no esta dentro de los limites del mundo hay un NullPointerException.
	 * @param p El punto en el que queremos averiguar si hay globos
	 * @return El n&uacute;mero de globos que hay en ese punto
	 */
	public int contarGlobos(Point p) {
		return this.globos[p.x-1][p.y-1];
	}
	
	/**
	 * Devuelve el numero de globos que hay en la posicion actual.
	 * @return El n&uacute;mero de globos que hay en la posicion actual.
	 */
	public int contarGlobos() {
		return this.globos[(this.posicion).x-1][(this.posicion).y-1];
	}
	
	/**
	 * Devuelve true si exite por lo menos un globo en la posicion actual.
	 * @return true si existe por lo menos un globo en la posicion actuel
	 */
	public boolean hayGlobos() {
		if ( this.globos[(this.posicion).x-1][(this.posicion).y-1] > 0 )
		{
			return true;
		}
		return false;
	}
	/**
	 * Devuelve un valor para decir si el robot se encuentra en la primera fila del mundo
	 * @return verdadero si esta en la primera fila, falso en caso contrario
	 */
	public boolean estaArriba() {
		return this.posicion.y==1;
	}
	/**
	 * Devuelve un valor para decir si el robot se encuentra en la ultima fila del mundo
	 * @return verdadero si esta en la ultima fila, falso en caso contrario
	 */
	public boolean estaAbajo() {
		return this.posicion.y==this.n;
	}
	/**
	 * Devuelve un valor para decir si el robot se encuentra en la primera fila del mundo
	 * @return verdadero si esta en la primera columna, falso en caso contrario
	 */
	public boolean estaIzquierda() {
		return this.posicion.x==1;
	}
	/**
	 * Devuelve un valor para decir si el robot se encuentra en la ultima columna del mundo
	 * @return verdadero si esta en la ultima columna, falso en caso contrario
	 */
	public boolean estaDerecha() {
		return this.posicion.x==this.n;
	}


	/**
	 * Reinitializes world attributes
	 * @paeam din  new size
	 * @param pos Robot's  position
	 * @param newB new number of balloons
	 * @param newC new number of chips
	 * @param clean:  if false then then the current balloons and chips are left on the board
	 *                 if true the cells are left without chips and without balloons.
	 */
	
	public void reinicializar(int dim, Point pos, int newB, int newC, boolean clean) {

		int i,j;
		int tempGlobos[][] = new int[dim][dim];
		int  minDim = (n<dim)?n: dim; 
		Point tempFichas[] = (Point [])fichas.toArray();
		
		n = dim;
		posicion = pos;
		misGlobos = newB;
		misFichas = newC;
		
		
		if (clean) {
			fichas.clear();		
			globos = tempGlobos;
		}
		else {	
			for(i=0; i<minDim; i++) {
				for (j=0; j<minDim; j++)
				{
					tempGlobos[i][j]=globos[i][j];
				}
			}
			globos = tempGlobos;			
			if (dim < n) {		
				for (i=0; i < tempFichas.length; i++) {
					Point pf1 = tempFichas[i];
					if (pf1.x > dim || pf1.y > dim) {
						fichas.remove(pf1);
					}
				}
			}
		}
		
//		El cambio es "nada"
		this.cambio = new Change();
		cambio.setEnd(new Point(n+1,n+1));
		cambio.setStart(new Point(n+1,n+1));
		//this.setChanged();
				//this.notifyObservers(this.cambio);
				pcs.firePropertyChange("theProperty", n, cambio);
}

	
	
	/** 
	 * Modifies the wolrd's size without changing anything else. the 
	 * size is decreased and the robots ennds up outside the new dimensions, it is placed at the end of the world
	 * @param dim new size
	 */
	
	public void reinicializar(int dim) {

		int i,j;
		int tempGlobos[][] = new int[dim][dim];
		int  minDim = Math.min(dim,n); 
		Point tempFichas[] = (Point [])fichas.toArray();
				
		
		
	     posicion.x = Math.min(dim,posicion.x);
		 posicion.y =  Math.min(dim,posicion.y);
				
		for(i=0; i<minDim; i++) {
			for (j=0; j<minDim; j++) {
				tempGlobos[i][j]=globos[i][j];
			}
		}	
		
		if (dim < n) {		
			for (i=0; i < tempFichas.length; i++) {
				Point pf1 = tempFichas[i];
				if (pf1.x > dim || pf1.y > dim) {
					fichas.remove(pf1);
				}
			}
		}
		
		globos = tempGlobos;			
		n = dim;
		
		this.cambio = new Change();
		cambio.setEnd(new Point(n+1,n+1));
		
				pcs.firePropertyChange("theProperty", n, cambio);
		
	}	
	
	/**
	 * verifies whether or not  robotis facing north
	 * @return true if robot is facing north; false otherwise
	 */
	public boolean facingNorth() {
		return this.orientacion==NORTH;
	}
	/**
	 * verifies whether or not  robotis facing south
	 * @return true if robot is facing south; false otherwise
	 */
	public boolean facingSouth() {
		return this.orientacion==SOUTH;
	}
	/**
	 * verifies whether or not  robotis facing east
	 * @return true if robot is facing east; false otherwise
	 */
	public boolean facingEast() {
		return this.orientacion==EAST;
	}
	/**
	 * verifies whether or not  robotis facing west
	 * @return true if robot is facing west; false otherwise
	 */
	public boolean facingWest() {
		return this.orientacion==WEST;
	}
	
	/**
	 * 
	 * @return The robot's oreintation SOUTH, EAST OR WEST).
	 */
	public int getOrientacion(){
		return this.orientacion;
	}
	
	/**
	 * Turns the robot 90 degrees to the right. 
	 */
	public void turnRight(){
		if(this.orientacion == NORTH){
			this.orientacion = EAST;
		}else if(this.orientacion == SOUTH){
			this.orientacion = WEST;
		}else if(this.orientacion == EAST){
			this.orientacion = SOUTH;
		}else {
			this.orientacion = NORTH;
		}
		informar();
	}
	
	
	
}
