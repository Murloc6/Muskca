/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MultiSources;

import Alignment.Alignment;
import Alignment.StringDistance;
import Candidate.CandidateComparator;
import Candidate.NodeCandidate.ClassCandidate;
import Candidate.NodeCandidate.IndividualCandidate;
import Candidate.ArcCandidate.LabelCandidate;
import Candidate.ArcCandidate.RelationCandidate;
import Candidate.ArcCandidate.TypeCandidate;
import Candidate.NodeCandidate.NodeCandidate;
import MultiSources.Solver.CandidatSolver;
import MultiSources.Solver.ExtensionSolver;
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
import muskca.Muskca;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author murloc
 */
public class Fusionner implements Serializable
{
   
    
    private ArrayList<IndividualCandidate>instCandidates;
    private ArrayList<ClassCandidate>classCandidates;
    
   /* private HashMap<String, Alignment> uriAlignment;
    private HashMap<String, Alignment> uriClassAlignment;*/
    
    private ArrayList<Source> sources;
    
    
    private float trustIcMax;
    private float trustRcMax;
    private float trustTcMax;
    private float trustLcMax;
    private float trustCcMax;
    
    private String mongodb;
   
    private int idAlign = 0;
    private int idClassAlign = 0;
    
    public int nbMongoSaved = 0;
    
    
    public Fusionner(String mongodb)
    {
        //this.uriAlignment = new HashMap<>();
        //this.uriClassAlignment = new HashMap<>();
        
        this.instCandidates = new ArrayList<>();
        this.classCandidates = new ArrayList<>();
        
        this.mongodb = mongodb;
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
                if(s.getName().compareToIgnoreCase(sourceName) == 0)
                {
                    ret = s;
                }
                i++;
            }
        }
        return ret;
    }
    
    private DBCollection connectMongo(String mongoCollection)
    {
        DBCollection collMongo = null;
        try
        {
            MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
            DB db = mongoClient.getDB(this.mongodb);
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
    
    
    public StringBuilder getPrologCandidatesDef(ArrayList<NodeCandidate> cands)
    {
        StringBuilder candidates = new StringBuilder();
        for(NodeCandidate cand : cands)
        {
            //System.out.println("candidat("+s+").");
            candidates = candidates.append("candidat(");
            candidates.append(cand.toPrologData());
            candidates.append(").\n");
        }
        return candidates;
    }
    
    public ArrayList<ClassCandidate> computeClassCandidate(String mongoCollection, StringBuilder data)
    {
        ArrayList<ClassCandidate> ret;
        
        
        DBCollection collMongo = null;
        if(mongoCollection != null)
        {
            collMongo = connectMongo(mongoCollection);
        }
        
        CandidatSolver cs = new CandidatSolver(data);
        ret = cs.getAllClassCandidates(this);
        int nbCand = ret.size();
        System.out.println("ClassCandidates generated : "+nbCand);
        this.classCandidates = ret;
        return ret;
    }
    
     public ArrayList<IndividualCandidate> computeIndCandidate(String mongoCollection, StringBuilder data, int nbSources)
    {
        ArrayList<IndividualCandidate> ret;
        
        
        DBCollection collMongo = null;
        if(mongoCollection != null)
        {
            collMongo = connectMongo(mongoCollection);
        }
        
        CandidatSolver cs = new CandidatSolver(data);
        ret = cs.getAllIndCandidates(this, nbSources);
        int nbCand = ret.size();
        System.out.println("IndividualCandidates generated : "+nbCand);
        this.instCandidates = ret;
        return ret;
    }
    
     
    public NodeCandidate getCandidateFromUris(ArrayList<String> uris)
    {
        NodeCandidate ret = null;
        boolean founded = false;
        for(IndividualCandidate ic : this.instCandidates)
        {
            if(ic.isSameCand(uris))
            {
                ret = ic;
                founded = true;
                break;
            }
        }
        if(!founded)
        {
            for(ClassCandidate cc : this.classCandidates)
            {
                if(cc.isSameCand(uris))
                {
                    ret = cc;
                    founded = true;
                    break;
                }
            }
        }
        
        return ret;
    }
     
    public StringBuilder allExtensionsToProlog(ArrayList<Extension> exts)
    {
        StringBuilder ret = new StringBuilder();
        for(Extension ext : exts)
        {
            ret = ret.append("extension(");
            ret = ret.append(ext.toPrologData());
            ret = ret.append(").\n");
        }
        return ret;
    }
     
    public ArrayList<Extension> computeExtensions(StringBuilder data, int nbCand)
    {
        ArrayList<Extension> ret;
        ArrayList<ArrayList<String>> curSols = new ArrayList<>();
        
        ExtensionSolver es = new ExtensionSolver(data);
        
        int nbMax = es.getMaxExtSize(nbCand);
        System.out.println("Nb max cand for extensions : "+nbMax);
        
        ret = es.getAllExtensions(nbMax, this);
        
        
        
        return ret;
    }
    
    
    public boolean isClassCandidate(String classUri, Source s)
    {
        boolean ret = false;
        for(ClassCandidate cc : this.classCandidates)
        {
            if(cc.hasElem(s, classUri))
            {
                ret = true;
            }
        }
        
        return ret;
    }
    
    public StringBuilder allCandidatesToPrologData()
    {
        StringBuilder ret = new StringBuilder();
        
        for(IndividualCandidate ic : this.instCandidates)
        {
            ret = ret.append("candidat(").append(ic.toPrologData()).append("). \n");
        }
        for(ClassCandidate cc : this.classCandidates)
        {
            ret = ret.append("candidat(").append(cc.toPrologData()).append("). \n");
        }
        
        return ret;
    }
    
    public String allCandidatesToString()
    {
        this.instCandidates.sort(new CandidateComparator());
        String ret = "Instance Candidate (nb : "+this.instCandidates.size()+" : \n";
        for(IndividualCandidate ic : this.instCandidates)
        {
            ret += ic.toString();
        }
        
        return ret;
    }
    
    public String allClassCandidatesToString()
    {
        this.classCandidates.sort(new CandidateComparator());
        String ret = "Class Candidate (nb : "+this.classCandidates.size()+") : \n";
        for(ClassCandidate cc : this.classCandidates)
        {
            ret += cc.toString();
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
        
        ret +="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n";
        ret += "PREFIX : <http://www.w3.org/ns/prov#>  \n";
        ret += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  \n";
        ret += "PREFIX owl: <http://www.w3.org/2002/07/owl#>  \n";
        ret += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  \n";
        
        
         ret += "PREFIX owl: <http://www.w3.org/2002/07/owl#>  \n" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n" +
                    "PREFIX xml: <http://www.w3.org/XML/1998/namespace>  \n" +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  \n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  \n" +
                    "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  \n" +
                    "PREFIX swrl: <http://www.w3.org/2003/11/swrl#>  \n" +
                    "PREFIX swrlb: <http://www.w3.org/2003/11/swrlb#>  \n" +
                    "PREFIX terms: <http://purl.org/dc/terms/>  \n" +
                    "PREFIX AgronomicTaxon: <http://ontology.irstea.fr/AgronomicTaxon#>  \n";
        return ret;
    }
    
    public SparqlProxy allCandidatesToProvo(String provoFile, String provoSpOut, String adomFile, String baseUri)
    {
        SparqlProxy spOutProvo = SparqlProxy.getSparqlProxy(provoSpOut);
        spOutProvo.clearSp();
        
       //this.instCandidates.sort(new InstanceCandidateComparator());
        spOutProvo.storeData(new StringBuilder(this.setPrefix()+"  INSERT DATA {"+this.getOwlFileToTtl(provoFile)+"}"));
        spOutProvo.storeData(new StringBuilder(this.setPrefix()+" INSERT DATA {"+this.getOwlFileToTtl(adomFile)+"}"));
        int numInst = 1;
        
        String query = this.setPrefix()+" INSERT DATA {";
        
        String uriKbMerge = baseUri+"ActivityKBMerge";
        
        query += "<"+baseUri+"Muskca/"+Muskca.muskcaVersion+"> rdf:type :SoftwareAgent.";
        query += "<"+uriKbMerge+"> rdf:type :Activity; :startedAtTime \""+Muskca.dateBegin+"\"^^xsd:dateTime; :endedAtTime \""+Muskca.dateEnd+"\"^^xsd:dateTime; :wasAssociatedWith <"+baseUri+"Muskca/"+Muskca.muskcaVersion+">.";
        query += "<"+baseUri+"weightedKB> rdf:type :Collection.";
        
        HashMap<Source, String> provoSourceUri = new HashMap<>();
        for (Source s : this.sources)
        {
            String sUri = baseUri+"Source/"+s.getName();
            provoSourceUri.put(s, sUri);
            query += "<"+sUri+"> rdf:type :Collection.";
            query += "<"+uriKbMerge+"> :used <"+sUri+">.";
            query += "<"+baseUri+"weightedKB> :wasDerivedFrom <"+sUri+">.";
        }
        query +="}";
        
        spOutProvo.storeData(new StringBuilder(query));
        
        for(IndividualCandidate ic : this.instCandidates)
        {
            spOutProvo.storeData(new StringBuilder(this.setPrefix()+" INSERT DATA {"+ic.toProvO(baseUri, numInst, provoSourceUri, uriKbMerge)+"}"));
            numInst ++;
        }
        
        return spOutProvo;
    }
    
    public String allCandidatesToCSV()
    {
         this.instCandidates.sort(new CandidateComparator());
        //String ret = "Instance Candidate (nb : "+this.instCandidates.size()+" : \n";
         String ret = "";
         int nb = 0;
        for(IndividualCandidate ic : this.instCandidates)
        {
            nb++;
            ret += ic.getTrustScore()+" \n";
        }
        
        return ret;
    }
    
    
    public void computeRelationCandidate(String mongoCollectionICHR, String relImp)
    {
         DBCollection collMongo = null;
        if(mongoCollectionICHR != null)
        {
           collMongo = connectMongo(mongoCollectionICHR);
        }
        int nbicHR = 0;
        for(IndividualCandidate ic : this.instCandidates)
        {
            ArrayList<String> uriHigherRank = new ArrayList<>();
            boolean relValid = false;
            for(Entry<Source, String> e : ic.getUriImplicate().entrySet())
            {
                boolean founded = false;
                ArrayList<String> urisHR = e.getKey().getRelImportant(e.getValue(), relImp);
                if(urisHR.size() > 0)
                {
                    String uriHR = urisHR.get(0);
                    
                    for(IndividualCandidate icHR : this.getIndCandidateFromUriImplicate(uriHR, e.getKey()))
                    {
                        relValid = true;
                        founded = true;
                        ArrayList<Source> sourcesHR = new ArrayList<>();
                        sourcesHR.add(e.getKey());
                        for(Source s : ic.getUriImplicate().keySet())
                        {
                            if(s != e.getKey() && relValid)
                            {
                                String uriIC = ic.getUriFromSource(s);
                                String uriICHR = icHR.getUriFromSource(s);
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
                            relCandidate.computeTrustScore(this.trustRcMax);
                            //ic.addIcHR(icHR, sourcesHR);
                            ic.addRelCandidate(relCandidate);
     
                            //this.icHRs.put(ic, icHR);
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
    
    private ArrayList<ClassCandidate> getClassCandidateFromUriImplicate(String uriType, Source s)
    {
        ArrayList<ClassCandidate> ret = new ArrayList<>();
        for(ClassCandidate cc : this.classCandidates)
        {
            if(cc.hasElem(s, uriType))
            {
                ret.add(cc);
            }
        }
        return ret;
    }
    
    private ArrayList<IndividualCandidate> getIndCandidateFromUriImplicate(String uriType, Source s)
    {
        ArrayList<IndividualCandidate> ret = new ArrayList<>();
        for(IndividualCandidate ic : this.instCandidates)
        {
            if(ic.hasElem(s, uriType))
            {
                ret.add(ic);
            }
        }
        return ret;
    }
    
    public void computeTypeCandidate(String mongoCollectionType)
    {
        String relImp = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
         DBCollection collMongo = null;
        if(mongoCollectionType != null)
        {
            collMongo = connectMongo(mongoCollectionType);
        }
        int nbtc = 0;
        for(IndividualCandidate ic : this.instCandidates)
        {
            HashMap<Source, String> uriType = null;
            String uriTypeCandidate = "";
            boolean relValid = false;
            for(Entry<Source, String> e : ic.getUriImplicate().entrySet())
            {
                boolean founded = false;
                ArrayList<String> urisType = e.getKey().getRelImportant(e.getValue(), relImp);
                if(urisType.size() > 0)
                {
                    uriTypeCandidate = urisType.get(0);
                    ClassCandidate cc = null;
                    if(uriTypeCandidate.compareTo("http://ontology.irstea.fr/AgronomicTaxon#Taxon") != 0)
                    {
                        if(uriTypeCandidate.startsWith("http://ontology.irstea.fr/AgronomicTaxon"))
                        {
                            uriType = new HashMap<>();
                            uriType.put(e.getKey(), "");
                            relValid = true;
                            //System.out.println("TEST : "+uriTypeCandidate);
                            for(Entry<Source, String> elem : ic.getUriImplicate().entrySet())
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
                                            uriType.put(s, "");
                                        }
                                    }
                                }
                            }
                        }
                        else
                        {
                           /* cc = this.getClassCandidateFromUriImplicate(e.getKey(), uriTypeCandidate);
                            if(cc != null)
                            {
                                uriType = new HashMap<>();
                                uriType.put(e.getKey(), uriTypeCandidate);
                                relValid = true;

                                 for(Entry<Source, String> elem : ic.getUriImplicate().entrySet())
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
                                            if(!cc.hasElem(s, otherTypeUri))
                                            {
                                                relValid = false;
                                            }cc
                                            else
                                            {
                                                uriType.put(s, otherTypeUri);
                                            }
                                        }
                                    }
                                }
                            }*/
                            /**
                             * TODO : Changer le traitement des ClassCandidate ici pour récupérer la liste des classes candidates générées
                             */
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
                        TypeCandidate tc = null;
                        if(cc != null)
                        {
                            tc = new TypeCandidate(ic, cc);
                        }
                        else
                        {
                            tc = new TypeCandidate(ic, uriTypeCandidate);
                        }
                         for(Entry<Source, String> eUriType : uriType.entrySet())
                        {
                            tc.addElem(eUriType.getKey(), eUriType.getValue());
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
    
     private void mergeLabelCandidate(IndividualCandidate ic, ArrayList<LabelCandidate> lcs, String l1, Source s1, String l2, Source s2, float simValue, String dataProp)
     {
        boolean founded = false;
        for(LabelCandidate lc : lcs)
        {
            if(lc.hasElem(s1, l1) && (lc.getUriFromSource(s2) == null))
            {
                lc.addElem(s2, l2);
                founded = true;
                lc.addValue(s2, simValue);
                break;
            }
            else if(lc.hasElem(s2, l2) && (lc.getUriFromSource(s1) == null))
            {
                lc.addElem(s1, l1);
                founded = true;
                lc.addValue(s1, simValue);
                break;
            }
        }
        if(!founded)
        {
            LabelCandidate newLC = new LabelCandidate(ic, dataProp, this.getSumSQ());
            newLC.addElem(s1, l1);
            newLC.addElem(s2, l2);
            newLC.addValue(s1, simValue);
            lcs.add(newLC);
        }
     }
    
     private ArrayList<LabelCandidate> generateLabelCandidate(String dataProp, IndividualCandidate ic)
     {
        HashMap<Source, ArrayList<String>> sourcesLabel = new HashMap<>();
        ArrayList<Source> sourcesInv= new ArrayList<>();
        for(Entry<Source, String> e : ic.getUriImplicate().entrySet())
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
    
    public void computeLabelCandidate(String mongoCollectionLabels, ArrayList<String> urisLabels)
    {
         DBCollection collMongo = null;
        if(mongoCollectionLabels != null)
        {
            collMongo = connectMongo(mongoCollectionLabels);
        }
        for(IndividualCandidate ic : this.instCandidates)
        {
            for(String dataProp : urisLabels)
            {
                ArrayList<LabelCandidate> lcs = this.generateLabelCandidate(dataProp, ic);
                if(lcs != null && lcs.size() > 0)
                {
                    //ic.addLabelCandidates(dataProp, lcs);
                    ic.addAllLabelsCandidate(lcs, trustLcMax, this.getSumSQ());
                }
            }
            ic.clearLabelsCandidates();
            if(mongoCollectionLabels != null)
            {
                ArrayList<String> lcTreated = new ArrayList<>();
                 for(LabelCandidate lc : ic.getLabelCandidates())
                 {
                    try
                    {
                         WriteResult wr =collMongo.insert(lc.toDBObject());
                    }
                    catch(NullPointerException ex)
                    {
                        System.err.println("ERROR Mongo Writer null (LC) ...");
                        System.exit(0);
                    }
                }
            }
        }
    }
    
    /*public void addAlignmentCandidateSP(Alignment a, Source s1, Source s2)
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
    
    public void addAlignmentClassCandidateSP(Alignment a, Source s1, Source s2)
    {
        StringBuilder query = new StringBuilder("PREFIX : <http://www.amarger.murloc.fr/AlignmentOntology#> INSERT DATA {");
        query.append("<"+a.getUri()+"> rdf:type :Element; :belongsTo <"+s1.getBaseUri()+">.");
        query.append("<"+a.getUriAlign()+"> rdf:type :Element; :belongsTo <"+s2.getBaseUri()+">.");
        query.append(":Alignment_"+this.idClassAlign+" rdf:type :Alignment; :alignBetween <"+a.getUri()+">; :alignBetween <"+a.getUriAlign()+">; :trustScore \""+a.getValue()+"\"^^xsd:float . }");
        //System.out.println(query);
        this.spClassAlignTemp.storeData(query);
        this.uriClassAlignment.put("http://www.amarger.murloc.fr/AlignmentOntology#Alignment_"+this.idClassAlign, a);
        this.idClassAlign ++;
    }*/
    
    /*public void initSources(ArrayList<Source> sources)
    {
        this.spAlignTemp.clearSp();
        this.spClassAlignTemp.clearSp();
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
            this.spClassAlignTemp.storeData(query);
        } 
        catch (IOException ex)
        {
            System.err.println("Error during reading the alignment_ontology.owl file...");
            System.exit(0);
        }
        this.sources = sources;
    }*/
    
    public String initPrologSources(ArrayList<Source> sources)
    {
        String sData = "sources([";
        for(Source s : sources)
        {
            sData += s.getName().toLowerCase()+", ";
        }
        sData = sData.substring(0, sData.lastIndexOf(","));
        sData += "]). \n";
        this.sources = sources;
        
        
        this.trustIcMax = this.getIcTrustMax();
        this.trustRcMax = this.getRcTrustMax();
        this.trustTcMax = this.getTcTrustMax();
        this.trustLcMax = this.getLcTrustMax();
        this.trustCcMax = this.getCcTrustMax();
        
        return sData;
    }
    
    public float getIcTrustMax()
    {
        int nbSources = sources.size();
        int maxPaire = (nbSources*(nbSources-1))/2;
        return maxPaire;
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
        return (float) 2;
    }
    
    public float getCcTrustMax()
    {
         int nbSources = sources.size();
        return  (nbSources*(nbSources-1))/2;
    }
}
