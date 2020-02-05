package uniandes.lym.robot.control;

import uniandes.lym.robot.kernel.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;


/**
 * Receives commands and relays them to the Robot. 
 * Integrantes: 
 * Germ�n Mart�nez - 201816347
 * Nicol�s Abondano - 201812467
 */

public class Interpreter   {

	/**
	 * Robot's world
	 */
	private RobotWorldDec world;   

	private HashMap<String, Integer> map = new HashMap<String, Integer>();

	private StringBuffer output = new StringBuffer("SYSTEM RESPONSE: -->\n");


	public Interpreter()
	{
	}


	/**
	 * Creates a new interpreter for a given world
	 * @param world 
	 */


	public Interpreter(RobotWorld mundo)
	{
		this.world =  (RobotWorldDec) mundo;

	}


	/**
	 * sets a the world
	 * @param world 
	 */

	public void setWorld(RobotWorld m) 
	{
		world = (RobotWorldDec) m;

	}



	/**
	 *  Processes a sequence of commands. A command is a letter  followed by a ";"
	 *  The command can be:
	 *  M:  moves forward
	 *  R:  turns right
	 *  
	 * @param input Contiene una cadena de texto enviada para ser interpretada
	 */

	public String process(String input) throws Error
	{   
		output=new StringBuffer("SYSTEM RESPONSE: -->\n");	
		input = input.replaceAll("\\s", "");

		int i;
		int n;
		boolean ok = true;
		n= input.length();

		i  = 0;
		try{
			while (i < n && ok){
				if(input.startsWith("ROBOT_R")) {
					input = input.substring(7);

					if(input.startsWith("VARS")) {
						input = input.substring(4);
						boolean todasVariables = false;
						while(!todasVariables) {
							if(input.startsWith("BEGIN")){
								todasVariables = true;
							}
							else {
								if(!Character.isLetter(input.charAt(0))) {
									throw new Error("La variable deber�a empezar con una letra \n");	
								}
								int k = 0;
								boolean casoEspecial = false;
								while(input.charAt(k) != ',' && !casoEspecial) {
									if(input.substring(k, k+5).equals("BEGIN")){
										casoEspecial = true;
									}
									else if(!(Character.isLetter(input.charAt(k)) || Character.isDigit(input.charAt(k)))) {
										throw new Error("La variable s�lo deber�a ser de tipo alfanum�rico \n");
									}
									else {
										k++;
									}
								}
								String variable = input.substring(0, k);
								map.put(variable, 0);
								if(casoEspecial == true) {
									input = input.substring(k);
								}
								else {
									input = input.substring(k+1);
								}
							}
						}
					}
					if(input.startsWith("BEGIN")) {
						input = input.substring(5);
						bloqueInstrucciones(input);
						ok = false;
					}
					else {
						throw new Error("La cadena deber�a empezar con VARS o BEGIN \n");
					}
				}
				else{
					StringTokenizer tokens = new StringTokenizer(input,";");
					String token;
					while(tokens.hasMoreTokens()) {
						token = tokens.nextToken();
						switch (token){
						case "M": 
							world.moveForward(1); 
							output.append("move \n");
							break;
						case "R": 
							world.turnRight(); 
							output.append("turnRignt \n");
							break;
						case "C": 
							world.putChips(1); 
							output.append("putChip \n");
							break;
						case "B": 
							world.putBalloons(1); 
							output.append("putBalloon \n");
							break;
						case "c": 
							world.pickChips(1); 
							output.append("getChip \n");
							break;
						case "b": 
							world.grabBalloons(1); 
							output.append("getBalloon \n");
							break;
						default: output.append("Unrecognized command:  "+ token); ok = false;
						}

						if (ok) {
							if  (i+1 == n)  { output.append("expected ';' ; found end of input; ");  ok = false ;}
							else if (input.charAt(i+1) == ';') 
							{
								i= i+2;
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									System.err.format("IOException: %s%n", e);
								}

							}
							else {output.append(" Expecting ;  found: "+ input.charAt(i+1)); ok=false;
							}
						}
					}

				}
			}
		}
		catch (Error e ){
			output.append("Error!!!  "+ e.getMessage());
		}
		return output.toString();
	}

	public void bloqueInstrucciones(String input){
		if(!input.endsWith("END")) {
			throw new Error("No tiene el formato esperado \n");
		}
		Iterator<String> it = separarSinParentesis(input, ';');
		while(it.hasNext()) {
			String act = it.next();
			if(act.endsWith("END")) {
				act = act.substring(0,act.length()-3);
			}
			if(act.length() != 0) {
				leerInstruccion(act);	
			}
		}
	}

	public void leerInstruccion(String instruccion) {
		if(instruccion.startsWith("Assign")){
			String parametro = instruccion.substring(7, instruccion.length() - 1);
			String[] parametros = parametro.split(",");
			if(parametros.length != 2) 
				throw new Error("Assign no tiene el formato esperado \n");

			verificarNombreVariable(parametros[0]);
			verificarNumero(parametros[1]);
			map.put(parametros[0], Integer.parseInt(parametros[1]));
			output.append("assign \n");

		}
		else if(instruccion.startsWith("MoveDir")) {
			String parametro = instruccion.substring(8, instruccion.length() - 1);
			String[] parametros = parametro.split(","); int n;

			switch(parametros[1]){
			case "front":
				n = 0;
				break;
			case "right":
				world.turnRight();output.append("turnRignt \n");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.err.format("IOException: %s%n", e);
				}
				n = 3;
				break;
			case "left":
				world.turnRight();output.append("turnRignt \n"); 
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.err.format("IOException: %s%n", e);
				}
				world.turnRight();output.append("turnRignt \n");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.err.format("IOException: %s%n", e);
				}
				world.turnRight();output.append("turnRignt \n");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.err.format("IOException: %s%n", e);
				}
				n = 1;
				break;
			case "back":
				world.turnRight();output.append("turnRignt \n");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.err.format("IOException: %s%n", e);
				}
				world.turnRight();output.append("turnRignt \n");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.err.format("IOException: %s%n", e);
				}
				n = 2;
				break;
			default: throw new Error("MoveDir no tiene el formato esperado \n");
			}
			move(parametros[0]);
			for(int i = 0; i<n; i++) {
				world.turnRight();
				output.append("turnRignt \n");
			}

		}
		else if(instruccion.startsWith("Move")){
			String variableInstruccion = instruccion.substring(5, instruccion.length() - 1);
			if(variableInstruccion.contains(",")) {
				String[] parametros = variableInstruccion.split(",");
				face(parametros[1]); 
				move(parametros[0]);
			}
			else {
				move(variableInstruccion);
			}
		}
		else if(instruccion.startsWith("Turn")){
			String parametro = instruccion.substring(5, instruccion.length() - 1);
			turn(parametro);
		}
		else if(instruccion.startsWith("Face")) {
			String parametro = instruccion.substring(5, instruccion.length() - 1);
			face(parametro);
		}
		else if(instruccion.startsWith("Put")) {
			String parametro = instruccion.substring(4, instruccion.length() - 1);
			String[] parametros = parametro.split(","); int n;
			if(parametros.length != 2) 
				throw new Error("Put no tiene el formato esperado \n");

			if(Character.isLetter(parametros[1].charAt(0))){
				verificarNombreVariable(parametros[1]);
				n = map.get(parametros[1]);
			}
			else if(Character.isDigit(parametros[1].charAt(0))){
				n = Integer.parseInt(parametros[1]);
			}
			else {
				throw new Error("Put no tiene el formato esperado \n");
			}
			if(parametros[0].equals("Balloons")) {
				world.putBalloons(n);
				output.append("putBalloon \n");
			}
			else if(parametros[0].equals("Chips")) {
				world.putChips(n);
				output.append("putChip \n");
			}
			else {
				throw new Error("Put no tiene el formato esperado \n");
			}
		}
		else if(instruccion.startsWith("Pick")) {
			String parametro = instruccion.substring(5, instruccion.length() - 1);
			String[] parametros = parametro.split(","); int n;
			if(parametros.length != 2) 
				throw new Error("Pick no tiene el formato esperado \n");

			if(Character.isLetter(parametros[1].charAt(0))){
				verificarNombreVariable(parametros[1]);
				n = map.get(parametros[1]);
			}
			else if(Character.isDigit(parametros[1].charAt(0))){
				n = Integer.parseInt(parametros[1]);
			}
			else {
				throw new Error("Pick no tiene el formato esperado \n");
			}
			if(parametros[0].equals("Balloons")) {
				world.grabBalloons(n);
				output.append("pickBalloon \n");
			}
			else if(parametros[0].equals("Chips")) {
				world.pickChips(n);
				output.append("pickChip \n");
			}
			else {
				throw new Error("Pick no tiene el formato esperado \n");
			}
		}
		else if(instruccion.startsWith("Skip")) {

		}
		else if(instruccion.startsWith("if")) {
			Iterator<String> it = 
					separarSinParentesis(instruccion.substring(3, instruccion.length() - 1), ',');
			boolean condition = leerCondicion(it.next());
			String b1 = it.next(); String b2 = it.next();
			if(condition)
				bloqueInstrucciones(b1.replaceFirst("BEGIN", ""));
			else
				bloqueInstrucciones(b2.replaceFirst("BEGIN", ""));	
		}
		else if(instruccion.startsWith("while")) {
			Iterator<String> it = 
					separarSinParentesis(instruccion.substring(6, instruccion.length() - 1), ',');
			String cond = it.next(); String b = it.next().replaceFirst("BEGIN", "");
			while(leerCondicion(cond))
				bloqueInstrucciones(b);

		}
		else if(instruccion.startsWith("Repeat")) {
			Iterator<String> it = 
					separarSinParentesis(instruccion.substring(7, instruccion.length() - 1), ',');
			String nT = it.next(); int n;
			String b = it.next().replaceFirst("BEGIN", "");
			if(Character.isLetter(nT.charAt(0))){
				verificarNombreVariable(nT);
				n = map.get(nT);
			}
			else if(Character.isDigit(nT.charAt(0))){
				n = Integer.parseInt(nT);
			}
			else {
				throw new Error("Formato no esperado \n");
			}			
			for (int i = 0; i < n; i++)
				bloqueInstrucciones(b);
		}
		else {
			throw new Error("Unrecognized command \n");
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.err.format("IOException: %s%n", e);
		}
	}

	private void move(String var) {
		if(Character.isLetter(var.charAt(0))){
			verificarNombreVariable(var);
			world.moveForward(map.get(var)); 
			output.append("move \n");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.err.format("IOException: %s%n", e);
			}
		}
		else if(Character.isDigit(var.charAt(0))){
			world.moveForward(Integer.parseInt(var)); 
			output.append("move \n");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.err.format("IOException: %s%n", e);
			}
		}
		else {
			throw new Error("Move no tiene el formato esperado \n");
		}
	}

	private void face(String dir) {
		switch(dir){
		case "north":
			while(!world.facingNorth()) {
				world.turnRight();
				output.append("turnRignt \n");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.err.format("IOException: %s%n", e);
				}
			}
			break;
		case "east":
			while(!world.facingEast()) {
				world.turnRight();
				output.append("turnRignt \n");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.err.format("IOException: %s%n", e);
				}
			}
			break;
		case "west":
			while(!world.facingWest()) {
				world.turnRight();
				output.append("turnRignt \n");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.err.format("IOException: %s%n", e);
				}
			}
			break;
		case "south":
			while(!world.facingSouth()) {
				world.turnRight();
				output.append("turnRignt \n");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.err.format("IOException: %s%n", e);
				}
			}
			break;
		default: throw new Error("Face no tiene el formato esperado \n");
		}
	}

	private void turn(String dir) {
		int lim;
		switch (dir) {
		case "left":
			lim = 3;
			break;
		case "right":
			lim = 1;
			break;
		case "around":
			lim = 2;
			break;
		default:
			throw new Error("Turn no tiene el formato correcto \n");
		}
		for(int j = 0; j < lim; j++) {
			world.turnRight();
			output.append("turnRignt \n");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.err.format("IOException: %s%n", e);
			}
		}
	}

	public void verificarNombreVariable(String variableAVerificar) {
		if(map.get(variableAVerificar) == null) {
			throw new Error("La variable no fue declarada \n");
		}
	}

	public void verificarNumero(String numeroAVerificar) {
		for(int j = 0; j < numeroAVerificar.length(); j++) {
			if(!Character.isDigit(numeroAVerificar.charAt(j))) {
				throw new Error("La variable s�lo deber�a ser de tipo num�rico \n");
			}
		}
	}

	public Iterator<String> separarSinParentesis(String text, char split) {
		int cont = 0;
		ArrayList<String> tokens = new ArrayList<String>();
		String actual = "";
		for (int i = 0; i < text.length(); i++) {
			char act = text.charAt(i);
			if( act == '(')
				cont++;
			else if(act == ')')
				cont--;

			if(act == split && cont == 0) {
				tokens.add(actual);actual = "";
			}
			else {
				actual += act;
			}

		}
		if(!actual.equalsIgnoreCase(""))
			tokens.add(actual);

		return tokens.iterator();
	}

	public boolean leerCondicion(String cond) throws Error {
		if(cond.startsWith("facing")) {
			String dir = cond.substring(7,cond.length()-1);

			switch (dir) {
			case "north":
				return world.facingNorth();
			case "east":
				return world.facingEast();
			case "west":
				return world.facingWest();
			case "south":
				return world.facingSouth();
			default:
				throw new Error("facing no tiene el formato esperado \n");

			}
		}
		else if(cond.startsWith("canPut")) {
			String parametro = cond.substring(7, cond.length() - 1);
			String[] parametros = parametro.split(","); int n;
			if(Character.isLetter(parametros[1].charAt(0))){
				verificarNombreVariable(parametros[1]);
				n = map.get(parametros[1]);
			}
			else if(Character.isDigit(parametros[1].charAt(0))){
				n = Integer.parseInt(parametros[1]);
			}
			else {
				throw new Error("canPut no tiene el formato esperado \n");
			}

			switch (parametros[0]) {
			case "chips":
				return world.freeSpacesForChips() >= n && world.getMisFichas() >= n && n >= 0;
			case "balloons":
				return world.getMisGlobos() >= n && n >= 0;
			default:
				throw new Error("canPut no tiene el formato esperado \n");
			}
		}
		else if(cond.startsWith("canPick")) {
			String parametro = cond.substring(8, cond.length() - 1);
			String[] parametros = parametro.split(","); int n;
			if(Character.isLetter(parametros[1].charAt(0))){
				verificarNombreVariable(parametros[1]);
				n = map.get(parametros[1]);
			}
			else if(Character.isDigit(parametros[1].charAt(0))){
				n = Integer.parseInt(parametros[1]); 
			}
			else {
				throw new Error("canPick no tiene el formato esperado \n");
			}

			switch (parametros[0]) {
			case "chips":
				return world.chipsToPick() >= n && n >= 0;
			case "balloons": 
				return world.contarGlobos() >= n && n >= 0;
			default:
				throw new Error("canPick no tiene el formato esperado \n");
			}
		}
		else if(cond.startsWith("canMove")) {
			String parametro = cond.substring(8, cond.length() - 1);
			switch (parametro) {
			case "north":
				return !world.estaArriba();
			case "south":
				return !world.estaAbajo();
			case "east":
				return !world.estaDerecha();
			case "west":
				return !world.estaIzquierda();
			default: 
				throw new Error("canMove no tiene el formato esperado");
			}
		}
		else if(cond.startsWith("not")) {
			cond = cond.replaceFirst("(", "").substring(0, cond.length()-2);

			return !leerCondicion(cond);
		}
		return false;
	}
}
