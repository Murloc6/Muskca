/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Alignment;

/**
 *
 * @author fabien.amarger
 */
public class StringDistance
{
    public static double JaroWinkler(String s1, String s2) {
    if (s1.equals(s2))
      return 1.0;

    // ensure that s1 is shorter than or same length as s2
    if (s1.length() > s2.length()) {
      String tmp = s2;
      s2 = s1;
      s1 = tmp;
    }

    // (1) find the number of characters the two strings have in common.
    // note that matching characters can only be half the length of the
    // longer string apart.
    int maxdist = s2.length() / 2;
    int c = 0; // count of common characters
    int t = 0; // count of transpositions
    int prevpos = -1;
    for (int ix = 0; ix < s1.length(); ix++) {
      char ch = s1.charAt(ix);

      // now try to find it in s2
      for (int ix2 = Math.max(0, ix - maxdist);
           ix2 < Math.min(s2.length(), ix + maxdist);
           ix2++) {
        if (ch == s2.charAt(ix2)) {
          c++; // we found a common character
          if (prevpos != -1 && ix2 < prevpos)
            t++; // moved back before earlier 
          prevpos = ix2;
          break;
        }
      }
    }

    // we don't divide t by 2 because as far as we can tell, the above
    // code counts transpositions directly.

    // System.out.println("c: " + c);
    // System.out.println("t: " + t);
    // System.out.println("c/m: " + (c / (double) s1.length()));
    // System.out.println("c/n: " + (c / (double) s2.length()));
    // System.out.println("(c-t)/c: " + ((c - t) / (double) c));
    
    // we might have to give up right here
    if (c == 0)
      return 0.0;

    // first compute the score
    double score = ((c / (double) s1.length()) +
                    (c / (double) s2.length()) +
                    ((c - t) / (double) c)) / 3.0;

    // (2) common prefix modification
    int p = 0; // length of prefix
    int last = Math.min(4, s1.length());
    for (; p < last && s1.charAt(p) == s2.charAt(p); p++)
      ;

    score = score + ((p * (1 - score)) / 10);

    // (3) longer string adjustment
    // I'm confused about this part. Winkler's original source code includes
    // it, and Yancey's 2005 paper describes it. However, Winkler's list of
    // test cases in his 2006 paper does not include this modification. So
    // is this part of Jaro-Winkler, or is it not? Hard to say.
    //
    //   if (s1.length() >= 5 && // both strings at least 5 characters long
    //       c - p >= 2 && // at least two common characters besides prefix
    //       c - p >= ((s1.length() - p) / 2)) // fairly rich in common chars
    //     {
    //     System.out.println("ADJUSTED!");
    //     score = score + ((1 - score) * ((c - (p + 1)) /
    //                                     ((double) ((s1.length() + s2.length())
    //                                                - (2 * (p - 1))))));
    // }

    // (4) similar characters adjustment
    // the same holds for this as for (3) above.
    
    return score;
  }
    
    
     public static int min(int x1, int x2, int x3)
    {
        if(x1 <= x2 && x1 <= x3)
            return x1;
        else if(x2 <= x1 && x2 <= x3)
            return x2;
        else if (x3 <= x1 && x3 <= x2)
                return x3;
        
        return 0;
    }
    
    public static int levenshtein(String s0, String s1) {
	int len0 = s0.length()+1;
	int len1 = s1.length()+1;
 
	// the array of distances
	int[] cost = new int[len0];
	int[] newcost = new int[len0];
 
	// initial cost of skipping prefix in String s0
	for(int i=0;i<len0;i++) cost[i]=i;
 
	// dynamicaly computing the array of distances
 
	// transformation cost for each letter in s1
	for(int j=1;j<len1;j++) {
 
		// initial cost of skipping prefix in String s1
		newcost[0]=j-1;
 
		// transformation cost for each letter in s0
		for(int i=1;i<len0;i++) {
 
			// matching current letters in both strings
			int match = (s0.charAt(i-1)==s1.charAt(j-1))?0:1;
 
			// computing cost for each transformation
			int cost_replace = cost[i-1]+match;
			int cost_insert  = cost[i]+1;
			int cost_delete  = newcost[i-1]+1;
 
			// keep minimum cost
			newcost[i] = min(cost_insert, cost_delete, cost_replace);
		}
 
		// swap cost/newcost arrays
		int[] swap=cost; cost=newcost; newcost=swap;
	}
 
	// the distance is the cost for transforming all letters in both strings
	return cost[len0-1];
}
    
}
