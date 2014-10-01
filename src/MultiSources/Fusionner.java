/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MultiSources;

import Alignment.Alignment;
import Alignment.StringDistance;
import Candidate.InstanceCandidate;
import Candidate.InstanceCandidateComparator;
import Candidate.LabelCandidate;
import Candidate.RelationCandidate;
import Candidate.TypeCandidate;
import Source.Source;
import Source.SparqlProxy;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author murloc
 */
public class Fusionner implements Serializable
{
   
    
    private ArrayList<InstanceCandidate>instCandidates;
    private SparqlProxy spAlignTemp;
    private HashMap<String, Alignment> uriAlignment;
    private ArrayList<Source> sources;
    private HashMap<InstanceCandidate, InstanceCandidate> icHRs;
   
    private int idAlign = 0;
    
    public int nbMongoSaved = 0;
    
    public Fusionner( String spUri)
    {
        this.spAlignTemp = SparqlProxy.getSparqlProxy(spUri);
        this.uriAlignment = new HashMap<>();
        
        this.instCandidates = new ArrayList<>();
        this.icHRs = new HashMap<>();
    }
    
    public ArrayList<InstanceCandidate> getHyp(Source s, String uri)
    {
        ArrayList<InstanceCandidate> ret = new ArrayList<>();
        for(InstanceCandidate ic : this.instCandidates)
        {
            if(ic.containsCandidate(s, uri))
            {
                ret.add(ic);
            }
        }
        return ret;
    }
    
    
    public Source getSourceByName(String sourceName)
    {
        Source ret = null;
        int i = 0;
        if(sourceName != null)
        {
            while (ret == null && i< this.sources.size())
            {
                Source s = this.sources.get(i);
                if(s.getName().compareTo(sourceName) == 0)
                {
                    ret = s;
                }
                i++;
            }
        }
        return ret;
    }
    
    public String getValueByNode(JsonNode jn, String var)
    {
        try
        {
            return jn.get(var).get("value").asText();
        }
        catch(NullPointerException e)
        {
            return null;
        }
    }
    
    private DBCollection connectMongo(String mongoCollection)
    {
        DBCollection collMongo = null;
        try
        {
            MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
            DB db = mongoClient.getDB( "valRKB" );
           collMongo = db.getCollection(mongoCollection);
           collMongo.remove(new BasicDBObject());
        } 
        catch (UnknownHostException ex)
        {
            System.err.println("ERROR during connection to mongoDB ...");
            System.exit(0);
        }
        return collMongo;
    }
    
    public void computeInstanceCandidate(String mongoCollection, float trustIcMax)
    {
          DBCollection collMongo = null;
        if(mongoCollection != null)
        {
            collMongo = connectMongo(mongoCollection);
        }
        System.out.println("BEGIN COMPUTE Instance CANDIDATE");
       
        
        String query = "PREFIX : <http://www.amarger.murloc.fr/AlignmentOntology#> \n SELECT  ?a ?b ?c ?saName ?sbName ?scName ?align1 ?align2 ?align3 \n" +
                "WHERE\n" +
"{\n" +
"{ \n" +
"	?a :belongsTo ?sa.\n" +
"	?b :belongsTo ?sb.\n" +
"	?align1 :alignBetween ?a.\n" +
"	?align1 :alignBetween ?b.\n" +
"	?sa :hasName ?saName.\n" +
"	?sb :hasName ?sbName.\n" +
"	OPTIONAL{\n" +
"		?c :belongsTo ?sc.\n" +
"		?sc :hasName ?scName.\n" +
"		?align2 :alignBetween ?b.\n" +
"		?align2 :alignBetween ?c.\n" +
"	}\n" +
"	OPTIONAL{\n" +
"		?c :belongsTo ?sc.\n" +
"		?sc :hasName ?scName.\n" +
"		?align3 :alignBetween ?c.\n" +
"		?align3 :alignBetween ?a.\n" +
"	}\n" +
"	FILTER(?a != ?b && ?sa != ?sb && ?b != ?c  && ?sb != ?sc &&  ?c != ?a && ?sc != ?sa)\n" +
"}\n" +
"UNION\n" +
"{\n" +
"	?align1 :alignBetween ?a.\n" +
"	?align1 :alignBetween ?b.\n" +
"	?a :belongsTo ?sa.\n" +
"	?b :belongsTo ?sb.\n" +
                "     ?sa :hasName ?saName.\n"+
                "      ?sb :hasName ?sbName.\n"+
"	FILTER( ?a != ?b &&\n" +
"	NOT EXISTS\n" +
"	{\n" +
"		?align2 :alignBetween ?c.\n" +
"		?align2 :alignBetween ?a.\n" +
"		?c :belongsTo ?sc.\n" +
"		FILTER( ?sc != ?sb && ?sc != ?sa)\n" +
"	} &&\n" +
"        NOT EXISTS\n" +
"	{\n" +
"		?align2 :alignBetween ?c.\n" +
"		?align2 :alignBetween ?b.\n" +
"		?c :belongsTo ?sc.\n" +
"		FILTER( ?sc != ?sb && ?sc != ?sa)\n" +
"	}\n" +
")\n" +
"}\n" +
"}";
        
        ArrayList<JsonNode> jsonCandidates = this.spAlignTemp.getResponse(query);
        ArrayList<String> hashCandidate ;
        HashMap<String, InstanceCandidate> icTreated = new HashMap<>();
        int nbCandidate = 0;
        for(JsonNode jn : jsonCandidates)
        {
            hashCandidate = new ArrayList<>();
            String a = this.getValueByNode(jn, "a");
            String b = this.getValueByNode(jn, "b");
            String c = this.getValueByNode(jn, "c");
            hashCandidate.add(a);
            hashCandidate.add(b);
            if(c != null)
            {
                hashCandidate.add(c);
            }
           Collections.sort(hashCandidate);
           String hashID = "";
           for(String s : hashCandidate)
           {
               if(s != null)
                hashID += s;
           }
           
           InstanceCandidate candidate = icTreated.get(hashID);
           if(candidate == null)
           {
               //System.out.println(jn);
               ArrayList<Alignment> aligns = new ArrayList<>();
               //System.out.println(hashID);
               InstanceCandidate ic = new InstanceCandidate();
               
               
               
               Source sa = this.getSourceByName(this.getValueByNode(jn, "saName"));
               Source sb = this.getSourceByName(this.getValueByNode(jn, "sbName"));
               String scName = this.getValueByNode(jn, "scName");
               Source sc = null;
               if(scName != null)
               {
                    sc = this.getSourceByName(scName);
               }
               
               Alignment a1 = this.uriAlignment.get(this.getValueByNode(jn, "align1"));
               Alignment a2 = this.uriAlignment.get(this.getValueByNode(jn, "align2"));
               Alignment a3 = this.uriAlignment.get(this.getValueByNode(jn, "align3"));
               if(a1 != null)
                    aligns.add(a1);
               if(a2 != null)
                    aligns.add(a2);
               if(a3 != null)
                    aligns.add(a3);
               
               ic.addElem(sa, a);
               ic.addElem(sb, b);
               if(c != null)
                    ic.addElem(sc, c);
               
               ic.addAlignments(aligns);
               
               ic.computeTrustScore(trustIcMax);
               
               icTreated.put(hashID, ic);
               this.instCandidates.add(ic);
               nbCandidate ++;
               
               if(mongoCollection != null)
               {
                   this.nbMongoSaved ++;
                   
                   try
                   {
                         WriteResult wr =collMongo.insert(ic.toDBObject());
                   }
                   catch(NullPointerException e)
                   {
                       System.err.println("ERROR Mongo Writer null ...");
                       System.err.println(e);
                       //System.exit(0);
                   }
               }
           }
        }
        
        /*for(InstanceCandidate ic : this.instCandidates)
        {
            ic.computeTrustScore();
            ret += ic.toString();
        }*/
        System.out.println("Nb candidate generated : "+nbCandidate);
    }
    
    public String allCandidatesToString()
    {
        this.instCandidates.sort(new InstanceCandidateComparator());
        String ret = "Instance Candidate (nb : "+this.instCandidates.size()+" : \n";
        for(InstanceCandidate ic : this.instCandidates)
        {
            ret += ic.toString();
        }
        
        return ret;
    }
    
    private String getOwlFileToTtl(String owlFile)
    {
        
//        String ret = "prefix : <http://www.w3.org/2002/07/owl#> \n" +
//"prefix owl: <http://www.w3.org/2002/07/owl#> \n" +
//"prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
//"prefix xml: <http://www.w3.org/XML/1998/namespace> \n" +
//"prefix xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
//"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ";
        
        /*String ret = "prefix : <http://www.w3.org/2002/07/owl#> \n" +
"prefix owl: <http://www.w3.org/2002/07/owl#> \n" +
"prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
"prefix xml: <http://www.w3.org/XML/1998/namespace> \n" +
"prefix xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
"prefix skos: <http://www.w3.org/2004/02/skos/core#> \n" +
"prefix swrl: <http://www.w3.org/2003/11/swrl#> \n" +
"prefix swrlb: <http://www.w3.org/2003/11/swrlb#> \n" +
"prefix terms: <http://purl.org/dc/terms/> \n" +
"prefix AgronomicTaxon: <http://ontology.irstea.fr/AgronomicTaxon#> \n" +
"base <http://ontology.irstea.fr/AgronomicTaxon> \n";*/
        
        //ret += "INSERT DATA {";
        String ret = "";
        try 
        {
            ret +=  IOUtils.toString( new FileInputStream(new File(owlFile)));
            //ret = ret.replaceAll("^@.+\\.$", "");   // remove Prefix (wrong syntax for SPARQL insert query)
        } 
        catch (IOException ex) 
        {
            System.err.println("Can't read provo file!");
            System.exit(0);
        }
        return ret;
    }
    
    private String setPrefix()
    {
        String ret = "";
        
        ret +="@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n";
        ret += "@prefix : <http://www.w3.org/ns/prov#> . \n";
        ret += "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . \n";
        ret += "@prefix owl: <http://www.w3.org/2002/07/owl#> . \n";
        ret += "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> . \n";
        
        
         ret += "@prefix : <http://www.w3.org/2002/07/owl#> . \n" +
                    "@prefix owl: <http://www.w3.org/2002/07/owl#> . \n" +
                    "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n" +
                    "@prefix xml: <http://www.w3.org/XML/1998/namespace> . \n" +
                    "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> . \n" +
                    "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . \n" +
                    "@prefix skos: <http://www.w3.org/2004/02/skos/core#> . \n" +
                    "@prefix swrl: <http://www.w3.org/2003/11/swrl#> . \n" +
                    "@prefix swrlb: <http://www.w3.org/2003/11/swrlb#> . \n" +
                    "@prefix terms: <http://purl.org/dc/terms/> . \n" +
                    "@prefix AgronomicTaxon: <http://ontology.irstea.fr/AgronomicTaxon#> . \n";
        return ret;
    }
    
    public String allCandidatesToProvo(String provoFile, String adomFile, String baseUri)
    {
        
        this.instCandidates.sort(new InstanceCandidateComparator());
        //String ret = "Instance Candidate (nb : "+this.instCandidates.size()+" : \n";
        String ret = this.setPrefix();
        ret += this.getOwlFileToTtl(provoFile);
        ret += this.getOwlFileToTtl(adomFile);
        int numInst = 1;
        for(InstanceCandidate ic : this.instCandidates)
        {
            ret += ic.toProvO(baseUri, numInst);
            numInst ++;
        }
        
        return ret;
    }
    
    public String allCandidatesToCSV()
    {
         this.instCandidates.sort(new InstanceCandidateComparator());
        //String ret = "Instance Candidate (nb : "+this.instCandidates.size()+" : \n";
         String ret = "";
         int nb = 0;
        for(InstanceCandidate ic : this.instCandidates)
        {
            nb++;
            ret += ic.getTrustScore()+" \n";
        }
        
        return ret;
    }
    
    public void computeRelationCandidate(String mongoCollectionICHR, String relImp, float trustRcMax)
    {
         DBCollection collMongo = null;
        if(mongoCollectionICHR != null)
        {
           collMongo = connectMongo(mongoCollectionICHR);
        }
        int nbicHR = 0;
        for(InstanceCandidate ic : this.instCandidates)
        {
            ArrayList<String> uriHigherRank = new ArrayList<>();
            boolean relValid = false;
            for(Entry<Source, String> e : ic.getElemCandidate().entrySet())
            {
                boolean founded = false;
                ArrayList<String> urisHR = e.getKey().getRelImportant(e.getValue(), relImp);
                if(urisHR.size() > 0)
                {
                    String uriHR = urisHR.get(0);
                    
                    for(InstanceCandidate icHR : this.getHyp(e.getKey(), uriHR))
                    {
                        relValid = true;
                        founded = true;
                        ArrayList<Source> sourcesHR = new ArrayList<>();
                        sourcesHR.add(e.getKey());
                        for(Source s : ic.getElemCandidate().keySet())
                        {
                            if(s != e.getKey() && relValid)
                            {
                                String uriIC = ic.getUriIC(s);
                                String uriICHR = icHR.getUriIC(s);
                                if(!s.isRelImportant(uriIC, relImp, uriICHR))
                                {
                                    relValid = false;
                                    founded = false;
                                }
                                else
                                {
                                    sourcesHR.add(s);
                                }
                            }
                        }
                        if(relValid)
                        {
                            RelationCandidate relCandidate = new RelationCandidate(relImp, ic, icHR, sourcesHR);
                            relCandidate.computeTrustScore(trustRcMax);
                            //ic.addIcHR(icHR, sourcesHR);
                            ic.addRelCandidate(relCandidate);
     
                            this.icHRs.put(ic, icHR);
                            nbicHR++;
                            
                             if(mongoCollectionICHR != null)
                            {
                                try
                                {
                                      WriteResult wr =collMongo.insert(relCandidate.toDBObject());
                                      this.nbMongoSaved ++;
                                }
                                catch(NullPointerException ex)
                                {
                                    System.err.println("ERROR Mongo Writer null ...");
                                    System.err.println(ex);
                                    //System.exit(0);
                                }
                            }
                            break;
                        }
                    }
                }
                if(founded)
                {
                    break;
                }
            }
        }
        System.out.println("NB ICHR : "+nbicHR);
    }
    
    public void computeTypeCandidate(String mongoCollectionType, float trustTcMax)
    {
        String relImp = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
         DBCollection collMongo = null;
        if(mongoCollectionType != null)
        {
            collMongo = connectMongo(mongoCollectionType);
        }
        int nbtc = 0;
        for(InstanceCandidate ic : this.instCandidates)
        {
            ArrayList<Source> uriType = null;
            String uriTypeCandidate = "";
            boolean relValid = false;
            for(Entry<Source, String> e : ic.getElemCandidate().entrySet())
            {
                boolean founded = false;
                ArrayList<String> urisHR = e.getKey().getRelImportant(e.getValue(), relImp);
                if(urisHR.size() > 0)
                {
                    uriTypeCandidate = urisHR.get(0);
                    if(uriTypeCandidate.compareTo("http://ontology.irstea.fr/AgronomicTaxon#Taxon") != 0)
                    {
                        uriType = new ArrayList<>();
                        uriType.add(e.getKey());
                        relValid = true;
                        //System.out.println("TEST : "+uriTypeCandidate);
                        for(Entry<Source, String> elem : ic.getElemCandidate().entrySet())
                        {
                            Source s = elem.getKey();
                            if(s != e.getKey() && relValid)
                            {
                                ArrayList<String> testUri = elem.getKey().getRelImportant(elem.getValue(), relImp);
                                String otherTypeUri = null;
                                if(testUri != null)
                                {
                                    otherTypeUri = testUri.get(0);
                                    //System.out.println("\t other : "+testUri.get(0));
                                }
                                if(otherTypeUri != null)
                                {
                                    if(otherTypeUri.compareTo(uriTypeCandidate)!= 0)
                                    {
                                        if(!(otherTypeUri.compareTo("http://ontology.irstea.fr/AgronomicTaxon#Taxon") == 0 && uriType.size() > 1))
                                        {
                                            relValid = false;
                                        }
                                    }
                                    else
                                    {
                                        uriType.add(s);
                                    }
                                }
                            }
                        }
                    }
                    if(relValid && uriType.size() > 1)
                    {
                        founded = true;
                        nbtc ++;
                        /*System.out.println("TYPE CANDIDATE FOUNDED !");
                        System.out.println(uriTypeCandidate+" --> ");
                        System.out.println(uriType);
                        System.out.println("------------");*/
                        TypeCandidate tc = new TypeCandidate(ic, uriTypeCandidate);
                        for(Source s : uriType)
                        {
                            tc.addElem(s, "");
                        }
                        tc.computeTrustScore(trustTcMax);
                        //ic.addTypeCandidate(uriTypeCandidate, uriType);
                        ic.addTypeCandidate(tc);

                         if(mongoCollectionType != null)
                        {
                            this.nbMongoSaved ++;
                           

                            try
                            {
                                  WriteResult wr =collMongo.insert(tc.toDBObject());
                            }
                            catch(NullPointerException ex)
                            {
                                System.err.println("ERROR Mongo Writer null ...");
                                System.err.println(ex);
                                //System.exit(0);
                            }
                        }
                        break;
                    }
                }
                if(founded)
                {
                    break;
                }
            }
        }
        System.out.println("NB tc : "+nbtc+" / "+this.instCandidates.size());
    }
    
     private void mergeLabelCandidate(InstanceCandidate ic, ArrayList<LabelCandidate> lcs, String l1, Source s1, String l2, Source s2, float simValue, String dataProp)
     {
        boolean founded = false;
        for(LabelCandidate lc : lcs)
        {
            if(lc.containsLabel(s1, l1) && !lc.hasLabelForsSource(s2))
            {
                lc.addElem(s2, l2);
                founded = true;
                lc.addValue(s2, simValue);
                break;
            }
            else if(lc.containsLabel(s2, l2) && !lc.hasLabelForsSource(s1))
            {
                lc.addElem(s1, l1);
                founded = true;
                lc.addValue(s1, simValue);
                break;
            }
        }
        if(!founded)
        {
            LabelCandidate newLC = new LabelCandidate(ic, dataProp);
            newLC.addElem(s1, l1);
            newLC.addElem(s2, l2);
            newLC.addValue(s1, simValue);
            lcs.add(newLC);
        }
     }
    
     private ArrayList<LabelCandidate> generateLabelCandidate(String dataProp, InstanceCandidate ic)
     {
        HashMap<Source, ArrayList<String>> sourcesLabel = new HashMap<>();
        ArrayList<Source> sourcesInv= new ArrayList<>();
        for(Entry<Source, String> e : ic.getElemCandidate().entrySet())
        {
            ArrayList<String> labels = e.getKey().getRelImportant(e.getValue(), dataProp);
            if(labels != null)
            {
                sourcesLabel.put(e.getKey(), labels);
                sourcesInv.add(e.getKey());
            }
        }
        ArrayList<LabelCandidate> lcs = new ArrayList<>();
        if(sourcesLabel.size() > 1)
        {
            for(int i = 0; i<sourcesLabel.size(); i++)
            {
                Source s1 = sourcesInv.get(i);
                ArrayList<String> l1 = sourcesLabel.get(s1);
                for(int j = i+1; j<sourcesLabel.size(); j++)
                {
                   Source s2 = sourcesInv.get(j);
                   ArrayList<String> l2 = sourcesLabel.get(s2);
                   for(String labelS1 : l1)
                   {
                       for(String labelS2 : l2)
                       {
                           float simValue = (float)StringDistance.JaroWinkler(labelS1, labelS2);
                           if(simValue > 0.92)
                           {
                               this.mergeLabelCandidate(ic, lcs, labelS1, s1, labelS2, s2, simValue, dataProp);
                           }
                       }
                   }
                }
            }
        }
        return lcs;
     }
    
    public void computeLabelCandidate(String mongoCollectionLabels, ArrayList<String> urisLabels, float trustLcMax)
    {
         DBCollection collMongo = null;
        if(mongoCollectionLabels != null)
        {
            this.connectMongo(mongoCollectionLabels);
        }
        for(InstanceCandidate ic : this.instCandidates)
        {
            for(String dataProp : urisLabels)
            {
                ArrayList<LabelCandidate> lcs = this.generateLabelCandidate(dataProp, ic);
                if(lcs != null && lcs.size() > 0)
                {
                    //ic.addLabelCandidates(dataProp, lcs);
                    ic.addAllLabelsCandidate(lcs);
                }
            }
            if(mongoCollectionLabels != null)
            {
                 for(LabelCandidate lc : ic.getLabelCandidates())
                 {
                    String typeURI = lc.getDataProp();
                    lc.computeTrustScore(trustLcMax);

                    try
                    {
                          WriteResult wr =collMongo.insert(lc.toDBObject());
                    }
                    catch(NullPointerException ex)
                    {
                        System.err.println("ERROR Mongo Writer null ...");
                        System.err.println(ex);
                        //System.exit(0);
                    }
                }
            }
        }
    }
    
    public void addAlignmentCandidateSP(Alignment a, Source s1, Source s2)
    {
        StringBuilder query = new StringBuilder("PREFIX : <http://www.amarger.murloc.fr/AlignmentOntology#> INSERT DATA {");
        query.append("<"+a.getUri()+"> rdf:type :Element; :belongsTo <"+s1.getBaseUri()+">.");
        query.append("<"+a.getUriAlign()+"> rdf:type :Element; :belongsTo <"+s2.getBaseUri()+">.");
        query.append(":Alignment_"+this.idAlign+" rdf:type :Alignment; :alignBetween <"+a.getUri()+">; :alignBetween <"+a.getUriAlign()+">; :trustScore \""+a.getValue()+"\"^^xsd:float . }");
        //System.out.println(query);
        this.spAlignTemp.storeData(query);
        this.uriAlignment.put("http://www.amarger.murloc.fr/AlignmentOntology#Alignment_"+this.idAlign, a);
        this.idAlign ++;
    }
    
    public void initSources(ArrayList<Source> sources)
    {
        this.spAlignTemp.clearSp();
        StringBuilder query = new StringBuilder("PREFIX : <http://www.amarger.murloc.fr/AlignmentOntology#> \n INSERT DATA {");
        try
        {
            query.append(FileUtils.readFileToString(FileUtils.getFile("in/alignment_ontology.owl")));
            //System.out.println(query);
            
            for(Source s : sources)
            {
                query.append(" <"+s.getBaseUri()+"> rdf:type :Source; :hasName \""+s.getName()+"\". ");
            }
            
            query.append("}");
            this.spAlignTemp.storeData(query);
        } 
        catch (IOException ex)
        {
            System.err.println("Error during reading the alignment_ontology.owl file...");
            System.exit(0);
        }
        this.sources = sources;
    }
    
    public float getIcTrustMax()
    {
        int nbSources = sources.size();
        int maxPaire = (nbSources*(nbSources-1))/2;
        float trustMax = maxPaire;
        for(Source s : sources)
        {
            //trustMax += s.getSourceQualityScore();
            //trustMax++;
        }
        return trustMax;
    }
    
    
    public float getSumSQ()
    {
        float ret = 0;
        for(Source s : this.sources)
        {
            //ret += s.getSourceQualityScore();
            ret ++;
        }
        return ret;
    }
    
    public float getRcTrustMax()
    {
        return (float) (1+this.getSumSQ());
    }
    
    public float getTcTrustMax()
    {
        return (float) (1+this.getSumSQ());
    }
    
    public float getLcTrustMax()
    {
        return (float) (2+this.getSumSQ());
    }
}
