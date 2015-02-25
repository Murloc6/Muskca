/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Candidate.NodeCandidate;

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

    
    private ArrayList<RelationCandidate> relCands;
    private ArrayList<TypeCandidate> typeCands;
    
    public IndividualCandidate()
    {
        super();
       
        this.relCands = new ArrayList<>();
        this.typeCands = new ArrayList<>();
    }
    
    public void addRelCandidate(RelationCandidate relC)
    {
        this.relCands.add(relC);
    }
    
    
    public void addTypeCandidate(TypeCandidate typeC)
    {
        this.typeCands.add(typeC);
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
        
        if(this.relCands.size() > 0)
        {
            int numRel = 1;
            for(RelationCandidate rc : this.relCands)
            {
                ret += rc.toProvO(baseUri, numRel, this.numInst, sourcesUri, this.uriImplicate,  this.uriOntObj, uriKbMerge);
                numRel++;
            }
        }
        
        return ret;
    }
}
