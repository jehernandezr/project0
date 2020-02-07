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
 * Juan Diego Castellanos
 * Jonatan Hernandez 201813833
 */

public class Interpreter   {

	/**
	 * Robot's world
	 */
	private RobotWorldDec world;   

	private HashMap<String, Integer> map = new HashMap<String, Integer>();

	private StringBuffer output;
	/*
	 * VARIABLES DEL LENGUAJE
	 */
	private String comando_BEGIN = "BEGIN";
	private String comando_END = "END";
	private String comando_ROBOT_R = "ROBOT_R";
	private String comando_VARS = "VARS";
	private String comando_facing = "facing:";
	private String comando_canPut = "canPut:";
	private String comando_canPick = "canPick:";
	private String comando_canMove = "canMove:";
	private String comando_not = "not:";
	private String comando_Assign = "assign:";
	private String comando_MoveDir = "move:";
	private String comando_Move = "move:";	
	private String comando_Turn = "turn:";
	private String comando_Face = "face:";
	private String comando_Put = "put:";
	private String comando_Chips = "chips";
	private String comando_Pick = "pick:";
	private String comando_if = "if:";
	private String comando_while = "while:";
	private String comando_Repeat = "repeat:";
	private String comando_Ballons = "ballons";
	private String comando_skip = "Skip";

	/**
	 * Constructor vacio
	 */
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
	 * Comprueba que las instrucciones esten completas,
	 * es decir que termine con END
	 * @param input
	 */
	public void bloqueInstrucciones(String input){
		if(!input.endsWith(comando_END)) {
			throw new Error("No es una entrada valida ya que no termina en END \n");
		}
		Iterator<String> it = separar(input, ';');
		while(it.hasNext()) {
			String act = it.next();
			if(act.endsWith(comando_END)) {
				act = act.substring(0,act.length()-3);
			}
			if(act.length() != 0) {
				leerInstruccion(act);	
			}
		}
	}

	/**
	 *  Processes a sequence of commands. A command is a letter  followed by a ";"
	 *  The command can be:
	 *  M:  moves forward
	 *  R:  turns right
	 *  
	 * @param in Contiene una cadena de texto enviada para ser interpretada
	 */
	public String process(String in) throws Error
	{   
		output= new StringBuffer("SYSTEM RESPONSE: -->\n");	
		in = in.replaceAll("\\s", "");

		int i;
		int n;
		boolean estaBien = true;
		n= in.length();

		i  = 0;
		try{
			while (i < n && estaBien){
				if(in.startsWith(comando_ROBOT_R)) {
					in = in.substring(7);

					if(in.startsWith(comando_VARS)) 
					{
						in = in.substring(4);
						boolean todasVariables = false;
						while(!todasVariables) 
						{
							if(in.startsWith(comando_BEGIN))
							{
								todasVariables = true;

							}
							else 
							{
								if(!Character.isLetter(in.charAt(0))) 
								{
									throw new Error("La variable debería empezar por una \n");	
								}
								int k = 0;
								boolean casoEspecial = false;
								while(in.charAt(k) != ',' && !casoEspecial)
								{
									if(in.substring(k, k+5).equals(comando_BEGIN))
									{
										casoEspecial = true;
									}
									else if(!(Character.isLetter(in.charAt(k)) || Character.isDigit(in.charAt(k)))) {
										throw new Error("La variable sólo debería ser de tipo alfanumérico \n");
									}
									else {
										k++;
									}
								}
								String variable = in.substring(0, k);
								map.put(variable, 0);
								if(casoEspecial == true) {
									in = in.substring(k);
								}
								else {
									in = in.substring(k+1);
								}
							}
						}
					}
					if(in.startsWith(comando_BEGIN)) {
						in = in.substring(5);
						bloqueInstrucciones(in);
						estaBien = false;
					}
					else {
						throw new Error("La cadena empiezar con BEGIN o VARS \n");
					}
				}
				else{
					StringTokenizer tokens = new StringTokenizer(in,";");
					String ficha;
					while(tokens.hasMoreTokens()) {
						ficha = tokens.nextToken();
						switch (ficha){

						case "b": 
							world.grabBalloons(1); 
							output.append("getBalloon \n");
							break;
						case "B": 
							world.putBalloons(1); 
							output.append("putBalloon \n");
							break;
						case "R": 
							world.turnRight(); 
							output.append("turnRignt \n");
							break;

						case "M": 
							world.moveForward(1); 
							output.append("move \n");
							break;

						case "C": 
							world.putChips(1); 
							output.append("putChip \n");
							break;

						case "c": 
							world.pickChips(1); 
							output.append("getChip \n");
							break;

						default: output.append("Comando no reconocido:  "+ ficha); estaBien = false;
						}

						if (estaBien) {
							if  (i+1 == n)  { output.append("expected ';' ; found end of input; ");  estaBien = false ;}
							else if (in.charAt(i+1) == ';') 
							{
								i= i+2;
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									System.err.format("IOException: %s%n", e);
								}

							}
							else {output.append(" Expecting ;  found: "+ in.charAt(i+1)); estaBien=false;
							}
						}
					}

				}
			}
		}
		catch (Error e ){
			output.append("Error!  "+ e.getMessage());
		}
		return output.toString();
	}

	/**
	 * Leer una condición 
	 * @param cond
	 * 
	 * @return
	 * @throws Error
	 */
	public boolean leerCondicion(String cond) throws Error {
		if(cond.startsWith(comando_facing)) {
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
				throw new Error("facing no tiene el formato correcto \n");

			}
		}
		else if(cond.startsWith(comando_canPut)) {
			String parametro = cond.substring(7, cond.length() - 1);
			String[] parametros = parametro.split("of:"); int n;
			if(Character.isLetter(parametros[1].charAt(0))){
				verificarNombreVariable(parametros[1]);
				n = map.get(parametros[1]);
			}
			else if(Character.isDigit(parametros[0].charAt(0))){
				n = Integer.parseInt(parametros[0]);
			}
			else {
				throw new Error("canPut no tiene el formato correcto \n");
			}

			switch (parametros[1]) {
			case "chips":
				return world.freeSpacesForChips() >= n && world.getMisFichas() >= n && n >= 0;
			case "balloons":
				return world.getMisGlobos() >= n && n >= 0;
			default:
				throw new Error("canPut no tiene el formato correcto \n");
			}
		}
		else if(cond.startsWith(comando_canPick)) {
			String parametro = cond.substring(8, cond.length() - 1);
			String[] parametros = parametro.split("of:"); int n;
			if(Character.isLetter(parametros[1].charAt(0))){
				verificarNombreVariable(parametros[1]);
				n = map.get(parametros[1]);
			}
			else if(Character.isDigit(parametros[0].charAt(0))){
				n = Integer.parseInt(parametros[0]); 
			}
			else {
				throw new Error("canPick no tiene el formato correcto \n");
			}

			switch (parametros[1]) {
			case "chips":
				return world.chipsToPick() >= n && n >= 0;
			case "balloons": 
				return world.contarGlobos() >= n && n >= 0;
			default:
				throw new Error("canPick no tiene el formato correcto \n");
			}
		}
		else if(cond.startsWith(comando_canMove)) {
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
				throw new Error("canMove no tiene el formato correcto");
			}
		}
		else if(cond.startsWith(comando_not)) {
			cond = cond.replaceFirst("(", "").substring(0, cond.length()-2);

			return !leerCondicion(cond);
		}
		return false;
	}

	/**
	 * Mueve el robot hacia adelante, en la direccion en la que esta mirando.
	 * @param var
	 */
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
			throw new Error("Move no tiene el formato correcto \n");
		}
	}

	/**
	 * Gira el robot acorde a la direccion entrada por parámetro
	 * @param dir direccion puede ser left, right, around
	 */
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

	/**
	 * verifica que la variable fue declarada de una manera correcta.
	 * @param variableAVerificar
	 */
	public void verificarNombreVariable(String variableAVerificar) {
		if(map.get(variableAVerificar) == null) {
			throw new Error("La variable no se encuentra \n");
		}
	}
	/**
	 * verifica que los datos que entren sean unicamente de tipo numerico.
	 * @param numeroAVerificar
	 */
	public void verificarNumero(String numeroAVerificar) {
		for(int j = 0; j < numeroAVerificar.length(); j++) {
			if(!Character.isDigit(numeroAVerificar.charAt(j))) {
				throw new Error("La variable sólo debería ser de tipo numérico \n");
			}
		}
	}

	/**
	 * separa el texto por el caracter dado
	 * @param text
	 * @param split
	 * @return un arreglo de String el cual contiene todo el texto
	 */
	public Iterator<String> separar(String text, char split) {

		ArrayList<String> tokens = new ArrayList<String>();
		String actual = "";
		for (int i = 0; i < text.length(); i++) {
			char act = text.charAt(i);


			if(act == split ) {
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

	/**
	 * Metodo que se encarga de leer la instruccion entrante
	 * verifica que esta tenga el formato esperado
	 * cumple la funcion, tales como while, repeat.  
	 * @param instruccion 
	 */

	public void leerInstruccion(String instruccion) {
		if(instruccion.startsWith(comando_Assign)&& (instruccion.subSequence(8, 11)== ("to:").subSequence(0, 3))){
			String parametro = instruccion.substring(8, instruccion.length() - 1);
			String[] parametros = parametro.split("to:");
			if(parametros.length != 2) 
				throw new Error("Assign no tiene el formato correcto \n");

			verificarNombreVariable(parametros[1]);
			verificarNumero(parametros[0]);
			map.put(parametros[1], Integer.parseInt(parametros[0]));
			output.append("assign \n");

		}
		else if(instruccion.startsWith(comando_MoveDir) && (instruccion.subSequence(6, 12)== ("inDir:").subSequence(0, 7))) {
			String parametro = instruccion.substring(6, instruccion.length() - 1);
			String[] parametros = parametro.split("inDir:"); int n;

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
			default: throw new Error(" El comando Move in Dir no tiene el formato correcto \n");
			}
			move(parametros[0]);
			for(int i = 0; i<n; i++) {
				world.turnRight();
				output.append("turnRignt \n");
			}

		}
		else if(instruccion.startsWith(comando_Move)){
			String variableInstruccion = instruccion.substring(5, instruccion.length() - 1);
			if(variableInstruccion.contains("toThe:")) {
				String[] parametros = variableInstruccion.split("toThe:");
				face(parametros[1]); 
				move(parametros[0]);
			}
			else {
				move(variableInstruccion);
			}
		}
		else if(instruccion.startsWith(comando_Turn)) {
			String parametro = instruccion.substring(5, instruccion.length() - 1);
			turn(parametro);
		}
		else if(instruccion.startsWith(comando_Face)) {
			String parametro = instruccion.substring(5, instruccion.length() - 1);
			face(parametro);
		}
		else if(instruccion.startsWith(comando_Put)) {
			String parametro = instruccion.substring(4, instruccion.length() - 1);
			String[] parametros = parametro.split("of:"); int n;
			if(parametros.length != 2) 
				throw new Error("Put no tiene el formato correcto \n");

			if(Character.isLetter(parametros[1].charAt(0))){
				verificarNombreVariable(parametros[1]);
				n = map.get(parametros[1]);
			}
			else if(Character.isDigit(parametros[1].charAt(0))){
				n = Integer.parseInt(parametros[1]);
			}
			else {
				throw new Error("Put no tiene el formato correcto \n");
			}
			if(parametros[0].equals(comando_Ballons)) {
				world.putBalloons(n);
				output.append("putBalloon \n");
			}
			else if(parametros[0].equals(comando_Chips)) {
				world.putChips(n);
				output.append("putChip \n");
			}
			else {
				throw new Error("Put no tiene el formato correcto \n");
			}
		}
		else if(instruccion.startsWith(comando_Pick)) {
			String parametro = instruccion.substring(5, instruccion.length() - 1);
			String[] parametros = parametro.split("of:"); int n;
			if(parametros.length != 2) 
				throw new Error("Pick no tiene el formato correcto \n");

			if(Character.isLetter(parametros[0].charAt(0))){
				verificarNombreVariable(parametros[0]);
				n = map.get(parametros[0]);
			}
			else if(Character.isDigit(parametros[0].charAt(0))){
				n = Integer.parseInt(parametros[0]);
			}
			else {
				throw new Error("Pick no tiene el formato correcto \n");
			}
			if(parametros[1].equals(comando_Ballons)) {
				world.grabBalloons(n);
				output.append("pickBalloon \n");
			}
			else if(parametros[1].equals(comando_Chips)) {
				world.pickChips(n);
				output.append("pickChip \n");
			}
			else {
				throw new Error("Pick no tiene el formato correcto \n");
			}
		}
		else if(instruccion.startsWith(comando_skip)) {

		}
		else if(instruccion.startsWith(comando_if)) {
			Iterator<String> it = 
					separar(instruccion.substring(3, instruccion.length() - 1), ':');
			boolean condition = leerCondicion(it.next());
			String b1 = it.next(); String b2 = it.next();
			b1=b1.replaceFirst("then", ""); b2=b2.replaceFirst("else", "");
			
			if(condition)
				bloqueInstrucciones(b1.replaceFirst("BEGIN", ""));
			else
				bloqueInstrucciones(b2.replaceFirst("BEGIN", ""));	
		}
		// verifica el caso en el que sea while.
		else if(instruccion.startsWith(comando_while)) {
			Iterator<String> it = 
					separar(instruccion.substring(5, instruccion.length() - 1), ':');
			String cond = it.next(); String b = it.next().replaceFirst("BEGIN", "");
			while(leerCondicion(cond))
				bloqueInstrucciones(b);

		}
		// verifica el caso en el que sea repeat.
		else if(instruccion.startsWith(comando_Repeat)) {
			Iterator<String> it = 
					separar(instruccion.substring(7, instruccion.length() - 1), ':');
			
			String nT = it.next().replaceFirst("BEGIN", ""); int n;
			if(nT.contains("times"))
				nT.replaceFirst("times", "");
			String b = it.next();
			if(Character.isLetter(b.charAt(0))){
				verificarNombreVariable(b);
				n = map.get(b);
			}
			else if(Character.isDigit(nT.charAt(0))){
				n = Integer.parseInt(nT);
			}
			else {
				throw new Error("Formato incorrecto \n");
			}			
			for (int i = 0; i < n; i++)
				bloqueInstrucciones(nT);
		}
		else {
			throw new Error("comando desconocido \n");
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.err.format("IOException: %s%n", e);
		}
	}

	/**
	 * metodo auxiliar para rotar
	 * @param dir
	 */
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
		default: throw new Error("Face no tiene el formato correcto \n");
		}
	}

}
