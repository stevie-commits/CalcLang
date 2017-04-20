/*
  store identifier with integer info
*/

import java.util.ArrayList;

public class NameIntTable {

  private static ArrayList<NameIntPair> table = new ArrayList<NameIntPair>();

  public NameIntTable() {
    table = new ArrayList<NameIntPair>();
  }

  // add given name, number pair
  public static void add( String s, double num ) {
    table.add( new NameIntPair( s, num ) );
  }

  public String toString() {
    String s = "----\n";
    for( int k=0; k<table.size(); k++ ) {
      NameIntPair pair = table.get(k);
      s += pair + "\n";
    }
    return s;
  }

  public int size() {
    return table.size();
  }

  public NameIntPair get( int index ) {
    return table.get( index );
  }

  public static double getNumber(String target ) {
    for( NameIntPair pair : table ) {
      if( pair.name.equals(target) )
        return pair.number;
    }
    return -1;
  }

  public String getName( int index ) {
    return table.get(index).name;
  }

  public double getNumber(int index ) {
    return table.get(index).number;
  }

  public void markInUse( int index ) {
    String s = table.get(index).name;
    s = s.substring( 0, s.length()-1 ); // toss the ? on end
    table.set( index, new NameIntPair(s,index) );
  }

  // this method returns cell number of an unused aux,
  // might need to make a new one if none unused
  public double getAux() {
    // search for unused aux:
    for( int k=0; k<table.size(); k++ ) {
      NameIntPair pair = table.get(k);
      if( pair.name.endsWith("?") ) {
        markInUse( k );
        return pair.number;
      }
    }

    // didn't find unused aux, make another
//    int num = Node.nextAux();
//    add( "$" + num, table.size() );
    return table.size()-1;
  }

  // mark this cell as unused aux
  public void releaseAux( int aux ) {
    for( int k=0; k<table.size(); k++ ) {
      NameIntPair pair = table.get(k);
      if( pair.number == aux ) {
        table.set( k, new NameIntPair( pair.name + "?", pair.number ) );
      }
    }
  }

}
