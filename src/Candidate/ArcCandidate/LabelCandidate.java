/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Candidate.ArcCandidate;

import Candidate.NodeCandidate.NodeCandidate;
import Source.Source;
import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Fabien
 */
public class LabelCandidate extends ArcCandidate
{
    private static int numInstGlob = 1;
    private int numInst = -1;
    
    private HashMap<Source, Float> values;
    
    public LabelCandidate(NodeCandidate fromCand, String dataProperty, float sumSQ)
    {
        super(fromCand, dataProperty);
        this.values = new HashMap<>();
    }
    
    public void addValue(Source s, float value)
    {
        this.values.put(s, value);
    }
    
    /*public void computeTrustScore(float trustLcMax)
    {
        //super.computeTrustScore(trustLcMax);
        this.trustScoreSimple = (float)this.label.keySet().size()/(float)3;
        float rootTrust = this.ic.getTrustScore();
        float sumStringDegree = 0;
        for(float f : this.jRValue.values())
        {
            sumStringDegree += f;
        }
        sumStringDegree /= this.sumSQ;
        
        this.trustScore = (rootTrust+sumStringDegree)/trustLcMax;
    }*/
    
    @Override
    public BasicDBObject toDBObject()
    {
        BasicDBObject doc = super.toDBObject();
        
        ArrayList<BasicDBObject> labelsObj = new ArrayList();
        for(Map.Entry<Source, String> l : this.getUriImplicate().entrySet())
        {
            BasicDBObject lObj = new BasicDBObject();
            lObj.append("source", l.getKey().getName());
            lObj.append("label", l.getValue());
            labelsObj.add(lObj);
        }
        doc.append("labels", labelsObj);
        
        return doc;
    }

     public String getUriOntObj(String baseUri)
    {
        String ret = "";
        
        if(this.numInst < 0)
        {
            this.numInst = LabelCandidate.numInstGlob;
            LabelCandidate.numInstGlob++;
        }
        ret = baseUri+this.sElem+"/"+this.numInst;
        
        return ret;
    }
    
    public String getUriCand(String baseUri)
    {
        String ret = "";
        
        if(this.numInst < 0)
        {
            this.numInst = LabelCandidate.numInstGlob;
            LabelCandidate.numInstGlob++;
        }
        ret = baseUri+this.sElem+"/Cand/"+this.numInst;
        
        return ret;
    }
    
    public String getObjectProvOValue()
    {
        return this.uriImplicate.get((Source)this.uriImplicate.keySet().toArray()[0]); //get the label of the first source 
    }
    
    /*public String toProvO(String baseUri, int numCand, int instCand, HashMap<Source, String> sourcesUri,HashMap<Source, String> uriInst, String uriOntObj, String uriKbMerge)
    {
         String uriCand =baseUri+this.sElem+"/Cand/"+instCand+"/"+numCand;
        String labelOO = this.getUriImplicate().get((Source)sourcesUri.keySet().toArray()[0]); //get the label of the first source
        String ret = "<"+uriCand+"> rdf:type :Entity; rdf:type rdf:Statement; rdf:subject <"+uriOntObj+">; rdf:predicate rdfs:label; rdf:object  \""+labelOO+"\".\n ";
        ret += "<"+uriKbMerge+"> :hadMember <"+uriCand+">.";
        ret += "<"+uriCand+"> <"+baseUri+"hadTrustScore> \""+this.getTrustScore()+"\"^^xsd:double.\n";
        int idStatement = 1;
        for( Entry<Source, String> e : this.uriImplicate.entrySet())
        {
            String uriStatement = baseUri+this.sElem+"/"+e.getKey().getName()+"/"+instCand+"/"+numCand+"/"+idStatement;
            ret += "<"+uriStatement+"> rdf:type :Entity; rdf:type rdf:Statement.\n";
            ret += "<"+uriStatement+"> rdf:subject <"+uriInst.get(e.getKey())+">; rdf:predicate rdfs:label; rdf:object \""+e.getValue()+"\". \n";
            ret += "<"+sourcesUri.get(e.getKey())+"> :hadMember <"+uriStatement+">.\n";
            ret += "<"+uriCand+"> :wasDerivedFrom <"+uriStatement+">. \n";
            idStatement++;
        }
        
        return ret;
    }*/

    @Override
    public int getNumInst() 
    {
        return this.numInst;
    }
    
}
