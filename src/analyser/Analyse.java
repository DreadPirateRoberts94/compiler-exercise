package analyser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import interpreter.ExecuteVM;
import interpreter.Node;
import interpreter.SimpleVisitorInterp;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import parser.*;
import models.SimpleStmtBlock;
import models.SimpleVisitorImpl;


import org.antlr.v4.runtime.CommonTokenStream;

public class Analyse {

	public static void main(String[] args) {
		
		String fileName = "./test.spl";
		
		try{   
			CharStream input = CharStreams.fromFileName(fileName);

			//create lexer
			SimpleLexer lexer = new SimpleLexer(input);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new ErrorListener("Errors.txt"));

			//create parser
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			SimpleParser parser = new SimpleParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new ErrorListener("Errors.txt"));

			//tell the parser to build the AST
			parser.setBuildParseTree(true);
			
			//build custom visitor
			SimpleVisitorImpl visitor = new SimpleVisitorImpl();

			//visit the root, this will recursively visit the whole tree
			//visitor.visitBlock(parser.block());


			SimpleVisitorInterp visitorInterp = new SimpleVisitorInterp();

			List<Node> codeList = visitorInterp.visitBlock(parser.block());

			codeList.add(new Node("halt", null, null, null, null));

			Node[] code = new Node[codeList.size()];

			code = codeList.toArray(code);

			HashMap<String, Integer> labelAdd = new HashMap<>();
			HashMap<String, Integer> labelRef = new HashMap<>();

			int i = 0;
			for (Node node: codeList) {
				switch (node.getInstr()){
					case ("beq"):
						labelRef.put(node.getArg3(), i);
						code[i] = node;
						i++;
						break;
					case ("b"):
						labelRef.put(node.getArg1(), i);
						code[i] = node;
						i++;
						break;
					case ("jal"):
						labelRef.put(node.getArg1(), i);
						code[i] = node;
						i++;
						break;
					default:
						if (node.getInstr().startsWith("label") || node.getInstr().startsWith("flabel")){
							labelAdd.put(node.getInstr(), i);
						} else {
							code[i] = node;
							i++;
						}
						break;
				}
			}

			Map<String, Integer> mapRef = new HashMap<>(labelRef);

			for (Map.Entry<String, Integer> entry: mapRef.entrySet()) {
				int index = entry.getValue();
				code[index].setOffset(labelAdd.get(entry.getKey()));
			}

			ExecuteVM vm = new ExecuteVM(code);

			vm.run();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
