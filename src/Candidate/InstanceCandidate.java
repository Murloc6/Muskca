/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Candidate;

import Alignment.Alignment;
import Source.Source;
import muskca.Muskca;
import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author fabien.amarger
 */
public class InstanceCandidate extends Candidate
{
    
     
    private static int numInstGlob = 1;
    private int numInst = -1;
    
     ArrayList<Alignment> aligns;
    
    
    private float trustSource = 0;
    private float trustAlign = 0;
    private float trustICHR = 0;
    
    /*private InstanceCandidate icHR;
    private ArrayList<Source> sourcesHR;
    
    private String typeCandidate = "";
    private ArrayList<Source> sourcesTypeCand;
    
    private HashMap<String, ArrayList<LabelCandidate>> labelCandidates;*/
    
    private ArrayList<RelationCandidate> relCands;
    private ArrayList<TypeCandidate> typeCands;
    private ArrayList<LabelCandidate> labelCands;
    
    public InstanceCandidate()
    {
        super();
        this.uriImplicate = new HashMap<>();
        this.aligns = new ArrayList<>();
        //this.labelCandidates = new HashMap<>();
        
        this.relCands = new ArrayList<>();
        this.typeCands = new ArrayList<>();
        this.labelCands = new ArrayList<>();
    }
    
    /*public void addLabelCandidates(String rel, ArrayList<LabelCandidate> lcs)
    {
        this.labelCandidates.put(rel, lcs);
    }
    
    public HashMap<String, ArrayList<LabelCandidate>> getLabelsCandidate()
    {
        return this.labelCandidates;
    }*/
    
    
    
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
    
    public void addAlignment(Alignment a)
    {
        if(a != null)
          this.aligns.add(a);
    }
     
     public HashMap<Source, String> getElemCandidate()
     {
         return this.uriImplicate;
     }
    
    public String getUriIC(Source s)
    {
        return this.uriImplicate.get(s);
    }
    
   /* public void addIcHR(InstanceCandidate icHR, ArrayList<Source> sourcesHR)
    {
        this.icHR = icHR;
        this.sourcesHR = sourcesHR;
        //this.computeTrustScore();
    }
    
    public void addTypeCandidate(String uriType, ArrayList<Source> sourcesTypeCandidate)
    {
        this.typeCandidate = uriType;
        this.sourcesTypeCand = sourcesTypeCandidate;
    }*/
    
    public void addRelCandidate(RelationCandidate relC)
    {
        this.relCands.add(relC);
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
    
    public void addTypeCandidate(TypeCandidate typeC)
    {
        this.typeCands.add(typeC);
    }
    
    public boolean containsCandidate(Source s, String uri)
    {
        boolean ret = false;
        String uriIC = this.uriImplicate.get(s);
        if(uriIC != null)
        {
            ret = uri.compareTo(uriIC) == 0;
        }
        return ret;
    }
    
    
    
     @Override
    public void computeTrustScore(float trustIcMax)
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
//        float sourcesHRTrustPart = 0;
//        if(this.icHR != null)
//        {
//            for(Source s : this.sourcesHR)
//            {
//                sourcesHRTrustPart += s.getSourceQualityScore();
//            }
//            sourcesHRTrustPart = sourcesHRTrustPart/2;
//        }
        
        //this.trustSource = sourceTrustPart;
        this.trustAlign = alignTrustPart;
        /*if(this.icHR != null)
            this.trustICHR = sourcesHRTrustPart*this.icHR.getTrustScore();*/
        
        //sourceTrustPart = 1f;
        
          //this.trustScore = (float) (1/(1+Math.exp(6-3*(sourceTrustPart * alignTrustPart)))); //sigmoid function to normalize
        //this.trustScore = (float) (1/(1+Math.exp(2-1*((sourceTrustPart * alignTrustPart)+this.trustICHR)))); //sigmoid function to normalize
        //this.trustScore = (float) (1/(1+Math.exp(2-1*(alignTrustPart)))); //sigmoid function to normalize
        this.trustScore = (float) ((alignTrustPart)/trustIcMax);
    }
    
    public void addAlignments(ArrayList<Alignment> aligns)
    {
        for (Alignment a : aligns)
        {
            if(a != null)
                this.addAlignment(a);
        }
    }
    
    
    
    @Override
    public String toString()
    {
        String ret = "Instance Candidate ("+this.trustScore+"): \n";
        for(Entry<Source, String> e : this.uriImplicate.entrySet())
        {
            ret += "\t "+e.getKey().getName()+" : "+e.getValue()+"\n";
        }
        for(Alignment a : this.aligns)
        {
            ret += "\t \t *** "+a.getUri()+" -->"+a.getUriAlign()+" ("+a.getValue()+") \n";
        }
        
        if(this.typeCands.size() > 0)
        {
            ret += "\t Type Candidate : \n";
            for(TypeCandidate tc : this.typeCands)
            {
                ret += tc.toString();
            }
        }
        
        if(this.labelCands.size() > 0)
        {
             ret += "\t Label Candidate : \n";
             for(LabelCandidate lc : this.labelCands)
             {
                 ret += lc.toString();
             }
        }
        
        if(this.relCands.size() > 0)
        {
            ret += "\t Relation Candidate : \n";
            for(RelationCandidate rc : this.relCands)
            {
                ret += rc.toString();
            }
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
           /*ArrayList<BasicDBObject> alignsObject = new ArrayList<>();
           for(Alignment al : aligns)
           {
               BasicDBObject alignDoc = new BasicDBObject();
               alignDoc.append("uri1", al.getUri());
               alignDoc.append("uri2", al.getUriAlign());
               alignDoc.append("trustScore", al.getValue());
               alignsObject.add(alignDoc);
           }
           doc.append("aligns", alignsObject);*/
           doc.append("trustScore", this.getTrustScore());

           return doc;
    }

    public String getUriOntObj(String baseUri)
    {
        String ret = "";
        
        if(this.numInst < 0)
        {
            this.numInst = InstanceCandidate.numInstGlob;
            InstanceCandidate.numInstGlob++;
        }
        ret = baseUri+this.sElem+"/"+this.numInst;
        
        return ret;
    }
    
    public String getUriCand(String baseUri)
    {
        String ret = "";
        
        if(this.numInst < 0)
        {
            this.numInst = InstanceCandidate.numInstGlob;
            InstanceCandidate.numInstGlob++;
        }
        ret = baseUri+this.sElem+"/Cand/"+this.numInst;
        
        return ret;
    }
    
    public String toProvO(String baseUri, int numInst, HashMap<Source, String> sourcesUri, String uriKbMerge)
    {
        String uriCand = this.getUriCand(baseUri);
        String uriOntObj = this.getUriOntObj(baseUri);
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
        
        if(this.typeCands.size() > 0)
        {
            int numType = 1;
            for(TypeCandidate tc : this.typeCands)
            {
                ret += tc.toProvO(baseUri, numType, this.numInst, sourcesUri, this.uriImplicate,  uriOntObj, uriKbMerge);
                numType ++;
            }
        }
        
        if(this.labelCands.size() > 0)
        {
            int numLabel = 1;
             for(LabelCandidate lc : this.labelCands)
             {
                 ret += lc.toProvO(baseUri, numLabel, this.numInst, sourcesUri, this.uriImplicate, uriOntObj, uriKbMerge);
                 numLabel ++;
             }
        }
        
        
        if(this.relCands.size() > 0)
        {
            int numRel = 1;
            for(RelationCandidate rc : this.relCands)
            {
                ret += rc.toProvO(baseUri, numRel, this.numInst, sourcesUri, this.uriImplicate,  uriOntObj, uriKbMerge);
                numRel++;
            }
        }
        
        return ret;
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
}
