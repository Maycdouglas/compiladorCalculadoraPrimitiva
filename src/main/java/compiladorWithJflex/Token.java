package compiladorWithJflex;

public class Token {

    public int line, column;
    public TOKEN_TYPE type;
    public String lexeme;
    public Object info;

    public Token(TOKEN_TYPE type, String lexeme, Object info, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.info = info;
        this.line = line;
        this.column = column;
    }

    public Token(TOKEN_TYPE type, String lexeme, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.info = null;
        this.line = line;
        this.column = column;
    }

    public Token(TOKEN_TYPE type, Object info, int line, int column) {
        this.type = type;
        this.lexeme = "";
        this.info = info;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return "[(" + line + "," + column + ") \"" + lexeme + "\" : <" + (info == null ? "" : info.toString()) + ">]";
    }

}
