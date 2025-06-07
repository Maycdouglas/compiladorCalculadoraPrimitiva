public class Token {

    // static + final cria uma constante global
    public static final int VAR = 1; // a,b,c...
    public static final int INT = 2; // 1,2,3...
    public static final int EQ = 3; // =
    public static final int SEMI = 4; // ;
    public static final int PLUS = 5; // +
    public static final int MULT = 6; // *
    public static final int EOF = -1; // end of line

    private int line, col;
    private int type;
    private String lexeme;

    public Token(int type, String lexeme, int line, int col) {
        this.line = line;
        this.col = col;
        this.type = type;
        this.lexeme = lexeme;
    }

}

