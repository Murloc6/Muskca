/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Candidate.NodeCandidate;

import Alignment.Alignment;
import Alignment.Alignments;
import Candidate.ArcCandidate.ArcCandidate;
import Candidate.ArcCandidate.LabelCandidate;
import Candidate.Candidate;
import Source.OntologicalElement.OntologicalElement;
import Source.Source;
import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    
    private void addLabelCandIfNotExists(LabelCandidate lc)
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
    
    public void addAllLabelsCandidate(ArrayList<LabelCandidate> labelCs, float trustLcMax)
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
        
        for(OntologicalElement elem : this.uriImplicate.values())
        {
            if(notFoundUris.contains(elem.getUri()))
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
    
    public void getImpliedAligns(Alignments aligns){
        ArrayList<OntologicalElement> oes = new ArrayList<>(this.uriImplicate.values());
        for(int i = 0; i< oes.size(); i++){
            for(int j = i+1; j < oes.size(); j++){
                Alignment al = aligns.getAlignment(oes.get(i).getUri()+oes.get(j).getUri());
                if(al != null){
                    this.aligns.add(al);
                }
            }
        }
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
    public ArrayList<Float> getAllTrustScore()
    {
        ArrayList<Float> ret = super.getAllTrustScore();
        for(Alignment a : this.aligns)
        {
            if(!ret.contains(a.getValue()))
            {
                ret.add(a.getValue());
            }
        }
        Collections.sort(ret);
        return ret;
    }
    
    
    @Override
    public float getUtilityWithMin(float min, int nbSources, float maxSourceQual)
    {
        float utilitySources = super.getUtilityWithMin(min, nbSources, maxSourceQual);
        int nbM = 0;
        for(Alignment a : this.aligns)
        {
            if( a.getValue() >= min)
            {
                nbM ++;
            }
        }
        int nbMaxMappings = ((nbSources * (nbSources-1))/2);
        float utilityMappings = (float)nbM/(float)nbMaxMappings;
        
        //TODO : set ponderation here!
        return (utilityMappings+utilitySources)/2;
        
    }

    
    @Override
    public void computeTrustScore(int nbSources, float maxSourceQual)
    {
        super.computeTrustScore(nbSources, maxSourceQual);
        for(ArcCandidate ac : this.getAllArcCandidates())
        {
            ac.computeTrustScore(nbSources, maxSourceQual);
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
        for(Entry<Source, OntologicalElement> e : this.uriImplicate.entrySet())
        {
            if(nc.hasElem(e.getKey(), e.getValue()))
            {
                ret = false;
                break;
            }
        }
       
        
        return ret;
    }
    
    
    public String toPrologData()
    {
        String ret = "[";
        for(OntologicalElement oe : this.getUriImplicate().values())
        {
            ret += "\""+oe+"\",";
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
        for(Entry<Source, OntologicalElement> e : this.uriImplicate.entrySet())
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
    
    public String toOWL(String baseUri)
    {
        this.uriOntObj = this.getUriOntObj(baseUri);
        String ret = "";
        for(Entry<Source, OntologicalElement> e : this.uriImplicate.entrySet())
        {
            ret += "<"+uriOntObj+"> owl:sameAs <"+e.getValue()+">.\n";
        }
        
         if(this.labelCands.size() > 0)
        {
             for(LabelCandidate lc : this.labelCands)
             {
                 ret += lc.toOWL(baseUri);
             }
        }
        
        return ret;
    }
    
}
