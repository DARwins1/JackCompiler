// Dylan Reed
// Project #10/11
// 3650.04
// 11/25/2023

import java.util.HashMap;

public class SymbolTable {
	private HashMap<String, SymbolInfo> classTable;
	private HashMap<String, SymbolInfo> subroutineTable;
	private int staticIndex;
	private int fieldIndex;
	private int argIndex;
	private int varIndex;
	
	public SymbolTable() {
		classTable = new HashMap<String, SymbolInfo>();
		startSubroutine();
		staticIndex = 0;
		fieldIndex = 0;
	}
	
	public void startSubroutine() {
		subroutineTable = new HashMap<String, SymbolInfo>();
		argIndex = 0;
		varIndex = 0;
	}
	
	public void define(String name, String type, String kind) {
		if (kind.equals("static")) {
			classTable.put(name, new SymbolInfo(type, kind, staticIndex));
			staticIndex++;
		} else if (kind.equals("field")) {
			classTable.put(name, new SymbolInfo(type, kind, fieldIndex));
			fieldIndex++;
		} else if (kind.equals("argument")) {
			subroutineTable.put(name, new SymbolInfo(type, kind, argIndex));
			argIndex++;
		} else if (kind.equals("var")) {
			subroutineTable.put(name, new SymbolInfo(type, kind, varIndex));
			varIndex++;
		} else {
			classTable.put(name, new SymbolInfo(type, kind, 0));
		}
	}
	
	public int varCount(String kind) {
		if (kind.equals("static")) {
			return staticIndex;
		} else if (kind.equals("field")) {
			return fieldIndex;
		} else if (kind.equals("argument")) {
			return argIndex;
		} else if (kind.equals("var")) {
			return varIndex;
		} else {
			return 0;
		}
	}
	
	public String kindOf(String name) {
		return getSymbolInfo(name).kind;
	}
	
	public String typeOf(String name) {
		return getSymbolInfo(name).type;
	}
	
	public int indexOf(String name) {
		return getSymbolInfo(name).number;
	}
	
	public boolean isDefined(String name) {
		return (getSymbolInfo(name) != null);
	}
	
	private SymbolInfo getSymbolInfo(String name) {
		SymbolInfo symbol = subroutineTable.get(name);
		if (symbol == null) {
			symbol = classTable.get(name);
		}
		return symbol;
	}
	
	// Holds the type, kind, and # of the symbol
	private class SymbolInfo {
		private String type;
		private String kind;
		private int number;
		
		public SymbolInfo(String type, String kind, int number) {
			this.type = type;
			this.kind = kind;
			this.number = number;
		}
	}
}
