// Dylan Reed
// Project #10/11
// 3650.04
// 11/25/2023

import java.io.PrintWriter;

public class VMWriter {
	private PrintWriter fileWriter;

	public VMWriter(PrintWriter fileWriter) {
		this.fileWriter = fileWriter;
	}

	public void writePush(String segment, int index) {
		fileWriter.println("push " + segment + " " + index);
	}

	public void writePop(String segment, int index) {
		fileWriter.println("pop " + segment + " " + index);
	}

	public void writeArithmetic(String command) {
		String operation = "";
		switch (command) {
		case "+":
			operation = "add";
			break;
		case "-":
			operation = "sub";
			break;
		case "*":
			writeCall("Math.multiply", 2);
			return;
		case "/":
			writeCall("Math.divide", 2);
			return;
		case "&amp;":
			operation = "and";
			break;
		case "|":
			operation = "or";
			break;
		case "&lt;":
			operation = "lt";
			break;
		case "&gt;":
			operation = "gt";
			break;
		case "=":
			operation = "eq";
			break;
		case "~":
			operation = "not";
			break;
		case "neg":
			operation = "neg";
			break;
		}

		if (!operation.isBlank()) {
			fileWriter.println(operation);
		}
	}

	public void writeLabel(String label) {
		fileWriter.println("label " + label);
	}

	public void writeGoto(String label) {
		fileWriter.println("goto " + label);
	}

	public void writeIf(String label) {
		fileWriter.println("if-goto " + label);
	}

	public void writeCall(String name, int nArgs) {
		fileWriter.println("call " + name + " " + nArgs);
	}

	public void writeFunction(String name, int nLocals) {
		fileWriter.println("function " + name + " " + nLocals);
	}

	public void writeReturn() {
		fileWriter.println("return");
	}

	public void close() {
		fileWriter.close();
	}
}
