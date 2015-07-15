/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Candidate.NodeCandidate;

import Candidate.ArcCandidate.ArcCandidate;
import Candidate.ArcCandidate.LabelCandidate;
import Candidate.ArcCandidate.RelationCandidate;
import Candidate.ArcCandidate.TypeCandidate;
import Source.Source;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author fabien.amarger
 */
public class IndividualCandidate extends NodeCandidate
{ 
    private static int numInstGlob = 1;
    private int numInst = -1;

    private ArrayList<TypeCandidate> typeCands;
    private ArrayList<RelationCandidate> relCands;
    
    public IndividualCandidate()
    {
        super();
        this.typeCands = new ArrayList<>();
        this.relCands = new ArrayList<>();
    }
    
//    public void addRelCandidate(RelationCandidate relC)
//    {
//        this.relCands.add(relC);
//    }
    
    
    public boolean addTypeCandidate(TypeCandidate typeC)
    {
        //this.typeCands.add(typeC);
        
         boolean isPresent = false;
        for(TypeCandidate typeCand : this.typeCands)
        {
            if(typeC.isSameCand(typeCand))
            {
                isPresent = true;
                break;
            }
            else if(typeCand.isSameCand(typeC))
            {
                this.typeCands.remove(typeCand);
                break;
            }
        }
        if(!isPresent)
            this.typeCands.add(typeC);
        return !isPresent;
    }
    
    public boolean addRelationCandidate(RelationCandidate rc)
    {
        boolean isPresent = false;
        for(RelationCandidate relCand : this.relCands)
        {
            if(rc.isSameCand(relCand))
            {
                isPresent = true;
                break;
            }
            else if(relCand.isSameCand(rc))
            {
                this.relCands.remove(relCand);
                break;
            }
        }
        if(!isPresent)
            this.relCands.add(rc);
        return !isPresent;
    }
    
    @Override
    public ArrayList<ArcCandidate> getAllArcCandidates()
    {
        ArrayList<ArcCandidate> ret = super.getAllArcCandidates();
        ret.addAll(this.typeCands);
        ret.addAll(this.relCands);
        return ret;
    }
    
    @Override
    public float getSumArcCandIntr()
    {
        float ret = super.getSumArcCandIntr();
        for(TypeCandidate tc : this.typeCands)
        {
            ret += tc.getTrustScore();
        }
        return ret;
    }
    
    @Override
        public float getSumArcCandImplied(NodeCandidate nc)
    {
        float ret = super.getSumArcCandImplied(nc);
        for(RelationCandidate rc : this.relCands)
        {
            if(rc.isToNc(nc))
            {
                ret += rc.getTrustScore();
//                System.out.println("RC trust score ---------------------------------------");
//                System.out.println(this.toString());
//                System.out.println(nc.toString());
//                System.out.println(rc.toString());
//                System.out.println("---------------------------------------------------------------------");
            }
        }
        return ret;
    }
    
    @Override
    public String toString()
    {
        String ret = super.toString();
        
        if(this.typeCands.size() > 0)
        {
            ret += "\t Type Candidate : \n";
            for(TypeCandidate tc : this.typeCands)
            {
                ret += tc.toString();
            }
        }
        if(this.relCands.size()>0)
        {
            for(RelationCandidate rc : this.relCands)
            {
                ret += "\t"+rc.toString();
            }
        }
        return ret;
    }
    
    
    @Override
     public String getUriOntObj(String baseUri)
    {
        String ret = "";
        
        if(this.numInst < 0)
        {
            this.numInst = IndividualCandidate.numInstGlob;
            IndividualCandidate.numInstGlob++;
        }
        ret = baseUri+this.sElem+"/"+this.numInst;
        
        return ret;
    }
    
    @Override
    public String getUriCand(String baseUri)
    {
        String ret = "";
        
        if(this.numInst < 0)
        {
            this.numInst = IndividualCandidate.numInstGlob;
            IndividualCandidate.numInstGlob++;
        }
        ret = baseUri+this.sElem+"/Cand/"+this.numInst;
        
        return ret;
    }
    
    @Override
    public int getNumInst()
    {
        return this.numInst;
    }
    
    @Override
    public String toProvO(String baseUri, int numInst, HashMap<Source, String> sourcesUri, String uriKbMerge)
    {
       String ret = super.toProvO(baseUri, numInst, sourcesUri, uriKbMerge);
        
        if(this.typeCands.size() > 0)
        {
            int numType = 1;
            for(TypeCandidate tc : this.typeCands)
            {
                ret += tc.toProvO(baseUri, numType, this.numInst, sourcesUri, this.uriImplicate,  this.uriOntObj, uriKbMerge);
                numType ++;
            }
        }
        
//        if(this.relCands.size() > 0)
//        {
//            int numRel = 1;
//            for(RelationCandidate rc : this.relCands)
//            {
//                ret += rc.toProvO(baseUri, numRel, this.numInst, sourcesUri, this.uriImplicate,  this.uriOntObj, uriKbMerge);
//                numRel++;
//            }
//        }
        
        return ret;
    }
    
     @Override
    public String toOWL(String baseUri)
    {
       String ret = super.toOWL(baseUri);
        
        if(this.typeCands.size() > 0)
        {
            int numType = 1;
            for(TypeCandidate tc : this.typeCands)
            {
                ret += tc.toOWL(baseUri);
                numType ++;
            }
        }
        
        if(this.relCands.size() > 0)
        {
            for(RelationCandidate rc : this.relCands)
            {
                ret += rc.toOWL(baseUri);
            }
        }
        
        return ret;
    }
}
