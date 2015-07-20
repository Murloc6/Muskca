/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Candidate.ArcCandidate;

import Candidate.Candidate;
import Candidate.NodeCandidate.NodeCandidate;
import Source.OntologicalElement.OntologicalElement;
import Source.Source;
import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author murloc
 */
public abstract class ArcCandidate extends Candidate
{
    protected NodeCandidate fromCandidate;
    protected String dataProperty;
    
    protected String uriOntObj = "";
    protected String uriCand = "";
    
    ArcCandidate(NodeCandidate from, String dataProperty)
    {
        super();
        this.dataProperty = dataProperty;
        this.fromCandidate = from;
    }

    public String getDataProp()
    {
        return this.dataProperty;
    }
    
    
    
    public abstract String getObjectProvOValue();
    
    @Override
    public String toProvO(String baseUri, int numInst, HashMap<Source, String> sourcesUri, String uriKbMerge) 
    {
        this.uriCand = this.getUriCand(baseUri);
        this.uriOntObj = this.getUriOntObj(baseUri);
        String ret = "<"+uriCand+"> rdf:type :Entity; rdf:type rdf:Statement; rdf:subject <"+uriOntObj+">; rdf:predicate <"+this.dataProperty+">; rdf:object  \""+this.getObjectProvOValue()+"\".\n ";
        ret += "<"+uriKbMerge+"> :hadMember <"+uriCand+">.";
        ret += "<"+uriCand+"> <"+baseUri+"hadTrustScore> \""+this.getTrustScore()+"\"^^xsd:double.\n";
        int idStatement = 1;
        for( Map.Entry<Source, OntologicalElement> e : this.uriImplicate.entrySet())
        {
            String uriStatement = baseUri+this.sElem+"/"+e.getKey().getName()+"/"+this.fromCandidate.getNumInst()+"/"+this.getNumInst()+"/"+idStatement;
            ret += "<"+uriStatement+"> rdf:type :Entity; rdf:type rdf:Statement.\n";
            ret += "<"+uriStatement+"> rdf:subject <"+this.fromCandidate.getUriImplicate().get(e.getKey())+">; rdf:predicate <"+this.dataProperty+">; rdf:object \""+e.getValue()+"\". \n";
            ret += "<"+sourcesUri.get(e.getKey())+"> :hadMember <"+uriStatement+">.\n";
            ret += "<"+uriCand+"> :wasDerivedFrom <"+uriStatement+">. \n";
            idStatement++;
        }
        
        return ret;
    }
    
    public abstract String toOWL(String baseUri);
    
}
