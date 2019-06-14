package models.Utilities;

import models.SimpleFTable;
import models.SimpleVTable;
import parser.SimpleParser;

import java.util.List;


public class FunctionInfo {

    private SimpleParser.BlockContext block;
    private SimpleVTable simpleVTable;
    private SimpleFTable simpleFTable;

    public FunctionInfo(SimpleFTable simpleFTable, SimpleVTable simpleVTable, SimpleParser.BlockContext block) {
        this.simpleFTable = simpleFTable;
        this.simpleVTable = simpleVTable;
        this.block = block;
    }


    public SimpleParser.BlockContext getBlock(){ return block; }

    public SimpleVTable getSimpleVTable() { return simpleVTable; }

    public SimpleFTable getSimpleFTable() { return simpleFTable; }

}
