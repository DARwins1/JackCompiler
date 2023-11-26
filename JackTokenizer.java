// Dylan Reed
// Project #10/11
// 3650.04
// 11/25/2023

import java.util.Scanner;

public class JackTokenizer {
	private Scanner lineReader;
	private String currentLine;
	private String currentToken;

	public JackTokenizer(Scanner fileReader) {
		lineReader = fileReader;
	}
	
	public void close() {
		lineReader.close();
	}

	public boolean hasMoreTokens() {
		return (lineReader.hasNextLine() || currentLine == null || currentLine.length() > 0);
	}

	public void advance() {
		// Get the next non-comment, non-blank line
		if (currentLine == null || currentLine.length() == 0) {
			// No more tokens on this line, go fetch the next one
			boolean multiLineComment = false;
			do {
				currentLine = lineReader.nextLine();

				// If there's a comment on this line, try to prune it off
				if (currentLine.indexOf("//") >= 0) {
					currentLine = currentLine.substring(0, currentLine.indexOf("//"));
				}
				if (currentLine.indexOf("/*") >= 0) {
					multiLineComment = true;
					currentLine = currentLine.substring(0, currentLine.indexOf("/*"));
				}
				if (multiLineComment && currentLine.indexOf("*") >= 0) {
					multiLineComment = true;
					if (currentLine.indexOf("*/") >= 0) {
						multiLineComment = false;
					}
					currentLine = currentLine.substring(0, currentLine.indexOf("*"));
				}
				currentLine = currentLine.trim();
			} while (currentLine.length() == 0 && lineReader.hasNextLine());
		}

		if (currentLine.isBlank()) {
			// Handles the edge case where there's a bunch of newlines at the end of the
			// file
			currentToken = "";
			return;
		}

		// Get the next token from this line
		int endIndex;
		if (currentLine.charAt(0) == '"') {
			// String constant! Group everything up to the next " as one token.
			endIndex = currentLine.substring(1).indexOf('"') + 2; // Incremented because the substring starts at 1
			currentToken = currentLine.substring(0, endIndex);
		} else {
			endIndex = currentLine.indexOf(" ");
			if (endIndex > 0) {
				currentToken = currentLine.substring(0, endIndex);
			} else {
				currentToken = currentLine;
			}

			// Check if we can break the token into smaller pieces
			if (isSymbol("" + currentToken.charAt(0))) {
				// The first character is a token (e.g. ".print")
				currentToken = currentToken.substring(0, 1);
				endIndex = 1;
			} else {
				// Check if there are tokens within a larger one (e.g. "main()")
				for (int i = 1; i < currentToken.length(); i++) {
					if (isSymbol("" + currentToken.charAt(i))) {
						// Found a symbol within the token
						currentToken = currentToken.substring(0, i);
						endIndex = i;
						break;
					}
				}
			}
		}

		if (endIndex >= 0 && endIndex < currentLine.length()) {
			currentLine = currentLine.substring(endIndex).trim();
		} else {
			currentLine = "";
		}
	}

	// KEYWORD
	// SYMBOL
	// IDENTIFIER
	// INT_CONST
	// STRING_CONST
	public String tokenType() {
		if (currentToken.contains("\"")) {
			return "stringConstant";
		} else if (isKeyword(currentToken)) {
			return "keyword";
		} else if (isSymbol(currentToken)) {
			return "symbol";
		} else if (isIntegerConstant(currentToken)) {
			return "integerConstant";
		} else {
			return "identifier";
		}
	}

	private boolean isKeyword(String input) {
		return (input.equals("class") || input.equals("constructor") || input.equals("function")
				|| input.equals("method") || input.equals("field") || input.equals("static") || input.equals("var")
				|| input.equals("int") || input.equals("char") || input.equals("boolean") || input.equals("void")
				|| input.equals("true") || input.equals("false") || input.equals("null") || input.equals("this")
				|| input.equals("let") || input.equals("do") || input.equals("if") || input.equals("else")
				|| input.equals("while") || input.equals("return"));
	}

	private boolean isSymbol(String input) {
		return (input.equals("{") || input.equals("}") || input.equals("(") || input.equals(")") || input.equals("[")
				|| input.equals("]") || input.equals(".") || input.equals(",") || input.equals(";") || input.equals("+")
				|| input.equals("-") || input.equals("*") || input.equals("/") || input.equals("&") || input.equals("|")
				|| input.equals("<") || input.equals(">") || input.equals("=") || input.equals("~"));
	}

	private boolean isIntegerConstant(String input) {
		try {
			Integer.parseInt(input);
			return true; // If the above line succeeds, this is an integer
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public String token() {
		// Special cases
		if (currentToken.equals("<")) {
			return "&lt;";
		}
		if (currentToken.equals(">")) {
			return "&gt;";
		}
		if (currentToken.equals("&")) {
			return "&amp;";
		}
		if (tokenType().equals("stringConstant")) {
			// Omit the starting and ending quotes
			return currentToken.substring(1, currentToken.length() - 1);
		}

		return currentToken;
	}

	public String keyWord() {
		return currentToken;
	}

	public String symbol() {
		return currentToken;
	}

	public String identifier() {
		return currentToken;
	}

	public String intVal() {
		return currentToken;
	}

	public String stringVal() {
		return currentToken;
	}
}
