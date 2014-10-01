/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Candidate;

import Source.Source;
import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author fabien.amarger
 */
public class TypeCandidate extends Candidate
{

    private InstanceCandidate ic;
    private String uriTypeCandidate;
    
    public TypeCandidate(InstanceCandidate ic, String uriTypeCandidate)
    {
        super();
        this.uriImplicate = new HashMap<>();
        this.ic = ic;
        this.uriTypeCandidate = uriTypeCandidate;
    }
    
    @Override
    public void addElem(Source s, String uriElem)
    {
        this.uriImplicate.put(s, uriElem);
    }

    @Override
    public void computeTrustScore(float trustMax)
    {
        this.trustScore = ic.getTrustScore();
        for(Source s : this.uriImplicate.keySet())
        {
            //relCandidateTrustScore += s.getSourceQualityScore();
            this.trustScore ++;
        }
        this.trustScore /= trustMax;
    }

    @Override
    public String toString()
    {
        String ret = "";
        
         ret += "\t ->("+this.trustScore+")  "+this.uriTypeCandidate+"(";
        for(Source s : this.uriImplicate.keySet())
        {
            ret += s.getName()+",";
        }
        ret += ") \n";
        
        return ret;
    }

    @Override
    public BasicDBObject toDBObject()
    {
         BasicDBObject doc = new BasicDBObject();
        ArrayList<BasicDBObject> icBDBO = new ArrayList<>();
        for(Map.Entry<Source, String> icE: ic.getElemCandidate().entrySet())
        {
            BasicDBObject icObj = new BasicDBObject();
            icObj.append("source", icE.getKey().getName());
            icObj.append("uri", icE.getValue());
            icBDBO.add(icObj);
        }
        doc.append("ic", icBDBO);

        doc.append("typeURI", uriTypeCandidate);


        ArrayList<String> listSourcesInvol = new ArrayList<>();
        for(Source s :this.uriImplicate.keySet() )
        {
            //relCandidateTrustScore += s.getSourceQualityScore();
            listSourcesInvol.add(s.getName());
        }
        doc.append("sources", listSourcesInvol);

        //Compute ir trust SCORE here! 
        //relCandidateTrustScore /= 2;
        
        doc.append("trustScore", this.trustScore);
        
        return doc;
    }

    @Override
    public String toProvO(String baseUri, int numCand)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
