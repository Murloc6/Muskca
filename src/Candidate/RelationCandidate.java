/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Candidate;

import Source.Source;
import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author fabien.amarger
 */
public class RelationCandidate extends Candidate
{
    private InstanceCandidate ic;
    private InstanceCandidate icHR;
    private ArrayList<Source> sourcesHR;
    
    private String relImp;
    
    public RelationCandidate(String relImp, InstanceCandidate ic, InstanceCandidate icHR, ArrayList<Source> sources)
    {
        super();
        this.ic = ic;
        this.icHR = icHR;
        this.sourcesHR = sources;
        this.relImp = relImp;
    }

    @Override
    public void addElem(Source s, String uriElem) 
    {
    }

    @Override
    public void computeTrustScore(float trustMax) 
    {
        //super.computeTrustScore(trustMax);
        this.trustScoreSimple = (float)this.sourcesHR.size()/(float)3;
        this.trustScore = this.ic.getTrustScore();
        this.trustScore += this.icHR.getTrustScore();
        this.trustScore /= 2;
        float sourcesQualPart = 0;
        for(Source s : this.sourcesHR)
        {
            //sourcesQualPart += s.getSourceQualityScore();
            sourcesQualPart ++;
        }
        this.trustScore += sourcesQualPart;
        this.trustScore /= trustMax;
    }
    
    @Override
    public String toString() {
        String ret = "";
        
         ret += "\t ->(Simple : "+this.trustScoreSimple+" | Degree : "+this.trustScore+")  <";
        for(String uriElem : this.icHR.uriImplicate.values())
        {
            ret += uriElem+" || ";
        }
        ret += ">\n";
        for(Source s : this.sourcesHR)
        {
            ret += "\t\t "+s.getName()+"("+s.getSourceQualityScore()+")\n";
        }


        return ret;
    }

    @Override
    public BasicDBObject toDBObject() 
    {
        BasicDBObject doc = new BasicDBObject();
        ArrayList<BasicDBObject> icBDBO = new ArrayList<>();
        for(Map.Entry<Source, String> icE: ic.getElemCandidate().entrySet())
        {
            BasicDBObject icObj = new BasicDBObject();
            icObj.append("source", icE.getKey().getName());
            icObj.append("uri", icE.getValue());
            icBDBO.add(icObj);
        }
        doc.append("ic", icBDBO);

        ArrayList<BasicDBObject> icHRBD = new ArrayList<>();
        for(Map.Entry<Source, String> icHRE : icHR.getElemCandidate().entrySet())
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

        doc.append("relation", relImp);

        doc.append("trustScore", this.getTrustScore());
        doc.append("trustScoreSimple", this.trustScoreSimple);
        
        return doc;
    }


    public String toProvO(String baseUri, int numCand, int instCand,  HashMap<Source, String> sourcesUri, HashMap<Source, String> uriInst, String uriOntObj, String uriKbMerge)
    {
        String rel = this.relImp;
        InstanceCandidate ic2 = this.icHR;
        String uriCand =baseUri+this.sElem+"/Cand/"+instCand+"/"+numCand;
        String ic2OntObj = ic2.getUriOntObj(baseUri);
        String ret = "<"+uriCand+"> rdf:type :Entity; rdf:type rdf:Statement; rdf:subject <"+uriOntObj+">; rdf:predicate <"+rel+">; rdf:object <"+ic2OntObj+">.\n ";
        ret += "<"+uriKbMerge+"> :hadMember <"+uriCand+">.";
        ret += "<"+uriCand+"> <"+baseUri+"hadTrustScore> \""+this.getTrustScore()+"\"^^xsd:double.\n";
        
        for( Source s : this.sourcesHR)
        {
            String uriStatement = baseUri+this.sElem+"/"+s.getName()+"/"+instCand+"/"+numCand;
            ret += "<"+uriStatement+"> rdf:type :Entity; rdf:type rdf:Statement.\n";
            ret += "<"+uriStatement+"> rdf:subject <"+uriInst.get(s)+">; rdf:predicate <"+rel+">; rdf:object <"+this.icHR.getUriIC(s)+">. \n";
            ret += "<"+sourcesUri.get(s)+"> :hadMember <"+uriStatement+">.\n";
            ret += "<"+uriCand+"> :wasDerivedFrom <"+uriStatement+">. \n";
        }
        
        return ret;
    }
    
}
