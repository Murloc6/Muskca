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
public class LabelCandidate extends Candidate
{
    private HashMap<Source, String> label;
    private HashMap<Source, Float> jRValue;
    
    private String dataProperty;
    
    private float sumSQ = 0;
    
    private InstanceCandidate ic;
    
    public LabelCandidate(InstanceCandidate ic, String dataProperty, float sumSQ)
    {
        super();
        this.label = new HashMap<>();
        this.ic = ic;
        this.jRValue = new HashMap<>();
        this.dataProperty = dataProperty;
        this.sumSQ = sumSQ;
    }
    
    public void addValue(Source s, float value)
    {
        if(!this.jRValue.containsKey(s))
        {
            this.jRValue.put(s, value);
        }
    }
    
    public void addElem(Source s, String label)
    {
        if(!this.label.containsKey(s))
        {
            this.label.put(s, label);
            //this.jRValue.put(s, value);
        }
    }
    
    public boolean hasLabelForsSource(Source s)
    {
        return (this.label.get(s) != null);
    }
    
    public boolean containsLabel(Source s, String label)
    {
        boolean ret = false;
        String l = this.label.get(s);
        if(l != null)
        {
            ret = l.compareTo(label) == 0;
        }
        return ret;
    }
    
    public HashMap<Source, String> getLabels()
    {
        return this.label;
    }
    
    public String getDataProp()
    {
        return this.dataProperty;
    }
    
    public float getTrustScore()
    {
        return this.trustScore;
    }
    
    public void computeTrustScore(float trustLcMax)
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
    }
    
    @Override
    public String toString()
    {
        String ret = "";
        
         ret += "\t --> Type : "+this.dataProperty+"\n";
        ret += "\t\t LC (Simple : "+this.trustScoreSimple+" | Degree : "+this.getTrustScore()+") ------- \n";
        for(Entry<Source, String> el : this.label.entrySet())
        {
            ret += "\t\t "+el.getKey().getName()+" -> "+el.getValue()+"\n";
        }
        ret +="\t\t --------- \n";
        
        return ret;
    }
    
    public String getRefId()
    {
        HashMap<String, String> sourcesLabels = new HashMap<>();
        ArrayList<String> sourcesList = new ArrayList<>();
        for(Source s : this.label.keySet())
        {
            sourcesLabels.put(s.getName(), this.label.get(s));
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

        doc.append("type", this.dataProperty);
        ArrayList<BasicDBObject> labelsObj = new ArrayList();
        for(Map.Entry<Source, String> l : this.getLabels().entrySet())
        {
            BasicDBObject lObj = new BasicDBObject();
            lObj.append("source", l.getKey().getName());
            lObj.append("label", l.getValue());
            labelsObj.add(lObj);
        }
        doc.append("labels", labelsObj);

        //System.out.println("TEST : "+this.getTrustScore());
        doc.append("trustScore", this.getTrustScore());
        doc.append("trustScoreSimple", this.trustScoreSimple);
        
        return doc;
    }

    public String toProvO(String baseUri, int numCand, int instCand, HashMap<Source, String> sourcesUri,HashMap<Source, String> uriInst, String uriOntObj, String uriKbMerge)
    {
         String uriCand =baseUri+this.sElem+"/Cand/"+instCand+"/"+numCand;
        String labelOO = this.label.get((Source)sourcesUri.keySet().toArray()[0]); //get the label of the first source
        String ret = "<"+uriCand+"> rdf:type :Entity; rdf:type rdf:Statement; rdf:subject <"+uriOntObj+">; rdf:predicate rdfs:label; rdf:object  \""+labelOO+"\".\n ";
        ret += "<"+uriKbMerge+"> :hadMember <"+uriCand+">.";
        ret += "<"+uriCand+"> <"+baseUri+"hadTrustScore> \""+this.getTrustScore()+"\"^^xsd:double.\n";
        int idStatement = 1;
        for( Entry<Source, String> e : this.label.entrySet())
        {
            String uriStatement = baseUri+this.sElem+"/"+e.getKey().getName()+"/"+instCand+"/"+numCand+"/"+idStatement;
            ret += "<"+uriStatement+"> rdf:type :Entity; rdf:type rdf:Statement.\n";
            ret += "<"+uriStatement+"> rdf:subject <"+uriInst.get(e.getKey())+">; rdf:predicate rdfs:label; rdf:object \""+e.getValue()+"\". \n";
            ret += "<"+sourcesUri.get(e.getKey())+"> :hadMember <"+uriStatement+">.\n";
            ret += "<"+uriCand+"> :wasDerivedFrom <"+uriStatement+">. \n";
            idStatement++;
        }
        
        return ret;
    }
    
}
