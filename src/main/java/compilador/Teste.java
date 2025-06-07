package compilador;

import java.io.IOException;

public class Teste {

    public static void main(String[] args) throws IOException {
        Scanner lex = new Scanner(args[0]);
        Token token = lex.nextToken();
        while(token.getType() != Token.EOF) {
            System.out.println("Token: " + token.getType() + " lexeme: " + token.getLexeme());
            token = lex.nextToken();
        }
    }

}
