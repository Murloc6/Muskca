/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Candidate;

import Alignment.Alignment;
import Source.Source;
import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author fabien.amarger
 */
public class ClassCandidate extends Candidate
{

    ArrayList<Alignment> aligns;
    
    
    public ClassCandidate()
    {
        super();
        this.aligns = new ArrayList<>();
        this.uriImplicate = new HashMap<>();
    }
         
      public void addAlignments(ArrayList<Alignment> aligns)
    {
        for (Alignment a : aligns)
        {
            if(a != null && !this.aligns.contains(a))
            {
                this.aligns.add(a);
            }
        }
    }
    
    @Override
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
    
    public boolean hasElem(String uri, Source s)
    {
        boolean ret = false;
        
        String uriImpl = this.uriImplicate.get(s);
        if(uriImpl != null)
        {
            if(uriImpl.compareToIgnoreCase(uri) == 0)
            {
                ret = true;
            }
        }
        
        return ret;
    }

    @Override
    public void computeTrustScore(float trustMax)
    {
        /*float sourceTrustPart = 0;
        for(Source s : this.uriImplicate.keySet())
        {
            sourceTrustPart +=  s.getSourceQualityScore();
        }*/
        float alignTrustPart = 0;
        for(Alignment a : this.aligns)
        {
            alignTrustPart += a.getValue();
        }
        

        this.trustScore = (float) ((alignTrustPart)/trustMax);
    }

    @Override
    public String toString()
    {
        String ret = "Class Candidate ("+this.trustScore+" ): \n";
        for(Map.Entry<Source, String> e : this.uriImplicate.entrySet())
        {
            ret += "\t "+e.getKey().getName()+"("+e.getKey().getSourceQualityScore()+") : "+e.getValue()+"\n";
        }
        for(Alignment a : this.aligns)
        {
            ret += "\t \t *** "+a.getUri()+" -->"+a.getUriAlign()+" ("+a.getValue()+") \n";
        }
        
        return ret;
    }

    @Override
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

           return doc;
    }

    public String toProvO(String baseUri, int numCand)
    {
        return "";
    }
    
}
