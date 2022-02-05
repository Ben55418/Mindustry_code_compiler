package scr;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Random;
import java.util.Scanner;

public class Compiler {
	public int saltLength = 5;
	
	private final String[] builtFunctions = {"max", "min", "abs"};
	private final String[][] operands = {{"^", "**", "%"}, {"*", "//", "/"}, {"+", "-"}, {"<<", ">>"}};
	private final String[] evaluators = {"<=", "<", ">=", ">", "<>", "!=", "===", "=="};
	private final String[] metaChars = {"*", "+", "?","^", "$"};
	private final String tag = getSaltString(saltLength);
	private Hashtable<String, String> opNames = new Hashtable<String, String>();
	
	//do stuff on start ------------------------------------------------------------------------------------------------------
	Compiler(){
		try {
			File file = new File("data/operators.txt");
		    Scanner reader = new Scanner(file);
		    while (reader.hasNextLine()) {
		    	String line = reader.nextLine();
		    	if(line.contains(":")) {
			    	String[] p = line.split(":");
			    	opNames.put(p[0], p[1]);
		    	}
		    }
		    reader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
	
	
	//what it initially ran, deals with functions
	public String fullCompile(String input) {
		
		//initial splitting and organizing lines
		String[] linesTemp = input.split("\n");
		ArrayList<String> linesSort = new ArrayList<String>();
		for(String line : linesTemp) {
			if(line.trim() != "") linesSort.add(line);
		}
		
		String[] lines = new String[linesSort.size()];
		for(int i = 0; i < linesSort.size(); i++) {
			lines[i] = linesSort.get(i).replaceAll("\t", " ");
		}
		
		//find and add functions
		for(int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if(isFunctionStatement(line)) {
				int startIndex = i;
			
				String onlyParameters = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
				String[] p = onlyParameters.split(",");
				
				String[] parameters = new String[p.length];
				
				for(int j = 0; j < p.length; j++) parameters[j] = p[j].trim(); 
				
				String content = "";
				i++;
				String l = lines[i];
				
				int afterLen = letterIndex(l);
				while(0 < letterIndex(l)){
					content += l.substring(afterLen) + "\n";
					i++;
					if(i >= lines.length) break;
					l = lines[i];
					afterLen = letterIndex(l);
				}
				int endIndex = i;
				
				for(int j = startIndex; j < endIndex; j++) {
					lines[j] = "";
				}
				
				// currently at the stage where lines the content of the lines are obtained and there is access to parameters, the plan is to
				// keep track of all created variables and add an "<tag>_<var name>_<functon name>" behind it so that the variables are unique and the function behaves 
				// within its own space. Access to outside created variables also needs to be kept in mind.
			}
		}
		
		
		
		//push the remaining code to be compiled
		String output = lines[0];
		for(int i = 1; i < lines.length; i++) {
			output += "\n" + lines[i];
		}
		
		return compile(output, 0);
	}
	
	
	//the main function ----------------------------------------------------------------------------------------------------
	private String compile(String input, int offset) {
		
		//initial splitting and organizing lines
		String[] linesTemp = input.split("\n");
		ArrayList<String> compiledLines = new ArrayList<String>();
		
		ArrayList<String> linesSort = new ArrayList<String>();
		for(String line : linesTemp) {
			if(line.trim() != "") linesSort.add(line);
		}
		
		String[] lines = new String[linesSort.size()];
		for(int i = 0; i < linesSort.size(); i++) {
			lines[i] = linesSort.get(i).replaceAll("\t", " ");
		}
		
		//full compile
		for(int i = 0; i < lines.length; i++) {
			String line = lines[i];
			
			if(isWhileLoop(line)) {
				String content = "";
				i++;
				String l = lines[i];
				int afterLen = letterIndex(l);
				while(0 < letterIndex(l) && i < lines.length){
					content += l.substring(afterLen) + "\n";
					i++;
					if(i >= lines.length) break;
					l = lines[i];
				}
				i--;
				
				for(String f : whileLoop(line, content, compiledLines.size() + offset).split("\n")) compiledLines.add(f);
			}
			
			else if(isIfStatement(line)) {
				String content = "";
				i++;
				String l = lines[i];
				int afterLen = letterIndex(l);
				while(0 < letterIndex(l)){
					content += l.substring(afterLen) + "\n";
					i++;
					if(i >= lines.length) break;
					l = lines[i];
				}
				
				if(isElifStatement(l) || isElseStatement(l)) {
					ArrayList<String> contentArray = new ArrayList<String>();
					ArrayList<String> lineArray = new ArrayList<String>();
					contentArray.add(content);
					lineArray.add(line);
					
					while (isElifStatement(l) || isElseStatement(l)) {
						lineArray.add(l);
						
						content = "";
						i++;
						l = lines[i];
						afterLen = letterIndex(l);
						
						while(0 < letterIndex(l)){
							content += l.substring(afterLen) + "\n";
							i++;
							if(i >= lines.length) break;
							l = lines[i];
						}
						contentArray.add(content);
					}
					
					for(String f : ifElifElseStatement(toStringArray(lineArray), toStringArray(contentArray), compiledLines.size() + offset).split("\n")) compiledLines.add(f);
				}
				
				else for(String f : ifStatement(line, content, compiledLines.size() + offset).split("\n")) compiledLines.add(f);
				
				i--;
			}
			
			else if(isAssignmentLine(line)) {
				for(String f : assignment(line).split("\n")) compiledLines.add(f);
			}
			
			else {
				compiledLines.add(line);
			}
			
		}
		
		String output = "";
		for(String i : compiledLines) {
			output += i + "\n";
		}
		output = output.substring(0, output.length()-1);
		return output;
	}
	
	// identify line types
	private Boolean isAssignmentLine(String input) {
		if(input.contains("=")) {
			String l = input.substring(0, input.indexOf('=')).trim();
			
			if((isEvaluatable(l) == false && isVariableName(l)) || getIndex(l) != "not found") {
				return true;
			}
		}
		return false;
	}
	
	private Boolean isWhileLoop(String input) {
		try {return input.substring(0, 5).equals("while") && input.charAt(input.length()-1) == ':';}
		catch(Exception e) {return false;}
	}
	
	private Boolean isIfStatement(String input) {
		try {return input.substring(0, 2).equals("if") && input.charAt(input.length()-1) == ':';}
		catch(Exception e) {return false;}
	}
	
	private Boolean isElifStatement(String input) {
		try {return input.substring(0, 4).equals("elif") && input.charAt(input.length()-1) == ':';}
		catch(Exception e) {return false;}
	}
	
	private Boolean isElseStatement(String input) {
		try {return input.substring(0, 4).equals("else") && input.charAt(input.length()-1) == ':';}
		catch(Exception e) {return false;}
	}
	
	private Boolean isFunctionStatement(String input) {
		try {return input.substring(0, 3).equals("def") && input.charAt(input.length()-1) == ':' && input.trim().charAt(input.length()-2) == ')';}
		catch(Exception e) {return false;}
	}
	
	
	
	
	
	// evaluations of lines --------------------------------------------------------------------------------------
	private String assignment(String line) { 
		String[] preOut;
		String output = "";
		String[] s = line.split("=", 2);
		String varName = s[0].strip();
		
		String gi = getIndex(varName);
		String writeIndex = "error";
		if(gi != "not found") {
			varName = tag + "_r";
			
			String internals = gi.substring(gi.indexOf("[") + 1, getMatchingBrackets(gi, gi.indexOf("[")));

			if(isEvaluatable(internals)){
				output += assignment(tag + "_i = " + internals) + "\n";
				writeIndex = tag + "_i";
			}
			else writeIndex = internals;
		}
		
		if(isEvaluatable(s[1])) {
			preOut = evaluate(s[1], 0);
			
			for(int i = 0; i < preOut.length; i++) {
				if(getOp(preOut[i]) != "not found") {
					String operand = getOp(preOut[i]);
					if(i+1 == preOut.length) {
						output += "op ";
						output += opNames.get(operand) + " ";
						output += varName + " ";
						String[] parts = preOut[i].split(addMeta(operand));
						output += parts[0].strip() + " ";
						output += parts[1].strip();
					}
					else {
						output += "op ";
						output += opNames.get(operand) + " ";
						output += tag + i + " ";
						String[] parts = preOut[i].split(addMeta(operand));
						output += parts[0].strip() + " ";
						output += parts[1].strip();
						output += "\n";
					}
				}
				
				else if(isBuiltFunc(preOut[i])) {
					if(i+1 == preOut.length) {
						String[] parts = preOut[i].split(" ");
						output += "op " + parts[0] + " ";
						output += varName + " ";
						for(int a = 1; a < parts.length; a++) output += parts[a] + " ";
						output += "\n";
					}
					else {
						String[] parts = preOut[i].split(" ");
						output += "op " + parts[0] + " ";
						output += tag + i + " ";
						for(int a = 1; a < parts.length; a++) output += parts[a] + " ";
						output += "\n";
					}
					
				}
				
				else if(getBoolEval(preOut[i]) != "not found") {
					String operand = getBoolEval(preOut[i]);
					if(i+1 == preOut.length) {
						output += "op ";
						output += opNames.get(operand) + " ";
						output += varName + " ";
						String[] parts = preOut[i].split(addMeta(operand));
						output += parts[0].strip() + " ";
						output += parts[1].strip();
					}
					else {
						output += "op ";
						output += opNames.get(operand) + " ";
						output += tag + i + " ";
						String[] parts = preOut[i].split(addMeta(operand));
						output += parts[0].strip() + " ";
						output += parts[1].strip();
						output += "\n";
					}
				}
				
				else if(preOut[i].substring(0, 5).equals("read ") && preOut[i].charAt(5) != '=') {
					if(i+1 == preOut.length) {
						output += "read ";
						output += varName + " ";
						output += preOut[i].split(" ")[1] + " ";
						output += preOut[i].split(" ")[2] + " ";
					}
					else {
						output += "read ";
						output += tag + i + " ";
						output += preOut[i].split(" ")[1] + " ";
						output += preOut[i].split(" ")[2] + " ";
						output += "\n";
					}
				}
			}
			if(gi != "not found") {
				output += "\nwrite " + varName + " " + gi.substring(0, gi.indexOf("[")) + " " + writeIndex;
			}
		}
		else {
			if(gi != "not found") {
				output = "write " + s[1].strip() + " " + gi.substring(0, gi.indexOf("[")) + " " + writeIndex;
			}
			else {
				output += "set " + varName + " ";
				output += s[1].strip();
			}
		}
		return output;
	}
	
	private String whileLoop(String input, String content, int index) {
		String logicEvaluator = compile(tag + "_w = " + input.substring(5, input.length()-1).trim(), index);
		int logicEvaluatorLines = (logicEvaluator.length() - logicEvaluator.replace("\n", "").length()) / "\n".length() + 1;
		String evaluatedContent = compile(content, index + logicEvaluatorLines + 1);
		int evaluatedContentLines = (evaluatedContent.length() - evaluatedContent.replace("\n", "").length()) / "\n".length() + 1;
		
		String out = logicEvaluator + "\n";
		out += "jump " + (index + logicEvaluatorLines + evaluatedContentLines + 2) + " equal " + tag + "_w false\n";
		out += evaluatedContent + "\n";
		out += "jump " + (index) + " always";
		
		return out;
	}
	
	private String ifStatement(String input, String content, int index) {
		String logicEvaluator = compile(tag + "_i = " + input.substring(2, input.length()-1).trim(), index);
		int logicEvaluatorLines = (logicEvaluator.length() - logicEvaluator.replace("\n", "").length()) / "\n".length() + 1;
		String evaluatedContent = compile(content, index + logicEvaluatorLines + 1);
		int evaluatedContentLines = (evaluatedContent.length() - evaluatedContent.replace("\n", "").length()) / "\n".length() + 1;
		
		String out = logicEvaluator + "\n";
		out += "jump " + (index + logicEvaluatorLines + evaluatedContentLines + 1) + " equal " + tag + "_i false\n";
		out += evaluatedContent;
		
		return out;
	}
	
	private String ifElifElseStatement(String[] lines, String[] contents, int index) {
		int len = lines.length;
		String[] logicEvaluator = new String[len];
		int[] logicEvaluatorLines = new int[len];
		String[] evaluatedContent = new String[len];
		int[] evaluatedContentLines = new int[len];

		int totalLogicEvaluatorLines = 0;
		int totalEvaluatedContentLines = 0;
		
		for(int i = 0; i < len; i++) {
			String line = lines[i];
			String content = contents[i];
			
			if(i < len - 1 || isElifStatement(line)) {
				if(i == 0) logicEvaluator[i] = compile(tag + "_i = " + line.substring(2, line.length()-1).trim(), index);
				else logicEvaluator[i] = compile(tag + "_i = " + line.substring(4, line.length()-1).trim(), index);
				logicEvaluatorLines[i] = (logicEvaluator[i].length() - logicEvaluator[i].replace("\n", "").length()) / "\n".length() + 1;
				evaluatedContent[i] = compile(content, index + logicEvaluatorLines[i] + 1 + totalLogicEvaluatorLines + totalEvaluatedContentLines);
				evaluatedContentLines[i] = (evaluatedContent[i].length() - evaluatedContent[i].replace("\n", "").length()) / "\n".length() + 1;
				
				totalLogicEvaluatorLines += logicEvaluatorLines[i] + 1;
				totalEvaluatedContentLines += evaluatedContentLines[i] + 1;
			}
			else {
				evaluatedContent[i] = compile(content, index + logicEvaluatorLines[i] + 1 + totalLogicEvaluatorLines + totalEvaluatedContentLines);
				evaluatedContentLines[i] = (evaluatedContent[i].length() - evaluatedContent[i].replace("\n", "").length()) / "\n".length() + 1;
				
				totalEvaluatedContentLines += evaluatedContentLines[i] + 1;
			}
		}
		
		int totalLen = totalLogicEvaluatorLines + totalEvaluatedContentLines;
		if(isElseStatement(lines[lines.length-1])) totalLen -= 1;
		String out = "";
		
		out += logicEvaluator[0] + "\n";
		out += "jump " + (index + logicEvaluatorLines[0] + evaluatedContentLines[0] + 2) + " equal " + tag + "_i false\n";
		out += evaluatedContent[0] + "\n";
		out += "jump " + (index + totalLen) + " always\n";
		
		int currentLen = logicEvaluatorLines[0] + evaluatedContentLines[0] + 2;
		
		for(int i = 1; i < len; i++) {
			if(i < len - 1 || isElifStatement(lines[i])) {
				out += logicEvaluator[i] + "\n";
				out += "jump " + (index + currentLen + logicEvaluatorLines[i] + evaluatedContentLines[i] + 2) + " equal " + tag + "_i false\n";
				out += evaluatedContent[i] + "\n";
				out += "jump " + (index + totalLen) + " always\n";
				
				currentLen += logicEvaluatorLines[i] + evaluatedContentLines[i] + 2;
			}
			else {
				out += evaluatedContent[i];
			}
		}
		
		return out.trim();
	}
	
	// evaluations of logic ----------------------------------------------------------------------------------------------
	private String[] evaluate(String statement, int stepper) {
		
		statement = statement.trim();
		ArrayList<String> output = new ArrayList<String>();

		int p = getLooseParentheseIndex(statement);

		if(getIndex(statement) != "not found") {
			p = statement.indexOf(getIndex(statement));
			int q = p + getIndex(statement).length();
			
			String left = statement.substring(0, p).trim();
			String right = statement.substring(q, statement.length());
			
			String inBrackets = statement.substring(statement.indexOf("[") + 1, q - 1);
			String bracketsEvaluated = inBrackets;
			
			if(isEvaluatable(inBrackets)) {
				for(String e : evaluate(inBrackets, stepper)) {
					output.add(e);
					stepper++;
				}
				bracketsEvaluated = tag + (stepper-1);
			}
			output.add("read " + getIndex(statement).substring(0, getIndex(statement).indexOf("[")) + " " + bracketsEvaluated);
				
			String leftSplit = "";
			String rightSplit = "";
			
			if(isEvaluatable(right)) rightSplit = right.substring(getEvalIndex(right));
			if(isEvaluatable(left)) leftSplit = left.substring(0, getLastEvalIndex(left));
			
			for(String e : evaluate((leftSplit + " " + tag + (stepper) + " " + rightSplit).trim(), stepper)) {
				output.add(e);
			}
		}
		
		else if(p != -1) {
			String left = statement.substring(0, p).trim();
			String right = statement.substring(getMatchingParenthese(statement, p), statement.length());
			
			String internals = statement.substring(p + 1, getMatchingParenthese(statement, p));

			for(String e : evaluate(internals, stepper)) {
				output.add(e);
				stepper++;
			}
			
			String leftSplit = "";
			String rightSplit = "";
			
			if(isEvaluatable(right)) rightSplit = right.substring(getEvalIndex(right));
			if(isEvaluatable(left)) leftSplit = left.substring(0, getLastEvalIndex(left));
			
			for(String e : evaluate((leftSplit + " " + tag + (stepper-1) + " " + rightSplit).trim(), stepper)) {
				output.add(e);
			}
		}
		
		else if(getBuiltFunc(statement) != "not found") {
			String func = getBuiltFunc(statement);
			int i = statement.indexOf(func);
			
			int end = getMatchingParenthese(statement, i + func.length());
			
			String left = statement.substring(0, i).trim();
			String right = statement.substring(end, statement.length());
			
			String internals = statement.substring(i + func.length() + 1, end);

			String leftSplit = "";
			String rightSplit = "";
			
			if(isEvaluatable(right)) rightSplit = right.substring(getEvalIndex(right));
			if(isEvaluatable(left)) leftSplit = left.substring(0, getLastEvalIndex(left));
			
			String o = evaluateInternals(internals, func, output, stepper);
			output.add(o);

			stepper++;
			for(String e : evaluate((leftSplit + " " + tag + (stepper-1) + " " + rightSplit).trim(), stepper)) {
				output.add(e);
			}
		}
		
		else if(getOp(statement) != "not found"){
			
			//evaluate operands
			String op = getOp(statement);
			int i = statement.indexOf(op);
			String left = statement.substring(0, i).trim();
			String right = statement.substring(i + op.length(), statement.length()).trim();
			
			String leftSplit = "";
			String rightSplit = "";
			
			if(isEvaluatable(right)) rightSplit = right.substring(getEvalIndex(right));
			if(isEvaluatable(left)) leftSplit = left.substring(0, getLastEvalIndex(left));
			
			output.add(statement.substring(leftSplit.length(), statement.length() - rightSplit.length()).trim());
			
			stepper++;
			for(String e : evaluate((leftSplit + " " + tag + (stepper-1) + " " + rightSplit).trim(), stepper)) {
				output.add(e);
			}
		}
		
		else if(getBoolEval(statement) != "not found") {
			
			String op = getBoolEval(statement);
			int i = statement.indexOf(op);
			String left = statement.substring(0, i).trim();
			String right = statement.substring(i + op.length(), statement.length()).trim();
			
			output.add(left + " " + op + " " + right);
		}
		
		return output.toArray(new String[output.size()]);
	}
	private String evaluateInternals(String internals, String func, ArrayList<String> output, int stepper) {
		String values[] = internals.split(",");
		String finalValues[] = new String[values.length];
		
		for(int v = 0; v < values.length; v++) {
			String value = values[v];
			if(isEvaluatable(value)) {
				String[] f = evaluate(value, stepper);
				for(String e : f){
					output.add(e);
					finalValues[v] = tag + stepper;
				}
				String o = func;
				for(String a : finalValues) o += " " + a;
				stepper++;
				return o;
			}
			else finalValues[v] = value;
		}
		String o = func;
		for(String value : finalValues) o += " " + value.trim();
		return o;
	}
	
	
	
	// custom functions ---------------------------------------------------------------------------------------------------
	private String getOp(String input) {
		String output = "not found";
		int i = input.length();
		int s = operands.length;
		for(int a = 0; a < operands.length; a++) {
			for(int b = 0; b < operands[a].length; b++) {
				String op = operands[a][b];
				if(input.contains(op)) {
					if(a < s) {
						output = op;
						i = input.indexOf(op);
						s = a;
					}
					else if(a == s && input.indexOf(op) < i) {
						output = op;
						i = input.indexOf(op);
						s = a;
					}
				}
			}
		}
		return output;
	}
	
	private String getBuiltFunc(String input) {
		String output = "not found";
		int i = input.length();
		for(int a = 0; a < builtFunctions.length; a++) {
			String op = builtFunctions[a];
			if(input.contains(op)) {
				Boolean execute = false;
				for(int x = input.indexOf(op) + op.length(); x < input.length(); x++) {
					if(input.charAt(x) == '(') {
						execute = true;
						break;
					}
					else if(input.charAt(x) != ' ') break;
				}
				if(execute) {
					if(a < i) {
						output = op;
						i = input.indexOf(op);
					}
				}
			}
		}
		return output;
	}
	
	private int getOpIndex(String input) {
		int i = input.length();
		for(String[] a : operands) for(String b : a) {
			if(input.contains(b)) {
				if(input.indexOf(b) < i) {
					i = input.indexOf(b);
				}
			}
		}
		return i;
	}
	
	private int getBuiltFuncIndex(String input) {
		int i = input.length();
		for(String a : builtFunctions) {
			if(input.contains(a)) {
				if(input.indexOf(a) < i) {
					i = input.indexOf(a);
				}
			}
		}
		return i;
	}
	
	private Boolean isBuiltFunc(String input) {
		for(String func : builtFunctions) {
			if(input.contains(func)) return true;
		}
		return false;
	}
	
	private String getBoolEval(String input) {
		int i = input.length();
		String out = "not found";
		for(String eval : evaluators) {
			int e = input.indexOf(eval);
			if(e != -1 && e < i) {
				out = eval;
				i = e;
			}
		}
		return out;
	}
	
	private int getLooseParentheseIndex(String input) {
		int i = input.indexOf('(');
		int j = getBuiltFuncIndex(input);
		int k = getBuiltFunc(input).length();
		
		if(j + k == i) {
			int x =  getLooseParentheseIndex(input.substring(i+1, input.length()));
			if(x == -1) return -1;
			else return x + i + 1;
		}
		else {
			return i;
		}
	}
	
	private int getMatchingParenthese(String input, int startIndex) {
		int count = 0;
		for(int i = startIndex+1; i < input.length(); i++) {
			if(input.charAt(i) == ')') {
				if(count == 0) return i;
				else count--;
			}
			if(input.charAt(i) == '(') {
				count++;
			}
		}
		return -1;
	}
	private int getMatchingBrackets(String input, int startIndex) {
		int count = 0;
		for(int i = startIndex+1; i < input.length(); i++) {
			if(input.charAt(i) == ']') {
				if(count == 0) return i;
				else count--;
			}
			if(input.charAt(i) == '[') {
				count++;
			}
		}
		return -1;
	}
	
	private Boolean isEvaluatable(String input) {
		if (getOp(input) == "not found" && getBuiltFunc(input) == "not found" && getBoolEval(input) == "not found" && input.contains("(") == false && getIndex(input) == "not found") {
			return false;
		}
		return true;
	}
	
	private int getEvalIndex(String input) {
		int i = getOpIndex(input);
		if(getBuiltFuncIndex(input) < i) i = getBuiltFuncIndex(input);
		if(getBoolEval(input) != "not found" && input.indexOf(getBoolEval(input)) < i) i = input.indexOf(getBoolEval(input));
		return i;
	}
	
	private int getLastEvalIndex(String input) {
		int i = -1;
		if(getLastOpIndex(input) > i) i = getLastOpIndex(input) + getLastOp(input).length();
		if(getLastBuiltFuncIndex(input) > i) i = getLastBuiltFuncIndex(input) + getLastBuiltFunc(input).length();
		if(input.indexOf(getLastBoolEval(input)) > i) i = input.indexOf(getLastBoolEval(input)) + getLastBoolEval(input).length();
		return i;
	}
	
	private String getLastOp(String input) {
		String output = "not found";
		int i = -1;
		int s = operands.length;
		for(int a = 0; a < operands.length; a++) {
			for(int b = 0; b < operands[a].length; b++) {
				String op = operands[a][b];
				if(input.contains(op)) {
					if(a < s) {
						output = op;
						i = input.lastIndexOf(op);
						s = a;
					}
					else if(a == s && input.lastIndexOf(op) > i) {
						output = op;
						i = input.lastIndexOf(op);
						s = a;
					}
				}
			}
		}
		return output;
	}
	
	private int getLastOpIndex(String input) {
		int i = -1;
		for(String[] a : operands) for(String b : a) {
			if(input.contains(b)) {
				if(input.lastIndexOf(b) > i) {
					i = input.lastIndexOf(b);
				}
			}
		}
		return i;
	}
	
	private String getLastBuiltFunc(String input) {
		String output = "not found";
		int i = -1;
		for(int a = 0; a < builtFunctions.length; a++) {
			String op = builtFunctions[a];
			if(input.contains(op)) {
				Boolean execute = false;
				for(int x = input.lastIndexOf(op) + op.length(); x < input.length(); x++) {
					if(input.charAt(x) == '(') {
						execute = true;
						break;
					}
					else if(input.charAt(x) != ' ') break;
				}
				if(execute) {
					if(a < i) {
						output = op;
						i = input.indexOf(op);
					}
				}
			}
		}
		return output;
	}
	
	private int getLastBuiltFuncIndex(String input) {
		int i = -1;
		for(String a : builtFunctions) {
			if(input.contains(a)) {
				if(input.lastIndexOf(a) < i) {
					i = input.lastIndexOf(a);
				}
			}
		}
		return i;
	}
	
	private String getLastBoolEval(String input) {
		int i = -1;
		String out = "not found";
		for(String eval : evaluators) {
			int e = input.lastIndexOf(eval);
			if(e != -1 && e > i) {
				out = eval;
				i = e;
			}
		}
		return out;
	}
	
	private String getIndex(String input) {
		int startBracketIndex = input.indexOf('[');
		if(startBracketIndex == -1) return "not found";
		
		int endIndex = getMatchingBrackets(input, startBracketIndex);
		int startIndex = 0;
		
		for(int i = startBracketIndex - 1; i >= 0; i--) {
			if(!isVariableChar(input.charAt(i))) {
				startIndex = i;
				break;
			}
		}
		
		return input.substring(startIndex, endIndex+1);
	}
	
	private int letterIndex(String input) {
		if(input.charAt(0) != ' ') return 0;
		for(int i = 0; i < input.length(); i++) {
			if(input.charAt(i) != ' ') return i;
		}
		return -1;
	}
	
	private String startVarChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_";
	private String varChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_123456789";
	private Boolean isVariableChar(char input) {
		return varChars.contains(String.valueOf(input));
	}
	
	private Boolean isVariableName(String input) {
		if(!startVarChars.contains(String.valueOf(input.charAt(0)))) return false;
		for(char c : input.toCharArray()) {
			if(!isVariableChar(c)) {
				return false;
			}
		}
		return true;
	}
	
	private String[] toStringArray(ArrayList<String> input) {
		String[] output = new String[input.size()];
		for(int i = 0; i < output.length; i++) {
			output[i] = input.get(i);
		}
		return output;
	}
	
	private String addMeta(String input) {
		for(String c : metaChars) {
			if(input.contains(c)) return "\\" + input;
		}
		return input;
	}
	
	private String getSaltString(int len) {
	    String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < len) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }
	
	
	
	
	//public static void main -----------------------------------------------------------------------------
	public static void main(String[] args) {
		
		Compiler Comp = new Compiler();
		String i = "";
		
		
		try {
			// File file = new File("2048game/2048control.txt");
			// File file = new File("2048game/2048display.txt");
			File file = new File("example.txt");
		    Scanner reader = new Scanner(file);
		    while (reader.hasNextLine()) {
		        String data = reader.nextLine();
		        i += data + "\n";
		    }
		    i = i.substring(0, i.length()-1);
		    reader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		
		System.out.println(i);
		System.out.println();
		
		long start = System.currentTimeMillis();
		
		String a = Comp.fullCompile(i);
		
		long end = System.currentTimeMillis();
		
		String[] lines = a.split("\n");
		int spaceNeeded = Integer.toString(lines.length).length();
		for(int j = 0; j < lines.length; j++) {
			System.out.print(j);
			for(int k = 0; k < spaceNeeded - Integer.toString(j).length(); k++) {
				System.out.print(" ");
			}
			System.out.println(" " + lines[j]);
		}
		
		System.out.println(lines.length + " end\n\n");
		
		System.out.println(start + " - " + end);
		System.out.println("comp time:" + (end - start));
		
		java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(a + "\nend"), null);
	}
}
