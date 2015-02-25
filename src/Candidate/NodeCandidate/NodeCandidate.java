/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Candidate.NodeCandidate;

import Alignment.Alignment;
import Candidate.ArcCandidate.LabelCandidate;
import Candidate.Candidate;
import Source.Source;
import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author murloc
 */
public abstract class NodeCandidate extends Candidate
{   
    private ArrayList<Alignment> aligns;
    
    private ArrayList<LabelCandidate> labelCands;
    
    protected String uriOntObj = "";
    protected String uriCand = "";
    
    public NodeCandidate()
    {
        super();
        this.aligns = new ArrayList<>();
        this.labelCands = new ArrayList<>();
    }
    
    public void addAlignment(Alignment al)
    {
        if(al != null)
          this.aligns.add(al);
    }
    
    public void addLabelCandidate(LabelCandidate labelC)
    {
        this.labelCands.add(labelC);
    }
    
    public void addAllLabelsCandidate(ArrayList<LabelCandidate> labelCs, float trustLcMax, float sumSQ)
    {
        for(LabelCandidate lc : labelCs)
        {
            lc.computeTrustScore(trustLcMax);
            this.labelCands.add(lc);
        }
    }
    
    public ArrayList<LabelCandidate> getLabelCandidates()
    {
        return this.labelCands;
    }
    
    public void clearLabelsCandidates()
    {
        ArrayList<LabelCandidate> newLabels = new ArrayList<>();
        ArrayList<String> lcTreated = new ArrayList<>();
        for(LabelCandidate lc : this.labelCands)
        {
           //String typeURI = lc.getDataProp();
            String refLC = lc.getRefId();
            if(!lcTreated.contains(refLC))
            {
               lcTreated.add(refLC);
               newLabels.add(lc);
            }
       }
        this.labelCands = newLabels;
    }
    
    @Override
    public void computeTrustScore(float nbSources)
    {
        super.computeTrustScore(nbSources);
        
        float trustDegree = 0;
        for(Alignment al : this.aligns)
        {
            trustDegree += al.getValue();
        }
        float nbMaxCouple = (nbSources*(nbSources-1))/2;
        this.trustDegreeScore = trustDegree/nbMaxCouple;
    }
    
    @Override
    public BasicDBObject toDBObject() {
        BasicDBObject doc = super.toDBObject();
        ArrayList<BasicDBObject> alignsObject = new ArrayList<>();
        for(Alignment al : aligns)
        {
            BasicDBObject alignDoc = new BasicDBObject();
            alignDoc.append("uri1", al.getUri());
            alignDoc.append("uri2", al.getUriAlign());
            alignDoc.append("trustScore", al.getValue());
            alignsObject.add(alignDoc);
        }
        doc.append("aligns", alignsObject);
        
        return doc;
    }
    
    public String toString()
    {
        String ret = super.toString();
        for(Alignment a : this.aligns)
        {
            ret += "\t \t *** "+a.getUri()+" -->"+a.getUriAlign()+" ("+a.getValue()+") \n";
        }
        
        if(this.labelCands.size() > 0)
        {
             ret += "\t Label Candidate : \n";
             for(LabelCandidate lc : this.labelCands)
             {
                 ret += lc.toString();
             }
        }
        
        return ret;
    }
    
    public boolean isSameCand(ArrayList<String> uris)
    {
        boolean ret = false;
        
        if(uris.size() == this.uriImplicate.values().size())
        {
            ret = true;
            for(String uriImpl : this.uriImplicate.values())
            {
                if(!uris.contains(uriImpl))
                {
                    ret = false;
                    break;
                }
            }
        }
        
        return ret;
    }
    
    public String toPrologData()
    {
        String ret = "[";
        for(String uri : this.getUriImplicate().values())
        {
            ret += "\""+uri+"\",";
        }
        ret = ret.substring(0, ret.lastIndexOf(","));
        ret += "]";
        return ret;
    }
    
    public String toProvO(String baseUri, int numInst, HashMap<Source, String> sourcesUri, String uriKbMerge)
    {
        this.uriCand = this.getUriCand(baseUri);
        this.uriOntObj = this.getUriOntObj(baseUri);
        String ret = "<"+uriCand+"> rdf:type :Entity; rdf:type rdf:Statement; rdf:subject <"+uriOntObj+">; rdf:predicate rdf:type; rdf:object owl:Thing.\n ";
        ret += "<"+uriCand+"> <"+baseUri+"hadTrustScore> \""+this.getTrustScore()+"\"^^xsd:double.\n";
        //String ret = "Instance Candidate ("+this.trustScore+" -- Source : "+this.trustSource+" | Aligns : "+this.trustAlign+" | ICHR : "+this.trustICHR+"): \n";
        for(Entry<Source, String> e : this.uriImplicate.entrySet())
        {
            //ret += "\t "+e.getKey().getName()+"("+e.getKey().getSourceQualityScore()+") : "+e.getValue()+"\n";
            ret += "<"+e.getValue()+"> rdf:type :Entity; rdf:type rdf:Statement.\n";
            ret += "<"+uriOntObj+"> owl:sameAs <"+e.getValue()+">.\n";
            ret += "<"+sourcesUri.get(e.getKey())+"> :hadMember <"+e.getValue()+">.\n";
            ret += "<"+uriCand+"> :wasDerivedFrom <"+e.getValue()+">. \n";
        }
        
        /*for(Alignment a : this.aligns)
        {
            ret += "\t \t *** "+a.getUri()+" -->"+a.getUriAlign()+" ("+a.getValue()+") \n";
        }*/
        
         if(this.labelCands.size() > 0)
        {
            int numLabel = 1;
             for(LabelCandidate lc : this.labelCands)
             {
                 //ret += lc.toProvO(baseUri, numLabel, numInst, sourcesUri, this.uriImplicate, uriOntObj, uriKbMerge);
                 ret += lc.toProvO(baseUri, numInst, sourcesUri, uriKbMerge);
                 numLabel ++;
             }
        }
        
        return ret;
    }
    
}
