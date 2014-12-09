/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@WebService
@Stateless(name = "webMethods")
/**
 *
 * @author anu
 */
public class webMethods {
    

    @PersistenceContext(unitName = "PolisPU")
    public EntityManager em;

    private String equal12(String order) {
        char[] orderelem = order.toCharArray();
        char[] newcheck = new char[orderelem.length];
        for (int i = 0; i < orderelem.length; i++) {
            if (orderelem[i] == '1') {
                newcheck[i] = '1';
            }
            if (orderelem[i] == '2') {
                newcheck[i] = '1';
            }
            if (orderelem[i] == '3') {
                newcheck[i] = '3';
            }
            if (orderelem[i] == '4') {
                newcheck[i] = '4';
            }
        }
        return String.valueOf(newcheck);
    }

    private String equal123(String order) {
        char[] orderelem = order.toCharArray();
        char[] newcheck = new char[orderelem.length];
        for (int i = 0; i < orderelem.length; i++) {
            if (orderelem[i] == '1') {
                newcheck[i] = '1';
            }
            if (orderelem[i] == '2') {
                newcheck[i] = '1';
            }
            if (orderelem[i] == '3') {
                newcheck[i] = '1';
            }
            if (orderelem[i] == '4') {
                newcheck[i] = '4';
            }
        }
        return String.valueOf(newcheck);
    }

    private String equal23(String order) {
        char[] orderelem = order.toCharArray();
        char[] newcheck = new char[orderelem.length];
        for (int i = 0; i < orderelem.length; i++) {
            if (orderelem[i] == '1') {
                newcheck[i] = '1';
            }
            if (orderelem[i] == '2') {
                newcheck[i] = '2';
            }
            if (orderelem[i] == '3') {
                newcheck[i] = '2';
            }
            if (orderelem[i] == '4') {
                newcheck[i] = '4';
            }
        }
        return String.valueOf(newcheck);
    }

    private String equal234(String order) {
        char[] orderelem = order.toCharArray();
        char[] newcheck = new char[orderelem.length];
        for (int i = 0; i < orderelem.length; i++) {
            if (orderelem[i] == '1') {
                newcheck[i] = '1';
            }
            if (orderelem[i] == '2') {
                newcheck[i] = '2';
            }
            if (orderelem[i] == '3') {
                newcheck[i] = '2';
            }
            if (orderelem[i] == '4') {
                newcheck[i] = '2';
            }
        }
        return String.valueOf(newcheck);
    }

    private String equal34(String order) {
        char[] orderelem = order.toCharArray();
        char[] newcheck = new char[orderelem.length];
        for (int i = 0; i < orderelem.length; i++) {
            if (orderelem[i] == '1') {
                newcheck[i] = '1';
            }
            if (orderelem[i] == '2') {
                newcheck[i] = '2';

            }
            if (orderelem[i] == '3') {
                newcheck[i] = '3';
            }
            if (orderelem[i] == '4') {
                newcheck[i] = '3';
            }
        }

        return String.valueOf(newcheck);
    }
/*
    @WebMethod(operationName = "findById")
    public List<Y2050> findById(@WebParam(name = "id") int id) {
        Query query = em.createNativeQuery("Y2050.findById", Y2050.class);
        query.setParameter("id", id);
        List<Y2050> results = query.getResultList();
        return results;
    }

    @WebMethod(operationName = "findByPolicy")
    public List<Y2050> findByPolicy(@WebParam(name = "policy") String policy) {
        Query query = em.createNamedQuery("Y2050.findByPolicy", Y2050.class);
        query.setParameter("policy", policy);
        List<Y2050> results = query.getResultList();
        return results;
    }

    @WebMethod(operationName = "findByBiodiv")
    public List<Y2050> findByBiodiv(@WebParam(name = "biodiv") double biodiv) {
        Query query = em.createNamedQuery("Y2050.findByBiodiv", Y2050.class);
        query.setParameter("biodiv", biodiv);
        List<Y2050> results = query.getResultList();
        return results;
    }

    @WebMethod(operationName = "findByCo2")
    public List<Y2050> findByCo2(@WebParam(name = "co2") double co2) {
        Query query = em.createNamedQuery("Y2050.findByCo2", Y2050.class);
        query.setParameter("co2", co2);
        List<Y2050> results = query.getResultList();
        return results;
    }

    @WebMethod(operationName = "findByCostfood")
    public List<Y2050> findByCostfood(@WebParam(name = "costfood") double costfood) {
        Query query = em.createNamedQuery("Y2050.findByCostfood", Y2050.class);
        query.setParameter("costfood", costfood);
        List<Y2050> results = query.getResultList();
        return results;
    }

    @WebMethod(operationName = "findByForestland")
    public List<Y2050> findByForestland(@WebParam(name = "forestland") double forestland) {
        Query query = em.createNamedQuery("Y2050.findByForestland", Y2050.class);
        query.setParameter("forestland", forestland);
        List<Y2050> results = query.getResultList();
        return results;
    }

    @WebMethod(operationName = "findByDistance")
    public List<Y2050> findByDistance(@WebParam(name = "distance") double distance) {
        Query query = em.createNamedQuery("Y2050.findByDistance", Y2050.class);
        query.setParameter("distance", distance);
        List<Y2050> results = query.getResultList();
        return results;
    }

    @WebMethod(operationName = "findByDominatedbycategory")
    public List<Y2050> findByDominatedbycategory(@WebParam(name = "dominatedbycategory") int dominated) {
        Query query = em.createNamedQuery("Y2050.findByDominatedbycategory", Y2050.class);
        query.setParameter("dominatedbycategory", dominated);
        List<Y2050> results = query.getResultList();
        return results;
    }

    @WebMethod(operationName = "findByDominatedbypool")
    public List<Y2050> findByDominatedbypool(@WebParam(name = "dominatedbypool") int dominated) {
        Query query = em.createNamedQuery("Y2050.findByDominatedbypool", Y2050.class);
        query.setParameter("dominatedbypool", dominated);
        List<Y2050> results = query.getResultList();
        return results;
    }

    @WebMethod(operationName = "findByMyorder")
    public List<Y2050> findByMyorder(@WebParam(name = "myorder") String order) {
        Query query = em.createNamedQuery("Y2050.findByMyorder", Y2050.class);
        query.setParameter("myorder", order);
        List<Y2050> results = query.getResultList();
        return results;
    }

    @WebMethod(operationName = "findByChosen")
    public List<Y2050> findByChosen(@WebParam(name = "chosen") int chosen) {
        Query query = em.createNamedQuery("Y2050.findByChosen", Y2050.class);
        query.setParameter("chosen", chosen);
        List<Y2050> results = query.getResultList();
        return results;
    }

    @WebMethod(operationName = "findByLiked")
    public List<Y2050> findByLiked(@WebParam(name = "liked") int liked) {
        Query query = em.createNamedQuery("Y2050.findByLiked", Y2050.class);
        query.setParameter("liked", liked);
        List<Y2050> results = query.getResultList();
        return results;
    }

    @WebMethod(operationName = "findByScore")
    public List<Y2050> findByScore(@WebParam(name = "score") double score) {
        Query query = em.createNamedQuery("Y2050.findByScore", Y2050.class);
        query.setParameter("score", score);
        List<Y2050> results = query.getResultList();
        return results;
    }

    @WebMethod
    public List<Y2050> findAll() {
        Query query = em.createNamedQuery("Y2050.findAll", Y2050.class);
        List<Y2050> results = query.getResultList();
        return results;
    }

    @WebMethod
    public double getScorePreset(@WebParam(name = "policy") String pol) {

        Query query = em.createNamedQuery("Y2050.findByPolicy", Y2050.class);
        query.setParameter("policy", pol);
        List<Y2050> results = query.getResultList();
        return results.get(0).getScore();
    }

    @WebMethod
    @javax.jws.WebResult(name = "getPolicyID")
    public int getPolicyID(@WebParam(name = "policy") String pol) {
        Query query = em.createNamedQuery("Y2050.findByPolicy", Y2050.class);
        query.setParameter("policy", pol);
        List<Y2050> results = query.getResultList();
        return results.get(0).getId();
    }

    @WebMethod(operationName = "increaseChosen")
    public void increaseChosen(@WebParam(name = "policy") String pol) {
        try {
            Query q = em.createNamedQuery("Y2050.findByPolicy");
            q.setParameter("policy", pol);

            Y2050 entry = (Y2050) q.getSingleResult();
            Integer chosen = entry.getChosen();
            chosen++;
            entry.setChosen(chosen);
            em.merge(entry);
            //em.createNativeQuery("UPDATE Y2050 SET chosen=chosen+1 WHERE policy ='" + pol + "'").executeUpdate();
        } catch (Exception exception) {
            String foo = "asdf";
        }
    }

    @WebMethod(operationName = "increaseLiked")
    public void increaseLiked(@WebParam(name = "policy") String pol) {
        try {
            Query q = em.createNamedQuery("Y2050.findByPolicy");
            q.setParameter("policy", pol);

            Y2050 entry = (Y2050) q.getSingleResult();
            Integer liked = entry.getLiked();
            liked++;
            entry.setLiked(liked);
            em.merge(entry);
            //em.createNativeQuery("UPDATE Y2050 SET liked=liked+1 WHERE policy ='" + pol + "'").executeUpdate();
        } catch (Exception e) {

        }
    }

    @WebMethod(operationName = "decreaseChosen")
    public void decreaseChosen(@WebParam(name = "policy") String pol) {
        try {
            Query q = em.createNamedQuery("Y2050.findByPolicy");
            q.setParameter("policy", pol);

            Y2050 entry = (Y2050) q.getSingleResult();
            Integer chosen = entry.getChosen();
            chosen--;
            entry.setChosen(chosen);
            em.merge(entry);
            //em.createNativeQuery("UPDATE Y2050 SET chosen=chosen-1 WHERE policy ='" + pol + "'").executeUpdate();
        } catch (Exception e) {
            String foo = "decrease fails";
        }
    }

    @WebMethod(operationName = "decreaseLiked")
    public void decreaseLiked(@WebParam(name = "policy") String pol) {
        try {
            Query q = em.createNamedQuery("Y2050.findByPolicy");
            q.setParameter("policy", pol);

            Y2050 entry = (Y2050) q.getSingleResult();
            Integer liked = entry.getLiked();
            liked--;
            entry.setLiked(liked);
            em.merge(entry);
            // em.createNativeQuery("UPDATE Y2050 SET liked=liked-1 WHERE policy ='" + pol + "'").executeUpdate();
        } catch (Exception e) {
        }
    }

    @WebMethod(operationName = "findPreferenceOrder")
    public List<Y2050> findPreferenceOrder(@WebParam(name = "myorder") String order) {
        char[] orderelem = order.toCharArray();
        List<String> myList = new ArrayList<>();
        myList.add(order);
        String newordercheck = "1111";
        String str12 = equal12(order);
        String str123 = equal123(order);
        String str23 = equal23(order);
        String str234 = equal234(order);
        String str34 = equal34(order);
        myList.add(newordercheck);
        if (!order.equals(str12) && !str12.equals(newordercheck)) {
            myList.add(str12);
        }
        if (!order.equals(str123) && !str123.equals(newordercheck) && !str123.equals(str12)) {
            myList.add(str123);
        }
        if (!order.equals(str23) && !str23.equals(str12) && !str23.equals(str123) && !str23.equals(newordercheck)) {
            myList.add(str23);
        }
        if (!order.equals(str234) && !str234.equals(str12) && !str234.equals(str123) && !str234.equals(newordercheck) && !str234.equals(str23)) {
            myList.add(str234);
        }
        if (!order.equals(str34) && !str34.equals(str12) && !str34.equals(str123) && !str34.equals(newordercheck) && !str34.equals(str23) && !str34.equals(str234)) {
            myList.add(str34);
        }
        List<Y2050> results = new ArrayList<>();
        for (int i = 0; i < myList.size(); i++) {
            Query query = em.createNativeQuery("SELECT * FROM Y2050 WHERE myorder ='" + myList.get(i) + "'", Y2050.class);
            query.setParameter("myorder", order);
            results.addAll(query.getResultList());
        }
        return results;
    }
    @WebMethod(operationName="objectivesPercentageByplace")
    public List<Y2050> objectivesPercentageByplace(@WebParam int place){
       List<Y2050> retList=new ArrayList<>();
        Query query = em.createNamedQuery("Y2050.findAll", Y2050.class);
        List<Y2050> results = query.getResultList();
        int total=0;
        for (int i = 0; i < results.size(); i++) {
            total += results.get(i).getChosen();
           
        }
       return retList;
    }
}  */    
}
