/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Candidate.ArcCandidate;

import Candidate.NodeCandidate.IndividualCandidate;
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
public class RelationCandidate extends ArcCandidate
{
    private static int numInstGlob = 1;
    private int numInst = -1;
    
    private IndividualCandidate icTo;
    private ArrayList<Source> sourcesHR;
    private String baseUri;
    
    
    public RelationCandidate(String relImp, IndividualCandidate ic, IndividualCandidate icTo, ArrayList<Source> sources)
    {
        super(ic, relImp);
        this.icTo = icTo;
        this.sourcesHR = sources;
    }
    
    @Override
    public String toString() {
        String ret = super.toString();
        ret += "\t\t ---- TO ---- \n";
        for(Entry<Source, String> e : this.icTo.getUriImplicate().entrySet())
        {
            ret += "\t\t"+e.getKey().getName()+" -> "+e.getValue()+"\n";
        }

        return ret;
    }

    @Override
    public BasicDBObject toDBObject() 
    {
        BasicDBObject doc = super.toDBObject();

        ArrayList<BasicDBObject> icHRBD = new ArrayList<>();
        for(Map.Entry<Source, String> icHRE : this.icTo.getUriImplicate().entrySet())
        {
            BasicDBObject icHRObj = new BasicDBObject();
            icHRObj.append("source", icHRE.getKey().getName());
            icHRObj.append("uri", icHRE.getValue());
            icHRBD.add(icHRObj);
        }
        doc.append("icHR", icHRBD);

        ArrayList<String> listSourcesInvol = new ArrayList<>();
        for(Source s :sourcesHR )
        {
            //relCandidateTrustScore += s.getSourceQualityScore();
            listSourcesInvol.add(s.getName());
        }
        doc.append("sources", listSourcesInvol);
        
        return doc;
    }


    public String toProvO(String baseUri, int numCand, int instCand,  HashMap<Source, String> sourcesUri, HashMap<Source, String> uriInst, String uriOntObj, String uriKbMerge)
    {
        this.baseUri = baseUri;
        String ret = super.toProvO(baseUri, numCand, sourcesUri, uriKbMerge);
        
        for( Source s : this.sourcesHR)
        {
            String uriStatement = baseUri+this.sElem+"/"+s.getName()+"/"+instCand+"/"+numCand;
            ret += "<"+uriStatement+"> rdf:type :Entity; rdf:type rdf:Statement.\n";
            ret += "<"+uriStatement+"> rdf:subject <"+uriInst.get(s)+">; rdf:predicate <"+this.dataProperty+">; rdf:object <"+this.icTo.getUriFromSource(s)+">. \n";
            ret += "<"+sourcesUri.get(s)+"> :hadMember <"+uriStatement+">.\n";
            ret += "<"+uriCand+"> :wasDerivedFrom <"+uriStatement+">. \n";
        }
        
        return ret;
    }

    @Override
    public String getObjectProvOValue() 
    {
        return this.icTo.getUriOntObj(this.baseUri);
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
