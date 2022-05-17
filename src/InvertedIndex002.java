

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Ehab, Elhussein
 */
/*
 * InvertedIndex - Given a set of text files, implement a program to create an
 * inverted index. Also create a user interface to do a search using that inverted
 * index which returns a list of files that contain the query term / terms.
 * The search index can be in memory.
 *

 */
import java.io.*;
import java.util.*;

//=====================================================================
class DictEntry2 { // dictionary part

    public int doc_freq = 0; // number of documents that contain the term
    public int term_freq = 0; // number of times the term is mentioned in the collection > probably not used
    public HashSet<Integer> postingList; // is connected to the dictionary i.e. each dictionary (containing a term) has a postingList

    DictEntry2() { // constructor

        postingList = new HashSet<Integer>();

    }
}

//=====================================================================
class Index2 {

    //--------------------------------------------
    Map<Integer, String> sources;  // store the doc_id and the file name
    HashMap<String, DictEntry2> index; // THe inverted index || String=> (term) MAPS DicEntry=>(frequency & posting list)
    //--------------------------------------------

    Index2() { // constructor
        sources = new HashMap<Integer, String>();
        index = new HashMap<String, DictEntry2>();
    }

    //---------------------------------------------
    public void printDictionary() {
        Iterator it = index.entrySet().iterator(); // defining iterator
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next(); // get an entry in the hashMap (index) i.e. (1 key,and it's value)
            DictEntry2 dd = (DictEntry2) pair.getValue(); // gets the dictionary(value) of the pair entry(index)
            HashSet<Integer> hset = dd.postingList;// (HashSet<Integer>) pair.getValue(); gets the posting list of the dictionary
            System.out.print("** [" + pair.getKey() /* term name IG */ + "," + dd.doc_freq + "] <" + dd.term_freq + "> =--> ");
            Iterator<Integer> it2 = hset.iterator(); // iterator to the posting list of the term
            while (it2.hasNext()) { // printing out the posting list
                System.out.print(it2.next() + ", ");
            }
            System.out.println("");
            //it.remove(); // avoids a ConcurrentModificationException
        }
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + index.size());
    }

    //-----------------------------------------------
    public void buildIndex(String[] files) {
        int i = 0; // references the DocID
        for (String fileName : files) { // for each file in collection
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                sources.put(i, fileName); // maps each file(doc) to a docID (incremented)
                String ln;
                while ((ln = file.readLine()) != null) { // reads each line in the file (Doc)
                    String[] words = ln.split("\\W+"); // splits the words using regular expression and store in arr {tokenization}
                    for (String word : words) {
                        word = word.toLowerCase(); // normalization
                        // check to see if the word is not in the dictionary
                        if (!index.containsKey(word)) {
                            index.put(word, new DictEntry2()); // puts a new dictionary to the term (word)
                        }
                        // add document id to the posting list
                        if (!index.get(word).postingList.contains(i)) { // if the word in the dictionary but the DocID is not in the posting list
                            index.get(word).doc_freq += 1; //set doc freq to the number of doc that contain the term
                            index.get(word).postingList.add(i); // add the posting to the posting list (NOT SORTED) //TODO: NEEDS TO BE SORTED
                        }
                        //set the term_freq in the collection
                        index.get(word).term_freq += 1;
                    }
                }
                printDictionary();
            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            i++;
        }
    }

    //--------------------------------------------------------------------------
    // query inverted index
    // takes a string of terms as an argument
    public String find(String phrase) {
        String[] words = phrase.split("\\W+");  // splits the argument phrase into words and saves into arr
        HashSet<Integer> res = new HashSet<Integer>(index.get(words[0].toLowerCase()).postingList); // res === posting list of 1st word

        for (String word : words) {
            res.retainAll(index.get(word).postingList); // remove elements in res that not exist in word.postingList (MERGING)
        }
        if (res.size() == 0) {
            System.out.println("Not found");
            return "";
        }
        ArrayList<Integer> SL = new ArrayList<Integer>(res);
        Collections.sort(SL);
        String result = "Found in: \n";
        for (int num : SL) {
            result += "\t" + sources.get(num) + "\n";
        }
        return result;
    }

    //----------------------------------------------------------------------------
    HashSet<Integer> intersect(HashSet<Integer> pL1, HashSet<Integer> pL2, String operator) { //TODO: AND
        HashSet<Integer> answer = new HashSet<Integer>();

        Iterator<Integer> pL1itr = pL1.iterator();
        Iterator<Integer> pL2itr = pL2.iterator();
        int doc1 = 0, doc2 = 0;

        if(operator.toLowerCase().equals("and")){

            if (pL1itr.hasNext()) {
                doc1 = pL1itr.next(); // assigning doc 1 to the 1st value in posting list 1
            }
            if (pL2itr.hasNext()) {
                doc2 = pL2itr.next(); // assigning doc 2 to the 1st value in posting list 2
            }

            while (pL1itr.hasNext() || pL2itr.hasNext()) {
                if (doc1 == doc2) {
                    answer.add(doc1);
                    if (pL1itr.hasNext()) {
                        doc1 = pL1itr.next();
                    }
                    if (pL2itr.hasNext()) {
                        doc2 = pL2itr.next();
                    }
                } else if (doc1 < doc2) {
                    if (pL1itr.hasNext()) {
                        doc1 = pL1itr.next();
                    } else {
                        return answer;
                    }
                } else {
                    if (pL2itr.hasNext()) {
                        doc2 = pL2itr.next();
                    } else {
                        return answer;
                    }
                }
            }
            if (doc1 == doc2) {
                answer.add(doc1);
            }
        }

        else if(operator.toLowerCase().equals("or")){
            while (pL1itr.hasNext()) {
                doc1 = pL1itr.next();
                answer.add(doc1);
            }
            while (pL2itr.hasNext()) {
                doc2 = pL2itr.next();
                answer.add(doc2);
            }
        }

        else if(operator.toLowerCase().equals("not")){
        /*Iterator<Integer> pL1itr = pL1.iterator();
        Iterator<Integer> pL2itr;
        int doc1 = 0, doc2 = 0;
        if (pL1itr.hasNext()) {
            doc1 = pL1itr.next(); // assigning doc 1 to the 1st value in posting list 1
        }
        while (pL1itr.hasNext()) {
            pL2itr = pL2.iterator();
            if (pL2itr.hasNext()) {
                doc2 = pL2itr.next();
            }
            while (pL2itr.hasNext()) {
                if (doc1 < doc2) {
                    answer.add(doc1);
                    if (pL1itr.hasNext()) {
                        doc1 = pL1itr.next();
                    }
                }
                else if(doc1 == doc2){
                    if (pL1itr.hasNext()) {
                        doc1 = pL1itr.next();
                    }
                    doc2 = pL2itr.next();
                }
                else {
                        doc2 = pL2itr.next();
                }
            }
            if(doc1 < doc2){
                answer.add(doc1);
            }
            doc1 = pL1itr.next();
        }*/
            while (pL1itr.hasNext()) {
                doc1 = pL1itr.next();
                answer.add(doc1);
            }
            while (pL2itr.hasNext()) {
                doc2 = pL2itr.next();
                answer.remove(doc2);
            }
        }


        return answer;
    }


    public String find_01(String phrase) { // 2 term phrase  2 postingsLists
        String result = "";
        String[] words = phrase.split("\\W+"); // splits on whiteSpace
        // 1- get first posting list
        HashSet<Integer> pL1 = new HashSet<Integer>(index.get(words[0].toLowerCase()).postingList);
        String operator = words[1];
        // 2- get second posting list
        HashSet<Integer> pL2 = new HashSet<Integer>(index.get(words[2].toLowerCase()).postingList);
        // 3- apply the algorithm
        HashSet<Integer> answer =  intersect(pL1, pL2, operator);
        System.out.println("Found in: ");
        for (int num : answer) {
            //System.out.println("\t" + sources.get(num));
            result += "\t" + sources.get(num) + "\n";
        }


        return result;

    }
//-----------------------------------------------------------------------

    public String find_02(String phrase) { // 3 lists // TODO:3 words (AND,OR,NOT)
        String result = "";
        String[] words = phrase.split("\\W+"); // splits on whiteSpace
        HashSet<Integer> postingList1 = (index.get(words[0].toLowerCase()).postingList);
        String operator1 = words[1];
        HashSet<Integer> postingList2 = (index.get(words[2].toLowerCase()).postingList);

        HashSet<Integer> resultBiWord = intersect(postingList1,postingList2,operator1);

        String operator2 = words[3];
        HashSet<Integer> postingList3 = (index.get(words[4].toLowerCase()).postingList);

        HashSet<Integer> answer = intersect(resultBiWord, postingList3, operator2);

        System.out.println("Found in: ");
        for (int num : answer) {
            //System.out.println("\t" + sources.get(num));
            result += "\t" + sources.get(num) + "\n";
        }
        return result;

    }
    //-----------------------------------------------------------------------

    public String find_03(String phrase) { // any number of terms non-optimized search
        String result = ""; int i = 2;
        String[] words = phrase.split("\\W+"); // splits on whiteSpace

        HashSet<Integer> answer = new HashSet<Integer>(index.get(words[0].toLowerCase()).postingList);

        while(i < (words.length)){
            answer = intersect(answer, index.get(words[i].toLowerCase()).postingList, words[i-1].toLowerCase());
            i+=2;
        }
        System.out.println("Found in: ");
        for (int num : answer) {
            //System.out.println("\t" + sources.get(num));
            result += "\t" + sources.get(num) + "\n";
        }
        return result;
    }
    //-----------------------------------------------------------------------

    public String find_04(String phrase) { // any number of terms optimized search
        String result = "";
        // write you code here
        return result;

    }
    //-----------------------------------------------------------------------

    public void compare(String phrase) { // optimized search
        long iterations=1000000;
        String result = "";
        long startTime = System.currentTimeMillis();
        for (long i = 1; i < iterations; i++) {
            result = find(phrase);
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(" (*) elapsed = " + estimatedTime+" ms.");
        //-----------------------------
        System.out.println(" result = " + result);
        startTime = System.currentTimeMillis();
        for (long i = 1; i < iterations; i++) {
            result = find_03(phrase);
        }
        estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(" (*) Find_03 non-optimized intersect  elapsed = " + estimatedTime +" ms.");
        System.out.println(" result = " + result);
        //-----------------------------
        startTime = System.currentTimeMillis();
        for (long i = 1; i < iterations; i++) {
            result = find_04(phrase);
        }
        estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(" (*) Find_04 optimized intersect elapsed = " + estimatedTime+" ms.");
        System.out.println(" result = " + result);
    }

   /* public String JacSim(String phrase , List<String> Doc1 , List<String> Doc2){
        String[] words = phrase.split("\\W+");
        ArrayList<Double> Result = new ArrayList<Double>();
        HashSet<String> res = new HashSet<String>();
        List<String> Query = new ArrayList<>(Arrays.asList(words));
        Double nemo =0.0;
        for (int i = 0; i < Doc1.size(); i++) {
            String thisword = Doc1.get(i);
            if (Query.contains(thisword.toLowerCase())){
                nemo++;
            }res.add(Doc1.get(i).toLowerCase());
        }
        res.addAll(Query);
        int y = res.size();
        Double coef = (Double) (nemo)/y;
        Result.add(coef);
        nemo=0.0;
        res.clear();
        for (int i = 0; i < Doc2.size(); i++) {
            String thisword = Doc2.get(i);
            if (Query.contains(thisword.toLowerCase())){
                Query.remove(Query.indexOf(thisword.toLowerCase()));
                nemo++;
            }res.add(Doc2.get(i).toLowerCase());
        }
        res.addAll(Query);
        y = res.size();
        coef = (Double) (nemo)/y;
        Result.add(coef);
        Collections.sort(Result , Collections.reverseOrder());
        for (int i = 1; i < Result.size()+1; i++) {
            System.out.print("Doc"+ i+" = ");
            float final_ = Result.get(i-1).floatValue();
            System.out.println(final_);
        }

        return "Done";
    } */

    public double jacquardCof(String phrase,String doc){
        /*algorithm (calculate jacquard similarity of a phrase on a certain document)
        * get the document and process it as a list of words
        * process the phrase query as list of words
        * get the intersection between the 2 lists (how many words in common)
        * get the union between the 2 lists
        * return the division value of the intersection and union
        * */
        try (BufferedReader file = new BufferedReader(new FileReader(doc))) { // process the document and get "words" var holds the values
            String ln;
            ArrayList<String> documentWords = new ArrayList<String>(); // holds all the terms of the document
             while ((ln = file.readLine()) != null) { // reads each line in the file (Doc)
                String [] Words = ln.split("\\W+"); // splits the words using regular expression and store in arr {tokenization}
                for (String word : Words) {
                    word = word.toLowerCase(); // normalization
                    documentWords.add(word);
                }
            }

            String[] query = phrase.split("\\W+");
            ArrayList<String> queryWords = new ArrayList<String>();
             for(String word : query){
                 queryWords.add(word.toLowerCase());
             }


            // intersection algorithm
            double intersectVal = 0.0;
            for(int i= 0; i <documentWords.size(); i++){
                if(queryWords.contains(documentWords.get(i).toLowerCase())){
                    intersectVal++;
                    queryWords.remove(documentWords.get(i));
                }
            }
            double unionVal = 0.0;
            Set<String> unionSet = new HashSet<String>();
            for(String word:query){
                unionSet.add(word.toLowerCase());
            }
            unionSet.addAll(documentWords);
            unionVal = unionSet.size();



//            System.out.println(intersectVal/unionVal);

            return (intersectVal/unionVal);


        } catch (IOException e) {
            System.out.println("File " + doc + " not found. Skip it");
        }
        return 0.0;
    }


    public String findPhraseJacquardSim(String phrase){
        /* algorithm
        * query on the phrase with 'OR' operator
        * retrieve all the documents containing words of phrase
        * calculate Jacquard similarity for each document against the phrase
        * sort the list of (related) documents on the Jacquard value
        * print the list to the user
        * KABOOM! (10/10)
        * */

        String[] words = phrase.split("\\W+"); // splits on whiteSpace
        NullPointerException e = new NullPointerException();
        HashSet<Integer> PL = new HashSet<Integer>();
        if(index.containsKey(words[0].toLowerCase())){
            PL = new HashSet<Integer>(index.get(words[0].toLowerCase()).postingList); // error when the first word doesn't exist
            System.out.print("");
        }
        for(int i =0 ; i < words.length; i++){
            if(!index.containsKey(words[i].toLowerCase())){
                continue;
            }
            else{
                PL = intersect(PL, index.get(words[i].toLowerCase()).postingList, "or");
            }

        }
        Map<Double, String> doc_jaccard = new TreeMap<Double, String>(Collections.reverseOrder());

        for(int num : PL){
         //   {{sources.get(num);}}  gets the name of the document using the ID's mentioned in the posting list
            doc_jaccard.put(jacquardCof(phrase, sources.get(num)),sources.get(num));

        }


        String result = "";
        for (Map.Entry m  : doc_jaccard.entrySet()) {
            //System.out.println("\t" + sources.get(num));
            result += "\t" + m.getValue() + ", jaccard coefficient: " + m.getKey() + "\n";
        }





        return result;
    }

}

//=====================================================================
public class InvertedIndex002 {

    public static void main(String args[]) throws IOException {
        Index2 index = new Index2();
        String phrase = "";

        index.buildIndex(new String[]{
                "src/docs/100.txt", // change it to your path e.g. "c:\\tmp\\100.txt"
                "src/docs/101.txt",
                "src/docs/102.txt",
                "src/docs/103.txt",
                "src/docs/104.txt",
                "src/docs/105.txt",
                "src/docs/106.txt",
                "src/docs/107.txt",
                "src/docs/108.txt",
                "src/docs/109.txt"
        });

        System.out.println(index.findPhraseJacquardSim("satisfy limited"));


//        do {
//            System.out.println("Print search phrase: ");
//            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//            phrase = in.readLine();
//            System.out.println("//////////////////////");
//        } while (!phrase.isEmpty());

//        System.out.println(index.jacquardCof("tdd", "src/docs/100.txt"));;
    }
}
