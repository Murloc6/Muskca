/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Source;

import Source.OntologicalElement.OntologicalElement;
import Alignment.Aligner;
import Alignment.Alignment;
import MultiSources.Fusionner;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author murloc
 */
public class Source implements Serializable
{
    private String name;
    private String baseUri;
    private SparqlProxy sp;
    private HashMap<String, ArrayList<Alignment>> allInd;
    private HashMap<String, ArrayList<Alignment>> allClasses;
    
    private HashMap<String, OntologicalElement> elems;
   
    private ArrayList<Aligner> aligners;
    
    private float trustScoreInstance = 0;
    private float trustScoreType = 0;
    private float trustScoreObjProp = 0;
    
    private float trustScoreInstanceInType = 0;
    private float trustScoreInstanceInObjProp = 0;
    
    private float sourceQualityScore = 1;

    private String tempFileName;
    
    private Fusionner fusionner;
    
    public Source(String name, String baseUri, String spUrl)
    {
        this.name = name;
        this.baseUri = baseUri;
        this.sp = SparqlProxy.getSparqlProxy(spUrl);
        this.allInd = new HashMap<>();
        this.allClasses = new HashMap<>();
        this.aligners = new ArrayList<>();
        this.elems = new HashMap<>();
        this.fusionner = fusionner;
    }
    
    public void setFusionner(Fusionner fusionner)
    {
        this.fusionner = fusionner;
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
    
    
    public void addClassAlignment(String sClass, Alignment a)
    {
        ArrayList<Alignment> cAligns = this.allClasses.get(sClass);
        if(cAligns == null)
        {
            cAligns = new ArrayList<>();
        }
        cAligns.add(a);
        this.allClasses.put(sClass, cAligns);
    }
    
    public void addIndAlignment(String sInd, Alignment a)
    {
        ArrayList<Alignment> cAligns = this.allInd.get(sInd);
        if(cAligns == null)
        {
            cAligns = new ArrayList<>();
        }
        cAligns.add(a);
        this.allInd.put(sInd, cAligns);
    }
    
    public ArrayList<String> getAllClasses()
    {
        ArrayList<String> ret = new ArrayList<>();
        
        for(String s : this.allClasses.keySet())
        {
            ret.add(s);
        }
        
        return ret;
    }
    
    public ArrayList<String> getAllInd()
    {
        ArrayList<String> ret = new ArrayList<>();
        for(String s : this.allInd.keySet())
        {
            ret.add(s);
        }
        
        return ret;
    }
    
    public Alignment getAlignmentInd(String uriFrom, String uriTo)
    {
        Alignment ret = null;
        ArrayList<Alignment> aligns = this.allInd.get(uriFrom);
        for(Alignment a : aligns)
        {
            if((a.getUri().equalsIgnoreCase(uriFrom) && a.getUriAlign().equalsIgnoreCase(uriTo)) || (a.getUri().equalsIgnoreCase(uriTo) && a.getUriAlign().equalsIgnoreCase(uriFrom)))
            {
                ret = a;
                break;
            }
        }
        return ret;
    }
    
    public Alignment getAlignmentClass(String uriFrom, String uriTo)
    {
        Alignment ret = null;
        ArrayList<Alignment> aligns = this.allClasses.get(uriFrom);
        for(Alignment a : aligns)
        {
            if((a.getUri().equalsIgnoreCase(uriFrom) && a.getUriAlign().equalsIgnoreCase(uriTo)) || (a.getUri().equalsIgnoreCase(uriTo) && a.getUriAlign().equalsIgnoreCase(uriFrom)))
            {
                ret = a;
                break;
            }
        }
        return ret;
    }
    
    public void addOntoElem(String uri, OntologicalElement elem){
        this.elems.put(uri, elem);
    }
    
    public OntologicalElement getElem(String uri){
        return this.elems.get(uri);
    }
    
    public ArrayList<OntologicalElement> getElems(){
        return new ArrayList<>(this.elems.values());
    }
    
    public ArrayList<String> getAllSameAs(String uri){
        ArrayList<String> ret = new ArrayList<>();
        String query = "SELECT DISTINCT ?uri WHERE{{<"+uri+"> owl:sameAs ?uri.} UNION {?uri owl:sameAs <"+uri+">.}}";
        for(JsonNode jn : this.sp.getResponse(query)){
            ret.add(jn.get("uri").get("value").asText());
        }
        return ret;
    }
   
    public BasicDBObject toDBObject()
    {
        BasicDBObject ret = new BasicDBObject();
        ret.append("name", this.name);
        ret.append("baseUri", this.baseUri);
        ret.append("qualityScore", this.sourceQualityScore);
        return ret;
    }
    
    
    public ArrayList<String> getAllIndividualUris(String uriClassInd)
    {
        ArrayList<String> ret = new ArrayList<>();
        String query = " SELECT ?uri WHERE { ?uri a ?cls. ?cls rdfs:subClassOf* <"+uriClassInd+">.} ";
        ArrayList<JsonNode> uris = this.sp.getResponse(query);
        for(JsonNode jn : uris)
        {
            ret.add(jn.get("uri").get("value").asText());
        }
            
        return ret;
    }
    
    public ArrayList<String> getAllClassUris(String baseModule)
    {
        ArrayList<String> ret = new ArrayList<>();
        String query = "SELECT DISTINCT ?uri WHERE {{?uri a owl:Class.} UNION {?uri rdfs:subClassOf ?a}} ";
        ArrayList<JsonNode> uris = this.sp.getResponse(query);
        for(JsonNode jn : uris)
        {
            if(jn.get("uri") != null){ 
                String uri =jn.get("uri").get("value").asText();
                if(!uri.startsWith(baseModule))
                {
                    ret.add(uri);
                }
            }
        }
            
        return ret; 
   }
    
    /*public ArrayList<String> getAllTaxons()
    {
        if(this.allInd.isEmpty())
        {
            // TODO : Put all the classes usefull in the params file and change the name with get all ind
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
                this.allInd.add(jn.get("uri").get("value").asText());
            }
        }
            
        return this.allInd;
    }*/
    
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
    
    
    public String getTempExport(String moduleFile)
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
                try {
                    FileUtils.writeStringToFile(tempFile, "<"+this.baseUri+"> a <http://www.w3.org/2002/07/owl#Ontology>. \n", true);
                    FileUtils.writeStringToFile(tempFile, this.fusionner.getOwlFileToTtl(moduleFile), true);
                } catch (IOException ex) {
                    System.err.println("Can't export module to "+this.name+" temp file! ("+this.tempFileName+")");
                }
            }
        }
        
        return this.tempFileName;
    }
    
    public StringBuilder classesToPrologData()
    {
        StringBuilder ret = new StringBuilder("oe("+this.name.toLowerCase()+", [");
        boolean first = true;
        
        for(String s : this.getAllClasses())
        {
            if(!first)
            {
                ret = ret.append(", ");
            }
            else
            {
                first = false;
            }
            ret = ret.append("\"").append(s).append("\"");
        }
        
        ret = ret.append("]). \n");
        return ret;
    }
    
    public StringBuilder indsToPrologData()
    {
        StringBuilder ret = new StringBuilder("oe("+this.name.toLowerCase()+", [");
        boolean first = true;
        
        for(String s : this.getAllInd())
        {
            if(!first)
            {
                ret = ret.append(", ");
            }
            else
            {
                first = false;
            }
            ret = ret.append("\"").append(s).append("\"");
        }
        
        ret = ret.append("]). \n");
        return ret;
    }
    
}
