/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Candidate.NodeCandidate;

import Candidate.ArcCandidate.RelationCandidate;
import Candidate.ArcCandidate.TypeCandidate;
import Source.Source;
import java.util.HashMap;


/**
 *
 * @author fabien.amarger
 */
public class ClassCandidate extends NodeCandidate
{
     private static int numInstGlob = 1;
    private int numInst = -1;
    
    public ClassCandidate()
    {
        super();
    }

     @Override
    public int getNumInst()
    {
        return this.numInst;
    }

    public String toProvO(String baseUri, int numCand)
    {
        return "";
    }
    
    public String toOWL(String baseUri)
    {
        String ret = "<"+this.getUriOntObj(baseUri)+"> a owl:Class.";
        ret += super.toOWL(baseUri);
        return ret;
    }
     
     @Override
     public String getUriOntObj(String baseUri)
    {
        String ret = "";
        
        if(this.numInst < 0)
        {
            this.numInst = ClassCandidate.numInstGlob;
            ClassCandidate.numInstGlob++;
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
            this.numInst = ClassCandidate.numInstGlob;
            ClassCandidate.numInstGlob++;
        }
        ret = baseUri+this.sElem+"/Cand/"+this.numInst;
        
        return ret;
    }
    
}
