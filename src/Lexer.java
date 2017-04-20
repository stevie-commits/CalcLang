/*  an instance of this class provides methods that produce a
    sequence of tokens following some Finite State Automata,
    with capability to put back tokens
*/

import java.util.*;
import java.io.*;

public class Lexer {
    public static String margin = "";

    private static String[] keywords = {"msg", "show", "newline", "input"};
    private static String[] bifs = {"sqrt", "exp", "sin", "cos"};


    // holds any number of tokens that have been put back
    private Stack<Token> stack;
    // the source of physical symbols
    private BufferedReader input;
    // one lookahead physical symbol
    private int lookahead;

    // construct a Lexer ready to produce tokens from a file
    public Lexer(String fileName) {
        try {
            input = new BufferedReader(new FileReader(fileName));
        } catch (Exception e) {
            error("Problem opening file named [" + fileName + "]");
        }

        stack = new Stack<Token>();
        lookahead = 0;  // indicates no lookahead symbol present
    }// constructor

    // produce the next token
    public Token getNext() {
        if (!stack.empty()) {
            //  produce the most recently putback token
            Token token = stack.pop();
            return token;
        } else {
            // produce a token from the input source
            int state = 1;  // state of DFA
            String data = "";  // specific info for the token
            boolean done = false;
            int sym;  // holds current symbol

            do {
                sym = getNextSymbol();

                if (state == 1) {
                    if (sym == 10 || sym == 13 || sym == 32) {
                        // state stays at 1
//                    } else if (sym == '#') {
//                        state = 2;
                    } else if (letter(sym)) {
                        state = 4;
                        data += (char) sym;
                    } else if (digit(sym)) {
                        state = 6;
                        data += (char) sym;
//                    } else if (sym == '-') {
//                        state = 5;
//                        data += "-";
                    } else if (sym == '\"') {
                        state = 7;
                    } else if (sym == '\'') {
                        state = 9;
                    } else if (sym == '=' || sym == '(' || sym == ')' ||
                            sym == '+' || sym == '-' || sym == '*' || sym == '/') {
                        state = 12;
                        data += (char) sym;
                    } else if (sym == -1) {// eof
                        state = 13;
                    } else {
                        error("Error in lexical analysis phase with symbol "
                                + sym + " in state " + state);
                    }
                }// state 1 ==================================================
                else if (state == 2) {
                    if (printable(sym)) {
                        // state stays at  2;
                    } else if (sym == 13) {
                        // toss it, stay in state 2
                    } else if (sym == 10 || sym == -1) {
                        state = 3;
                    } else {
                        error("error in FA in state 2---illegal symbol " +
                                sym + " in comment");
                    }
                }//=====================================================
                else if (state == 3) {// comment finished, but no token produced,
                    // so restart
                    System.out.println("changing to state 1");
                    state = 1;
                    data = "";
                    putBackSymbol(sym);
                }//=====================================================
                else if (state == 4) {
                    if (letter(sym) || digit(sym)) {
                        // stay in state 4
                        data += (char) sym;
                    } else {
                        done = true;
                        putBackSymbol(sym);
                    }
                }//=====================================================
                else if (state == 5) {
                    if (digit(sym)) {
                        data += (char) sym;
                    }
                    else {
                        done = true;
                        putBackSymbol(sym);
                    }
                }// =====================================================
                else if (state == 6) {
                    if (digit(sym) ) {
                        // stay in state 6
                        data += (char) sym;
                    } else if ( sym == '.' ) {
                        state = 5;
                        data += (char) sym;
                    }
                    else {
                        done = true;
                        putBackSymbol(sym);
                    }
                }// =====================================================
                else if (state == 7) {
                    if (sym == '\"') {
                        state = 8;
                    } else if (printable(sym)) {
                        // stay in state 7;
                        data += (char) sym;
                    } else {
                        error("unclosed string literal");
                    }
                }// =====================================================
                else if (state == 8) {
                    done = true;
                    putBackSymbol(sym);
                }// =====================================================
                else if (state == 9) {
                    // in Otter, ''' is legal!
                    if (printable(sym)) {
                        data += (char) sym;
                        state = 10;
                    } else {
                        error("illegal symbol " + sym + "after \' in state 9");
                    }
                }// =====================================================
                else if (state == 10) {
                    if (sym == '\'')
                        state = 11;
                    else {
                        error("illegal symbol " + sym + " when expecting closing \'");
                    }
                }// =====================================================
                else if (state == 11) {
                    done = true;
                    putBackSymbol(sym);
                }// =====================================================
                else if (state == 12) {
                    done = true;
                    putBackSymbol(sym);
                }// =====================================================
                else if (state == 13) {
                    done = true;
                }// ====================================================
                else {
                    error("Unknown state " + state + " in Lexer");
                }

            } while (!done);

            // generate token depending on stopping state
            Token token;

            if (state == 4) {// reserved word, bifs, or user-defined id
                for (int k = 0; k < keywords.length; k++)
                    if (keywords[k].equals(data)) {
                        token = new Token(data, "");
                        return token;
                    }

                for (int k = 0; k < bifs.length; k++)
                    if (bifs[k].equals(data)) {
                        token = new Token("bifs", data);
                        return token;
                    }
                token = new Token("id", data);
                return token;
            } else if (state == 5) {
                token = new Token ( "num", data );
                return token;
            }
            else if (state == 6) {// numeric token
                token = new Token("num", data);
                return token;
            } else if (state == 8) {// string literal
                token = new Token("string", data);
                return token;
            } else if (state == 11) {// char literal
                token = new Token("num", "" + (int) data.charAt(0));
                return token;
            } else if (state == 12) {// special single symbol
                token = new Token("single", data);
                return token;
            } else if (state == 13) {// eof
                token = new Token("eof", "");
                return token;
            } else {// Lexer error
                error("somehow Lexer FA halted in inappropriate state " + state);
                return null;
            }
        }

    }// getNext

    public Token getToken() {
        Token token = getNext();
        //System.out.println("                                   got token: " + token);
        return token;
    }

    public void putBack(Token token) {
        //System.out.println(margin + "put back token " + token.toString());
        stack.push(token);
    }

    // next physical symbol is the lookahead symbol if there is one,
    // otherwise is next symbol from file
    private int getNextSymbol() {
        int result = -1;

        if (lookahead == 0) {// is no lookahead, use input
            try {
                result = input.read();
            } catch (Exception e) {
            }
        } else {// use the lookahead and consume it
            result = lookahead;
            lookahead = 0;
        }
        return result;
    }

    private void putBackSymbol(int sym) {
        if (lookahead == 0) {// sensible to put one back
            lookahead = sym;
        } else {
            System.out.println("Oops, already have a lookahead " + lookahead +
                    " when trying to put back symbol " + sym);
            System.exit(1);
        }
    }// putBackSymbol

    private boolean letter(int code) {
        return 'a' <= code && code <= 'z' ||
                'A' <= code && code <= 'Z';
    }

    private boolean digit(int code) {
        return '0' <= code && code <= '9';
    }

    private boolean printable(int code) {
        return ' ' <= code && code <= '~';
    }

    private static void error(String message) {
        System.out.println(message);
        System.exit(1);
    }

    public static void main(String[] args) throws Exception {
        System.out.print("Enter file name: ");
        Scanner keys = new Scanner(System.in);
        String name = keys.nextLine();

        Lexer lex = new Lexer(name);
        Token token;

        do {
            token = lex.getNext();
            System.out.println(token.toString());
        } while (!token.getKind().equals("eof"));

    }

}
