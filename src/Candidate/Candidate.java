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

/**
 *
 * @author Fabien
 */
public abstract class Candidate 
{
    protected float trustDegreeScore = 0;
    protected float trustSimpleScore = 0;
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
    
    public  void computeTrustScore(float nbSources)
    {
        this.trustSimpleScore = (float)this.uriImplicate.keySet().size()/nbSources;
    }
    
    public float getTrustScore()
    {
        return this.trustSimpleScore;
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
        String ret = this.sElem+" Candidate (Simple : "+this.trustSimpleScore+" | Degree : "+this.trustDegreeScore+"): \n";
        for(Map.Entry<Source, String> e : this.uriImplicate.entrySet())
        {
            ret += "\t "+e.getKey().getName()+" : "+e.getValue()+"\n";
        }
        return ret;
    }
    
    public BasicDBObject toDBObject()
    {
         BasicDBObject doc = new BasicDBObject();
        ArrayList<String> elemCandidates = new ArrayList<>();
        for(String e : this.uriImplicate.values())
        {
            if(!e.isEmpty())
                    elemCandidates.add(e);
        }
        doc.append("elemCandidates", elemCandidates);
        doc.append("trustScore", this.getTrustScore());
        doc.append("trustScoreSimple", this.trustSimpleScore);

        return doc;
    }
    
    public abstract String getUriCand(String baseUri);
    public abstract String getUriOntObj(String baseUri);
    public abstract int getNumInst();
    public abstract String toProvO(String baseUri, int numInst, HashMap<Source, String> sourcesUri, String uriKbMerge);
    
    
}
