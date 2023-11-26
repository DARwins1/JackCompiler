// Dylan Reed
// Project #10/11
// 3650.04
// 11/25/2023

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class JackCompiler {

	public static void main(String[] args) {
		String userInput;
		if (args.length != 0) {
			userInput = args[0];
		} else {
			// Get input from the user
			Scanner keyboardReader = new Scanner(System.in);
			System.out.print("Name of Jack file or folder: ");
			userInput = keyboardReader.nextLine();
			keyboardReader.close();
		}

		File target = new File(System.getProperty("user.dir") + "\\" + userInput);
		File[] files;

		if (target.isFile()) {
			// User gave a single file
			files = new File[] { target };
		} else {
			// User gave a folder
			// Get every file that ends with ".jack"
			files = target.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".jack");
				}
			});
		}

		if (files == null) {
			// User-specified file(s) could not be found
			System.out.println("Error: '" + userInput + "' could not be found!");
			System.out.println();
			System.exit(0);
		}

		// Loop through each file...
		for (int i = 0; i < files.length; i++) {
			String fileName = files[i].getPath().substring(0, files[i].getPath().indexOf('.'));
			String tokenFileName = fileName + "T.xml";
			String analyzedFileName = fileName + ".xml";
			String compiledFileName = fileName + ".vm";
			
			// TOKENIZATION
			try {
				// Create a JackTokenizer for this file
				Scanner fileReader = new Scanner(files[i]);
				JackTokenizer tokenizer = new JackTokenizer(fileReader);

				// Make a new tokenized XML file
				try {
					// NOTE: If a file with this name exists, it will be overwritten!
					PrintWriter fileWriter = new PrintWriter(new FileWriter(tokenFileName));

					// Now we can start tokenizing the Jack code
					fileWriter.println("<tokens>");
					while (tokenizer.hasMoreTokens()) {
						tokenizer.advance(); // Get the next token

						if (tokenizer.token().isBlank()) {
							break;
						}

						// Print the token
						fileWriter.println("<" + tokenizer.tokenType() + "> " + tokenizer.token() + " </"
								+ tokenizer.tokenType() + ">");
					}
					fileWriter.println("</tokens>");
					System.out.println("File '" + files[i].getName() + "' has been tokenized!");

					fileWriter.close();
				} catch (IOException e) {
					System.out.println("Error: '" + tokenFileName + "' could not be written!");
				}
				tokenizer.close();
			} catch (FileNotFoundException e) {
				// User-specified file could not be found
				System.out.println("Error: '" + files[i] + "' could not be found!");
				System.out.println();
				System.exit(0);
			}
			
			// COMPILATION
			try {
				// Create a CompilationEngine for this file
				Scanner fileReader = new Scanner(new File(tokenFileName));

				// Make a new XML file
				try {
					// NOTE: If a file with this name exists, it will be overwritten!
					PrintWriter xmlWriter = new PrintWriter(new FileWriter(analyzedFileName));
					
					// Make a new VM file
					try {
						// NOTE: If a file with this name exists, it will be overwritten!
						PrintWriter vmWriter = new PrintWriter(new FileWriter(compiledFileName));
						
						// Make a new VM file
						
						CompilationEngine compiler = new CompilationEngine(fileReader, xmlWriter, vmWriter);

						// Let the CompilationEngine do its thing
						compiler.compileClass();
						
						System.out.println("File '" + files[i].getName() + "' has been compiled!");

						compiler.close();
					} catch (IOException e) {
						System.out.println("Error: '" + compiledFileName + "' could not be written!");
					}
				} catch (IOException e) {
					System.out.println("Error: '" + analyzedFileName + "' could not be written!");
				}
			} catch (FileNotFoundException e) {
				// User-specified file could not be found
				System.out.println("Error: '" + tokenFileName + "' could not be found!");
				System.out.println();
				System.exit(0);
			}
		}
	}
}
