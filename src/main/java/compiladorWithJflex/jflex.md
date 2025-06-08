# Instruções JFLEX  

As seções são separadas por `%%`.

## Comandos
- `java -jar jflex-1.9.1.jar Exemplo.flex`
  - gera a classe do Analisador Léxico

## Seções
### Primeira Seção
- É copiada antes da declaração da classe do analisador léxico. 
- Nesta seção que deve incluir `imports` e `declaração de pacotes`.
### Segunda Seção
- Determina parâmetros que serão utilizados pelo JFLEX.
  - `%unicode`: determina que irá trabalhar com caracteres `unicode`
  - `%line`: para monitorar as linhas
  - `%column`: para monitorar as colunas
  - `%class NomeClasse`: define o nome da classe JAVA que será criada
  - `%function nomeFuncaoProximoToken`: define o nome da funcao que faz a análise léxica
  - `%type NomeClasseToken`: define o tipo do retorno da função de análise léxica
  - `%{}%`: define código que você deseja implementar na classe
  - `%init{ %init}`: código que deve ser inserido no construtor da classe gerada
  - `Macro`: determina expressões regulares com um nome pré-definido
    - Exemplos: 
      - FimDeLinha = \r|\n|\r\n
      - Brancos = {FimDeLinha} | [ \t\f]
      - numero = [:digit:] [:digit:]*
      - identificador = [:lowercase:]
      - letras = [:letter:]
      - maiusculas = [:upercase:]
### Terceira Seção
- `<YYNITIAL>{}`: É como se fosse um `if else`. 
- Determina o que fazer de acordo com o token lido
  - `{identificador}  {return symbol(TOKEN_TYPE.ID);}`
  - `{numero} {return symbol(TOKEN_TYPE.NUM, Integer.parseInt(yytext()) );}`
  - `"=" {return symbol(TOKEN_TYPE.EQ);}`
  - `{Brancos} { /* Não faz nada */ }` 
- `[^] {}`: é como se fosse um `else`
- Determina o que fazer quando não encaixou com nenhum tipo anterior