package interpreter;

import javafx.util.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import parser.SimpleBaseVisitor;
import parser.SimpleParser;

import java.util.LinkedList;
import java.util.List;

public class SimpleVisitorInterp extends SimpleBaseVisitor<List<Node>> {


    SimpleVTableWithOffset simpleVTableWithOffset = new SimpleVTableWithOffset();

    private int nestingLevel = 0;

    @Override
    public List<Node> visitStatement(SimpleParser.StatementContext ctx) {
        //visit the first child, this works for every case
        return visit(ctx.getChild(0));
    }


    @Override
    public List<Node> visitBlock(SimpleParser.BlockContext ctx) {

        simpleVTableWithOffset.scopeEntry();
        nestingLevel++;

        List<String> variablesDeclared = visitBlockAndGetDeclaration(ctx);

        List<Node> codeBlock = new LinkedList<>();

        for(int i = variablesDeclared.size()-1; i >= 0; i--){
            codeBlock.add(new Node("addi", "sp", 0, "sp", "-1"));
        }

        //list for saving children statements
        List<Node> statementCode = new LinkedList<>();

        //visit each children
        for (SimpleParser.StatementContext stmtCtx : ctx.statement()){
            statementCode.addAll(visitStatement(stmtCtx));
        }

        nestingLevel--;
        simpleVTableWithOffset.scopeExit();

        codeBlock.addAll(statementCode);

        for (Node node: codeBlock) {
            System.out.println("instr: " + node.getInstr() + " arg1: "+ node.getArg1()+" offset: "+node.getOffset()+" arg2: "+ node.getArg2()+" arg3: " + node.getArg3());
        }
        System.out.println("\n\n\n");


        return codeBlock;
    }


    public List<String> visitBlockAndGetDeclaration(SimpleParser.BlockContext ctx){

        List<String> variablesDeclared = new LinkedList<>();

        for (SimpleParser.StatementContext stmtCtx : ctx.statement()){
             //meaning that we have found a declaration
             if(stmtCtx.getChild(0).getClass() == SimpleParser.DeclarationContext.class ){
                SimpleParser.DeclarationContext declaration = (SimpleParser.DeclarationContext) stmtCtx.getChild(0);
                //getting the variable identifier
                variablesDeclared.add(declaration.ID().getText());

                simpleVTableWithOffset.varDeclaration(declaration.ID().getText());
                simpleVTableWithOffset.newIdentifierDeclaration(declaration.ID().getText(), "");
             }
        }


        return variablesDeclared;
    }

    @Override
    public List<Node> visitPrint(SimpleParser.PrintContext ctx) {

        List<Node> nodeList = visitExp(ctx.exp());

        return nodeList;
    }

    @Override
    public List<Node> visitExp(SimpleParser.ExpContext ctx){
        List<Node> expCode = new LinkedList<>();

        if(ctx.op == null){
            return visitTerm(ctx.left);
        } else if(ctx.op.getText().equals("+") || ctx.op.getText().equals("-")){
            List<Node> leftNodes;
            List<Node> rightNodes;

            leftNodes = visitTerm(ctx.left);
            expCode.addAll(leftNodes);
            expCode.addAll(push("a"));
            rightNodes = visitExp(ctx.right);
            expCode.addAll(rightNodes);
            expCode.add(top("t"));
            if (ctx.op.getText().equals("+")){
                expCode.add(new Node("add", "a", 0, "a", "t"));
            } else {
                expCode.add(new Node("sub", "a", 0, "t", "a"));
            }
            expCode.add(pop());

            return expCode;
        } else {
            System.out.println("Error visitExp");
        }

        return expCode;
    }

    @Override
    public List<Node> visitTerm(SimpleParser.TermContext ctx){
        List<Node> termCode = new LinkedList<>();

        if (ctx.op == null){
            return visitFactor(ctx.left);
        } else if(ctx.op.getText().equals("*") || ctx.op.getText().equals("/")){
            List<Node> leftNodes;
            List<Node> rightNodes;

            leftNodes = visitFactor(ctx.left);
            termCode.addAll(leftNodes);
            termCode.addAll(push("a"));
            rightNodes = visitTerm(ctx.right);
            termCode.addAll(rightNodes);
            termCode.add(top("t"));
            if(ctx.op.getText().equals("*")){
                termCode.add(new Node("time", "a", 0, "a", "t"));
            } else {
                termCode.add(new Node("divided", "a", 0, "a", "t")); // (a/t)
            }
            termCode.add(pop());

            return  termCode;
        } else {
            System.out.println("Error visitExp");
        }

        return termCode;
    }

    @Override
    public List<Node> visitFactor(SimpleParser.FactorContext ctx){
        List<Node> factorCode = new LinkedList<>();

        if(ctx.ROP() == null && ctx.op == null){
            return visitValue(ctx.left);
        } else {
            List<Node> leftNodes;
            List<Node> rightNodes;

            leftNodes = visitValue(ctx.left);
            factorCode.addAll(leftNodes);
            factorCode.addAll(push("a"));
            rightNodes = visitValue(ctx.right);
            factorCode.addAll(rightNodes);
            factorCode.add(top("t"));

            if (ctx.op.getText().equals("==")){
                factorCode.add(new Node("eq", "a", 0, "a", "t"));
            } else if (ctx.ROP().getText().equals("!=")){
                factorCode.add(new Node("noteq", "a", 0, "a", "t"));
            } else if (ctx.ROP().getText().equals("<")){
                factorCode.add(new Node("smaller", "a", 0, "a", "t"));
            } else if (ctx.ROP().getText().equals(">")){
                factorCode.add(new Node("greater", "a", 0, "a", "t"));
            } else if (ctx.ROP().getText().equals(">=")){
                factorCode.add(new Node("smalleq", "a", 0, "a", "t"));
            } else if (ctx.ROP().getText().equals("<=")){
                factorCode.add(new Node("greateq", "a", 0, "a", "t"));
            } else if (ctx.op.getText().equals("&&")){
                factorCode.add(new Node("and", "a", 0, "a", "t"));
            } else if (ctx.op.getText().equals("||")){
                factorCode.add(new Node("or", "a", 0, "a", "t"));
            }

            factorCode.add(pop());
            return factorCode;
        }
    }

    @Override
    public List<Node> visitValue(SimpleParser.ValueContext ctx){
        List<Node> valueCode = new LinkedList<>();

        if(ctx.INTEGER() != null){
            valueCode.add(new Node("lw", "a", 0, ctx.INTEGER().getText(), null));
            return valueCode;
        } else if (ctx.getText().equals("true")){
            valueCode.add(new Node("lw", "a", 0, "1", null));
            return valueCode;
        } else if(ctx.getText().equals("false")){
            valueCode.add(new Node("lw", "a", 0, "0", null));
            return valueCode;
        } else if (ctx.exp() != null){
            valueCode.addAll(visitExp(ctx.exp()));
            return valueCode;
        } else if (ctx.ID() != null){

            return visitId(ctx.ID().getText());
        }

        return valueCode;
    }


    public List<Node> visitId(String id){
        List<Node> idCode = new LinkedList<>();

        Pair<Integer, Integer> offsetAndNestingLevel =  simpleVTableWithOffset.getOffsetAndNestingLevel(id);

        idCode.add(new Node("lw","al", 0,"fp",null));
        for(int i = 0; i < nestingLevel - (int) offsetAndNestingLevel.getValue(); i++){
            idCode.add(new Node("lw","al",0,"al",null));
        }

        int offset = (int) offsetAndNestingLevel.getKey();
        idCode.add(new Node("lw","a", offset, "al", null));

        return idCode;
    }

    @Override
    public List<Node> visitDeclaration(SimpleParser.DeclarationContext ctx){
        List<Node> codeDeclaration = new LinkedList<>();

        if(ctx.type() == null){

        } else {

            String id = ctx.ID().getText();
            List<Node> codeExp = visitExp(ctx.exp());

            Pair<Integer, Integer> offsetAndNestingLevel =  simpleVTableWithOffset.getOffsetAndNestingLevel(id);

            codeDeclaration.add(new Node("lw","al", 0,"fp",null));
            for(int i = 0; i < nestingLevel - (int) offsetAndNestingLevel.getValue(); i++){
                codeDeclaration.add(new Node("lw","al",0,"al",null));
            }

            int offset = (int) offsetAndNestingLevel.getKey();
            codeDeclaration.add(new Node("sw","a", offset, "al", null));

        }

        return codeDeclaration;
    }

    private Node top(String register){
        return new Node("lw", register, 0, "sp", null);
    }

    private Node pop(){
        return new Node("addi", "sp", 0, "sp", "1");
    }

    private List<Node> push(String register){
        List<Node> pushCode = new LinkedList<>();
        pushCode.add(new Node("addi", "sp", 0, "sp", "-1"));
        pushCode.add(new Node("sw", register, 0, "sp", null));
        return pushCode;
    }


}
