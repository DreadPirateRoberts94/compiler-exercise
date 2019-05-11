package models;

import models.behavior.BTBase;

import java.util.List;

public class SimpleStmtDeclaration extends SimpleStmt {
    private String exp;

    public SimpleStmtDeclaration(String exp) {
        this.exp = exp;
    }

    @Override
    public List<SemanticError> checkSemantics(Environment e) {
        return null;
    }

    @Override
    public BTBase inferBehavior(Environment e) {
        return null;
    }
}
