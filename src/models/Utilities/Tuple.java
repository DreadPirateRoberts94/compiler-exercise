package models.Utilities;


public class Tuple<Type, Value> {
    public final Type type;
    public final Value value;
    public Tuple(Type type, Value value) {
        this.type = type;
        this.value = value;
    }
}
