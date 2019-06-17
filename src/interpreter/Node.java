package interpreter;

public class Node {
    private String instr;
    private String arg1;
    private Integer offset;
    private String arg2;
    private String arg3;

    public Node(String instr, String arg1, Integer offset, String arg2, String arg3){
        this.instr = instr;
        this.arg1 = arg1;
        this.offset = offset;
        this.arg2 = arg2;
        this.arg3 = arg3;
    }

    public String getInstr(){
        return this.instr;
    }

    public String getArg1(){
        return this.arg1;
    }

    public Integer getOffset(){
        return  this.offset;
    }

    public String getArg2(){
        return this.arg2;
    }

    public String getArg3(){
        return this.arg3;
    }

}

