package models;

import models.Utilities.FunctionInfo;
import parser.*;
import java.util.LinkedList;
import java.util.List;

public class SimpleVisitorImpl extends SimpleBaseVisitor<SimpleElementBase> {

	private SimpleVTable simpleVTable = new SimpleVTable();
	private SimpleFTable simpleFTable = new SimpleFTable();

	private SimpleFInvocation functionsInfo = new SimpleFInvocation();

	private FunctionCallStack functionCallStack = new FunctionCallStack();

	private Boolean hasScopeBeenAlreadyDeclared = false;

	private String identifier = null;

	private int insideDeclaration = 0;

	private int insidecall = 0;

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

        }

        //no inside declaration
        if(insideDeclaration == 0) {

            for (SimpleParser.ExpContext exp : ctx.exp()) {
                if (exp.left.left.left.ID() != null && exp.op == null && exp.left.op == null && exp.left.left.op == null) {
                    params.add(exp.left.left.left.ID().getText());
                } else {
                    params.add(null);
                }
            }

            insidecall--;


            //to avoid typing recursion
            if (identifier != id) {
                //to change eventually formal parameters with actual
                if (identifier != null) {
                    for (int j = 0; j < params.size(); j++) {
                        for (int i = functionCallStack.size()-1; i >= 0 ; i--) {
                            List<String> actualsParamsPrevFunction = functionCallStack.getActualParamsNthFunction(i);
                            List<String> formalParamsPrevFunction = simpleFTable.getFunctionFormalParams(functionCallStack.getIdentifierNthFunction(i));
                            System.out.println("formal params: " + formalParamsPrevFunction + " fun id: " +  functionCallStack.getIdentifierNthFunction(i));

                            String tmp = params.get(j);

                            if (formalParamsPrevFunction.indexOf(tmp) != -1) {
                                params.set(j, actualsParamsPrevFunction.get(j));
                                break;
                            }
                        }
                    }
                }
                System.out.println("function called -> "+ id +" with params " + params +"\n insidedeclaration: "+ insideDeclaration+"\n");

                //System.out.println("ID funzione: " + id + "\nparametri: " + params +"\ntypelist: " +typeList + "\n");


                functionCallStack.functionCall(id, params);

                SimpleFTable tmpSimpleFTable = new SimpleFTable(simpleFTable);
                SimpleVTable tmpSimpleVTable = new SimpleVTable(simpleVTable);

                identifier = id;
                FunctionInfo functionInfo = null;

                FunctionInfo functionEnv = functionsInfo.getFunctionInfo(id);

                simpleVTable = functionEnv.getSimpleVTable();
                simpleFTable = functionEnv.getSimpleFTable();
                visitBlock(functionEnv.getBlock());

                identifier = null;
                simpleFTable = tmpSimpleFTable;
                simpleVTable = tmpSimpleVTable;
            }
        }
        if(insideDeclaration == 0){
            functionCallStack.functionExit();
            insidecall++;
        }

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

		return new SimpleStmtIfthenelse();
	}

	public SimpleElementBase visitDeclaration(SimpleParser.DeclarationContext ctx) {
        System.out.println(ctx.getText());
		//if the type is null we have discovered a function declaration
		if (ctx.type() == null) {

                System.out.println("visitFunction declaration");

            insideDeclaration--;
            String id = ctx.ID().getText();

            List<SimpleParser.ParameterContext> paramList = ctx.parameter();
            if (paramList.size() > 0) hasScopeBeenAlreadyDeclared = true;
            simpleFTable.newFunctionDeclaration(id, paramList, simpleVTable, hasScopeBeenAlreadyDeclared);

            SimpleFTable copyFTable = new SimpleFTable(simpleFTable);
            SimpleVTable copyVTable = new SimpleVTable(simpleVTable);
            functionsInfo.functionCall(id, copyFTable, copyVTable, ctx.block());

            System.out.println(functionsInfo.getFunctionInfo(id));
            //Visit fun block
            SimpleStmtBlock block = (SimpleStmtBlock) visit(ctx.block());

            System.out.println("exit function declaration");
            insideDeclaration++;

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

        System.out.println("Deleting...");

        if(insideDeclaration == 0){

            //construct delete expression with variable id
			String id = ctx.ID().getText();

            for(int i = functionCallStack.size()-1; i >= 0; i--){
                List<String> actualParams = functionCallStack.getActualParamsNthFunction(i);
                List<String> formalParams = simpleFTable.getFunctionFormalParams(functionCallStack.getIdentifierNthFunction(i));

                System.out.println("Function: "+ functionCallStack.getIdentifierNthFunction(i)+" parametri attuali "+ actualParams +" parametri formali " + formalParams);

                for (String formalParam: formalParams) {
					if(id.equals(formalParam)){
                        String actualParam = actualParams.get(formalParam.indexOf(formalParam));
						if (simpleVTable.getVarType(actualParam).equals("err")){
							System.out.println("Delete su ID " + actualParam + " non dichiarato");
						} else {
							simpleVTable.deleteIdentifier(actualParam);
						}
                        return new SimpleStmtDelete(ctx.ID().getText());
                    }
                }
            }

            if (simpleVTable.getVarType(ctx.ID().getText()).equals("err")){
                System.out.println("Delete su ID " + ctx.ID().getText() + " non dichiarato");
            } else {
                simpleVTable.deleteIdentifier(ctx.ID().getText());
            }
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
