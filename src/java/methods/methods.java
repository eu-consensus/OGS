/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package methods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author anu
 */
public class methods {

    public static class polComparator implements Comparator<policy> {

        public int compare(policy p1, policy p2) {
            return Double.compare(p1.getDistance(), p2.getDistance());
        }
    }

    public static class polComparator2 implements Comparator<policy> {

        public int compare(policy p1, policy p2) {
            return Integer.compare(p1.getDominated(), p2.getDominated());

        }
    }

    public static class polComparatorRDD implements Comparator<policy> {

        public int compare(policy p1, policy p2) {
            if (Integer.compare(p1.getDominated(), p2.getDominated()) > 0) {
                return 1;
            } else if (Integer.compare(p1.getDominated(), p2.getDominated()) < 0) {
                return -1;
            } else {
                if (Integer.compare(p1.getRank(), p2.getRank()) > 0) {
                    return 1;
                } else if (Integer.compare(p1.getRank(), p2.getRank()) < 0) {
                    return -1;
                } else {
                    if (Integer.compare(p1.getDominatedbycategory(), p2.getDominatedbycategory()) > 0) {
                        return 1;
                    } else if (Integer.compare(p1.getDominatedbycategory(), p2.getDominatedbycategory()) < 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }
        }
    }

    public static void paretoL(List<policy> theList) {

        for (int i = 0; i < theList.size(); i++) {
            double[] data = theList.get(i).getObjectives();

            for (int j = i + 1; j < theList.size(); j++) {
                double[] element = theList.get(j).getObjectives();
                boolean bigger = true;
                boolean smaller = true;
                boolean equal = true;

                for (int w = 0; w < data.length; w++) {

                    if (data[w] > element[w]) {
                        bigger = true && bigger;
                        smaller = false;
                        equal = false;

                    } else if (data[w] == element[w]) {
                        equal = true && equal;
                    } else {
                        bigger = false;
                        smaller = true && smaller;
                        equal = false;
                    }

                }
                if (!equal) {
                    if (bigger) {
                        theList.get(j).setDominated(theList.get(j).getDominated() + 1);

                    }
                    if (smaller) {
                        theList.get(i).setDominated(theList.get(i).getDominated() + 1);

                    }
                }
            }
        }

    }

    public static List<policy> dominationBYcategory(List<policy> theList,boolean[] minmax) {
        Hashtable<String, List<policy>> myhash = new Hashtable<>();
        List<policy> temp = new ArrayList<>();
        List<policy> gatherList = new ArrayList<>();
        Iterator<policy> iterator = theList.iterator();
        while (iterator.hasNext()) {
            policy obj = iterator.next();
            String keyName = obj.getOrder();
            //an uparxei key prosthese to object sti lista alliws 
            //ftiaxe nea lista me key kai prosthese to prwto stoixeio
            if (myhash.containsKey(keyName)) {
                temp = myhash.get(keyName);
                temp.add(obj);
            } else {
                List<policy> tempList = new ArrayList<>();
                tempList.add(obj);
                myhash.put(keyName, tempList);
            }
        }
        Enumeration<String> e = myhash.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            temp = myhash.get(key);
            for (int i = 0; i < temp.size(); i++) {
                double[] data = temp.get(i).getObjectives();

                for (int j = i + 1; j < temp.size(); j++) {
                    double[] element = temp.get(j).getObjectives();
                    boolean bigger = true;
                    boolean smaller = true;
                    boolean equal = true;

                    for (int w = 0; w < data.length; w++) {
                        if (minmax[w]) {
                            int result = maximizationofObjective(data[w], element[w]);
                            if (result == 1) {
                                bigger = true && bigger;
                                smaller = false;
                                equal = false;
                            } else if (result == 0) {
                                equal = true && equal;
                            } else {
                                bigger = false;
                                smaller = true && smaller;
                                equal = false;
                            }
                        } else {
                            int result = minimizationofObjective(data[w], element[w]);
                            if (result == 1) {
                                bigger = true && bigger;
                                smaller = false;
                                equal = false;
                            } else if (result == 0) {
                                equal = true && equal;
                            } else {
                                bigger = false;
                                smaller = true && smaller;
                                equal = false;
                            }
                        }

                    }
                    if (!equal) {
                        if (bigger) {
                            temp.get(j).setDominatedbycategory(temp.get(j).getDominatedbycategory() + 1);

                        }
                        if (smaller) {
                            temp.get(i).setDominatedbycategory(temp.get(i).getDominatedbycategory() + 1);

                        }
                    }
                }

            }
            gatherList.addAll(temp);
        }
        return gatherList;
    }

    //returning 1 if it does what the function implies. 
    //eg. if a is smaller than b then a is minimizing the objective in comparison to b 
    public static int minimizationofObjective(double a, double b) {
        if (a > b) {
            return -1;
        } else if (a == b) {
            return 0;
        } else {
            return 1;
        }
    }

    public static int maximizationofObjective(double a, double b) {
        if (a > b) {
            return 1;
        } else if (a == b) {
            return 0;
        } else {
            return -1;
        }
    }

    public static double[] weight(int obj, String weig) {
        double[] ret = new double[obj];
        String[] temp = weig.split(" ");
        for (int i = 0; i < temp.length; i++) {
            ret[i] = Double.parseDouble(temp[i]);
        }
        return ret;
    }

    //create an array with what to do with each objective criteria (either maximize or minimize)
    public static boolean[] minmax(String minmaxstr) {
        char[] minmaxc = minmaxstr.toCharArray();
        boolean[] minmax = new boolean[minmaxc.length];
        for (int i = 0; i < minmax.length; i++) {
            minmax[i] = false;
        }
        for (int i = 0; i < minmaxc.length; i++) {
            if (minmaxc[i] == '+') {
                minmax[i] = true;
            }
        }
        return minmax;
    }
    
    //find ranges and their % of appearance 
       public static double[] find_space(List<maj> test, int total) {

        double[] space = new double[3];
        for (int u = 0; u < space.length; u++) {
            space[u] = 0.0;
        }

        int k = (int) Math.round(test.size() * 0.2);
        if (k < 1) {
            k = 1;
        }
        double threshold = 30;//the threshold we use to verify preference

        for (int w = 0; w < test.size(); w++) {
            int temp_amount = 0;
            for (int i = 0; i < k; i++) {

                if (w + i < test.size()) {//make certain we dont get out of bounds 
                    if (test.get(w).getCount() == 0) {//if the previous value is chosen 0 times then we better start from the next one in creating the spaces in order not to create a space with 100-0-0-0-50
                        break;
                    }
                    temp_amount += test.get(w + i).getCount();
                    if (Double.compare((double) temp_amount * 100 / total, threshold) > 0) {

                        if (space[2] < temp_amount * 100 / total) {
                            space[2] = temp_amount * 100 / total;
                            space[0] = test.get(w).getValue(); //start value 
                            space[1] = test.get(w + i).getValue(); // end value if no one else added then i=0 and it gives the start value
                        }
                    }
                }
            }
            temp_amount = 0;
        }
        return space;
    }
public static List<maj> merge(List<maj> majList) {
        List<maj> merged = new ArrayList<maj>();
        Hashtable<Double, Integer> hashList = new Hashtable<Double, Integer>();
        for (maj temp : majList) {
            if (hashList.containsKey(temp.getValue())) {
                hashList.put(temp.getValue(), hashList.get(temp.getValue()) + temp.getCount());
            } else {
                hashList.put(temp.getValue(), temp.getCount());
            }
        }
        Enumeration<Double> e = hashList.keys();
        while (e.hasMoreElements()) {
            Double key = e.nextElement();
            maj temp1 = new maj();
            temp1.setValue(key);
            temp1.setCount(hashList.get(key));
            merged.add(temp1);
        }

        return merged;
    }

    public static orderel mergeor(List<orderel> orderList) {
        orderel merged = new orderel();
        Hashtable<String, Integer> hashList = new Hashtable<>();

        for (orderel temp : orderList) {
            if (hashList.containsKey(temp.getValue())) {
                hashList.put(temp.getValue(), hashList.get(temp.getValue()) + temp.getCount());
            } else {
                hashList.put(temp.getValue(), temp.getCount());
            }
        }
        Enumeration<String> e = hashList.keys();
        orderel temp1 = new orderel();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            int count = 0;
            if (count < hashList.get(key)) {
                temp1.setValue(key);
                temp1.setCount(hashList.get(key));
                count = hashList.get(key);
            }
        }
        return temp1;
    }
    
    //an oi times einai sunexeis tupou 0.1-0.2-0.3 ktl tote to diastima tha antistoixei sto 20% tou diastimatos 
    //an den einai tote to diastima tha antistoixei sto 20% twn sinolikwn timwn pou emfanizontai :)
    public static class MajComparator implements Comparator<maj> {

        @Override
        public int compare(maj p1, maj p2) {
            return Double.compare(p1.getValue(), p2.getValue());
        }
    }

 
//Finding pareto frontiers provided the minimization or maximization criteria of each objective O(n^2)
    public static List<policy> paretoM(List<policy> theList, boolean[] minmax) {

        for (int i = 0; i < theList.size(); i++) {
            double[] data = theList.get(i).getObjectives();

            for (int j = i + 1; j < theList.size(); j++) {
                double[] element = theList.get(j).getObjectives();
                boolean bigger = true;
                boolean smaller = true;
                boolean equal = true;

                for (int w = 0; w < data.length; w++) {
                    if (minmax[w]) {
                        int result = maximizationofObjective(data[w], element[w]);
                        if (result == 1) {
                            bigger = true && bigger;
                            smaller = false;
                            equal = false;
                        } else if (result == 0) {
                            equal = true && equal;
                        } else {
                            bigger = false;
                            smaller = true && smaller;
                            equal = false;
                        }
                    } else {
                        int result = minimizationofObjective(data[w], element[w]);
                        if (result == 1) {
                            bigger = true && bigger;
                            smaller = false;
                            equal = false;
                        } else if (result == 0) {
                            equal = true && equal;
                        } else {
                            bigger = false;
                            smaller = true && smaller;
                            equal = false;
                        }
                    }
                }
                if (!equal) {
                    if (bigger) {
                        theList.get(j).setDominated(theList.get(j).getDominated() + 1);
                      //  theList.get(j).setSi(theList.get(i).getPolicyName());
                        theList.get(i).setSiR(Integer.toString(theList.get(j).getID()));
                    }
                    if (smaller) {
                        theList.get(i).setDominated(theList.get(i).getDominated() + 1);
                      //  theList.get(i).setSi(theList.get(j).getPolicyName());
                        theList.get(j).setSiR(Integer.toString(theList.get(i).getID()));
                    }
                }
            }
        }
        return theList;
    }

    public static Hashtable<String, List<policy>> createLists(List<policy> theList) {

        Hashtable<String, List<policy>> finalL = new Hashtable<>();
        List<policy> temp = new ArrayList<>();

        Iterator<policy> iterator = theList.iterator();
        while (iterator.hasNext()) {
            policy obj = iterator.next();
            String keyName = obj.getOrder();
            //an uparxei key prosthese to object sti lista alliws 
            //ftiaxe nea lista me key kai prosthese to prwto stoixeio
            if (finalL.containsKey(keyName)) {
                temp = finalL.get(keyName);
                temp.add(obj);
            } else {
                List<policy> myList = new ArrayList<>();
                myList.add(obj);
                finalL.put(keyName, myList);
            }
        }
        return finalL;
    }

    public static List<policy> paretoPref(List<policy> temp, boolean[] minmax) {
        List<policy> finalL = new ArrayList<>();
        Collections.sort(temp, new polComparator());
        Iterator<policy> ex;
        while (!temp.isEmpty()) {
            double[] data = temp.get(0).getObjectives();
            policy keeper = temp.get(0);
            ex = temp.iterator();
            while (ex.hasNext()) {
                policy temp2 = ex.next();
                double[] element = temp2.getObjectives();
                boolean bigger = true;
                boolean smaller = true;
                boolean equal = true;
                for (int w = 0; w < data.length; w++) {
                    if (minmax[w]) {
                        int result = maximizationofObjective(data[w], element[w]);
                        if (result == 1) {
                            bigger = true && bigger;
                            smaller = false;
                            equal = false;
                        } else if (result == 0) {
                            equal = true && equal;
                        } else {
                            bigger = false;
                            smaller = true && smaller;
                            equal = false;
                        }
                    } else {
                        int result = minimizationofObjective(data[w], element[w]);
                        if (result == 1) {
                            bigger = true && bigger;
                            smaller = false;
                            equal = false;
                        } else if (result == 0) {
                            equal = true && equal;
                        } else {
                            bigger = false;
                            smaller = true && smaller;
                            equal = false;
                        }
                    }
                }// an to keeper einai megalitero  apo to ex.next se oles tis parametrous tote to ex.next einai dominated kai prepei na fygei apo ti lista
                //an to keeper einai iso se oles tis parametrous me to ex.next tote den exw logo na kratisw 2 teleiws idia stoixeia kai to afairw
                if (bigger || equal) {
                    ex.remove();
                }
//sugkrinw ta upoloipa tis listas me to kainourgio data (kai menoun ta prwta misa tis listas pou exoun idi mpei sto temp 
                //an teleiwsei i diadikasia koitazw sto upoloipo 1o miso an meinei kai apo ekei tote einai pareto kai to prosthetw sti teliki lista 
                if (smaller) {
                    data = element;
                    keeper = temp2;
                    ex = temp.iterator();
                }
            }//an exei teleiwsei i lista kai auto den einai mikrotero apo kapoio allo tote einai dominant kai to prosthetw sti lista!
            finalL.add(keeper);
        }
        return finalL;
    }

    public static List<policy> putProf(List<policy> theList, boolean[] minmax) {
        Hashtable<String, List<policy>> mylist = createLists(theList);
        Enumeration<String> e = mylist.keys();
        List<policy> rList = new ArrayList<>();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            rList.addAll(paretoPref(mylist.get(key), minmax));
        }
        return paretoPref(rList, minmax);
    }

    public static List<policy> nsga2(List<policy> theList, boolean[] minmax) {

        for (int i = 0; i < theList.size(); i++) {
            double[] data = theList.get(i).getObjectives();

            for (int j = i + 1; j < theList.size(); j++) {
                double[] element = theList.get(j).getObjectives();
                boolean bigger = true;
                boolean smaller = true;
                boolean equal = true;

                for (int w = 0; w < data.length; w++) {
                    if (minmax[w]) {
                        int result = maximizationofObjective(data[w], element[w]);
                        if (result == 1) {
                            bigger = true && bigger;
                            smaller = false;
                            equal = false;
                        } else if (result == 0) {
                            equal = true && equal;
                        } else {
                            bigger = false;
                            smaller = true && smaller;
                            equal = false;
                        }
                    } else {
                        int result = minimizationofObjective(data[w], element[w]);
                        if (result == 1) {
                            bigger = true && bigger;
                            smaller = false;
                            equal = false;
                        } else if (result == 0) {
                            equal = true && equal;
                        } else {
                            bigger = false;
                            smaller = true && smaller;
                            equal = false;
                        }
                    }
                }
                if (!equal) {
                    if (bigger) {
                        theList.get(j).setDominated(theList.get(j).getDominated() + 1);
                        theList.get(i).setSi(theList.get(j).getPolicyName());

                    }
                    if (smaller) {
                        theList.get(i).setDominated(theList.get(i).getDominated() + 1);
                        theList.get(j).setSi(theList.get(i).getPolicyName());

                    }
                }
            }
        }
        return theList;
    }

    public static List<policy> nsga2FH(List<policy> theList, boolean[] minmax) {

        int rank = 1;
        List<policy> fList = new ArrayList<>();
        List<policy> retList = new ArrayList<>();
        Hashtable<String, policy> myList = new Hashtable<>();
        Iterator<policy> iterator = theList.iterator();
        int number = theList.get(0).getObjectives().length;
        int number2 = theList.get(0).getPolicyParameters().length;
        //keep in Ni the domination count from the previous stage

        while (iterator.hasNext()) {
            policy pol = new policy(number, number2);
            pol = iterator.next();
            pol.setNi(pol.getDominated());
            String keyName = Integer.toString(pol.getID());
            myList.put(keyName, pol);
        }

        while (!myList.isEmpty()) {
            Enumeration<String> e = myList.keys();
            while (e.hasMoreElements()) {
                String key = e.nextElement();
                policy temp = myList.get(key);
                if (temp.getNi() == 0) {
                    temp.setRank(rank);
                    fList.add(temp);
                    myList.remove(key);
                }
            }
            //myList now contains only dominated values
            retList.addAll(fList);
            for (policy myp : fList) {
                for (String mystr : myp.getSiR().split(" , ")) {
                    if (myList.containsKey(mystr)) {
                        policy temp11 = myList.get(mystr);
                        temp11.setNi(temp11.getNi() - 1);
                    }
                }
            }
            //removed non dominated values from domination count 
            //visit j in Si and reduce nj count by one
            rank++;
            fList.clear();
        }
        return retList;
    }

    public static List<policy> spea2(List<policy> theList, boolean[] minmax) {

        return theList;
    }

    public static List<policy> fonflem(List<policy> theList, boolean[] minmax) {

        return theList;
    }

    public static List<policy> weighted(List<policy> theList, boolean[] minmax, double[] weights) {
        List<policy> retList = new ArrayList<>();
        for (policy temp : theList) {
            double[] obj = temp.getObjectives();
            for (int i = 0; i < obj.length; i++) {
                obj[i] = obj[i] * weights[i];
            }
        }
        retList = paretoM(theList, minmax);
        return retList;
    }

      public static List<policy> paretoG(List<policy> theList, boolean[] minmax) {

        for (int i = 0; i < theList.size(); i++) {
            double[] data = theList.get(i).getObjectives();

            for (int j = i + 1; j < theList.size(); j++) {
                double[] element = theList.get(j).getObjectives();
                boolean bigger = true;
                boolean smaller = true;
                boolean equal = true;

                for (int w = 0; w < data.length; w++) {
                    if (minmax[w]) {
                        int result = maximizationofObjective(data[w], element[w]);
                        if (result == 1) {
                            bigger = true && bigger;
                            smaller = false;
                            equal = false;
                        } else if (result == 0) {
                            equal = true && equal;
                        } else {
                            bigger = false;
                            smaller = true && smaller;
                            equal = false;
                        }
                    } else {
                        int result = minimizationofObjective(data[w], element[w]);
                        if (result == 1) {
                            bigger = true && bigger;
                            smaller = false;
                            equal = false;
                        } else if (result == 0) {
                            equal = true && equal;
                        } else {
                            bigger = false;
                            smaller = true && smaller;
                            equal = false;
                        }
                    }
                }
                if (!equal) {
                    if (bigger) {
                        theList.get(j).setDominatedbycategory(theList.get(j).getDominatedbycategory() + 1);
                    }
                    if (smaller) {
                        theList.get(i).setDominatedbycategory(theList.get(i).getDominatedbycategory()+ 1);
                    }
                }
            }
        }
        return theList;
    }

}
