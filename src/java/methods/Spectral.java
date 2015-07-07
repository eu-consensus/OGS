/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package methods;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author anu
 */
public class Spectral {

  public  Map<Integer, List<Integer>> cluster = new HashMap<>();
  public  Map<Integer, List<Integer>> clusterFinal = new HashMap<>();

    public double minkowski(double[] table1, double[] table2, double p) {
        //1<p<2   1=manhattan 2= euclidean oo=chebychev 
        double dist = 0;
        double sum = 0;
        for (int i = 0; i < table1.length && i < table2.length; i++) {
            sum += Math.pow(Math.abs(table1[i] - table2[i]), p);
        }
        dist = Math.pow(sum, 1.0 / p);
        return dist;
    }

    public double euclidean(double[] table1, double[] table2) {

        double dist = 0;
        for (int i = 0; i < table1.length && i < table2.length; i++) {
            dist += Math.pow((table2[i] - table1[i]), 2);
        }
        dist = Math.sqrt(dist);
        return dist;
    }

    public double manhattan(double[] table1, double[] table2) {

        double dist = 0;
        for (int i = 0; i < table1.length && i < table2.length; i++) {
            dist += Math.abs(table2[i] - table1[i]);
        }
        return dist;
    }

    //symmetric normalized laplacian L=D-A Lsym=D^-1/2LD^-1/2=I-D^(-1/2)AD^(-1/2)
    public double[][] Lsym(int total, double[] deg, double[][] adjacent) {
        double[][] Lsym = new double[total][total];
        for (int i = 0; i < total; i++) {
            for (int j = 0; j < total; j++) {
                if (i == j & deg[i] != 0) {
                    Lsym[i][j] = 1;
                } else if (i != j & adjacent[i][j] != 0) {
                    double prnm = Math.sqrt(deg[i] * deg[j]);
                    Lsym[i][j] = -(1 / prnm);
                } else {
                    Lsym[i][j] = 0;
                }
            }
        }
        return Lsym;
    }

    //random walk normalized laplacian matrix Lrw=D^(-1)L=ID^(-1)A
    public double[][] Lrw(int total, double[] deg, double[][] adjacent) {
        double[][] Lrw = new double[total][total];
        for (int i = 0; i < total; i++) {
            for (int j = 0; j < total; j++) {
                if (i == j & deg[i] != 0) {
                    Lrw[i][j] = 1;
                } else if (i != j & adjacent[i][j] != 0) {
                    double prnm = Math.sqrt(deg[i]);
                    Lrw[i][j] = -(1 / prnm);
                } else {
                    Lrw[i][j] = 0;
                }
            }
        }
        return Lrw;
    }

    //fully connected graph Wij=exp(-d^2/2σ^2)
//the parameter σ controls the width of the neighborhoods
    public double[][] fcg(List<policy> mypol, double s) {
        int total = mypol.size();
        double[][] fcg = new double[total][total];
        for (int i = 0; i < total; i++) {
            policy elem1 = mypol.get(i);
            for (int j = 0; j < total; j++) {
                if (i != j) {
                    policy elem2 = mypol.get(j);
                    double d2 = Math.pow(euclidean(elem1.getObjectives(), elem2.getObjectives()), 2);
                    double s2 = Math.pow(s, 2);
                    double weight = Math.exp(-(0.5 * d2 / s2));
                    fcg[i][j]=weight;
                } else {
                    fcg[i][j] = 1;
                }
            }
        }
        return fcg;
    }

    //k-nearest neighbor graph
 //TODO MATRIX MUST BE SYMMETRIC---WRONG
    public double[][] knng(List<policy> mypol, int k) {
        int total = mypol.size();
        double[][] knng = new double[total][total];
        for (int i = 0; i < total; i++) {
            for (int j = 0; j < total; j++) {
                knng[i][j] = 0;
            }
        }
        for (int i = 0; i < total; i++) {
            policy elem1 = mypol.get(i);
            double d[] = new double[total];
            for (int j = 0; j < total; j++) {
                if (i != j) {
                    policy elem2 = mypol.get(j);
                    d[j] = euclidean(elem1.getObjectives(), elem2.getObjectives());
                } else {
                    d[j] = -1;
                }
            }
            List<Integer> knearest = retknearest(d, k);
            for (int nearest : knearest) {
               if(knng[i][nearest]==0.0 && knng[nearest][i]==0.0){
                   knng[i][nearest] = d[nearest];
                   knng[nearest][i] = d[nearest];
               }
                
            }
        }
        return knng;
    }

    public List<Integer> retknearest(double[] table, int k) {
        List<Integer> positions = new ArrayList<>();
        double[] sorted = table.clone();
        Arrays.sort(sorted);
        //logically the negative one is the smallest so i just ignore it :)
        for (int i = 1; i < k+1; i++) {
            double smallest = sorted[i];
            for (int t = 0; t < table.length; t++) {
                boolean belongs = false;
                for (int pos : positions) {
                    if (t == pos) {
                        belongs = true;
                    }
                }
                if (smallest == table[t] & !belongs) {
                    positions.add(t);
                }
            }

        }
        return positions;
    }

    //ε-neighborhood graph
    public double[][] eng(List<policy> mypol, double e) {
        int total = mypol.size();
        double[][] eng = new double[total][total];
        for (int i = 0; i < total; i++) {
            policy elem1 = mypol.get(i);
            for (int j = 0; j < total; j++) {
                if (i != j) {
                    policy elem2 = mypol.get(j);
                    double d = euclidean(elem1.getObjectives(), elem2.getObjectives());
                    if (d < e) {
                        eng[i][j] = 1;
                    } else {
                        eng[i][j] = 0;
                    }
                } else {
                    eng[i][j] = 0;//will not put it a neighbor to itself
                }
            }
        }
        return eng;
    }

    public double[] degMatrix(double[][] adjacent) {
        double[] deg = new double[adjacent.length];

        for (int i = 0; i < adjacent.length; i++) {
            double sum = 0.0;
            for (int j = 0; j < adjacent[0].length; j++) {
                if (i != j) {
                    sum += adjacent[i][j];
                }
            }
            deg[i] = sum;
        }
        return deg;
    }

    public Spectral(List<policy> mypol, int k) {

        for (int i = 0; i < k; i++) {
            cluster.put(i, new ArrayList<Integer>());
        }
        double[][] adjacent = fcg(mypol, 1);
        double[] deg = degMatrix(adjacent);
        Matrix A = new Matrix(Lsym(mypol.size(), deg, adjacent));
        EigenvalueDecomposition mat = A.eig();
        Matrix V = mat.getV();//get the eigenvalue matrix
        Matrix T = new Matrix(V.getRowDimension(), V.getColumnDimension());
        double[] sum = new double[V.getRowDimension()];
        //sum[i]= (Σ(uik)^2) ^1/2
        for (int i = 0; i < V.getRowDimension(); i++) {
            double colsum = 0.0;
            for (int j = 0; j < k; j++) {
                colsum += Math.pow(V.get(i, j), 2);
            }
            sum[i] = Math.sqrt(colsum);
        }
        //tij=uij/sum[i]
        for (int i = 0; i < V.getRowDimension(); i++) {
            for (int j = 0; j < V.getColumnDimension(); j++) {
               double e = V.get(i, j) / sum[i];
                T.set(i, j, e);
            }
        }
        //cluster with k-means yi into clusters where yi = ith ROW of T
        for (int i = 0; i < V.getRowDimension(); i++) {
            List<Double> mydoubleL = new ArrayList<>();
            double[] mydouble = new double[V.getColumnDimension()];
            for (int j = 0; j < V.getColumnDimension(); j++) {
                mydouble[j] = T.get(i, j);
                mydoubleL.add(T.get(i, j));
            }
            double[] old_centroids = setoldcentroids(mydoubleL, k);
            reloop(mydouble, k, old_centroids);
            //get subclusters and add them up to final cluster
            Iterator it = cluster.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                List<Integer> mpol = (List<Integer>) pair.getValue();
                int key = (Integer) pair.getKey();
                if (clusterFinal.containsKey(key)) {
                    clusterFinal.get(key).addAll(mpol);
                } else {
                    clusterFinal.put(key, mpol);
                }
            }
        }
    }

    //Kmeans algorithm
    public double[] setoldcentroids(List<Double> mypol, int kmeans) {
        //shuffle the list -take k first elementsgetObjectives()
        double[] old_centroids;
        old_centroids = new double[mypol.size()];
        Collections.shuffle(mypol);
        for (int i = 0; i < kmeans; i++) {
            old_centroids[i] = mypol.get(i);
            cluster.put(i, new ArrayList<Integer>());
        }
        return old_centroids;
    }
//get all list and add closest elements in cluster

    public void addClosest1(double[] mypol, int kmeans, double[] old_centroids) {
        double[] sum = new double[kmeans];
        for (int w = 0; w < mypol.length; w++) {
            for (int i = 0; i < kmeans; i++) {
                sum[i] += Math.pow(mypol[w] - old_centroids[i], 2);
            }

            for (int i = 0; i < kmeans; i++) {
                sum[i] = Math.sqrt(sum[i]);
            }
            int smallest = 0;//keep smallest id 
            for (int i = 1; i < kmeans; i++) {
                if (sum[smallest] > sum[i]) {
                    smallest = i;
                }
            }
            //smallest distance is at ith element
            if (cluster.containsKey(smallest)) {
                List<Integer> lpol = cluster.get(smallest);
                lpol.add(w);
                cluster.put(smallest, lpol);
            }
        }
    }

    //I dont need to keep lists if criteria is old_centroid=new_centroid i just need to find the final centroids and calculate last clusters
    public double[] findCentroids(int kmeans) {
        double[] new_centroids = new double[kmeans];
        Iterator it = cluster.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            List<Integer> mpol = (List<Integer>) pair.getValue();
            double[] sum_centroid = new double[kmeans];
            int key = (Integer) pair.getKey();
            for (int temp : mpol) {
                sum_centroid[key] += temp;
            }
            new_centroids[key] = sum_centroid[key] / mpol.size();
        }
        return new_centroids;
    }

    public void reloop(double[] mypol, int kmeans, double[] old_centroids) {
        boolean same = false;
        int counter = 0;
        double[] new_centroids = new double[kmeans];
        while (!same) {
            same = true;
            counter++;
            System.out.println(counter);
            addClosest1(mypol, kmeans, old_centroids);
            new_centroids = findCentroids(kmeans);
            for (int i = 0; i < new_centroids.length; i++) {
                if (new_centroids[i] == old_centroids[i]) {
                    same = same & true;
                } else {
                    same = same & false;
                }
            }
        }
        if (!same) {
            for (int i = 0; i < new_centroids.length; i++) {
                old_centroids[i] = new_centroids[i];
            }
            Iterator it = cluster.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                int key = (Integer) pair.getKey();
                cluster.put(key, new ArrayList<Integer>());
            }
        }
    }
}
