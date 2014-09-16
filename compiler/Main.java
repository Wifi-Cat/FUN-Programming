package compiler;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
	public static String[] tokens = { "start", "end", "loop", "endloop",
			"bool", "endbool", "out" };

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		int numPrograms = sc.nextInt();
		for (int i = 0; i < numPrograms; i++) {
			System.out.printf("PROGRAM %d:\n", i + 1);
			String endKeyword = null;
			Dictionary<String, Integer> vars = new Hashtable<String, Integer>();
			sc.next();// skip start keyword
			try {
				String output = evaluateBody(sc, "end", vars);
				System.out.println(output);
			} catch (InputMismatchException e) {
				System.out.println("ERRORS EXIST");
			}
		}
		System.out.println("END OF OUTPUT");
	}

	public static String evaluateBody(Scanner sc, String endKeyword,
			Dictionary<String, Integer> vars) {
		String output = "";
		String token = sc.next();
		while (!token.equals(endKeyword)) {
			if (isKeyword(token)) {
				switch (token) {
				case "start":
					output += evaluateBody(sc, "end", vars);
					break;
				case "loop":
					// first evaluate expression
					int loopExpression = parseExpression(sc, vars);
					String loopBody = getLoopBody(sc);
					for (int i = 0; i < loopExpression; i++) {
						Scanner loopScanner = new Scanner(loopBody);
						output += evaluateBody(loopScanner, "endloop", vars);
					}
					break;
				case "bool":
					// first evaluate condition
					boolean boolCondition = parseCondition(sc, vars);
					if (boolCondition) {
						output += evaluateBody(sc, "endbool", vars);
					} else {
						sc.nextLine();
						sc.nextLine();
					}
					break;
				case "out":
					// get output expression or string literal
					String toBeWritten = parseOutput(sc, vars);
					output += toBeWritten + "\n";
					break;
				case "end":
				case "endloop":
				case "endbool":
				default:
					// error in keyword matching
					throw new InputMismatchException();
				}
			} else {
				// assignment
				parseAssignment(token, sc, vars);
			}
			token = sc.next();
		}
		return output;
	}

	private static void parseAssignment(String key, Scanner sc,
			Dictionary<String, Integer> vars) throws InputMismatchException {
		if (isKeyword(key)) {
			throw new InputMismatchException();
		}
		String line = sc.nextLine().trim();
		String tokens[] = line.split("=");
		if (tokens.length != 2) {
			throw new InputMismatchException();
		}
		int value = parseExpression(tokens[1], vars);
		vars.put(key, value);
	}

	private static String getLoopBody(Scanner sc) {
		String loopBody = "";
		int loopCount = 1;
		while (loopCount > 0) {
			String potentialLoopLine = sc.nextLine().trim();
			if (isKeyword(potentialLoopLine.split(" ")[0])) {
				if (potentialLoopLine.split(" ")[0].equals("loop")) {
					loopCount++;
				} else if (potentialLoopLine.equals("endloop")) {
					loopCount--;
				}
			}
			loopBody += potentialLoopLine + "\n";
		}
		return loopBody;
	}

	private static String parseOutput(Scanner sc,
			Dictionary<String, Integer> vars) throws InputMismatchException {
		// get the whole line
		String line = sc.nextLine().trim();
		if (line.charAt(0) == '"') {
			// this is a string literal
			return parseString(line);
		} else {
			// this is an expression
			return parseExpression(line, vars) + "";
		}
	}

	private static boolean parseCondition(Scanner sc,
			Dictionary<String, Integer> vars) throws InputMismatchException {
		String line = sc.nextLine();

		String[] tokens = line.split("<");
		if (tokens.length != 2) {
			// not enough tokens
			throw new InputMismatchException();
		}
		// evaluate tokens
		return parseExpression(tokens[0], vars) < parseExpression(tokens[1],
				vars);
	}

	private static String parseString(String line)
			throws InputMismatchException {
		if (line.charAt(0) == '"' && line.charAt(line.length() - 1) == '"') {
			// valid string
			return line.substring(1, line.length() - 1);
		} else {
			throw new InputMismatchException();
		}
	}

	private static int parseExpression(Scanner sc,
			Dictionary<String, Integer> vars) {
		return parseExpression(sc.nextLine(), vars);
	}

	private static int parseExpression(String line,
			Dictionary<String, Integer> vars) throws InputMismatchException {
		String[] tokens = line.split("\\+");
		int expressionResult = 0;
		for (String t : tokens) {
			String trimmed = t.trim();
			if (isKeyword(trimmed)) {
				throw new InputMismatchException();
			}
			try {
				// assume token is a constant
				expressionResult += Integer.parseInt(trimmed);
			} catch (NumberFormatException e) {
				// whoops, it's a variable
				int var = parseVar(trimmed, vars);
				expressionResult += var;
			}
		}
		return expressionResult;
	}

	private static int parseVar(String token, Dictionary<String, Integer> vars)
			throws InputMismatchException {
		String key = token.trim();
		if (!key.matches("[a-z]+")) {
			throw new InputMismatchException();
		}
		// assume the variable is defined
		int var;
		try {
			var = vars.get(key);
		} catch (NullPointerException ee) {
			// the var is not defined
			vars.put(key, 0);
			var = 0;
		}
		return var;
	}

	private static boolean isKeyword(String token) {
		for (String t : tokens) {
			if (t.equals(token)) {
				return true;
			}
		}
		return false;
	}

}
