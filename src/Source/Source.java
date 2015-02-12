/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Source;

import Alignment.Aligner;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author murloc
 */
public class Source implements Serializable
{
    private String name;
    private String baseUri;
    private SparqlProxy sp;
    private ArrayList<String> allTaxons;
    
    private ArrayList<Aligner> aligners;
    
    private float trustScoreInstance = 0;
    private float trustScoreType = 0;
    private float trustScoreObjProp = 0;
    
    private float trustScoreInstanceInType = 0;
    private float trustScoreInstanceInObjProp = 0;
    
    private float sourceQualityScore = 0;

    private String tempFileName;
    
    public Source(String name, String baseUri, String spUrl)
    {
        this.name = name;
        this.baseUri = baseUri;
        this.sp = SparqlProxy.getSparqlProxy(spUrl);
        this.allTaxons = new ArrayList<>();
        this.aligners = new ArrayList<>();
    }
    

    public void setScores(float freshness, float reputation, float ADOMSim)
    {
        this.sourceQualityScore = SourceQualityScore.computeQualityScore(freshness, reputation, ADOMSim);
    }
    
    public float getSourceQualityScore()
    {
        return this.sourceQualityScore;
    }
    
    public float  getTrustScoreInstance()
    {
        return this.trustScoreInstance;
    }
    public float getTrustScoreType()
    {
        return this.trustScoreType;
    }
    public float getTrustScoreObjProp()
    {
        return this.trustScoreObjProp;
    }
    public float getTrustScoreInstanceInType()
    {
        return this.trustScoreInstanceInType;
    }
    public float getTrustScoreInstanceInObjProp()
    {
        return this.trustScoreInstanceInObjProp;
    }
    
    public String getBaseUri()
    {
        return this.baseUri;
    }
    
   
    
    public ArrayList<String> getAllTaxons()
    {
        if(this.allTaxons.isEmpty())
        {
            String query = "SELECT ?uri WHERE { { SELECT ?uri WHERE { ?uri a <http://ontology.irstea.fr/AgronomicTaxon#Taxon>.}} "+
                    "UNION { SELECT ?uri WHERE { ?uri a <http://ontology.irstea.fr/AgronomicTaxon#ClassRank>} }"+
                    "UNION { SELECT ?uri WHERE { ?uri a <http://ontology.irstea.fr/AgronomicTaxon#FamilyRank>} }"+
                    "UNION { SELECT ?uri WHERE { ?uri a <http://ontology.irstea.fr/AgronomicTaxon#GenusRank>} }"+
                    "UNION { SELECT ?uri WHERE { ?uri a <http://ontology.irstea.fr/AgronomicTaxon#KingdomRank>} }"+
                    "UNION { SELECT ?uri WHERE { ?uri a <http://ontology.irstea.fr/AgronomicTaxon#OrderRank>} }"+
                    "UNION { SELECT ?uri WHERE { ?uri a <http://ontology.irstea.fr/AgronomicTaxon#PhylumRank>} }"+
                    "UNION { SELECT ?uri WHERE { ?uri a <http://ontology.irstea.fr/AgronomicTaxon#SpecyRank>} }"+
                    "UNION { SELECT ?uri WHERE { ?uri a <http://ontology.irstea.fr/AgronomicTaxon#VarietyRank>} }}";
            ArrayList<JsonNode> uris = this.sp.getResponse(query);
            for(JsonNode jn : uris)
            {
                this.allTaxons.add(jn.get("uri").get("value").asText());
            }
        }
            
        return this.allTaxons;
    }
    
    public ArrayList<String> getRelImportant(String uri, String uriRelImp)
    {
        ArrayList<String> ret = new ArrayList<>();
        String query = "SELECT ?uri WHERE { <"+uri+"> <"+uriRelImp+"> ?uri}";
        ArrayList<JsonNode> uris = this.sp.getResponse(query);
        for(JsonNode jn : uris)
        {
            ret.add(jn.get("uri").get("value").asText());
        }
        return ret;
    }
    
    public boolean isRelImportant(String uri1, String uriRelImp, String uri2)
    {
        String query = "ASK {<"+uri1+"> <"+uriRelImp+"> <"+uri2+">}";
        return this.sp.sendAskQuery(query);
    }
    
    public boolean isType(String uri1, String uriRelImp, String uri2)
    {
        String query = "ASK {<"+uri1+"> <"+uriRelImp+"> <"+uri2+">}";
        boolean ret = this.sp.sendAskQuery(query);
        return ret;
    }
    
    public ArrayList<String> getAllPredicateUri(String uriObject, String uriRel)
    {
        ArrayList<String> ret = new ArrayList<>();
        String query = "SELECT ?pred { <"+uriObject+"> <"+uriRel+"> ?pred. }";
        ArrayList<JsonNode> urisPreds = this.sp.getResponse(query);
        for(JsonNode jn : urisPreds)
        {
            ret.add(jn.get("pred").get("value").asText());
        }
        return ret;
    }
    
    public ArrayList<JsonNode> getAllLabels(String uri)
    {
        String query = "SELECT ?l WHERE { <"+uri+"> (<http://ontology.irstea.fr/AgronomicTaxon#hasScientificName> | <http://ontology.irstea.fr/AgronomicTaxon#hasVernacularName>) ?l.}";
        
        return this.sp.getResponse(query);
    }
    
    public ArrayList<JsonNode> getAllPrefLabesl(String uri)
    {
        return this.sp.getResponse("SELECT ?l WHERE { <"+uri+"> <http://ontology.irstea.fr/AgronomicTaxon#hasScientificName> ?l. FILTER (langMatches( lang(?l), 'FR') || langMatches( lang(?l), 'EN') || langMatches( lang(?l), '')). }");
    }
    
    public String getTypeUri(String uri)
    {
        String ret = null;
        String query ="SELECT ?uri WHERE{<"+uri+"> a ?uri}";
        ArrayList<JsonNode> resp = this.sp.getResponse(query);
        if(resp.size() > 0)
        {
            ret = resp.get(0).get("uri").get("value").textValue();
        }
        return ret;
    }
    
    public boolean isSameType(String uri, String typeUri)
    {
        return this.sp.sendAskQuery("ASK {?type rdfs:subClassOf * <"+typeUri+">. <"+uri+"> a ?type.}");
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public void addAligner(Aligner a)
    {
        this.aligners.add(a);
    }
    
    
    public String getTempExport()
    {
        if(this.tempFileName == null)
        {
            String dateFileName = new SimpleDateFormat("dd-MM_HH-mm").format(new Date());
            this.tempFileName = "temp/"+this.getName()+"_tmp_"+dateFileName; // /out and .owl automatically added by the SparlProxy class
            File tempFile = this.sp.writeKBFile(this.tempFileName);
            if(tempFile != null)
            {
                //this.tempFileName = tempFile.getAbsolutePath();
                this.tempFileName = tempFile.getName();
            }
        }
        
        return this.tempFileName;
    }
    
}
