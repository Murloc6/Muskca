/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Candidate.NodeCandidate;

import Alignment.Alignment;
import Candidate.ArcCandidate.ArcCandidate;
import Candidate.ArcCandidate.LabelCandidate;
import Candidate.ArcCandidate.RelationCandidate;
import Candidate.Candidate;
import Source.Source;
import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    
    private static int curId = 1;
    
    private int id;
    private boolean alreadyValidated = false;
    
    protected String uriOntObj = "";
    protected String uriCand = "";
    
    public NodeCandidate()
    {
        super();
        this.id = NodeCandidate.curId;
        NodeCandidate.curId++;
        this.aligns = new ArrayList<>();
        this.labelCands = new ArrayList<>();
    }
    
    public void setId(int id)
    {
        this.id = id;
    }
    
    public int getId()
    {
        return this.id;
    }
    
    public void validate()
    {
        this.alreadyValidated = true;
    }
    
    public boolean isValidated()
    {
        return this.alreadyValidated;
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
    
    private void addLabelCandIfNoteExists(LabelCandidate lc)
    {
        boolean alreadyExists = false;
         for(LabelCandidate labelCand : this.labelCands)
        {
            if(lc.isSameCand(labelCand))
            {
                alreadyExists = true;
                break;
            }
            else if(labelCand.isSameCand(lc))
            {
                this.labelCands.remove(labelCand);
                break;
            }
        }
        if(!alreadyExists)
            this.labelCands.add(lc);
    }
    
    public void addAllLabelsCandidate(ArrayList<LabelCandidate> labelCs, float trustLcMax, float sumSQ)
    {
        for(LabelCandidate lc : labelCs)
        {
            //lc.computeTrustScore(trustLcMax);
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
    
    public boolean containsOneOfUri(ArrayList<String> notFoundUris)
    {
        boolean ret = true;
        
        for(String s : this.uriImplicate.values())
        {
            if(notFoundUris.contains(s))
            {
                ret = false;
                break;
            }
        }
        return ret;
    }
    
    public ArrayList<ArcCandidate> getAllArcCandidates()
    {
        ArrayList<ArcCandidate> ret = new ArrayList<>();
        ret.addAll(this.labelCands);
        return ret;
    }
    
    public float getSumArcCandIntr()
    {
        float ret = 0;
        for(LabelCandidate lc : this.labelCands)
        {
            ret += lc.getTrustScore();
        }
        return ret;
    }
    
    public float getSumArcCandImplied(NodeCandidate nc)
    {
        float ret = 0;
        return ret;
    }
    
//    @Override
//    public void computeTrustDegreeScore(float nbSources)
//    {
//        float trustDegree = 0;
//        for(Alignment al : this.aligns)
//        {
//            trustDegree += al.getValue();
//        }
//        float nbMaxCouple = (nbSources*(nbSources-1))/2;
//        this.trustDegreeScore = trustDegree/nbMaxCouple;
//    }
    
    @Override
    public void computeTrustDegreeScore(int nbSources)
    {
        Collections.sort(this.aligns, new Comparator<Alignment>() {
            @Override
            public int compare(Alignment  a1, Alignment  a2)
            {
                float a1Value = a1.getValue();
                float a2Value = a2.getValue();
                return (a1Value < a2Value ? -1 :(a1Value == a2Value ? 0 : 1));
            }
        });
        
        float trust = 0;
        float curVal = 0;
        float preVal = 0;
        if(!this.aligns.isEmpty())
        {
            for(int i = 0; i< this.aligns.size(); i++)
            {
                Alignment a = this.aligns.get(i);
                curVal = a.getValue();
                if(curVal != preVal)
                {
                    trust += (curVal - preVal)*this.mu(this.aligns.size()-i, nbSources);
                    preVal = curVal;
                }
            }
        }
        else // nodecandidate alone
        {
            trust = (float) this.mu(0.5f, nbSources);
        }
        this.trustDegreeScore = trust;
    }
    
    @Override
    public void computeTrustScore(int nbSources)
    {
        super.computeTrustScore(nbSources);
        for(ArcCandidate ac : this.getAllArcCandidates())
        {
            ac.computeTrustScore(nbSources);
        }
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
        doc.append("ncId", this.id);
        doc.append("alreadyValidated", this.alreadyValidated);
        
        return doc;
    }
    
    public String toString()
    {
        String ret = "["+this.id+"] "+super.toString();
        for(Alignment a : this.aligns)
        {
            ret += "\t \t *** "+a.getUri()+" -->"+a.getUriAlign()+" ("+a.getValue()+") \n";
        }
        ret += "\t arcImpl : "+this.getSumArcCandIntr()+"\n";
        if(this.labelCands.size() > 0)
        {
             for(LabelCandidate lc : this.labelCands)
             {
                 ret += "\t"+lc.toString();
             }
        }
        return ret;
    }
    
    public boolean isCompatible(NodeCandidate nc)
    {
        boolean ret = true;
        for(Entry<Source, String> e : this.uriImplicate.entrySet())
        {
            if(nc.hasElem(e.getKey(), e.getValue()))
            {
                ret = false;
                break;
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
         
         //TODO add relation candidates here
        
        return ret;
    }
    
}
