package methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DBScan {

    public Map<Integer, List<Integer>> cluster = new HashMap<>();
    public Map<Integer, Boolean> visited = new HashMap<>();
    public Map<Integer, Boolean> clustered = new HashMap<>();
    public Map<Integer, Boolean> clusterRemove = new HashMap<>();

    public DBScan(List<policy> mypol, double eps, int minPts) {
        for (policy mpol : mypol) {
            if (!visited.containsKey(mpol.getID())) {
                List<policy> neighbors = getClosestNeighbors(mypol, mpol, eps);
                visited.put(mpol.getID(), Boolean.TRUE);
                if (neighbors.size() >= minPts) {//point is NOISE
                    cluster.put(mpol.getID(), new ArrayList<Integer>());
                    clustered.put(mpol.getID(), Boolean.TRUE);
                    expandCluster(mypol, mpol, neighbors, eps, minPts);
                }
            }
        }
    }

    public List<policy> expandCluster(List<policy> mypol, policy p, List<policy> neighbors, double eps, int minPts) {
        List<policy> ret = new ArrayList<>();
        for (policy temp : neighbors) {
            List<policy> closest = new ArrayList<>();
            if (!visited.containsKey(temp.getID())) {
                closest = getClosestNeighbors(mypol, temp, eps);//TODO check minpts before add)
                visited.put(temp.getID(), Boolean.TRUE);
                if (closest.size() >= minPts) {
                    for (policy cl : closest) {
                        if (!clustered.containsKey(cl.getID())) {
                            clustered.put(cl.getID(), true);
                            cluster.get(p.getID()).add(cl.getID());
                        }
                    }
                }
            }
        }
        return ret;
    }

    public List<policy> getClosestNeighbors(List<policy> mpol, policy elem, double eps) {
        Iterator<policy> it = mpol.iterator();
        List<policy> ret = new ArrayList<>();

        while (it.hasNext()) {
            double sum = 0.0;
            policy temp = it.next();
            if (!visited.containsKey(temp.getID())) {
                for (int w = 0; w < temp.getObjectives().length; w++) {
                    sum += Math.pow(elem.getObjectives()[w] - temp.getObjectives()[w], 2);
                }
                if (Math.sqrt(sum) < eps) {//if euclidean distance <ε then add in cluster 
                    ret.add(temp);
                }
            }
        }
        return ret;
    }

    public void getClusters() {
        Iterator it = cluster.entrySet().iterator();

        while (it.hasNext()) {
            List<Integer> ret = new ArrayList<>();
            Map.Entry pair = (Map.Entry) it.next();
            List<Integer> temp = (List<Integer>) pair.getValue();
            int key = (Integer) pair.getKey();

            if (!temp.isEmpty()) {
                if (!clusterRemove.containsKey(key)) {
                    for (int tt : temp) {
                        if (cluster.containsKey(tt)) {
                            ret.addAll(cluster.get(tt));
                            clusterRemove.put(tt, Boolean.TRUE);
                        }
                    }
                    ret.addAll(retAll(ret));
                    temp.addAll(ret);
                    cluster.put(key, temp);
                } else {
                    it.remove();
                }
            }else{
                it.remove();//if it is empty then its not a cluster
            }
        }
//        //remove all empty lists that belong in other clusters
//        it = cluster.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry) it.next();
//            List<Integer> temp = (List<Integer>) pair.getValue();
//            int key = (Integer) pair.getKey();
//            if (temp.isEmpty()) {
//                if (clusterRemove.containsKey(key)) {
//                    it.remove();
//                }
//            }
//        }
    }

    public List<Integer> retAll(List<Integer> mlist) {
        List<Integer> ret = new ArrayList<>();
        Iterator<Integer> it = mlist.iterator();

        while (it.hasNext()) {
            int temp = it.next();
            if (cluster.containsKey(temp)) {
                ret.addAll(retAll(cluster.get(temp)));
                clusterRemove.put(temp, true);
            }
        }
        return ret;
    }
}
