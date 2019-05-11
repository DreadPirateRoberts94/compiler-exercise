package models.Utilities;


public class Tuple<Var, Type, Value> {
    public final Var var;
    public final Type type;
    public final Value value;
    public Tuple(Var var, Type type, Value value) {
        this.var = var;
        this.type = type;
        this.value = value;
    }
}
