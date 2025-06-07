# Compilador de Calculadora Primitiva

### Comandos:

`javac -d target/classes src/main/java/compilador/*.java` - Compilar projeto
- `javac`: compilador **JAVA**. Compila os arquivos `.java` para `.class`
- `-d target/classes`: define o diretório de saída dos arquivos `.class` compilados
  - `-d` significa *destiny*
- `src/main/java/compilador/*.java`: indica quais arquivos **JAVA** devem ser compilados 
  - `*` significa que são todos arquivos `.java` daquele diretório

`java -cp target/classes compilador.Teste src/main/sample1.txt` - Executar projeto
- `java`: é o interpretador da **JVM**. Executa um programa **JAVA** compilado
- `-cp target/classes`: Define o local onde estão os arquivos `.class`
  - `-cp` significa *class path*
- `compilador.Teste`: é o nome completo (com pacote) da classe que possui o `public static void main(String[] args)`
- `src/main/sample1.txt`: é um argumento passado ao programa
  - geralmente usado dentro do `args[0]` no método `main`

### Automato:

![Captura de tela 2025-06-07 083556](https://github.com/user-attachments/assets/7789296f-d87d-44c3-9bc7-e247d8e13df5)

### Exemplo de arquivo de entrada:

![image](https://github.com/user-attachments/assets/752b4f25-04e3-442b-8fa4-95b56b2850f7)
