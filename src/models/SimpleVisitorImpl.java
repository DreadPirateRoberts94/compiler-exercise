package models;

import javafx.util.Pair;
import parser.*;

import java.util.LinkedList;
import java.util.List;

public class SimpleVisitorImpl extends SimpleBaseVisitor<SimpleElementBase> {

	public SimpleVTable simpleVTable = new SimpleVTable();
	public SimpleFTable simpleFTable = new SimpleFTable();

	private SimpleFInvocation functionsInfo = new SimpleFInvocation();

	private FunctionCallStack functionCallStack = new FunctionCallStack();

	private Boolean hasScopeBeenAlreadyDeclared = false;

	private String identifier = null;

	private int insideDeclaration = 0;

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
	public SimpleElementBase visitFunctioncall(SimpleParser.FunctioncallContext ctx){
		String id = ctx.ID().getText();

		LinkedList<String> params = new LinkedList<>();
		LinkedList<String> typeList = new LinkedList<>();

		//Check type if insideDeclaration
		if(insideDeclaration < 0) {
			for (SimpleParser.ExpContext exp : ctx.exp()) {
				if (exp.left.left.left.ID() != null && exp.op == null && exp.left.op == null && exp.left.left.op == null) {
					params.add(exp.left.left.left.ID().getText());
					typeList.add(simpleVTable.getVarType(exp.left.left.left.ID().getText()));
				} else {
					params.add(null);
					typeList.add(null);
				}
			}
			simpleFTable.useFunction(id, params, typeList, simpleVTable);

		} else { //Skip typeCheck
			for (SimpleParser.ExpContext exp : ctx.exp()) {
				if (exp.left.left.left.ID() != null && exp.op == null && exp.left.op == null && exp.left.left.op == null) {
					params.add(exp.left.left.left.ID().getText());
				} else {
					params.add(null);
				}
			}
		}

		//to avoid typing recursion
		if (!identifier.equals(id)) {

			functionCallStack.functionCall(id, params);

			identifier = id;
			simpleVTable.scopeEntry();

			for (Pair param: simpleFTable.getFunctionFormalParamsAndType(id)) {
				simpleVTable.newIdentifierDeclaration((String) param.getKey(),(String) param.getValue());
			}

			List<String> formalParamsFunctionCalled = simpleFTable.getFunctionFormalVarParams(id);

			for (int i = 0; i < params.size(); i++) {
				if(formalParamsFunctionCalled.get(i) != null){ //meaning passed by var
					simpleVTable.createAssociationBetweenIdentifiers(formalParamsFunctionCalled.get(i),params.get(i));
				}
			}

			visitBlock(functionsInfo.getFunctionBlock(id));

			simpleVTable.scopeExit();

			identifier = null;
		}
		functionCallStack.functionExit();

		return new SimpleStmtFunctioncall(id);
	}

	@Override
	public SimpleElementBase visitBlock(SimpleParser.BlockContext ctx) {

		//list for saving children statements
		List<SimpleStmt> children = new LinkedList<>();
		//A new HashMap will be created on the tail of the HashTable List
		if(!hasScopeBeenAlreadyDeclared){
			simpleVTable.scopeEntry();
		} else {
			hasScopeBeenAlreadyDeclared = false;
		}
		simpleFTable.scopeEntry();

		//visit each children
		for (SimpleParser.StatementContext stmtCtx : ctx.statement()){
			children.add((SimpleStmt) visitStatement(stmtCtx));
		}

		//construct block statement expression
		SimpleStmtBlock block = new SimpleStmtBlock(children);

		simpleVTable.scopeExit();
		simpleFTable.scopeExit();
		return block;
	}

	public SimpleElementBase visitIfthenelse(SimpleParser.IfthenelseContext ctx){
		String type;

		if (ctx.exp() != null){
			type = visitExp(ctx.exp()).getType();
			if (!type.equals("bool")){
				System.out.println("Condizione dell'if non conforme");
			}
		}

		SimpleParser.BlockContext ifBlock = ctx.block(0); //if block
		SimpleParser.BlockContext thenBlock = ctx.block(1); //then block

		if(ifBlock != null && thenBlock != null){

			SimpleVTable ifSimpleVTable = new SimpleVTable(simpleVTable);

			SimpleVTable thenSimpleVTable = new SimpleVTable(simpleVTable);

			SimpleVTable tmpSimpleVTable = new SimpleVTable(simpleVTable);

			simpleVTable = new SimpleVTable(ifSimpleVTable);

			visitBlock(ifBlock);

			ifSimpleVTable = new SimpleVTable(simpleVTable);

			simpleVTable = new SimpleVTable(thenSimpleVTable);

			visitBlock(thenBlock);

			thenSimpleVTable = new SimpleVTable(simpleVTable);

			simpleVTable = new SimpleVTable(tmpSimpleVTable);

			if(!ifSimpleVTable.equals(thenSimpleVTable) || !thenSimpleVTable.equals(ifSimpleVTable)){
				System.out.println("if then else non bilanciato");
			}
		}

		return new SimpleStmtIfthenelse();
	}

	public SimpleElementBase visitDeclaration(SimpleParser.DeclarationContext ctx) {
		//if the type is null we have discovered a function declaration
		if (ctx.type() == null) {

			insideDeclaration--;
			String id = ctx.ID().getText();

			List<SimpleParser.ParameterContext> paramList = ctx.parameter();
			if (paramList.size() > 0) hasScopeBeenAlreadyDeclared = true;

			simpleFTable.newFunctionDeclaration(id, paramList, simpleVTable, hasScopeBeenAlreadyDeclared);

			functionsInfo.functionCall(id, ctx.block());

			//Visit fun block
			visit(ctx.block());

			insideDeclaration++;

			return new SimpleStmtDeclaration(id);

		} else {
			String type = ctx.type().getText();

			String id = ctx.ID().getText();

			String value = ctx.exp().getText();

			SimpleStmtExp exp = visitExp(ctx.exp());

			if(!type.equals(exp.getType())){
				System.out.println("Tipo della dichiarazione di " + id + " non compatibile con quello dell'espressione ");
			}

			simpleVTable.newIdentifierDeclaration(id, type);

			return new SimpleStmtDeclaration(value);
		}
	}

	@Override
	public SimpleStmtExp visitExp(SimpleParser.ExpContext ctx){
		String leftType;
		String rightType;

		if(ctx.op == null){
			return new SimpleStmtExp(visitTerm(ctx.left).getType());
		} else if(ctx.op.getText().equals("+") || ctx.op.getText().equals("-")){
			leftType = visitTerm(ctx.left).getType();
			SimpleStmtExp exp = visitExp(ctx.right);
			rightType = exp.getType();
			return  new SimpleStmtExp(leftType.equals(rightType) ? "int" : "err");
		} else {
			System.out.println("Error visitExp");
		}

		return new SimpleStmtExp("err");
	}

	@Override
	public SimpleStmtExp visitTerm(SimpleParser.TermContext ctx){
		String leftType;
		String rightType;

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
		String leftType;
		String rightType;

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
		String type;

		if(ctx.INTEGER() != null){
			type = "int";
		} else if (ctx.getText().equals("true") || ctx.getText().equals("false")){
			type = "bool";
		} else if (ctx.exp() != null){
			SimpleStmtExp exp = visitExp(ctx.exp());
			type = exp.getType();
		} else if (ctx.ID() != null){
			if(simpleVTable.isVarDeclared(ctx.ID().getText())) {
				type = simpleVTable.getVarType(ctx.ID().getText());
			} else {
				type = "err";
			}
		} else {
			type = "err";
		}
		return new SimpleStmtExp(type);
	}


	@Override
	public SimpleElementBase visitDeletion(SimpleParser.DeletionContext ctx) {

		if (simpleVTable.getVarType(ctx.ID().getText()).equals("err")){
			System.out.println("Delete su ID " + ctx.ID().getText() + " non dichiarato");
		} else {
			simpleVTable.deleteIdentifier(ctx.ID().getText());
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
