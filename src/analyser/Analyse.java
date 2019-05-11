package analyser;

import java.io.IOException;
import java.util.List;

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
			SimpleStmtBlock mainBlock = (SimpleStmtBlock) visitor.visitBlock(parser.block());


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
