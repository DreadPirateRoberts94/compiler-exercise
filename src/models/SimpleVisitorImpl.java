package models;

import parser.*;
import java.util.LinkedList;
import java.util.List;

public class SimpleVisitorImpl extends SimpleBaseVisitor<SimpleElementBase> {

	private SimpleVTable simpleVTable = new SimpleVTable();
	private SimpleFTable simpleFTable = new SimpleFTable();

	private Boolean hasScopeBeenAlreadyDeclared = false;

	public SimpleVisitorImpl(){
		simpleFTable.scopeEntry();
	}

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

		simpleVTable.isVarDeclared(id);

		//construct assignment expression
		return new SimpleStmtAssignment(exp, id);
	}

	@Override
	public  SimpleElementBase visitFunctioncall(SimpleParser.FunctioncallContext ctx){

		String id = ctx.ID().getText();

		LinkedList<String> idList = new LinkedList<>();
		LinkedList<String> typeList = new LinkedList<>();

		for (SimpleParser.ExpContext exp : ctx.exp()){
			typeList.add(visitExp(exp).getType());
			if (exp.left.left.left.ID() != null && exp.op == null && exp.left.op == null && exp.left.left.op == null){
				idList.add(exp.left.left.left.ID().getText());
			} else {
				idList.add(null);
			}
		}

		simpleFTable.useFunction(id, idList, typeList, simpleVTable);

		return new SimpleStmtFunctioncall(id);
	}

	@Override
	public SimpleElementBase visitBlock(SimpleParser.BlockContext ctx) {

		//list for saving children statements
		List<SimpleStmt> children = new LinkedList<SimpleStmt>();
		//A new HashMap will be created on the tail of the HashTable List
		if(!hasScopeBeenAlreadyDeclared){
			simpleVTable.scopeEntry();
		} else {
			hasScopeBeenAlreadyDeclared = false;
		}
		simpleFTable.scopeEntry();


		System.out.println("SimpleVTable " + simpleVTable.identifiersList);

		//visit each children
		for (SimpleParser.StatementContext stmtCtx : ctx.statement())
			children.add((SimpleStmt) visitStatement(stmtCtx));

		//construct block statement expression
		SimpleStmtBlock block = new SimpleStmtBlock(children);

		simpleVTable.scopeExit();
		simpleFTable.scopeExit();
		System.out.println("SimpleVTable " +simpleVTable.identifiersList);

		return block;
	}

	public SimpleElementBase visitDeclaration(SimpleParser.DeclarationContext ctx) {
		if (ctx.type() == null){
			String id = ctx.ID().getText();

			List<SimpleParser.ParameterContext> paramList = ctx.parameter();
			if(paramList.size() > 0) hasScopeBeenAlreadyDeclared = true;
			simpleFTable.newFunctionDeclaration(id, paramList, simpleVTable, hasScopeBeenAlreadyDeclared);

			//Visit fun block
			SimpleStmtBlock block = (SimpleStmtBlock) visit(ctx.block());

			return new SimpleStmtDeclaration(id);

		} else {
			String type = ctx.type().getText();

			String id = ctx.ID().getText();

			String value = ctx.exp().getText();

			SimpleStmtExp exp = (SimpleStmtExp) visitExp(ctx.exp());

			if(!type.equals(exp.getType())){
				System.out.println("Tipo della dichiarazione di " + id + " non compatibile con quello dell'espressione ");
			}

			simpleVTable.newIdentifierDeclaration(id, type);

			return new SimpleStmtDeclaration(value);
		}
	}

	@Override
	public SimpleStmtExp visitExp(SimpleParser.ExpContext ctx){
		String leftType = "";
		String rightType = "";

		if(ctx.op == null){
			return new SimpleStmtExp(visitTerm(ctx.left).getType());
		} else if(ctx.op.getText().equals("+") || ctx.op.getText().equals("-")){
			leftType = visitTerm(ctx.left).getType();
			SimpleStmtExp exp = (SimpleStmtExp) visitExp(ctx.right);
			rightType = exp.getType();
			return  new SimpleStmtExp(leftType.equals(rightType) ? "int" : "err");
		} else {
			System.out.println("Error visitExp");
		}

		return new SimpleStmtExp("err");
	}

	@Override
	public SimpleStmtExp visitTerm(SimpleParser.TermContext ctx){
		String leftType = "";
		String rightType = "";

		if(ctx.op == null){
			return new SimpleStmtExp(visitFactor(ctx.left).getType());
		} else if(ctx.op.getText().equals("*") || ctx.op.getText().equals("/")){
				SimpleStmtExp rightTerm = visitTerm(ctx.right);
				rightType = rightTerm.getType();
				leftType = visitFactor(ctx.left).getType();
			return  new SimpleStmtExp(leftType.equals(rightType) ? "int" : "err");
		} else {
			System.out.println("Error visitExp");
		}

		return new SimpleStmtExp("err");

	}

	@Override
	public SimpleStmtExp visitFactor(SimpleParser.FactorContext ctx){
		String leftType = "";
		String rightType = "";

		if(ctx.ROP() == null && ctx.op == null){
			return  new SimpleStmtExp(visitValue(ctx.left).getType());
		} else {
			leftType = visitValue(ctx.left).getType();
			rightType = visitValue(ctx.right).getType();

			if (ctx.op != null && (ctx.op.getText().equals("&&") || ctx.op.getText().equals("||"))){

				return new SimpleStmtExp((leftType.equals(rightType)) && leftType.equals("bool") ? "bool" : "err");
			}else if (ctx.ROP() != null && ctx.ROP().getSymbol() != null){
				return new SimpleStmtExp((leftType.equals(rightType)) && leftType.equals("int") ? "bool" : "err");
			}
		}

		return new SimpleStmtExp("err");
	}

	@Override
	public SimpleStmtExp visitValue(SimpleParser.ValueContext ctx){
		String type = "";

		if(ctx.INTEGER() != null){
			type = "int";
		} else if (ctx.getText().equals("true") || ctx.getText().equals("false")){
			type = "bool";
		} else if (ctx.exp() != null){
			SimpleStmtExp exp = (SimpleStmtExp) visitExp(ctx.exp());
			type = exp.getType();
		} else if (ctx.ID() != null){
			type = simpleVTable.getVarType(ctx.ID().getText());
		} else {
			type = "err";
		}

		return new SimpleStmtExp(type);
	}

	@Override
	public SimpleElementBase visitDeletion(SimpleParser.DeletionContext ctx) {

		//construct delete expression with variable id
		if (simpleVTable.getVarType(ctx.ID().getText()).equals("err") && !simpleFTable.isFunDeclared(ctx.ID().getText())){
			System.out.println("Delete su ID " + ctx.ID().getText() + " non dichiarato");
		}

		return new SimpleStmtDelete(ctx.ID().getText());
	}


	@Override
	public SimpleElementBase visitPrint(SimpleParser.PrintContext ctx) {

		//get expression
		SimpleExp exp = (SimpleExp) visit(ctx.exp());

		//construct delete expression with variable id
		if (ctx.exp().left.left.left.ID() != null){
			if (simpleVTable.getVarType(ctx.exp().left.left.left.ID().getText()).equals("err")){
				System.out.println("Print su ID " + ctx.exp().left.left.left.ID().getText() + " non dichiarato");
			}
		}

		//construct print exp
		return new SimpleStmtPrint(exp);
	}
}
