/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Candidate.ArcCandidate;

import Candidate.NodeCandidate.NodeCandidate;
import Source.OntologicalElement.OntologicalElement;
import Source.Source;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author fabien.amarger
 */
public class RelationCandidate extends ArcCandidate
{
    private static int numInstGlob = 1;
    private int numInst = -1;
    
    private NodeCandidate ncTo;
    private ArrayList<Source> sourcesImpl;
    
    
    public RelationCandidate(NodeCandidate ncFrom, String relImp, NodeCandidate ncTo, ArrayList<Source> sourcesImpl)
    {
        super(ncFrom, relImp);
        this.ncTo = ncTo;
        this.sourcesImpl = sourcesImpl;
        for(Source s : sourcesImpl)
        {
            String uriSubject = ncFrom.getUriFromSource(s).getUri();
            String uriObject = ncTo.getUriFromSource(s).getUri();
            this.addElem(s, uriSubject+" "+relImp+" "+uriObject);
        }
    }
    
    public boolean isToNc(NodeCandidate nc)
    {
        return this.ncTo == nc;
    }
    
//    @Override
//    public String toString() {
//        String ret = super.toString();
//        ret += "\t\t ---- TO ---- \n";
//        for(Entry<Source, String> e : this.ncTo.getUriImplicate().entrySet())
//        {
//            ret += "\t\t"+e.getKey().getName()+" -> "+e.getValue()+"\n";
//        }
//
//        return ret;
//    }



    public String toProvO(String baseUri, int numCand, int instCand,  HashMap<Source, String> sourcesUri, HashMap<Source, OntologicalElement> uriInst, String uriOntObj, String uriKbMerge)
    {
        String ret = super.toProvO(baseUri, numCand, sourcesUri, uriKbMerge);
        
        for( Source s : this.sourcesImpl)
        {
            String uriStatement = baseUri+this.sElem+"/"+s.getName()+"/"+instCand+"/"+numCand;
            ret += "<"+uriStatement+"> rdf:type :Entity; rdf:type rdf:Statement.\n";
            ret += "<"+uriStatement+"> rdf:subject <"+uriInst.get(s)+">; rdf:predicate <"+this.dataProperty+">; rdf:object <"+this.ncTo.getUriFromSource(s)+">. \n";
            ret += "<"+sourcesUri.get(s)+"> :hadMember <"+uriStatement+">.\n";
            ret += "<"+uriCand+"> :wasDerivedFrom <"+uriStatement+">. \n";
        }
        
        return ret;
    }
    
    @Override
    public String toOWL(String baseUri)
    {
        return "<"+this.fromCandidate.getUriOntObj(baseUri)+"> <"+this.dataProperty+"> <"+this.ncTo.getUriOntObj(baseUri)+">. ";
    }

    @Override
    public String getObjectProvOValue() 
    {
        //return this.ncTo.getUriOntObj(baseUri);
        return "TODO";
    }

    @Override
    public String getUriOntObj(String baseUri)
    {
        String ret = "";
        
        if(this.numInst < 0)
        {
            this.numInst = RelationCandidate.numInstGlob;
            RelationCandidate.numInstGlob++;
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
            this.numInst = RelationCandidate.numInstGlob;
            RelationCandidate.numInstGlob++;
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
