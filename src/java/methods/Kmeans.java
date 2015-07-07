/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package methods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Kmeans {


    double[][] old_centroids;
    double[][] new_centroids;
    //in the end the clusters must remain the same after iteration. Because it is difficult to keep copies of clusters to verify we will check centroids
    //if cluster contents dont change then centroids dont change. Although its a mean value statistically there will be a difference
    public Map<Integer, List<policy>> cluster = new HashMap<>();

    public Kmeans(List<policy> mypol, int kmeans) {
        //shuffle the list -take k first elements
        old_centroids = new double[kmeans][mypol.get(0).getObjectives().length];
        Collections.shuffle(mypol);
        for (int i = 0; i < kmeans; i++) {
            policy newpol = mypol.get(i);
            old_centroids[i] = newpol.getObjectives();
            cluster.put(i, new ArrayList<policy>());
        }
    }
//get all list and add closest elements in cluster

    public void addClosest1(List<policy> mypol, int kmeans, int allobj) {

        for (policy mpol : mypol) {
            double[] sum = new double[kmeans];
            for (int i = 0; i < kmeans; i++) {
                for (int w = 0; w < allobj; w++) {
                    sum[i] += Math.pow(mpol.getObjectives()[w] - old_centroids[i][w], 2);
                }
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
                List<policy> lpol = cluster.get(smallest);
                lpol.add(mpol);
                cluster.put(smallest, lpol);
            }
        }
    }

    //I dont need to keep lists if criteria is old_centroid=new_centroid i just need to find the final centroids and calculate last clusters
    public double[][] findCentroids(int kmeans, int allobj) {
        double[][] new_centroids = new double[kmeans][allobj];
        Iterator it = cluster.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            List<policy> mpol = (List<policy>) pair.getValue();
            double[] sum_centroid = new double[mpol.get(0).getObjectives().length];
            int key = (Integer) pair.getKey();
            for (policy temp : mpol) {
                for (int i = 0; i < temp.getObjectives().length; i++) {
                    sum_centroid[i] += temp.getObjectives()[i];
                }
            }
            for (int i = 0; i < allobj; i++) {
                new_centroids[key][i] = sum_centroid[i] / mpol.size();
            }
        }
        return new_centroids;
    }

    public void reloop(List<policy> mypol, int kmeans) {
        boolean same = false;
        int counter = 0;
        while (!same) {
            same = true;
            counter++;
            System.out.println(counter);
            addClosest1(mypol, kmeans, mypol.get(0).getObjectives().length);
            new_centroids = findCentroids(kmeans, mypol.get(0).getObjectives().length);
            for (int i = 0; i < new_centroids.length; i++) {
                for (int j = 0; j < new_centroids[0].length; j++) {
                    if (new_centroids[i][j] == old_centroids[i][j]) {
                        same = same & true;
                    }else {
                        same=same&false;
                    }
                }
            }
            if (!same) {
                for (int i = 0; i < new_centroids.length; i++) {
                    for (int j = 0; j < new_centroids[0].length; j++) {
                        old_centroids[i][j] = new_centroids[i][j];
                    }
                }
                Iterator it = cluster.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    int key = (Integer) pair.getKey();
                    cluster.put(key, new ArrayList<policy>());
                }
            }
        }
    }
}
