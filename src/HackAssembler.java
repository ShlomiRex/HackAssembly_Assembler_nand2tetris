import javafx.util.Pair;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;

public class HackAssembler {

	final TripletString[] instruction_table = new TripletString[] { new TripletString("0", "", "101010"),
			new TripletString("1", "", "111111"), new TripletString("-1", "", "111010"),
			new TripletString("D", "", "001100"), new TripletString("A", "M", "110000"),
			new TripletString("!D", "", "001101"), new TripletString("!A", "!M", "110001"),
			new TripletString("-D", "", "001111"), new TripletString("-A", "-M", "110011"),
			new TripletString("D+1", "", "011111"), new TripletString("A+1", "M+1", "110111"),
			new TripletString("D-1", "", "001110"), new TripletString("A-1", "M-1", "110010"),
			new TripletString("D+A", "D+M", "000010"), new TripletString("D-A", "D-M", "010011"),
			new TripletString("A-D", "M-D", "000111"), new TripletString("D&A", "D&M", "000000"),
			new TripletString("D|A", "D|M", "010101") };

	final PairString[] destination_table = new PairString[] { new PairString("M", "001"), new PairString("D", "010"),
			new PairString("MD", "011"), new PairString("A", "100"), new PairString("AM", "101"),
			new PairString("AD", "110"), new PairString("AMD", "111") };

	final PairString[] jump_table = new PairString[] {
			new PairString("JGT", "001"),
			new PairString("JEQ", "010"),
			new PairString("JGE", "011"),
			new PairString("JLT", "100"),
			new PairString("JNE", "101"),
			new PairString("JLE", "110"),
			new PairString("JMP", "111") };

	final Pair<String, Integer>[] symbol_table = new Pair[] { new Pair("@R0", 0), new Pair("@R1", 1),
			new Pair("@R2", 2), new Pair("@R3", 3), new Pair("@R4", 4), new Pair("@R5", 5), new Pair("@R6", 6),
			new Pair("@R7", 7), new Pair("@R8", 8), new Pair("@R9", 9), new Pair("@R10", 10), new Pair("@R11", 11),
			new Pair("@R12", 12), new Pair("@R13", 13), new Pair("@R14", 14), new Pair("@R15", 15), new Pair("@SP", 0),
			new Pair("@LCL", 1), new Pair("@ARG", 2), new Pair("@THIS", 3), new Pair("@THAT", 4),
			new Pair("@SCREEN", 16384), new Pair("@KBD", 24576), };

	ArrayList<Pair<Integer, String>> labels_table = new ArrayList<>();
	ArrayList<Pair<String, Integer>> user_symbols = new ArrayList<>();

	int PC = 0;

	public HackAssembler(File file) throws IOException {
		File output = new File("output.txt");
		PrintWriter out = new PrintWriter(output);
		File output_clean = new File("output_clean.txt");
		PrintWriter out2 = new PrintWriter(output_clean);

		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		String line;

		// If label line, skip PC by 1.
		boolean label_line = false;
		// Search for labels
		while ((line = bufferedReader.readLine()) != null) {
			if (line.isEmpty() || line.startsWith("//"))
				continue;
			if (line.contains("//")) {
				int index = line.indexOf("//");
				line = line.substring(0, index);
			}
			line = line.trim();
			line.replaceAll(" ", "");

			label_line = false;
			if (line.startsWith("(") && line.endsWith(")")) {
				line = line.substring(1, line.length() - 1);
				labels_table.add(new Pair<>(PC, line));
				label_line = true;
			}

			if (label_line == false)
				PC++;
		}

		bufferedReader.close();
		PC = 0;
		bufferedReader = new BufferedReader(new FileReader(file));

		// Search for user saved symbol
		while ((line = bufferedReader.readLine()) != null) {
			if (line.isEmpty() || line.startsWith("//"))
				continue;
			if (line.contains("//")) {
				int index = line.indexOf("//");
				line = line.substring(0, index);
			}
			line = line.trim();
			line.replaceAll(" ", "");

			if (line.startsWith("@") == false)
				continue;

			String symbol_text = line.substring(1, line.length());

			boolean isNumber = false;
			try {
				Integer.parseInt(symbol_text);
				isNumber = true;
			} catch (Exception e) {

			}
			if (isNumber)
				continue;

			// Check that it's not saved symbol
			boolean isSavedSymbol = false;
			for (Pair<String, Integer> p : symbol_table) {
				if (p.getKey().equals(line)) {
					isSavedSymbol = true;
					break;
				}
			}
			if (isSavedSymbol)
				continue;

			// Check that it's not a user label
			boolean isUserLabel = false;
			for (Pair<Integer, String> p : labels_table) {
				if (p.getValue().equals(symbol_text)) {
					isUserLabel = true;
					break;
				}
			}
			if (isUserLabel)
				continue;

			// Check saved user symbols
			if (user_symbols.isEmpty()) {
				user_symbols.add(new Pair<String, Integer>(line, 16));
				continue;
			}

			boolean isExists = false;

			for (Pair<String, Integer> p : user_symbols) {
				if (p.getKey().equals(line)) {
					// Symbol already exists. Do nothing.
					isExists = true;
					break;
				}
			}

			if (isExists == false) {
				int last_num = user_symbols.get(user_symbols.size() - 1).getValue().intValue();
				user_symbols.add(new Pair<String, Integer>(line, last_num + 1));
			}

		}
		
		
		
		

		bufferedReader.close();
		PC = 0;
		bufferedReader = new BufferedReader(new FileReader(file));
		String binary_line = "";
		label_line = false;

		
		
		
		
		
		
		// Big While
		while ((line = bufferedReader.readLine()) != null) {
			binary_line = "";
			if (line.isEmpty() || line.startsWith("//"))
				continue;
			if (line.contains("//")) {
				int index = line.indexOf("//");
				line = line.substring(0, index);
			}
			line = line.trim();
			line.replaceAll(" ", "");

			System.out.println(PC + "  :  " + line);

			label_line = false;
			if (line.startsWith("@")) {
				// A instruction
				binary_line += "0";
				try {
					// Is this @XXXX where X is number?
					int num = Integer.parseInt(line.substring(1, line.length()));
					String num_as_bin = Integer.toBinaryString(num);
					if (num_as_bin.length() >= 16) {
						System.err.println("ERROR: Number " + num + " is too big!");
						System.exit(1);
					}

					// Pad binary with zeros
					int num_of_zeros_to_pad = 16 - 1 - num_as_bin.length();
					for (int i = 0; i < num_of_zeros_to_pad; i++)
						binary_line += "0";

					binary_line += num_as_bin;

				} catch (Exception e) {
					// Couldn't parse number.
					// Try symbols.

					boolean found = false;
					for (Pair<String, Integer> p : symbol_table) {
						if (p.getKey().equals(line)) {
							String num_as_bin = Integer.toBinaryString(p.getValue().intValue());
							int num_of_zeros_to_pad = 16 - 1 - num_as_bin.length();
							for (int i = 0; i < num_of_zeros_to_pad; i++)
								binary_line += "0";
							binary_line += num_as_bin;
							found = true;
							break;
						}
					}

					if (found == true) {
						out.println(binary_line + "\t//" + line);
						out2.println(binary_line);

						System.out.println("Result: " + binary_line);
						System.out.println("\n\n");
						binary_line = "";

						if (label_line == false)
							PC++;
						continue;
					}
					
					// Search in labels
					String label_text = line.substring(1, line.length());
					boolean foundLabel = false;
					for (Pair<Integer, String> lbl : labels_table) {
						if (lbl.getValue().equals(label_text)) {
							int line_num = lbl.getKey().intValue();
							String num_as_bin = Integer.toBinaryString(line_num);
							// Pad zeros
							// Pad binary with zeros
							int num_of_zeros_to_pad = 16 - 1 - num_as_bin.length();
							for (int i = 0; i < num_of_zeros_to_pad; i++)
								binary_line += "0";
							binary_line += num_as_bin;

							label_line = true;
							foundLabel = true;
							break;
						}
					}
					
					if(foundLabel) {
						out.println(binary_line + "\t//" + line);
						out2.println(binary_line);

						System.out.println("Result: " + binary_line);
						System.out.println("\n\n");
						binary_line = "";

						if (label_line == false)
							PC++;
						continue;
					}

					System.out.println("Searching for saved symbols");
					for (Pair<String, Integer> p : user_symbols) {
						if (p.getKey().equals(line)) {
							System.out.println("Found user saved symbol: " + p.getKey() + " At: "
									+ p.getValue().intValue());
							int num = p.getValue().intValue();
							String num_as_bin = Integer.toBinaryString(num);
							// Pad binary with zeros
							int num_of_zeros_to_pad = 16 - 1 - num_as_bin.length();
							for (int i = 0; i < num_of_zeros_to_pad; i++)
								binary_line += "0";
							binary_line += num_as_bin;
							break;
						}
					}
				}
			} else if (line.startsWith("(") && line.endsWith(")")) {
				// do nothing
				continue;
			} else {
				// C instruction
				String ram = getRAMBinary(line);

				// Destination
				String destination = null;
				if (line.contains("=")) {
					String before_eq = line.substring(0, line.indexOf("="));
					for (PairString ps : destination_table) {
						if (ps.a.toLowerCase().equals(before_eq.toLowerCase())) {
							destination = ps.b;
							break;
						}
					}
				}

				// Jump
				String jump = null;
				if (line.contains(";")) {
					String after_jmp = line.substring(line.indexOf(";") + 1, line.length());
					for (PairString ps : jump_table) {
						if (ps.a.toLowerCase().equals(after_jmp.toLowerCase())) {
							jump = ps.b;
							break;
						}
					}
				}

				// Instruction
				String instruction = null; // As binary to add to binary line
				String instruction_line; // As temp line
				if (line.contains(";")) {
					instruction_line = line.substring(0, line.indexOf(';'));
				} else {
					instruction_line = line;
				}

				if (instruction_line.contains("=")) {
					// EX: instruction_line = D=D-M (D=D-M;JMP)
					int index_of_eq = instruction_line.indexOf('=');
					String after_eq = instruction_line.substring(index_of_eq + 1, instruction_line.length());
					for (TripletString ts : instruction_table) {
						if (ts.a.toLowerCase().equals(after_eq.toLowerCase())
								|| ts.b.toLowerCase().equals(after_eq.toLowerCase())) {
							instruction = ts.c;
							break;
						}
					}
				} else {
					// EX: instruction_line = D (D; JMP)
					for (TripletString ts : instruction_table) {
						if (ts.a.toLowerCase().equals(instruction_line.toLowerCase())
								|| ts.b.toLowerCase().equals(instruction_line.toLowerCase())) {
							instruction = ts.c;
							break;
						}
					}
				}

				binary_line += "111";
				binary_line += ram;
				binary_line += instruction;
				if (destination == null)
					binary_line += "000";
				else
					binary_line += destination;
				if (jump == null)
					binary_line += "000";
				else
					binary_line += jump;
			}

			out.println(binary_line + "\t//" + line);
			out2.println(binary_line);

			System.out.println("Result: " + binary_line);
			System.out.println("\n\n");
			binary_line = "";

			if (label_line == false)
				PC++;
		}

		bufferedReader.close();
		out.close();
		out2.close();
	}

	/**
	 *
	 * @param line
	 *            - Origional assembly instruction line
	 * @return String as 0 or 1 representing 4th bit in hack assembly
	 */
	public String getRAMBinary(String line) {
		int ind_of_eq = line.indexOf('=');
		if (ind_of_eq != -1) {
			String after_eq = line.substring(ind_of_eq + 1, line.length());
			if (after_eq.contains("M"))
				return "1";
			else
				return "0";
		} else {
			int index_of_jmp = line.indexOf(';');
			if (index_of_jmp != -1) {
				String before_jmp = line.substring(0, index_of_jmp);
				if (before_jmp.contains("M"))
					return "1";
				else
					return "0";
			} else {
				return null;
			}
		}
	}

	public String getJumpBinary(String line) {
		for (PairString ps : jump_table) {
			if (line.contains(ps.a)) {
				return ps.b;
			}
		}
		return "000";
	}

	public static void main(String[] args) throws IOException {
		String current_dir = "C:\\Users\\Shlomi\\Desktop\\nand2tetris\\nand2tetris\\projects\\06\\rect";
		JFileChooser fileChooser = new JFileChooser(current_dir);
		fileChooser.showOpenDialog(null);
		File file = fileChooser.getSelectedFile();
		HackAssembler hackAssembler = new HackAssembler(file);
	}
}

class TripletString {
	String a, b, c;

	public TripletString(String a, String b, String c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
}

class PairString {
	String a, b;

	public PairString(String a, String b) {
		this.a = a;
		this.b = b;
	}
}

/*
 * //Maybe its label? boolean isLabel = false; for(Pair<Integer,String>
 * lbl:labels_table) { if(lbl.getValue().equals(line_without_at_sign)) { isLabel
 * = true; int num = lbl.getKey().intValue(); String num_as_bin =
 * Integer.toBinaryString(num); if(num_as_bin.length() >= 16) {
 * System.err.println("ERROR: Number "+num + " is too big!"); System.exit(1); }
 * //Add number to binary string int num_of_zeros_to_pad =
 * 16-1-num_as_bin.length(); for(int i = 0; i < num_of_zeros_to_pad; i++)
 * binary_line += "0"; binary_line += num_as_bin; break; } }
 * 
 * if(isLabel == false) { if(line_without_at_sign.startsWith("R")) { try {
 * String str_after_R = line_without_at_sign.substring(1,
 * line_without_at_sign.length()); int num = Integer.parseInt(str_after_R); if
 * (num >= 16) { System.err.println("ERROR: Number after R is illegal!");
 * System.exit(1); } String num_as_bin = Integer.toBinaryString(num); //Add
 * number to binary string int num_of_zeros_to_pad = 16-1-num_as_bin.length();
 * for(int i = 0; i < num_of_zeros_to_pad; i++) binary_line += "0"; binary_line
 * += num_as_bin; } catch (Exception e2) {
 * System.err.println("ERROR: Couldn't parase string after @R"); System.exit(1);
 * } } System.err.println("ERROR: Couldn't find label: " +
 * line_without_at_sign); System.exit(1);
 * 
 * }
 */