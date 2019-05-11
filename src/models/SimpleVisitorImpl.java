package models;

import parser.*;
import java.util.LinkedList;
import java.util.List;

public class SimpleVisitorImpl extends SimpleBaseVisitor<SimpleElementBase> {

	private SimpleVTable simpleVTable = new SimpleVTable();
	private SimpleFTable simpleFTable = new SimpleFTable();

	@Override
	public SimpleElementBase visitStatement(SimpleParser.StatementContext ctx) {
		//visit the first child, this works for every case
		return visit(ctx.getChild(0));
	}

	@Override
	public SimpleStmtAssignment visitAssignment(SimpleParser.AssignmentContext ctx) {
		//get expression
		SimpleExp exp = (SimpleExp) visit(ctx.exp());

		//get id of variable
		String id = ctx.ID().getText();

		simpleVTable.useVariable(id);

		//construct assignment expression
		return new SimpleStmtAssignment(exp, id);
	}

	@Override
	public  SimpleElementBase visitFunctioncall(SimpleParser.FunctioncallContext ctx){

		String id = ctx.ID().getText();

		LinkedList<String> expList = new LinkedList<>();

		for (SimpleParser.ExpContext exp : ctx.exp()){
			expList.add(exp.getText());
		}

		simpleFTable.useFunction(id, expList, simpleVTable);

		return new SimpleStmtFunctioncall(id);
	}

	@Override
	public SimpleElementBase visitBlock(SimpleParser.BlockContext ctx) {

		//list for saving children statements
		List<SimpleStmt> children = new LinkedList<SimpleStmt>();

		//A new HashMap will be created on the tail of the HashTable List
		simpleVTable.scopeEntry();
		simpleFTable.scopeEntry();

		//visit each children
		for (SimpleParser.StatementContext stmtCtx : ctx.statement())
			children.add((SimpleStmt) visitStatement(stmtCtx));

		//construct block statement expression
		SimpleStmtBlock block = new SimpleStmtBlock(children);

		simpleVTable.scopeExit();
		simpleFTable.scopeExit();

		return block;
	}

	public SimpleElementBase visitDeclaration(SimpleParser.DeclarationContext ctx) {
		if (ctx.type() == null){
			String id = ctx.ID().getText();

			List<SimpleParser.ParameterContext> paramList = ctx.parameter();

			simpleFTable.newFunctionDeclaration(id, paramList);

			//Visit fun block
			SimpleStmtBlock block = (SimpleStmtBlock) visit(ctx.block());

			return new SimpleStmtDeclaration(id);

		} else {
			String type = ctx.type().getText();

			String id = ctx.ID().getText();

			String value = ctx.exp().getText();

			SimpleStmtExp exp = (SimpleStmtExp) visitExp(ctx.exp());

			if( type != exp.getType()){
				System.out.println("Tipo dell'espressione non compatibile con la dichiarazione.");
			}

			simpleVTable.newIdentifierDeclaration(id, type);

			return new SimpleStmtDeclaration(value);
		}
	}

	@Override
	public SimpleExp visitExp(SimpleParser.ExpContext ctx){
		String type = "";

		if(ctx.op == null){
			type = visitTerm(ctx.left).getType();
		} else if(ctx.op.getText() == "+" || ctx.op.getText() == "-"){
			type = "int";
		} else {
			System.out.println("Error visitExp");
		}

		return new SimpleStmtExp(type);
	}

	@Override
	public SimpleStmtExp visitTerm(SimpleParser.TermContext ctx){
		String type = "";

		if(ctx.op == null){
			type = visitFactor(ctx.left).getType();
		} else if(ctx.op.getText() == "*" || ctx.op.getText() == "/"){
			type = "int";
		} else {
			System.out.println("Error visitExp");
		}

		return new SimpleStmtExp(type);

	}

	@Override
	public SimpleStmtExp visitFactor(SimpleParser.FactorContext ctx){
		String type = "";

		if(ctx.ROP().getSymbol() == null);

		return new SimpleStmtExp(type);
	}


	@Override
	public SimpleElementBase visitDeletion(SimpleParser.DeletionContext ctx) {

		//construct delete expression with variable id

		return new SimpleStmtDelete(ctx.ID().getText());
	}


	@Override
	public SimpleElementBase visitPrint(SimpleParser.PrintContext ctx) {

		//get expression
		SimpleExp exp = (SimpleExp) visit(ctx.exp());

		//construct print exp

		return new SimpleStmtPrint(exp);
	}
}
