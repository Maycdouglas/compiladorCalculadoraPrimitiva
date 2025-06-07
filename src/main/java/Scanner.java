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

    private final int ST_BAD = -1;

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
        transitionFunction = new int [13][11];

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
        transitionFunction[ST_PLUS][CAT_EOF] = ST_ERROR;
        transitionFunction[ST_PLUS][CAT_LETTER] = ST_ERROR;
        transitionFunction[ST_PLUS][CAT_DIGIT] = ST_ERROR;
        transitionFunction[ST_PLUS][CAT_PLUS] = ST_ERROR;
        transitionFunction[ST_PLUS][CAT_MULT] = ST_ERROR;
        transitionFunction[ST_PLUS][CAT_EQ] = ST_ERROR;
        transitionFunction[ST_PLUS][CAT_SEMI] = ST_ERROR;
        transitionFunction[ST_PLUS][CAT_DIV] = ST_ERROR;
        transitionFunction[ST_PLUS][CAT_BKL] = ST_SKIP;
        transitionFunction[ST_PLUS][CAT_WS] = ST_SKIP;
        transitionFunction[ST_PLUS][CAT_ANY] = ST_ERROR;

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






    private boolean isFinal(int state){
        return false;
    }

    private void runAFD(int state) {

    }

    public Token nextToken() {
        int state = ST_INIT;
        runAFD(state);

        state = stack.pop();
        if(!isFinal(state) && state != ST_BAD){

        }
    }

}
