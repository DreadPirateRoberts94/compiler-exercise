package models;

import java.util.List;

public class SimpleStmtExp extends SimpleExp {

    private String type;

    public SimpleStmtExp(String type) { this.type = type; }

    public String getType() {
        return type;
    }

    @Override
    public List<SemanticError> checkSemantics(Environment e) {
        return null;
    }

    @Override
    public int getValue(Environment e) {
        return 0;
    }
}
