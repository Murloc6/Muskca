/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Candidate;

import Source.Source;
import com.mongodb.BasicDBObject;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Fabien
 */
public abstract class Candidate 
{
    protected float trustChoquet = 0;
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
    
    protected float mu(float n, float nMax)
    {
        double lambdan = this.lambdaCompute(n, 2, 1.5f);
        double lambdaN = this.lambdaCompute((float)nMax, 2, 1.5f);
        double lambda0 = this.lambdaCompute(0f, 2, 1.5f);
        double mu = (lambdan-lambda0)/(lambdaN-lambda0);
        
        return (float)mu;
    }
    
    protected float mu(int n, int nMax)
    {
        return this.mu((float) n , (float)nMax);
    }
    
    public ArrayList<Float> getAllTrustScore()
    {
        ArrayList<Float> ret = new ArrayList<>();
        for(Source s : this.uriImplicate.keySet())
        {
            if(!ret.contains(s.getSourceQualityScore()))
            {
                ret.add(s.getSourceQualityScore());
            }
        }
        Collections.sort(ret);
        return ret;
    }
    
    public float getUtilityWithMin(float min, int nbSources, float maxSourceQual)
    {
        int ret = 0;
        for(Source s : this.uriImplicate.keySet())
        {
            if(s.getSourceQualityScore() >= min)
            {
                ret ++;
            }
        }
        
        return this.mu(ret, maxSourceQual);
        
    }
    
    public void computeTrustScore(int nbSources , float maxSourceQual)
    {
        float prevThres = 0;
        float trustTemp = 0;
        for(float thres : this.getAllTrustScore())
        {
            trustTemp += (thres-prevThres) * this.getUtilityWithMin(thres, nbSources, maxSourceQual);
            prevThres = thres;
        }
        
        
        this.trustChoquet = (float) (Math.round(trustTemp*100.0)/100.0);
    }
    
    public float getTrustScore()
    {
        return this.trustChoquet;
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
        String ret = this.sElem+" Candidate (Choquet : "+this.trustChoquet+"): \n";
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
        //doc.append("trustDegree", this.trustDegreeScore);

        return doc;
    }
    
    public abstract String getUriCand(String baseUri);
    public abstract String getUriOntObj(String baseUri);
    public abstract int getNumInst();
    public abstract String toProvO(String baseUri, int numInst, HashMap<Source, String> sourcesUri, String uriKbMerge);
    
    
}
