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
 * @author Fabien
 */
public class LabelCandidate extends Candidate
{
    private HashMap<Source, String> label;
    private HashMap<Source, Float> jRValue;
    
    private String dataProperty;
    
    
    private InstanceCandidate ic;
    
    public LabelCandidate(InstanceCandidate ic, String dataProperty)
    {
        super();
        this.label = new HashMap<>();
        this.ic = ic;
        this.jRValue = new HashMap<>();
        this.dataProperty = dataProperty;
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
        float avgJR = 0;
        for(float f : this.jRValue.values())
        {
            avgJR += f;
        }
        avgJR /= this.jRValue.size();
        
        float sourcesScore = 0;
        for(Source s : this.label.keySet())
        {
            //sourcesScore += s.getSourceQualityScore();
            sourcesScore ++;
        }

        this.trustScore = (this.jRValue.size()+this.ic.getTrustScore()+avgJR+sourcesScore)/trustLcMax;
    }
    
    @Override
    public String toString()
    {
        String ret = "";
        
         ret += "\t --> Type : "+this.dataProperty+"\n";
        ret += "\t\t LC ("+this.getTrustScore()+") ------- \n";
        for(Entry<Source, String> el : this.label.entrySet())
        {
            ret += "\t\t "+el.getKey().getName()+" -> "+el.getValue()+"\n";
        }
        ret +="\t\t --------- \n";
        
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

        //Compute ir trust SCORE here! 
        System.out.println("TEST : "+this.getTrustScore());
        doc.append("trustScore", this.getTrustScore());

        //System.out.println(doc);
        
        return doc;
    }

    @Override
    public String toProvO(String baseUri, int numCand)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
