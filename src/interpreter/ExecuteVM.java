package interpreter;

public class ExecuteVM {
    public static final int CODESIZE = 10000;
    public static final int MEMSIZE = 10000;
    private Node[] code;
    private int[] memory = new int[MEMSIZE];
    private int ip = 0;         // ip = instruction pointer
    private int sp = MEMSIZE;   // sp = stack pointer
    private int fp = MEMSIZE;   // fp = frame pointer
    private int ra;             // ra = return address
    private int al;             // al = access link
    private int a;              // a = stores the value of an exp
    private int t;              // t = temporary register

    public ExecuteVM(Node[] code) {
        this.code = code;
    }


    public void run() {
        while (true) {
            Node bytecode = code[ip++]; // fetch
            int v1, v2;
            int address;

            switch (bytecode.getInstr()) {
                case ("printi"):
                    System.out.println(bytecode.getArg1());
                    break;

                case ("HALT"):
                    return;
            }
        }
    }
}
