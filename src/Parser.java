/*
    This class provides a recursive descent parser of Joint
    (a joint project of both sections, very similar to both
     Otter and Blunt),
    creating a parse tree which is then translated to
    VPL code
*/

import java.util.*;
import java.io.*;

public class Parser {

    private Lexer lex;

    public Parser(Lexer lexer) {
        lex = lexer;
    }

    public static void main(String[] args) throws Exception {
        System.out.print("Enter file name: ");
        Scanner keys = new Scanner(System.in);
        String name = keys.nextLine();
        Lexer lex = new Lexer(name);
        Parser parser = new Parser(lex);

        Node root = parser.parseStatement();  // parser.parseProgram();

        TreeViewer viewer = new TreeViewer("Parse Tree", 0, 0, 800, 500, root);

    }

    public Node parseProgram() {
        //System.out.println("-----> parsing program:");

        Token token = lex.getToken();

        if (token.isKind("eof")) {
            return new Node(token);
        } else {// no globals
            lex.putBack(token);
        }

        Node statements = parseStatements();

        Node root = new Node("program", statements, null, null);
        return root;
    }

    private Node parseExpression() {
        //System.out.println("-----> parsing expression:");
        Token token1 = lex.getToken();
        if (token1.isKind("num") || token1.isKind("id")) {
            Token token2 = lex.getNext();

            if (!token2.isKind("single")) {// IF THERE IS NO SINGLE AFTER
                lex.putBack(token2);
                lex.putBack(token1);
                Node first = parseTerm();
                return new Node("expression", first, null, null);
            } else if (token2.isKind("single")) {// if a number with a single after
                if (token2.matches("single", "-") || token2.matches("single", "+") ) {
                    lex.putBack(token1);
                    Node first = parseTerm();
                    Node second = new Node(token2);
                    Node third = parseExpression();
                    return new Node("expression", first, second, third);
                } else {
                    lex.putBack(token2);
                    lex.putBack(token1);
                    Node first = parseTerm();
                    return new Node("expression", first, null, null);
                }
            }

        } else if (token1.isKind("single") ) {
            lex.putBack(token1);
            Node first = parseTerm();
        } else if (token1.isKind("bifs") ) {
            lex.putBack(token1);
            Node first = parseTerm();
            return new Node ("expression", first, null, null);
        }

        return new Node(token1);
    }// parseExpression

    private Node parseTerm() {
        //System.out.println("-----> parsing Term:");
        Token token1 = lex.getToken();
        if (token1.isKind("num") || token1.isKind("id")) {
            Token token2 = lex.getNext();

            if (!token2.isKind("single")) {// if number is term
                lex.putBack(token2);
                lex.putBack(token1);
                Node first = parseFactor();
                return new Node("term", first, null, null);
            } else if (token2.isKind("single")) {// if a number with a single after
                if (token2.matches("single", "*") || token2.matches("single", "/") ) {
                    lex.putBack(token1);
                    Node first = parseFactor();
                    Node second = new Node(token2);
                    Node third = parseTerm();
                    return new Node("term", first, second, third);
                } else if (token2.matches("single", ")" ) ) {
                    lex.putBack(token2);
                    lex.putBack(token1);
                    Node first = parseFactor();
                    return new Node ("term", first, null, null);
                }
            } else {
                lex.putBack(token2);
                lex.putBack(token1);
                Node first = parseFactor();
                return new Node("term", first, null, null);
            }
        } else if (token1.isKind("bifs") ){
            lex.putBack(token1);
            Node first = parseFactor();
            return new Node("term", first, null, null);
        } else if (token1.matches("single", "-") ){
            lex.putBack(token1);
            Node first = parseFactor();
            return new Node ("term", first, null, null);
        }
        return new Node(token1);
    }// parseTerm

    private Node parseFactor() {
        //System.out.println("-----> parsing Factor:");
        Token token = lex.getToken(); //id token

        if (token.isKind("id")) {
            // id (single) express
            Token token2 = lex.getToken(); //paran
            if (token2.matches("single", "(")) {
                Token token3 = lex.getToken();
                Node first = new Node(token);
                Node second = new Node(token2);
                Node third = parseExpression();
                Token token4 = lex.getToken();
                errorCheck(token4, "single", ")");
                return new Node("Factor", first, second, third);
            }
            // id only
            else {
                lex.putBack(token2);
                Node first = new Node(token);
                return new Node("factor", first, null, null);
            }
        } else if (token.isKind("num")) {
            Node first = new Node(token);
            return new Node("factor", first, null, null);
        } else if (token.matches("single", "(")) {
            Node first = new Node(token);
            Node second = parseExpression();
            Token token2 = lex.getToken();
            errorCheck(token2, ")");
            Node third = new Node(token2);
            return new Node("factor", first, second, third);
        } else if (token.matches("single", "-")) {
            Node first = new Node(token);
            Node second = parseFactor();
            return new Node("factor", first, second, null);
        } else if (token.isKind("bifs") ) {
            Node first = new Node(token);//bif function
            Token token2 = lex.getToken();
            Node second = parseExpression();
            Token token3 = lex.getToken();
            return new Node ("factor", first, second, null);
        }

        return new Node(token);

    }// parseFactor

    private Node parseStatements() {
        //System.out.println("-----> parsing statements:");
        Node first = parseStatement();
        Token token = lex.getToken();
        if (token.isKind("eof")) {
            Node eofNode = new Node(token);
            return new Node("statements", first, eofNode, null);
        } else {
            lex.putBack(token);
            Node second = parseStatements();
            return new Node("statements", first, second, null);
        }
    }

    private Node parseStatement() {
        //System.out.println("-----> parsing statement:");

        Token token = lex.getToken();
        Node first = null, second = null, third = null;
        String info = "";   // specify info for <statement> node

        if (token.isKind("id")) {
            Token token2 = lex.getToken();

            if (token2.matches("single", "=")) {// must equal sign
                info = "store";
                first = new Node(token);
                second = new Node(token2);
                third = parseExpression();
                return new Node("statement", first, second, third);
            } else if (token2.isKind("single")) {

            } else {// error
                System.out.println("Error:  illegal first token " + token + " to start <statement>");
                System.exit(1);
            }
        } else if (token.isKind("msg") ) {
            Token token2 = lex.getToken();
            errorCheck(token2, "string");
            //lex.putBack(token2);
            first = new Node(token);
            second = new Node(token2);
            return new Node("statement", first, second, null);
        } else if (token.isKind("show") ) {
            first = new Node (token);
            second = parseExpression();
            third = parseExpression();
            return new Node ( "statement", first, second, third);
        } else if (token.isKind("newline") ) {
            first = new Node (token);
            return new Node ("statement", first, null, null);
        } else if (token.isKind("input") ) {
            first = new Node (token);
            Token token2 = lex.getToken();
            errorCheck(token2, "string");
            second = new Node (token2);
            Token token3 = lex.getToken();
            errorCheck(token3, "id");
            third = new Node (token3);
            return new Node ("statement", first, second, third );
        }
        return new Node("statement", info, first, second, third);
    }//parseStatement

    // check whether token is correct kind
    private void errorCheck(Token token, String kind) {
        if (!token.isKind(kind)) {
            System.out.println("Error:  expected " + token + " to be of kind " + kind);
            System.exit(1);
        }
    }

    // check whether token is correct kind and details
    private void errorCheck(Token token, String kind, String details) {
        if (!token.isKind(kind) || !token.getDetails().equals(details)) {
            System.out.println("Error:  expected " + token + " to be kind=" + kind + " and details=" + details);
            System.exit(1);
        }
    }

}
