// Analisador Léxico

// Implementação dirigida por tabela

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.Stack;

public class Scanner {


    // classe auxiliar
    private class CharInput {
        int line, column;
        char ch;

        CharInput(char ch, int line, int column) {
            this.line = line;
            this.column = column;
            this.ch = ch;
        }
    }

    private Stack<CharInput> buffer;

    private PushbackInputStream file;
    private int line, column;
    private String lexeme;

    private Stack<Integer> stack; // pilha dos estados sendo processador na simulação do autômato

    private boolean flag_eof = false;

    // Estados do automato

    private final int ST_INIT = 0; // estado inicial do autômato
    private final int ST_1 = 1;
    private final int ST_2 = 2;
    private final int ST_3 = 3;
    private final int ST_4 = 4;
    private final int ST_VAR = 5;
    private final int ST_INT = 6;
    private final int ST_PLUS = 7;
    private final int ST_MULT = 8;
    private final int ST_EQ = 9;
    private final int ST_SEMI = 10;
    private final int ST_EOF = 11;
    private final int ST_SKIP = 12;
    private final int ST_ERROR = 13;

    private final int ST_BAD = -2;

    // Tipos de Token
    private final int Type[] = {Token.VAR, Token.INT, Token.PLUS, Token.MULT, Token.EQ, Token.SEMI, Token.EOF};

    // Categorias
    private final int CAT_EOF = 0; // END OF FILE - fim do arquivo
    private final int CAT_LETTER = 1; // a..z and A..Z
    private final int CAT_DIGIT = 2; // 0,1,...,9
    private final int CAT_PLUS = 3; // +
    private final int CAT_MULT = 4; // *
    private final int CAT_EQ = 5; // =
    private final int CAT_SEMI = 6; // ;
    private final int CAT_DIV = 7; // / barra
    private final int CAT_BKL = 8; // BREAK LINE - quebra de linha
    private final int CAT_WS = 9; // WHITE SPACE - espaço branco
    private final int CAT_ANY = 10;

    // Função de Transição
    private final int[][] transitionFunction;

    public Scanner(String path) throws IOException {
        file = new PushbackInputStream(new FileInputStream(path));

        buffer = new Stack<CharInput>();

        // 13 estados (exceto o estado de erro e 11 terminais)
        transitionFunction = new int[13][11];

        // Transições do Estado Inicial
        transitionFunction[ST_INIT][CAT_EOF] = ST_EOF;
        transitionFunction[ST_INIT][CAT_LETTER] = ST_VAR;
        transitionFunction[ST_INIT][CAT_DIGIT] = ST_INT;
        transitionFunction[ST_INIT][CAT_PLUS] = ST_PLUS;
        transitionFunction[ST_INIT][CAT_MULT] = ST_MULT;
        transitionFunction[ST_INIT][CAT_EQ] = ST_EQ;
        transitionFunction[ST_INIT][CAT_SEMI] = ST_SEMI;
        transitionFunction[ST_INIT][CAT_DIV] = ST_1;
        transitionFunction[ST_INIT][CAT_BKL] = ST_SKIP;
        transitionFunction[ST_INIT][CAT_WS] = ST_SKIP;
        transitionFunction[ST_INIT][CAT_ANY] = ST_ERROR;

        // Transições do Estado 1 // Estado onde inicia o primeiro indicativo de comentário em linha ou em bloco
        transitionFunction[ST_1][CAT_EOF] = ST_ERROR;
        transitionFunction[ST_1][CAT_LETTER] = ST_ERROR;
        transitionFunction[ST_1][CAT_DIGIT] = ST_ERROR;
        transitionFunction[ST_1][CAT_PLUS] = ST_ERROR;
        transitionFunction[ST_1][CAT_MULT] = ST_3;
        transitionFunction[ST_1][CAT_EQ] = ST_ERROR;
        transitionFunction[ST_1][CAT_SEMI] = ST_ERROR;
        transitionFunction[ST_1][CAT_DIV] = ST_2;
        transitionFunction[ST_1][CAT_BKL] = ST_ERROR;
        transitionFunction[ST_1][CAT_WS] = ST_ERROR;
        transitionFunction[ST_1][CAT_ANY] = ST_ERROR;

        // Transições do Estado 2 - Estado onde inicia o comentário em linha em si
        transitionFunction[ST_2][CAT_EOF] = ST_SKIP;
        transitionFunction[ST_2][CAT_LETTER] = ST_2;
        transitionFunction[ST_2][CAT_DIGIT] = ST_2;
        transitionFunction[ST_2][CAT_PLUS] = ST_2;
        transitionFunction[ST_2][CAT_MULT] = ST_2;
        transitionFunction[ST_2][CAT_EQ] = ST_2;
        transitionFunction[ST_2][CAT_SEMI] = ST_2;
        transitionFunction[ST_2][CAT_DIV] = ST_2;
        transitionFunction[ST_2][CAT_BKL] = ST_SKIP;
        transitionFunction[ST_2][CAT_WS] = ST_2;
        transitionFunction[ST_2][CAT_ANY] = ST_2;

        // Transições do Estado 3 - Estado onde inicia o comentário em bloco em si
        transitionFunction[ST_3][CAT_EOF] = ST_3;
        transitionFunction[ST_3][CAT_LETTER] = ST_3;
        transitionFunction[ST_3][CAT_DIGIT] = ST_3;
        transitionFunction[ST_3][CAT_PLUS] = ST_3;
        transitionFunction[ST_3][CAT_MULT] = ST_4;
        transitionFunction[ST_3][CAT_EQ] = ST_3;
        transitionFunction[ST_3][CAT_SEMI] = ST_3;
        transitionFunction[ST_3][CAT_DIV] = ST_3;
        transitionFunction[ST_3][CAT_BKL] = ST_3;
        transitionFunction[ST_3][CAT_WS] = ST_3;
        transitionFunction[ST_3][CAT_ANY] = ST_3;

        // Transições do Estado 4 - Estado onde pode iniciar a finalização do comentário em bloco
        transitionFunction[ST_4][CAT_EOF] = ST_ERROR; // pois não fechou o comentário em bloco
        transitionFunction[ST_4][CAT_LETTER] = ST_3;
        transitionFunction[ST_4][CAT_DIGIT] = ST_3;
        transitionFunction[ST_4][CAT_PLUS] = ST_3;
        transitionFunction[ST_4][CAT_MULT] = ST_4;
        transitionFunction[ST_4][CAT_EQ] = ST_3;
        transitionFunction[ST_4][CAT_SEMI] = ST_3;
        transitionFunction[ST_4][CAT_DIV] = ST_SKIP;
        transitionFunction[ST_4][CAT_BKL] = ST_3;
        transitionFunction[ST_4][CAT_WS] = ST_3;
        transitionFunction[ST_4][CAT_ANY] = ST_3;

        // Transições do Estado VAR
        transitionFunction[ST_VAR][CAT_EOF] = ST_ERROR;
        transitionFunction[ST_VAR][CAT_LETTER] = ST_ERROR;
        transitionFunction[ST_VAR][CAT_DIGIT] = ST_ERROR;
        transitionFunction[ST_VAR][CAT_PLUS] = ST_ERROR;
        transitionFunction[ST_VAR][CAT_MULT] = ST_ERROR;
        transitionFunction[ST_VAR][CAT_EQ] = ST_ERROR;
        transitionFunction[ST_VAR][CAT_SEMI] = ST_ERROR;
        transitionFunction[ST_VAR][CAT_DIV] = ST_ERROR;
        transitionFunction[ST_VAR][CAT_BKL] = ST_ERROR;
        transitionFunction[ST_VAR][CAT_WS] = ST_ERROR;
        transitionFunction[ST_VAR][CAT_ANY] = ST_ERROR;

        // Transições do Estado INT
        transitionFunction[ST_INT][CAT_EOF] = ST_ERROR;
        transitionFunction[ST_INT][CAT_LETTER] = ST_ERROR;
        transitionFunction[ST_INT][CAT_DIGIT] = ST_INT;
        transitionFunction[ST_INT][CAT_PLUS] = ST_ERROR;
        transitionFunction[ST_INT][CAT_MULT] = ST_ERROR;
        transitionFunction[ST_INT][CAT_EQ] = ST_ERROR;
        transitionFunction[ST_INT][CAT_SEMI] = ST_ERROR;
        transitionFunction[ST_INT][CAT_DIV] = ST_ERROR;
        transitionFunction[ST_INT][CAT_BKL] = ST_ERROR;
        transitionFunction[ST_INT][CAT_WS] = ST_ERROR;
        transitionFunction[ST_INT][CAT_ANY] = ST_ERROR;

        // Transições do Estado PLUS
        transitionFunction[ST_PLUS][CAT_EOF] = ST_ERROR;
        transitionFunction[ST_PLUS][CAT_LETTER] = ST_ERROR;
        transitionFunction[ST_PLUS][CAT_DIGIT] = ST_ERROR;
        transitionFunction[ST_PLUS][CAT_PLUS] = ST_ERROR;
        transitionFunction[ST_PLUS][CAT_MULT] = ST_ERROR;
        transitionFunction[ST_PLUS][CAT_EQ] = ST_ERROR;
        transitionFunction[ST_PLUS][CAT_SEMI] = ST_ERROR;
        transitionFunction[ST_PLUS][CAT_DIV] = ST_ERROR;
        transitionFunction[ST_PLUS][CAT_BKL] = ST_ERROR;
        transitionFunction[ST_PLUS][CAT_WS] = ST_ERROR;
        transitionFunction[ST_PLUS][CAT_ANY] = ST_ERROR;

        // Transições do Estado MULT
        transitionFunction[ST_MULT][CAT_EOF] = ST_ERROR;
        transitionFunction[ST_MULT][CAT_LETTER] = ST_ERROR;
        transitionFunction[ST_MULT][CAT_DIGIT] = ST_ERROR;
        transitionFunction[ST_MULT][CAT_PLUS] = ST_ERROR;
        transitionFunction[ST_MULT][CAT_MULT] = ST_ERROR;
        transitionFunction[ST_MULT][CAT_EQ] = ST_ERROR;
        transitionFunction[ST_MULT][CAT_SEMI] = ST_ERROR;
        transitionFunction[ST_MULT][CAT_DIV] = ST_ERROR;
        transitionFunction[ST_MULT][CAT_BKL] = ST_ERROR;
        transitionFunction[ST_MULT][CAT_WS] = ST_ERROR;
        transitionFunction[ST_MULT][CAT_ANY] = ST_ERROR;

        // Transições do Estado EQ
        transitionFunction[ST_EQ][CAT_EOF] = ST_ERROR;
        transitionFunction[ST_EQ][CAT_LETTER] = ST_ERROR;
        transitionFunction[ST_EQ][CAT_DIGIT] = ST_ERROR;
        transitionFunction[ST_EQ][CAT_PLUS] = ST_ERROR;
        transitionFunction[ST_EQ][CAT_MULT] = ST_ERROR;
        transitionFunction[ST_EQ][CAT_EQ] = ST_ERROR;
        transitionFunction[ST_EQ][CAT_SEMI] = ST_ERROR;
        transitionFunction[ST_EQ][CAT_DIV] = ST_ERROR;
        transitionFunction[ST_EQ][CAT_BKL] = ST_ERROR;
        transitionFunction[ST_EQ][CAT_WS] = ST_ERROR;
        transitionFunction[ST_EQ][CAT_ANY] = ST_ERROR;

        // Transições do Estado SEMI
        transitionFunction[ST_SEMI][CAT_EOF] = ST_ERROR;
        transitionFunction[ST_SEMI][CAT_LETTER] = ST_ERROR;
        transitionFunction[ST_SEMI][CAT_DIGIT] = ST_ERROR;
        transitionFunction[ST_SEMI][CAT_PLUS] = ST_ERROR;
        transitionFunction[ST_SEMI][CAT_MULT] = ST_ERROR;
        transitionFunction[ST_SEMI][CAT_EQ] = ST_ERROR;
        transitionFunction[ST_SEMI][CAT_SEMI] = ST_ERROR;
        transitionFunction[ST_SEMI][CAT_DIV] = ST_ERROR;
        transitionFunction[ST_SEMI][CAT_BKL] = ST_ERROR;
        transitionFunction[ST_SEMI][CAT_WS] = ST_ERROR;
        transitionFunction[ST_SEMI][CAT_ANY] = ST_ERROR;

        // Transições do Estado SKIP
        transitionFunction[ST_SKIP][CAT_EOF] = ST_ERROR;
        transitionFunction[ST_SKIP][CAT_LETTER] = ST_ERROR;
        transitionFunction[ST_SKIP][CAT_DIGIT] = ST_ERROR;
        transitionFunction[ST_SKIP][CAT_PLUS] = ST_ERROR;
        transitionFunction[ST_SKIP][CAT_MULT] = ST_ERROR;
        transitionFunction[ST_SKIP][CAT_EQ] = ST_ERROR;
        transitionFunction[ST_SKIP][CAT_SEMI] = ST_ERROR;
        transitionFunction[ST_SKIP][CAT_DIV] = ST_ERROR;
        transitionFunction[ST_SKIP][CAT_BKL] = ST_SKIP;
        transitionFunction[ST_SKIP][CAT_WS] = ST_SKIP;
        transitionFunction[ST_SKIP][CAT_ANY] = ST_ERROR;

        // Transições do Estado EOF
        transitionFunction[ST_EOF][CAT_EOF] = ST_ERROR;
        transitionFunction[ST_EOF][CAT_LETTER] = ST_ERROR;
        transitionFunction[ST_EOF][CAT_DIGIT] = ST_ERROR;
        transitionFunction[ST_EOF][CAT_PLUS] = ST_ERROR;
        transitionFunction[ST_EOF][CAT_MULT] = ST_ERROR;
        transitionFunction[ST_EOF][CAT_EQ] = ST_ERROR;
        transitionFunction[ST_EOF][CAT_SEMI] = ST_ERROR;
        transitionFunction[ST_EOF][CAT_DIV] = ST_ERROR;
        transitionFunction[ST_EOF][CAT_BKL] = ST_ERROR;
        transitionFunction[ST_EOF][CAT_WS] = ST_ERROR;
        transitionFunction[ST_EOF][CAT_ANY] = ST_ERROR;

    }

    // Identifica qual a categoria do char lido
    private int charCat(int ch) {
        if(flag_eof){
            return CAT_EOF;
        }
        if(ch >= 'a' && ch <= 'z'){
            return CAT_LETTER;
        }
        if(ch >= '0' && ch <= '9'){
            return CAT_DIGIT;
        }
        switch (ch) {
            case '+':
                return CAT_PLUS;
            case '*':
                return CAT_MULT;
            case '=':
                return CAT_EQ;
            case ';':
                return CAT_SEMI;
            case '/':
                return CAT_DIV;
            case '\n':
                return CAT_BKL;
            case ' ':
            case '\t':
            case '\r':
                return CAT_WS;
            default:
                return CAT_ANY;
        }
    }

    private boolean isFinal(int state){

        if(state >= ST_VAR && state < ST_SKIP || state == ST_EOF){
            return true;
        }

        return false;
    }

    private boolean isSkip(int state){
        return state == ST_SKIP;
    }

    private char nextChar() throws IOException{
        int ch = file.read(); // armazena o char lido como um inteiro
        if(ch == -1) { //-1 representa o inteiro que o fim de um arquivo retorna
            flag_eof = true;
            return '\0';
        }
        if(ch == 10) { // 10 é o valor do \n (quebra de linha) como inteiro no código ASCII
            line++;
            column = 0;
        } else {
            column++;
        }

        buffer.push(new CharInput((char) ch, line, column));
        return (char) ch;
    }

    // função para voltar um caractere no processo de leitura
    private void rollback() throws IOException{

        // obtem linha e coluna do caracter do topo da pilha, sem remove-lo
        line = buffer.peek().line;
        column = buffer.peek().column;

        // insere o caracter do topo da linha de volta ao arquivo
        file.unread(buffer.pop().ch);

        // remove o ultimo caracter do lexeme, pois faz parte do lexeme atual
        lexeme = lexeme.substring(0, lexeme.length() - 1);
    }

    private void runAFD(int state) throws IOException {

        // inicializa
        lexeme = "";
        stack.clear();
        stack.push(ST_BAD);

        // processa
        char ch;
        int category;

        while(state != ST_ERROR) {
            ch = nextChar();
            lexeme = lexeme + ch;
            stack.push(state);
            category = charCat(ch);
            state = transitionFunction[state][category];
        }

        // fez uma transição a mais
        rollback();
    }

    public Token nextToken() throws IOException {

        if(flag_eof){
            return new Token(Token.EOF, "", line, column);
        }

        int state = ST_INIT;

        do {
            runAFD(state);
        } while(isSkip(stack.peek()));

        state = stack.pop();

        while(!isFinal(state) && state != ST_BAD) {
            state = stack.pop();
            rollback();
        }

        if(!isFinal(state)){
            buffer.clear();
            return new Token(Type[state - ST_VAR], lexeme, line, column);
        } else {
            System.out.println("Error: INVALID TOKEK AT LINE " + line + ", COLUMN " + column);
            System.exit(1);
        }

        return null;
    }

}
