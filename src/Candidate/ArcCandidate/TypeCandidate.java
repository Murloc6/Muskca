/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Candidate.ArcCandidate;

import Candidate.Candidate;
import Candidate.NodeCandidate.ClassCandidate;
import Candidate.NodeCandidate.IndividualCandidate;
import Source.OntologicalElement.OntologicalElement;
import Source.Source;
import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author fabien.amarger
 */
public class TypeCandidate extends ArcCandidate
{
    private static int numInstGlob = 1;
    private int numInst = -1;
    
    private ClassCandidate cc;
    private String uriTypeCandidate;
    private String baseUri;
    
    public TypeCandidate(IndividualCandidate ic, String uriTypeCandidate)
    {
        super(ic, "rdf:type");
        this.uriTypeCandidate = uriTypeCandidate;
    }
    
    public TypeCandidate (IndividualCandidate ic, ClassCandidate cc)
    {
        super(ic, "rdf;type");
        this.cc = cc;
    }
    
    @Override
    public String toString()
    {
        String ret = super.toString();
        if(this.cc != null)
        {
            ret += "\n \t \t Class Candidate (";
            for(Entry<Source, OntologicalElement> e : this.cc.getUriImplicate().entrySet())
            {
                ret += e.getKey().getName()+" -> "+e.getValue()+" | ";
            }
            ret += ") (";
        }
        else
        {
            ret += "\t\t"+this.uriTypeCandidate+"\n";
        }

       return ret;
    }
    
    public String toProvO(String baseUri, int numCand, int instCand, HashMap<Source, String> sourcesUri, HashMap<Source, OntologicalElement> uriInst, String uriOntObj, String uriKbMerge)
    {
        this.baseUri = baseUri;
        String ret = super.toProvO(baseUri, numInst, sourcesUri, uriKbMerge);
        return ret;
    }
    
    public String toOWL(String baseUri)
    {
        String ret = "<"+this.fromCandidate.getUriOntObj(baseUri)+"> a <"+this.uriTypeCandidate+">.";
        return ret;
    }

    @Override
    public String getObjectProvOValue() 
    {
        String ret = "";
        if(this.cc == null)
        {
            ret = this.uriTypeCandidate;
        }
        else
        {
            this.cc.getUriOntObj(this.baseUri);
        }
        return ret;
    }

     @Override
    public String getUriOntObj(String baseUri)
    {
        String ret = "";
        
        if(this.numInst < 0)
        {
            this.numInst = TypeCandidate.numInstGlob;
            TypeCandidate.numInstGlob++;
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
            this.numInst = TypeCandidate.numInstGlob;
            TypeCandidate.numInstGlob++;
        }
        ret = baseUri+this.sElem+"/Cand/"+this.numInst;
        
        return ret;
    }

    @Override
    public int getNumInst() 
    {
        return this.numInst;
    }
    
}
