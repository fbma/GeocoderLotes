

import java.io.PrintWriter;
import java.util.ArrayList;

public class LetterPairSimilarity {
	/** @return lexical similarity value in the range [0,1] */
	   public static double compareStrings(String str1, String str2) {
	       ArrayList pairs1 = wordLetterPairs(str1.toUpperCase());
	       ArrayList pairs2 = wordLetterPairs(str2.toUpperCase());
	       int intersection = 0;
	       int union = pairs1.size() + pairs2.size();
	       for (int i=0; i<pairs1.size(); i++) {
	           Object pair1=pairs1.get(i);
	           for(int j=0; j<pairs2.size(); j++) {
	               Object pair2=pairs2.get(j);
	               if (pair1.equals(pair2)) {
	                   intersection++;
	                   pairs2.remove(j);
	                   break;
	               }
	           }
	       }
	       return (2.0*intersection)/union;
	   }
	   
	   public static double calculaParecido(String nombreBuscado, String nombreDevuelto, String tipoViaBUscado, String tipoViaDevuelto, String portalBuscado, String portalDevuelto, PrintWriter w3) {
	       
		   w3.println("------------------------------------------------------------------------------------------------------------------");
		   //w3.println("Buscamos: " + tipoViaBUscado + " " + nombreBuscado + " , " + portalBuscado);
		   w3.println("Encontardo: " + tipoViaDevuelto + " " + nombreDevuelto + " , " + portalDevuelto);
		   // Parecido de nombres
		   double parecido = compareStrings(nombreBuscado, nombreDevuelto); 
		   parecido = parecido * 0.85;
		   
		   // tipo via
		   if(tipoViaBUscado.equalsIgnoreCase(tipoViaDevuelto))
			   parecido = parecido + 0.10;
		   
		   if((portalBuscado.equalsIgnoreCase("s/n")) || (portalBuscado.equalsIgnoreCase(portalDevuelto)))
			   parecido = parecido + 0.05;
		  
		   w3.println("Parecido = " + parecido);
		   //w3.println("------------------------------------------------------------------------------------------------------------------");
		   
		   
	       return parecido;
	   }
	
	/** @return an array of adjacent letter pairs contained in the input string */
	   private static String[] letterPairs(String str) {
	       int numPairs = str.length()-1;
	       if(numPairs < 0)
	    	   return new String[]{};
	       String[] pairs = new String[numPairs];
	       for (int i=0; i<numPairs; i++) {
	           pairs[i] = str.substring(i,i+2);
	       }
	       return pairs;
	   }
	   
	   /** @return an ArrayList of 2-character Strings. */

	   private static ArrayList wordLetterPairs(String str) {
	       ArrayList allPairs = new ArrayList();
	       // Tokenize the string and put the tokens/words into an array 
	       String[] words = str.split("\\s");
	       // For each word
	       for (int w=0; w < words.length; w++) {
	           // Find the pairs of characters
	           String[] pairsInWord = letterPairs(words[w]);
	           for (int p=0; p < pairsInWord.length; p++) {
	               allPairs.add(pairsInWord[p]);
	           }
	       }
	       return allPairs;

	   }
}
