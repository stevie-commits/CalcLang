//package CS3130PPL.Joint;

/*
  implement whole process
  of taking a Joint source file
  and producing translation to
  a VPL file
*/

import java.util.Scanner;

public class CalcLang {

    public static void main(String[] args) throws Exception {
        Scanner keys = new Scanner(System.in);
        System.out.print("Enter Joint file name: ");
        String name = keys.nextLine();
        Lexer lex = new Lexer(name);
        Parser parser = new Parser(lex);

        Node root = parser.parseProgram();

        TreeViewer viewer = new TreeViewer("Parse Tree", 0, 0, 1200, 800, root);

        //TODO: Write evaluate / execute methods.
        root.execute();

    }

}

