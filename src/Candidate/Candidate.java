/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Candidate;

import Source.Source;
import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Fabien
 */
public abstract class Candidate 
{
    protected float trustDegreeScore = 0;
    protected float trustMixScore = 0;
    protected HashMap<Source,  String> uriImplicate;
    protected String sElem;
    
    public Candidate()
    {
        this.sElem = this.getClass().getSimpleName();
        this.uriImplicate = new HashMap<>();
    }
    
    public HashMap<Source, String> getUriImplicate()
    {
        return this.uriImplicate;
    }
    
    public String getUriFromSource(Source s)
    {
        return this.uriImplicate.get(s);
    }
    
    public boolean hasElem(Source s, String uri)
    {
        boolean ret = false;
        String uriCand = this.uriImplicate.get(s);
        if(uriCand != null)
        {
            ret = uri.compareTo(uriCand) == 0;
        }
        return ret;
    }
    
    public void addElem(Source s, String uriElem)
    {
        if(s != null && uriElem != null)
        {
            String uri = this.uriImplicate.get(s);
            if(uri == null)
            {
                this.uriImplicate.put(s, uriElem);
            }
            else
            {
                System.err.println("ERROR, candidate doublon");
                System.exit(0);
            }
        }
    }
    
    public boolean isSameCand(Candidate c)
    {
        boolean ret = true;
        
        for(Entry<Source, String> e : this.uriImplicate.entrySet())
        {
            String uriTest = c.getUriFromSource(e.getKey());
            if(uriTest == null || uriTest.compareTo(e.getValue()) != 0)
            {
                ret = false;
                break;
            }
        }
        
        return ret;
    }
    
    private double lambdaCompute(float x, float x0, float gamma)
    {
        return Math.atan((x-x0)/gamma);
    }
    
    protected double mu(float n, int nMax)
    {
        double lambdan = this.lambdaCompute(n, 2, 1.5f);
        double lambdaN = this.lambdaCompute((float)nMax, 2, 1.5f);
        double lambda0 = this.lambdaCompute(0f, 2, 1.5f);
        double mu = (lambdan-lambda0)/(lambdaN-lambda0);
        
        return mu;
    }
    
    protected double mu(int n, int nMax)
    {
        return this.mu((float) n , nMax);
    }
    
    public void computeTrustDegreeScore(int nbSources)
    {
        this.trustDegreeScore = (float)this.uriImplicate.keySet().size()/nbSources;
    }
    
    public  void computeMixTrustScore(int nbSources)
    {
        //this.trustMixScore = (float) (this.trustDegreeScore*this.mu(this.uriImplicate.keySet().size(), nbSources));
        this.trustMixScore = this.trustDegreeScore;
    }
    
    public void computeTrustScore(int nbSources)
    {
        this.computeTrustDegreeScore(nbSources);
        this.computeMixTrustScore(nbSources);
    }
    
    public float getTrustScore()
    {
        return this.trustMixScore;
    }
    
    public String getRefId()
    {
        HashMap<String, String> sourcesLabels = new HashMap<>();
        ArrayList<String> sourcesList = new ArrayList<>();
        for(Source s : this.uriImplicate.keySet())
        {
            sourcesLabels.put(s.getName(), this.uriImplicate.get(s));
            sourcesList.add(s.getName());
        }
        Collections.sort(sourcesList);
        
        String ret = "";
        for(String s : sourcesList)
        {
            ret += s+"->"+sourcesLabels.get(ret)+"/";
        }
        return ret;
    }
    
    public String toString()
    {
        String ret = this.sElem+" Candidate (Degree : "+this.trustDegreeScore+" | Mix : "+this.trustMixScore+"): \n";
        for(Map.Entry<Source, String> e : this.uriImplicate.entrySet())
        {
            ret += "\t "+e.getKey().getName()+" : "+e.getValue()+"\n";
        }
        return ret;
    }
    
    public BasicDBObject toDBObject()
    {
         BasicDBObject doc = new BasicDBObject();
        ArrayList<BasicDBObject> elemCandidates = new ArrayList<>();
        for(Entry<Source,String> e : this.uriImplicate.entrySet())
        {
            Source s = e.getKey();
            String elem = e.getValue();
            if(!elem.isEmpty())
            {
                BasicDBObject elemC = new BasicDBObject();
                elemC.append("source", s.toDBObject());
                elemC.append("elem", elem);
                elemCandidates.add(elemC);
            }
        }
        doc.append("elemCandidates", elemCandidates);
        doc.append("trustScore", this.getTrustScore());
        doc.append("trustDegree", this.trustDegreeScore);

        return doc;
    }
    
    public abstract String getUriCand(String baseUri);
    public abstract String getUriOntObj(String baseUri);
    public abstract int getNumInst();
    public abstract String toProvO(String baseUri, int numInst, HashMap<Source, String> sourcesUri, String uriKbMerge);
    
    
}
