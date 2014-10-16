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
import java.util.Map.Entry;

/**
 *
 * @author fabien.amarger
 */
public class TypeCandidate extends Candidate
{

    private InstanceCandidate ic;
    private String uriTypeCandidate;
    private ClassCandidate cc;
    
    public TypeCandidate(InstanceCandidate ic, String uriTypeCandidate)
    {
        super();
        this.uriImplicate = new HashMap<>();
        this.ic = ic;
        this.uriTypeCandidate = uriTypeCandidate;
    }
    
    public TypeCandidate (InstanceCandidate ic, ClassCandidate cc)
    {
        super();
        this.uriImplicate = new HashMap<>();
        this.ic = ic;
        this.cc = cc;
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
        if(this.cc != null)
        {
            this.trustScore += this.cc.trustScore;
            this.trustScore /= 2;
        }
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
        
         ret += "\t ->("+this.trustScore+")  ";
         if(this.cc != null)
         {
             ret += "\n \t \t Class Candidate (";
             for(Entry<Source, String> e : this.cc.uriImplicate.entrySet())
             {
                 ret += e.getKey()+" -> "+e.getValue()+" | ";
             }
             ret += ") (";
         }
         else
         {
             ret += this.uriTypeCandidate+"(";
         }
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

    public String toProvO(String baseUri, int numCand, int instCand, HashMap<Source, String> sourcesUri, HashMap<Source, String> uriInst, String uriOntObj, String uriKbMerge)
    {
        String uriCand =baseUri+this.sElem+"/Cand/"+instCand+"/"+numCand;
        String uriTypeOO = baseUri+this.sElem+"/"+instCand+"/"+numCand;
        String ret = "<"+uriCand+"> rdf:type :Entity; rdf:type rdf:Statement; rdf:subject <"+uriOntObj+">; rdf:predicate rdf:type; rdf:object   <"+uriTypeOO+">.\n ";
        ret += "<"+uriCand+"> <"+baseUri+"hadTrustScore> \""+this.getTrustScore()+"\"^^xsd:double.\n";
        
        for( Entry<Source, String> e : this.uriImplicate.entrySet())
        {
            String uriStatement = baseUri+this.sElem+"/"+e.getKey().getName()+"/"+instCand+"/"+numCand;
            ret += "<"+uriStatement+"> rdf:type :Entity; rdf:type rdf:Statement.\n";
            ret += "<"+uriStatement+"> rdf:subject <"+uriInst.get(e.getKey())+">; rdf:predicate rdf:type; rdf:object <"+e.getValue()+">. \n";
            ret += "<"+uriTypeOO+"> owl:sameAs <"+uriStatement+">.\n";
            ret += "<"+sourcesUri.get(e.getKey())+"> :hadMember <"+uriStatement+">.\n";
            ret += "<"+uriCand+"> :wasDerivedFrom <"+uriStatement+">. \n";
        }
        
        return ret;
    }
    
}
