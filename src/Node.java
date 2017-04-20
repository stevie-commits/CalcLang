/*  a Node holds one node of a parse tree
    with several pointers to children used
    depending on the kind of node
*/

import javax.naming.Name;
import java.util.*;
import java.io.*;
import java.awt.*;

public class Node {
    public static int count = 0;  // maintain unique id for each node

    private int id;

    private String kind;  // non-terminal or terminal category for the node
    private String info;  // extra information about the node such as
    // the actual identifier for an I

    /* need for input code */
    Scanner in = new Scanner(System.in);
    String idString;

    // references to children in the parse tree
    private Node first, second, third;

    // construct a common node with no info specified
    public Node(String k, Node one, Node two, Node three) {
        kind = k;
        info = "";
        first = one;
        second = two;
        third = three;
        id = count;
        count++;
//        System.out.println(this);
    }

    // construct a node with specified info
    public Node(String k, String inf, Node one, Node two, Node three) {
        kind = k;
        info = inf;
        first = one;
        second = two;
        third = three;
        id = count;
        count++;
//        System.out.println(this);
    }

    // construct a node that is essentially a token
    public Node(Token token) {
        kind = token.getKind();
        info = token.getDetails();
        first = null;
        second = null;
        third = null;
        id = count;
        count++;
//        System.out.println(this);
    }

    public String toString() {
        return "#" + id + "[" + kind + "," + info + "]";
    }

    // produce array with the non-null children
    // in order
    private Node[] getChildren() {
        int count = 0;
        if (first != null) count++;
        if (second != null) count++;
        if (third != null) count++;
        Node[] children = new Node[count];
        int k = 0;
        if (first != null) {
            children[k] = first;
            k++;
        }
        if (second != null) {
            children[k] = second;
            k++;
        }
        if (third != null) {
            children[k] = third;
            k++;
        }

        return children;
    }

    //******************************************************
    // graphical display of this node and its subtree
    // in given camera, with specified location (x,y) of this
    // node, and specified distances horizontally and vertically
    // to children
    public void draw(Camera cam, double x, double y, double h, double v) {

        System.out.println("draw node " + id);

        // set drawing color
        cam.setColor(Color.black);

        String text = kind;
        if (!info.equals("")) text += "(" + info + ")";
        cam.drawHorizCenteredText(text, x, y);

        // positioning of children depends on how many
        // in a nice, uniform manner
        Node[] children = getChildren();
        int number = children.length;
        System.out.println("has " + number + " children");

        double top = y - 0.75 * v;

        if (number == 0) {
            return;
        } else if (number == 1) {
            children[0].draw(cam, x, y - v, h / 2.1, v);
            cam.drawLine(x, y, x, top);
        } else if (number == 2) {
            children[0].draw(cam, x - h / 2.1, y - v, h / 2, v);
            cam.drawLine(x, y, x - h / 2, top);
            children[1].draw(cam, x + h / 2, y - v, h / 2, v);
            cam.drawLine(x, y, x + h / 2, top);
        } else if (number == 3) {
            children[0].draw(cam, x - h, y - v, h / 2.1, v);
            cam.drawLine(x, y, x - h, top);
            children[1].draw(cam, x, y - v, h / 2, v);
            cam.drawLine(x, y, x, top);
            children[2].draw(cam, x + h, y - v, h / 2, v);
            cam.drawLine(x, y, x + h, top);
        } else {
            System.out.println("no Node kind has more than 3 children???");
            System.exit(1);
        }

    }// draw


    public void execute(){

//        System.out.println(kind + ": " + info);
        if(this.kind.equals("eof")){

            /** Handle EOF node type **/
            System.out.print("Reached end of file.");

        } else if(this.kind.equals("program")){

            /** Handle program node type **/

            if(first != null && first.kind.equals("statements")){
                first.execute();
            }
        } else if(this.kind.equals("statements")){

            /** Handle statements node type **/

            if(second.kind.equals("eof")){
                if(first.kind.equals("statement")){
                    first.execute();
                }
            } else{
                first.execute();
                second.execute();
            }
        }
        /** Handle statement node type **/
        else if (this.kind.equals("statement")) {

            /** Handle statement msg type **/
            if (first.kind.equals("msg")) {
                if (second != null && second.kind.equals("string") && second.info != null) {
                    System.out.print(second.info);
                }
            }

            /** Handle statement newline type **/
            else if (first.kind.equals("newline")) {
                System.out.println();
            }

            /** Handle statement input type **/
            else if(first.kind.equals("input")) {
                if (second != null && second.kind.equals("string") && second.info != null) {
                    System.out.print(second.info); //print request
                    idString = third.info; // save the id name
                    double input = in.nextDouble(); // stored the user input
                    NameIntTable.add(idString, input); // stored id , input in table
                }
            }

            /** Handle statement print type **/
            else if(first.kind.equals("show") ){
                if ( second != null ) {
                    double retDouble = second.evaluate();
                    System.out.println(retDouble);
                }
            }

            /** Handle statement id type **/
            else if (first.kind.equals("id") ){
                if (second.kind.equals("single") && second.info.equals("=")){
                    if (third.kind.equals("expression")){
                        NameIntTable.add(first.info, third.evaluate() );
                    }
                   // executeTable.add(first.info, Double.parseDouble(third.info));
                }
            }//id

        }//statement
    }//execute

    private double evaluate() {
        double retDouble = 0;

        if (this.kind.equals("expression") ) {
            if (second == null) {// send to Term
                retDouble = first.evaluate();
            } else if (second.kind.equals("single") ){
                if ( second.info.equals("+")){
                    retDouble = first.evaluate() + third.evaluate();
                } else if (second.info.equals("-") ){
                    retDouble = first.evaluate() - third.evaluate();
                }
            }
        }//expression

        if (this.kind.equals("term") ) {
            if (second == null) {
                retDouble = first.evaluate();
            } else if (second.kind.equals("single")) {
                if (second.info.equals("*")) {
                    retDouble = first.evaluate() * third.evaluate();
                } else if (second.info.equals("/")) {
                    retDouble = first.evaluate() / third.evaluate();
                }
            }
        }//term

        if (this.kind.equals("factor") ){
            if (second == null && first.kind.equals("num") ){
                retDouble = Double.parseDouble(first.info);
            } else if (second == null && first.kind.equals("id") ){
                retDouble = NameIntTable.getNumber(first.info);
            } else if (second != null && first.kind.equals("bifs") ){
                String bif = first.info;
                double temp = second.evaluate();
                retDouble = bifDouble(bif,temp);
            } else if (first.kind.equals("expression") ){
                retDouble = first.evaluate();
            } else if (first.kind.equals("single") && second != null && second.kind.equals("factor") ){
                if (first.info.equals("-") ){
                    retDouble = second.evaluate() * (-1);
                }
            }
        }//factor
        return retDouble;
    }

    private double bifDouble(String bif, double temp) {
        double bifbifDouble = 0;

        if (bif.equals("sqrt") ){
            bifbifDouble = Math.sqrt(temp);

        } else if (bif.equals("exp") ){
            bifbifDouble = Math.exp(temp);

        } else if (bif.equals("sin") ){
            bifbifDouble = Math.sin(temp);

        } else if (bif.equals("cos") ){
            bifbifDouble = Math.sin(temp);
        }

        return bifbifDouble;

    }

}// Node
