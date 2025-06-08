package compiladorWithJflex;

import java.io.FileReader;
import java.io.IOException;

public class Teste {

    public static void main(String[] args) throws IOException {
        LextTest lx = new LextTest(new FileReader(args[0]));
        Token token = lx.nextToken();
        while(token != null){
            System.out.println(token.toString());
            token = lx.nextToken();
        }
        System.out.println("Total de tokens lidos " + lx.readedTokens());
    }

}
