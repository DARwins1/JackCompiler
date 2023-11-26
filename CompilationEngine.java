// Dylan Reed
// Project #10/11
// 3650.04
// 11/25/2023

import java.io.PrintWriter;
import java.util.Scanner;

public class CompilationEngine {
	// Used for analyzing
	private Scanner xmlInput;
	private PrintWriter xmlOutput;
	private String currentLine;
	private String tokenType;
	private String token;

	// Used for compiling
	private boolean outputVM;
	private SymbolTable sTable;
	private VMWriter vmWriter;
	private String className;
	private int whileLabelIndex;
	private int ifLabelIndex;
	private String subRoutineType;
	private int numClassFields;

	public CompilationEngine(Scanner xmlInput, PrintWriter xmlOutput, PrintWriter vmOutput) {
		this.xmlInput = xmlInput;
		this.xmlOutput = xmlOutput;
		sTable = new SymbolTable();
		vmWriter = new VMWriter(vmOutput);
		outputVM = true;
		whileLabelIndex = 0;
		ifLabelIndex = 0;
		numClassFields = 0;
		advanceTReader();
	}

	public void close() {
		xmlInput.close();
		xmlOutput.close();
		if (outputVM) {
			vmWriter.close();
		}
	}

	private void advanceTReader() {
		currentLine = xmlInput.nextLine();
		tokenType = currentLine.substring(1, currentLine.indexOf('>'));
		if (currentLine.indexOf(' ') > 0) {
			token = currentLine.substring(currentLine.indexOf(' ') + 1, currentLine.lastIndexOf(' '));
		} else {
			token = "";
		}
	}

	private void printlnA() {
		xmlOutput.println(currentLine);
		advanceTReader();
	}

	// Push a variable's value onto the stack
	private void writePushVariable(String varName) {
		if (!outputVM) {
			return;
		} else {
			String kind = sTable.kindOf(varName);
			int index = sTable.indexOf(varName);

			String segment = kind;
			if (segment.equals("var")) {
				segment = "local";
			} else if (segment.equals("field")) {
				segment = "this";
			}

			vmWriter.writePush(segment, index);
		}
	}

	// Set a variable to be a value popped from the stack
	private void writeSetVariable(String varName) {
		if (!outputVM) {
			return;
		} else {
			String kind = sTable.kindOf(varName);
			int index = sTable.indexOf(varName);

			String segment = kind;
			if (segment.equals("var")) {
				segment = "local";
			} else if (segment.equals("field")) {
				segment = "this";
			}

			vmWriter.writePop(segment, index);
		}
	}

	public void compileClass() {
		// Go to the start of the class
		while (token.isBlank() || !token.equals("class")) {
			advanceTReader();
		}

		xmlOutput.println("<class>");

		printlnA(); // `class`
		className = token; // Store className
		printlnA(); // className
		printlnA(); // '{'

		while (!tokenType.equals("symbol")) {
			if (token.equals("field") || token.equals("static")) {
				numClassFields += compileClassVarDec(); // classVarDec*
			} else if (token.equals("constructor") || token.equals("function") || token.equals("method")) {
				compileSubroutine(); // subroutineDec*
			}
		}
		printlnA(); // '}'

		xmlOutput.println("</class>");
	}

	public int compileClassVarDec() {
		xmlOutput.println("<classVarDec>");

		String varName;
		int numFields = 0;

		String kind = token;
		printlnA(); // ('static' | 'field')
		String type = token;
		printlnA(); // type
		varName = token;
		printlnA(); // varName
		sTable.define(varName, type, kind);
		if (kind.equals("field")) {
			numFields++;
		}
		while (token.equals(",")) {
			printlnA(); // ','
			varName = token;
			printlnA(); // varName
			sTable.define(varName, type, kind);
			if (kind.equals("field")) {
				numFields++;
			}
		}
		printlnA(); // ';'

		xmlOutput.println("</classVarDec>");

		return numFields;
	}

	public void compileSubroutine() {
		xmlOutput.println("<subroutineDec>");

		sTable.startSubroutine();

		String kind = token;
		printlnA(); // ('constructor' | 'function' | 'method')
		subRoutineType = token;
		printlnA(); // ('void' | type)
		String subroutineName = token;
		printlnA(); // subroutineName
		printlnA(); // '('
		if (kind.equals("method")) {
			// Make sure 'this' is set as argument 0
			sTable.define("this", className, "argument");
		}
		compileParameterList(); // parameterList
		printlnA(); // ')'

		xmlOutput.println("<subroutineBody>");

		printlnA(); // '{'
		int numLocals = 0;
		while (token.equals("var")) {
			numLocals += compileVarDec(); // varDec*
		}

		if (kind.equals("method")) {
			vmWriter.writeFunction(className + "." + subroutineName, numLocals);
			// Set pointer 0 to point to 'this'
			writePushVariable("this");
			vmWriter.writePop("pointer", 0);
		} else if (kind.equals("constructor")) {
			vmWriter.writeFunction(className + "." + subroutineName, numLocals);
			vmWriter.writePush("constant", numClassFields);
			vmWriter.writeCall("Memory.alloc", 1); // Allocate space for the new object
			vmWriter.writePop("pointer", 0); // Assign the object to "this"
			sTable.define(className, className, "class");
		} else { // function
			vmWriter.writeFunction(className + "." + subroutineName, numLocals);
		}

		compileStatements(); // statements
		printlnA(); // '}'

		xmlOutput.println("</subroutineBody>");

		xmlOutput.println("</subroutineDec>");
	}

	public void compileParameterList() {
		xmlOutput.println("<parameterList>");

		String type;
		String varName;

		if (tokenType.equals("keyword") || tokenType.equals("identifier")) {
			type = token;
			printlnA(); // type
			varName = token;
			printlnA(); // varName
			sTable.define(varName, type, "argument");
			while (token.equals(",")) {
				printlnA(); // ','
				type = token;
				printlnA(); // type
				varName = token;
				printlnA(); // varName
				sTable.define(varName, type, "argument");
			}
		}

		xmlOutput.println("</parameterList>");
	}

	public int compileVarDec() {
		xmlOutput.println("<varDec>");

		int numLocals = 0;
		String varName;

		printlnA(); // 'var'
		String type = token;
		printlnA(); // type
		varName = token;
		printlnA(); // varName
		sTable.define(varName, type, "var");
		numLocals++;
		while (token.equals(",")) {
			printlnA(); // ','
			varName = token;
			printlnA(); // varName
			sTable.define(varName, type, "var");
			numLocals++;
		}
		printlnA(); // ';'

		xmlOutput.println("</varDec>");

		return numLocals;
	}

	public void compileStatements() {
		xmlOutput.println("<statements>");

		while (tokenType.equals("keyword")) {
			switch (token) {
			case "do":
				compileDo(); // doStatement
				break;
			case "let":
				compileLet(); // letStatement
				break;
			case "while":
				compileWhile(); // whileStatement
				break;
			case "return":
				compileReturn(); // returnStatement
				break;
			case "if":
				compileIf(); // ifStatement
				break;
			}
		}

		xmlOutput.println("</statements>");
	}

	public void compileDo() {
		xmlOutput.println("<doStatement>");

		printlnA(); // 'do'
		String subRoutineName = token;
		printlnA(); // (subRoutineName | className | varName)
		while (token.equals(".")) {
			printlnA(); // '.'
			subRoutineName = subRoutineName + "." + token;
			printlnA(); // (subRoutineName | varName)
		}
		printlnA(); // '('

		int numArgs = 0;

		if (subRoutineName.indexOf('.') < 0) {
			// If no dot, assume the subroutine belongs to the current class
			subRoutineName = className + "." + subRoutineName;
		}

		if (sTable.isDefined(subRoutineName.substring(0, subRoutineName.indexOf('.')))
				&& !subRoutineName.substring(subRoutineName.lastIndexOf('.') + 1).equals("new")) {
			// If we're calling a (non-constructor) method of an object, pass in that object
			// HACK: This assumes all constructors are named "new()"!
			String objectName = subRoutineName.substring(0, subRoutineName.indexOf('.'));
			// Swap the variable name of the object with its class name
			subRoutineName = sTable.typeOf(objectName) + subRoutineName.substring(subRoutineName.indexOf('.'));

			if (objectName.equals(className)) {
				vmWriter.writePush("pointer", 0); // Push "this" onto the stack
			} else {
				writePushVariable(objectName); // Push the object onto the stack
			}

			numArgs++; // Increment the amount of arguments
		}

		numArgs += compileExpressionList(); // expressionList
		printlnA(); // ')'
		printlnA(); // ';'

		vmWriter.writeCall(subRoutineName, numArgs);
		vmWriter.writePop("temp", 0); // Pop the return value off of the stack (we don't do anything with it)

		xmlOutput.println("</doStatement>");
	}

	public void compileLet() {
		xmlOutput.println("<letStatement>");

		boolean isArray = false;

		printlnA(); // 'let'
		String varName = token;
		printlnA(); // varName
		if (token.equals("[")) {
			isArray = true;
			printlnA(); // '['
			compileExpression(); // expression
			writePushVariable(varName);
			vmWriter.writeArithmetic("+"); // Calculate array entry location
			printlnA(); // ']'
		}
		printlnA(); // '='
		compileExpression(); // expression
		printlnA(); // ';'

		if (!isArray) {
			writeSetVariable(varName);
		} else {
			// Set this value into the array entry
			vmWriter.writePop("temp", 0); // Hold onto value
			vmWriter.writePop("pointer", 1); // Get the array entry location
			vmWriter.writePush("temp", 0); // Push the value back onto the stack
			vmWriter.writePop("that", 0); // Place the value into the array entry
		}

		xmlOutput.println("</letStatement>");
	}

	public void compileWhile() {
		xmlOutput.println("<whileStatement>");

		String loopLabel = "WHILE_EXP" + whileLabelIndex;
		String endLabel = "WHILE_END" + whileLabelIndex++;
		vmWriter.writeLabel(loopLabel);

		printlnA(); // 'while'
		printlnA(); // '('
		compileExpression(); // expression
		printlnA(); // ')'
		vmWriter.writeArithmetic("~"); // Not the output of the expression...
		vmWriter.writeIf(endLabel); // And end the loop if true
		printlnA(); // '{'
		compileStatements(); // statements
		printlnA(); // '}'

		vmWriter.writeGoto(loopLabel);
		vmWriter.writeLabel(endLabel);

		xmlOutput.println("</whileStatement>");
	}

	public void compileReturn() {
		xmlOutput.println("<returnStatement>");

		printlnA(); // 'return'
		if (!token.equals(";")) {
			compileExpression(); // expression
		}
		printlnA(); // ';'

		if (subRoutineType.equals("void")) {
			// If void type, set the stack's return value as 0
			vmWriter.writePush("constant", 0);
		}

		vmWriter.writeReturn();

		xmlOutput.println("</returnStatement>");
	}

	public void compileIf() {
		xmlOutput.println("<ifStatement>");

		String ifLabel = "IF_TRUE" + ifLabelIndex;
		String elseLabel = "IF_FALSE" + ifLabelIndex;
		String endLabel = "IF_END" + ifLabelIndex++;
		boolean hasElse = false;

		printlnA(); // 'if'
		printlnA(); // '('
		compileExpression(); // expression
		printlnA(); // ')'
		vmWriter.writeIf(ifLabel); // Go to the else block if true
		vmWriter.writeGoto(elseLabel); // Otherwise go to the else block
		printlnA(); // '{'
		vmWriter.writeLabel(ifLabel);
		compileStatements(); // statements
		printlnA(); // '}'
		if (token.equals("else")) {
			hasElse = true;
			vmWriter.writeGoto(endLabel);
			printlnA(); // 'else'
			printlnA(); // '{'
			vmWriter.writeLabel(elseLabel);
			compileStatements(); // statements
			printlnA(); // '}'
		}

		if (!hasElse) {
			vmWriter.writeLabel(elseLabel);
		} else {
			vmWriter.writeLabel(endLabel);
		}

		xmlOutput.println("</ifStatement>");
	}

	public void compileExpression() {
		xmlOutput.println("<expression>");
		compileTerm(); // term
		while (isOp(token)) {
			String operation = token;
			printlnA(); // op
			compileTerm(); // term
			vmWriter.writeArithmetic(operation);
		}
		xmlOutput.println("</expression>");
	}

	private boolean isOp(String input) {
		return (input.equals("+") || input.equals("-") || input.equals("*") || input.equals("/")
				|| input.equals("&amp;") || input.equals("|") || input.equals("&gt;") || input.equals("&lt;")
				|| input.equals("="));
	}

	public void compileTerm() {
		xmlOutput.println("<term>");
		if (tokenType.equals("integerConstant")) {
			vmWriter.writePush("constant", Integer.parseInt(token)); // Push the constant onto the stack
			printlnA(); // integerConstant
		} else if (tokenType.equals("stringConstant")) {
			vmWriter.writePush("constant", token.length());
			vmWriter.writeCall("String.new", 1); // Create a new string
			for (int i = 0; i < token.length(); i++) {
				// Populate the string one char at a time
				vmWriter.writePush("constant", (int) token.charAt(i));
				vmWriter.writeCall("String.appendChar", 2);
			}
			printlnA(); // stringConstant
		} else if (token.equals("~")) {
			// Variable logical not (e.g. ~x)
			printlnA(); // '~'
			if (token.equals("(")) {
				printlnA(); // '('
				compileExpression(); // expression (e.g. ~(x > 3))
				printlnA(); // ')'
			} else {
				compileTerm(); // single term (e.g. ~x)
			}

			vmWriter.writeArithmetic("~"); // Not the value of whatever the next term is
		} else if (token.equals("-")) {
			// Variable negation (e.g. -x)
			printlnA(); // '-'
			compileTerm();
			vmWriter.writeArithmetic("neg"); // Negate the value of whatever the next term is
		} else if (tokenType.equals("keyword")) {
			if (token.equals("true") || token.equals("false")) {
				// True/False
				vmWriter.writePush("constant", 0); // Value for False
				if (token.equals("true")) {
					vmWriter.writeArithmetic("~"); // Not to True
				}
				printlnA(); // 'true' | 'false'
			} else if (token.equals("this")) {
				vmWriter.writePush("pointer", 0); // Get "this"
				printlnA(); // 'this'
			} else if (token.equals("null")) {
				vmWriter.writePush("constant", 0); // Get null
				printlnA(); // 'this'
			}
		} else if (isOp(token)) {
			printlnA(); // unaryOp
			compileTerm(); // term
		} else if (token.equals("(")) {
			printlnA(); // '('
			compileExpression(); // expression
			printlnA(); // ')'
		} else if (tokenType.equals("identifier")) {
			String prevToken = token;
			printlnA(); // (varName | subroutineName | className)
			if (token.equals("[")) {
				printlnA(); // `[`
				compileExpression();
				writePushVariable(prevToken);
				vmWriter.writeArithmetic("+");
				printlnA(); // `]`
				vmWriter.writePop("pointer", 1); // Get the array entry location
				vmWriter.writePush("that", 0); // Push the entry's value
			} else if (token.equals("(")) {
				printlnA(); // `(`
				compileExpressionList(); // expressionList
				printlnA(); // `)`
			} else if (token.equals(".")) {
				String subRoutineName = prevToken;
				while (token.equals(".")) {
					printlnA(); // '.'
					subRoutineName = subRoutineName + "." + token;
					printlnA(); // (subRoutineName | varName)
				}
				printlnA(); // '('

				int numArgs = 0;

				if (subRoutineName.indexOf('.') < 0) {
					// If no dot, assume the subroutine belongs to the current class
					subRoutineName = className + "." + subRoutineName;
				}

				if (sTable.isDefined(subRoutineName.substring(0, subRoutineName.indexOf('.')))
						&& !subRoutineName.substring(subRoutineName.lastIndexOf('.') + 1).equals("new")) {
					// If we're calling a (non-constructor) method of an object, pass in that object
					// HACK: This assumes all constructors are named "new()"!
					String objectName = subRoutineName.substring(0, subRoutineName.indexOf('.'));
					// Swap the variable name of the object with its class name
					subRoutineName = sTable.typeOf(objectName) + subRoutineName.substring(subRoutineName.indexOf('.'));

					if (objectName.equals(className)) {
						vmWriter.writePush("pointer", 0); // Push "this" onto the stack
					} else {
						writePushVariable(objectName); // Push the object onto the stack
					}

					numArgs++; // Increment the amount of arguments
				}

				numArgs += compileExpressionList(); // expressionList
				printlnA(); // ')'

				vmWriter.writeCall(subRoutineName, numArgs);
			} else {
				// Simple variable
				writePushVariable(prevToken); // Push the variable's value onto the stack
			}
		}
		xmlOutput.println("</term>");
	}

	public int compileExpressionList() {
		xmlOutput.println("<expressionList>");

		int numExpressions = 0;

		if (!token.equals(")")) {
			compileExpression();
			numExpressions++;
			while (token.equals(",")) {
				printlnA(); // ','
				compileExpression();
				numExpressions++;
			}
		}
		xmlOutput.println("</expressionList>");

		return numExpressions;
	}
}
