package interpreter;

import javafx.util.Pair;
import models.SimpleElementBase;
import models.SimpleFTable;
import models.SimpleStmtFunctioncall;
import parser.SimpleBaseVisitor;
import parser.SimpleParser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SimpleVisitorInterp extends SimpleBaseVisitor<List<Node>> {


    SimpleVTableWithOffset simpleVTableWithOffset = new SimpleVTableWithOffset();

    SimpleFTableWithLabel simpleFTableWithLabel = new SimpleFTableWithLabel();

    private int nestingLevel = 0;

    private int labelCounter = 0;

    private boolean insideDeclaration = false;

    @Override
    public List<Node> visitStatement(SimpleParser.StatementContext ctx) {
        //visit the first child, this works for every case
        return visit(ctx.getChild(0));
    }


    @Override
    public List<Node> visitBlock(SimpleParser.BlockContext ctx) {

        List<Node> codeBlock = new ArrayList<>();

        List<String> variablesDeclared = new LinkedList<>();

        Boolean _insideDeclaration = insideDeclaration;
        insideDeclaration = false;

        if(_insideDeclaration == false){
            simpleVTableWithOffset.scopeEntry();
            simpleFTableWithLabel.scopeEntry();
            nestingLevel++;
            variablesDeclared = visitBlockAndGetDeclaration(ctx);

            for(int i = variablesDeclared.size()-1; i >= 0; i--){
                codeBlock.add(addi("sp", "sp", "-1"));
            }

            codeBlock.addAll(push("fp"));
            codeBlock.add(move("fp", "sp"));
        }


        //list for saving children statements
        List<Node> statementCode = new ArrayList<>();

        //visit each children
        for (SimpleParser.StatementContext stmtCtx : ctx.statement()){
            statementCode.addAll(visitStatement(stmtCtx));
        }

        codeBlock.addAll(statementCode);

        if(_insideDeclaration == false){
            simpleFTableWithLabel.scopeExit();
            simpleVTableWithOffset.scopeExit();
            nestingLevel--;

            codeBlock.add(top("fp"));
            codeBlock.add(pop());

            for (String var: variablesDeclared) {
                codeBlock.add(pop());
            }

        }


        int i = 0;
        for (Node node: codeBlock) {
            System.out.println("instr: " + (i++) + " " + node.getInstr() + " arg1: "+ node.getArg1()+" offset: "+node.getOffset()+" arg2: "+ node.getArg2()+" arg3: " + node.getArg3());
        }
        System.out.println("\n\n\n\n");

        return codeBlock;
    }


    private List<String> visitBlockAndGetDeclaration(SimpleParser.BlockContext ctx){

        List<String> variablesDeclared = new ArrayList<>();

        for (SimpleParser.StatementContext stmtCtx : ctx.statement()){
            //meaning that we have found a declaration
            if(stmtCtx.getChild(0).getClass() == SimpleParser.DeclarationContext.class ){
                SimpleParser.DeclarationContext declaration = (SimpleParser.DeclarationContext) stmtCtx.getChild(0);
                if(declaration.type() != null){
                    //getting the variable identifier
                    variablesDeclared.add(declaration.ID().getText());

                    simpleVTableWithOffset.varDeclaration(declaration.ID().getText());
                }
            }
        }


        return variablesDeclared;
    }

    @Override
    public List<Node> visitPrint(SimpleParser.PrintContext ctx) {

        List<Node> printCode = new ArrayList<>();

        List<Node> expCode = visitExp(ctx.exp());

        printCode.addAll(expCode);
        printCode.add(print("a"));

        return printCode;
    }

    @Override
    public List<Node> visitIfthenelse(SimpleParser.IfthenelseContext ctx) {

        List<Node> ifThenElseCode = new ArrayList();

        String b_true = getFreshLabel();
        String end_if = getFreshLabel();

        List<Node> codeExp = visitExp(ctx.exp());
        ifThenElseCode.addAll(codeExp);
        ifThenElseCode.add(li("t", "1"));
        ifThenElseCode.add(beq("a", "t", b_true));
        List<Node> elseCode = visitBlock(ctx.block(1));
        ifThenElseCode.addAll(elseCode);
        ifThenElseCode.add(b(end_if));
        ifThenElseCode.add(label(b_true));
        List<Node> thenCode = visitBlock(ctx.block(0));
        ifThenElseCode.addAll(thenCode);
        ifThenElseCode.add(label(end_if));

        return ifThenElseCode;
    }

    @Override
    public List<Node> visitExp(SimpleParser.ExpContext ctx){

        List<Node> expCode = new ArrayList<>();

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
                expCode.add(add("a", "a", "t"));
            } else {
                expCode.add(sub("a", "t", "a"));
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

        List<Node> termCode = new ArrayList<>();

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
                termCode.add(time("a", "a", "t"));
            } else {
                termCode.add(divide("a", "a", "t")); // (a/t)
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

        List<Node> factorCode = new ArrayList<>();

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
            } else if (ctx.op.getText().equals("!=")){
                factorCode.add(new Node("noteq", "a", 0, "a", "t"));
            } else if (ctx.op.getText().equals("<")){
                factorCode.add(new Node("smaller", "a", 0, "a", "t"));
            } else if (ctx.op.getText().equals(">")){
                factorCode.add(new Node("greater", "a", 0, "a", "t"));
            } else if (ctx.op.getText().equals(">=")){
                factorCode.add(new Node("smalleq", "a", 0, "a", "t"));
            } else if (ctx.op.getText().equals("<=")){
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

        List<Node> valueCode = new ArrayList<>();

        if(ctx.INTEGER() != null){
            valueCode.add(li("a", ctx.INTEGER().getText()));
            return valueCode;
        } else if (ctx.getText().equals("true")){
            valueCode.add(li("a", "1"));
            return valueCode;
        } else if(ctx.getText().equals("false")){
            valueCode.add(li("a", "0"));
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

        List<Node> idCode = new ArrayList<>();
        Pair<Integer, Integer> offsetAndNestingLevel =  simpleVTableWithOffset.getOffsetAndNestingLevel(id);
        System.out.println(offsetAndNestingLevel);
        System.out.println(nestingLevel);

        idCode.add(move("al", "fp"));
        for(int i = 0; i < nestingLevel - (int) offsetAndNestingLevel.getValue(); i++){
            idCode.add(lw("al", 0, "al"));
        }

        int offset = (int) offsetAndNestingLevel.getKey();
        idCode.add(lw("a", offset, "al"));

        return idCode;
    }

    @Override
    public List<Node> visitDeclaration(SimpleParser.DeclarationContext ctx){

        List<Node> codeDeclaration = new ArrayList<>();

        if(ctx.type() == null){
            List<String> variablesDeclared = visitBlockAndGetDeclaration(ctx.block());

            String endFunctionLabel = getFreshLabel();
            codeDeclaration.add(b(endFunctionLabel));

            simpleVTableWithOffset.scopeEntry();
            nestingLevel++;

            List<String> paramsList = new LinkedList<>();

            for (SimpleParser.ParameterContext param: ctx.parameter()){
                paramsList.add(param.ID().getText());
            }

            paramsList.addAll(variablesDeclared);

            for (String param: paramsList){
                simpleVTableWithOffset.varDeclaration(param);
            }

            String freshLabel = simpleFTableWithLabel.newFunctionDeclaration(ctx.ID().getText(), variablesDeclared);
            codeDeclaration.add(label(freshLabel));
            codeDeclaration.add(move("fp", "sp"));

            codeDeclaration.addAll(push("ra"));

            insideDeclaration = true;
            simpleFTableWithLabel.scopeEntry();

            codeDeclaration.addAll(visitBlock(ctx.block()));

            simpleFTableWithLabel.scopeEntry();

            codeDeclaration.add(top("ra"));
            codeDeclaration.add(pop());

            codeDeclaration.add(top("fp"));
            codeDeclaration.add(pop());

            for (int i = 0; i < paramsList.size(); i++) {
                codeDeclaration.add(addi("sp", "sp", "1"));
            }

            codeDeclaration.add(jr("ra"));

            codeDeclaration.add(label(endFunctionLabel));


            nestingLevel--;
            simpleVTableWithOffset.scopeExit();


        } else {

            String id = ctx.ID().getText();

            List<Node> codeExp = visitExp(ctx.exp());

            codeDeclaration.addAll(codeExp);

            Pair<Integer, Integer> offsetAndNestingLevel =  simpleVTableWithOffset.getOffsetAndNestingLevel(id);

            codeDeclaration.add(move("al", "fp"));

            for(int i = 0; i < nestingLevel - offsetAndNestingLevel.getValue(); i++){
                codeDeclaration.add(lw("al", 0, "al"));
            }

            int offset = offsetAndNestingLevel.getKey();
            codeDeclaration.add(sw("a", offset, "al"));

        }

        return codeDeclaration;
    }

    @Override
    public List<Node> visitFunctioncall(SimpleParser.FunctioncallContext ctx){

        List<Node> functionCallCode = new ArrayList<>();

        List<SimpleParser.ExpContext> paramsList = ctx.exp();


        List<String> variablesDeclared = simpleFTableWithLabel.getVariablesDeclared(ctx.ID().getText());


        simpleVTableWithOffset.scopeEntry();


        if(variablesDeclared != null) {
            //loading local variables
            for (int i = variablesDeclared.size() - 1; i >= 0; i--) {
                functionCallCode.add(addi("sp", "sp", "-1"));
            }
        }
        //loading parameters
        for(int i = paramsList.size()-1; i >= 0; i--){
            functionCallCode.addAll(visitExp(paramsList.get(i)));

            functionCallCode.addAll(push("a"));
        }




        functionCallCode.add(move("al","fp"));

        for(int i = 0; i < nestingLevel - simpleFTableWithLabel.getNestingLevel(ctx.ID().getText()); i++){
            functionCallCode.add(lw("al",0,"al"));
        }
        functionCallCode.addAll(push("al"));

        simpleVTableWithOffset.scopeExit();

        String functionLabel = simpleFTableWithLabel.getFunctionLabel(ctx.ID().getText());
        functionCallCode.add(jal(functionLabel));

        return functionCallCode;
    }

    private Node top(String register){
        return lw(register, 0, "sp");
    }

    private Node pop(){
        return addi("sp", "sp", "1");
    }

    private List<Node> push(String register){
        List<Node> pushCode = new ArrayList<>();
        pushCode.add(addi("sp", "sp", "-1"));
        pushCode.add(sw(register, 0, "sp"));
        return pushCode;
    }

    private Node lw(String arg1, Integer offset, String arg2){
        return new Node ("lw", arg1, offset, arg2, null);
    }

    private Node li(String arg1, String arg2){
        return new Node ("li", arg1, null, arg2, null);
    }

    private Node sw(String arg1, Integer offset, String arg2){
        return new Node ("sw", arg1, offset, arg2, null);
    }

    private Node addi(String arg1, String arg2, String arg3){
        return new Node ("addi", arg1, null, arg2, arg3);
    }

    private Node add(String arg1, String arg2, String arg3){
        return new Node ("add", arg1, null, arg2, arg3);
    }

    private Node sub(String arg1, String arg2, String arg3) {
        return new Node("sub", arg1, null, arg2, arg3);
    }

    private Node move(String arg1, String arg2) {
        return new Node("move", arg1, null, arg2, null);
    }

    private Node print(String arg1) {
        return new Node("print", arg1, null, null, null);
    }

    private Node time(String arg1, String arg2, String arg3) {
        return new Node("time", arg1, null, arg2, arg3);
    }

    private Node divide(String arg1, String arg2, String arg3) {
        return new Node("divide", arg1, null, arg2, arg3);
    }

    private Node beq(String arg1, String arg2, String arg3) {
        return new Node("beq", arg1, null, arg2, arg3);
    }

    private Node label(String label) {
        return new Node(label, null, null, null, null);
    }

    private Node b(String label) {
        return new Node("b", label, null, null, null);
    }

    private Node jr(String register) {
        return new Node("jr", register, null, null, null);
    }

    private Node jal(String label) {
        return new Node("jal", label, null, null, null);
    }

    private String getFreshLabel(){
        return "label" + labelCounter++;
    }

}
