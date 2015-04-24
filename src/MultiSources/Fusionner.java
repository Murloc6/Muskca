/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MultiSources;

import Alignment.StringDistance;
import Candidate.CandidateComparator;
import Candidate.NodeCandidate.ClassCandidate;
import Candidate.NodeCandidate.IndividualCandidate;
import Candidate.ArcCandidate.LabelCandidate;
import Candidate.ArcCandidate.RelationCandidate;
import Candidate.ArcCandidate.TypeCandidate;
import Candidate.NodeCandidate.NodeCandidate;
import MultiSources.Solver.GLPK.ExtensionGlpkSolver;
import MultiSources.Solver.Prolog.CandidatSolver;
import MultiSources.Solver.Prolog.ExtensionSolver;
import Source.Source;
import Source.SparqlProxy;
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
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;
import muskca.FilePerso;
import muskca.Muskca;
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
    
//    private String mongodb;
//    private String mongoServer;
//    private int mongoPort;
//    private HashMap<String, String> mongoDbs;
    
    private String dataPrologSources;
    private StringBuilder dataPrologClass;
    private StringBuilder dataPrologInd;
    
    private ArrayList<String> urisLabelsImp;
    private ArrayList<String> urisRelImp;
    private String uriTypeBase;
    private ArrayList<String> urisTypeImp;
    
    private int idAlign = 0;
    private int idClassAlign = 0;
    
    public int nbMongoSaved = 0;
    
    
    public Fusionner(ArrayList<String> urisLabelsImp, ArrayList<String> urisRelImp, String uriTypeBase, ArrayList<String> urisTypeImp)
    {
        //this.uriAlignment = new HashMap<>();
        //this.uriClassAlignment = new HashMap<>();
        
        this.instCandidates = new ArrayList<>();
        this.classCandidates = new ArrayList<>();
        
//        this.mongoDbs = mongoDbs;
//        this.mongodb = this.mongoDbs.get("mongodb");
//        this.mongoServer = this.mongoDbs.get("mongoServer");
//        this.mongoPort = Integer.parseInt(this.mongoDbs.get("mongoPort"));
        
        this.urisLabelsImp = urisLabelsImp;
        this.urisRelImp = urisRelImp;
        this.uriTypeBase = uriTypeBase;
        this.urisTypeImp = urisTypeImp;
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
    
    public ArrayList<Source> getSources()
    {
        return this.sources;
    }
    
    public ArrayList<String> getImpRels()
    {
        return this.urisRelImp;
    }
    
//    public String getMongoDb(String db)
//    {
//        return this.mongoDbs.get(db);
//    }
    
//    private DBCollection connectMongo(String mongoCollection)
//    {
//        DBCollection collMongo = null;
//        try
//        {
//            MongoClient mongoClient = new MongoClient( this.mongoServer , this.mongoPort);
//            DB db = mongoClient.getDB(this.mongodb);
//           collMongo = db.getCollection(mongoCollection);
//           collMongo.remove(new BasicDBObject());
//        } 
//        catch (UnknownHostException ex)
//        {
//            System.err.println("ERROR during connection to mongoDB ...");
//            System.exit(0);
//        }
//        return collMongo;
//    }
    
    public void setDataPrologClass(StringBuilder dataPrologClass)
    {
        this.dataPrologClass = dataPrologClass;
    }
    
    public void setDataPrologInd(StringBuilder dataPrologInd)
    {
        this.dataPrologInd = dataPrologInd;
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
    
    public ArrayList<ClassCandidate> computeClassCandidate(String mongoDb)
    {
        ArrayList<ClassCandidate> ret;
        //String mongoCollection = this.getMongoDb(mongoDb);
        
        int nbSources = this.sources.size();
        
//        DBCollection collMongo = null;
//        if(mongoCollection != null)
//        {
//            collMongo = connectMongo(mongoCollection);
//        }
        
        CandidatSolver cs = new CandidatSolver(this.dataPrologClass);
        ret = cs.getAllClassCandidates(this, nbSources);
        
         ArrayList<ClassCandidate> aloneCands = new ArrayList<>();
        
        System.out.println("Start considering the alone class candidates (so sad!)");
        for(Source s : this.sources)
        {
            ArrayList<String> uriAlones = s.getAllClassUris(this.uriTypeBase);
            for(String uriAlone : uriAlones)
            {
                boolean isAlreadyImplied = false;
                for(ClassCandidate cc : ret)
                {
                    if(cc.hasElem(s, uriAlone))
                    {
                       isAlreadyImplied = true;
                       break;
                    }
                }
                if(!isAlreadyImplied)
                {
                    ClassCandidate aloneCc = new ClassCandidate();
                    aloneCc.addElem(s, uriAlone);
                    aloneCands.add(aloneCc);
                }
            }
        }
        ret.addAll(aloneCands);
        
        int nbCand = ret.size();
        System.out.println("ClassCandidates generated : "+nbCand+" ("+aloneCands.size()+" alone cands)");
        this.classCandidates = ret;
        return ret;
    }
    
     public ArrayList<IndividualCandidate> computeIndCandidate(String mongoDb)
    {
        ArrayList<IndividualCandidate> ret;
        //String mongoCollection = this.mongoDbs.get(mongoDb);
        int nbSources = this.sources.size();
        
//        DBCollection collMongo = null;
//        if(mongoCollection != null)
//        {
//            collMongo = connectMongo(mongoCollection);
//        }
        
        CandidatSolver cs = new CandidatSolver(this.dataPrologInd);
        ret = cs.getAllIndCandidates(this, nbSources);
        
        ArrayList<IndividualCandidate> aloneCands = new ArrayList<>();
        
        System.out.println("Start considering the alone individuals candidates (so sad!)");
        for(Source s : this.sources)
        {
            for(String uriClassInd : this.urisTypeImp)
            {
                ArrayList<String> uriAlones = s.getAllIndividualUris(uriClassInd);
                for(String uriAlone : uriAlones)
                {
                    boolean isAlreadyImplied = false;
                    for(IndividualCandidate ic : ret)
                    {
                        if(ic.hasElem(s, uriAlone))
                        {
                           isAlreadyImplied = true;
                           break;
                        }
                    }
                    if(!isAlreadyImplied)
                    {
                        IndividualCandidate aloneIc = new IndividualCandidate();
                        aloneIc.addElem(s, uriAlone);
                        aloneCands.add(aloneIc);
                    }
                }
            }
        }
        ret.addAll(aloneCands);
        int nbCand = ret.size();
        System.out.println("IndividualCandidates generated : "+nbCand+" ("+aloneCands.size()+" alone cands)");
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
    
    
    public ExtensionGlpkSolver initSolver(ArrayList<NodeCandidate> allCands)
    {
        ExtensionGlpkSolver solver = new ExtensionGlpkSolver();
        solver.initProblem(allCands, this);
        return solver;
    }
    
    public Extension getNextExtension(ExtensionGlpkSolver solver)
    {
        Extension ext = null;
        solver.updateDataFile();
        if(solver.solve())
        {
            ext = solver.getSolution();
            solver.freeMemory();
            int i = 0;
            System.out.println("New extension to validate ("+ext.getCandidates().size()+" candidates)");
            //System.out.println(ext);
        }
        
        return ext;
    }
    
    
    public void computeTrustScore()
    {
        int nbSources =  this.sources.size();
        for(NodeCandidate nc : this.getAllNodeCandidates())
        {
            nc.computeTrustScore(nbSources);
        }
    }
    
    public float getSumArcTrust(NodeCandidate nc1, NodeCandidate nc2)
    {
        float ret = 0;
        
        
        return ret;
    }
    
    /*public Extension getValidatedExtension(ArrayList<NodeCandidate> allCands)
    {
        
        
        
        
        boolean validatedExtensionFounded = false;
        while(!validatedExtensionFounded)
        {
            
            
                
            }
            else
            {
                System.err.println("Error : no extension founded...");
                ext = null;
                validatedExtensionFounded = true;
            }
        }
        
        
        if(validatedExtensionFounded && ext != null)
        {
            System.out.println("Best extension founded! ");
            System.out.println(ext);
        }
        else
        {
            System.out.println("None extension exists with these constraints...");
        }
        
        
        return ext;
    }*/
    
    
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
            ret +=  IOUtils.toString( new FileInputStream(new FilePerso(owlFile)));
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
    
    public ArrayList<NodeCandidate> getAllNodeCandidates()
    {
        ArrayList<NodeCandidate> ret = new ArrayList<>();
        ret.addAll(this.instCandidates);
        ret.addAll(this.classCandidates);
        return ret;
    }
    
    public void computeRelationCandidate(String mongoCollectionICHR, String relImp)
    {
//         DBCollection collMongo = null;
//        if(mongoCollectionICHR != null)
//        {
//           collMongo = connectMongo(mongoCollectionICHR);
//        }
        int nbicHR = 0;
        for(IndividualCandidate ic : this.instCandidates) // for each node candidate nc
        {
            ArrayList<String> uriHigherRank = new ArrayList<>();
            boolean relValid = false;
            for(Entry<Source, String> e : ic.getUriImplicate().entrySet()) // for each elem in nc
            {
                boolean founded = false;
                ArrayList<String> urisHR = e.getKey().getRelImportant(e.getValue(), relImp); // get all the uris from <nc> <relIm> <uriHR>
                if(urisHR.size() > 0) // if uriHR exists (at least one)
                {
                    String uriHR = urisHR.get(0); //consider all objectproperties functionnal (can't be 1:n)
                    
                    for(NodeCandidate ncHR : this.getNodeCandidateFromUriImplicate(uriHR, e.getKey())) // get all node candidate wich uriHR is implicated
                    {
                        founded = false;
                        ArrayList<Source> sourcesHR = new ArrayList<>();
                        sourcesHR.add(e.getKey());
                        for(Source s : ic.getUriImplicate().keySet()) // for all sources in nc
                        {
                            if(s != e.getKey()) // if source is different from the initial founded ncHR source
                            {
                                String uriIC = ic.getUriFromSource(s);
                                String uriICHR = ncHR.getUriFromSource(s);
                                if(s.isRelImportant(uriIC, relImp, uriICHR)) // if <nc> <relImp> <uriICHR> exists (rel exsits in other source)
                                {
                                    sourcesHR.add(s); // add the source on the implied sources
                                    founded = true;
                                }
                            }
                        }
                        //Create a new relation candidate for each nodecandidate whith the associated sourcesHR
                        //if( sourcesHR.size() > 1)
                        //{
                            RelationCandidate relCandidate = new RelationCandidate(ic,relImp, ncHR, sourcesHR);
                            //relCandidate.computeTrustScore(this.trustRcMax);
                            //ic.addIcHR(icHR, sourcesHR);
                            if(ic.addRelationCandidate(relCandidate))
                            {
                                nbicHR++;
                            }
                            //this.icHRs.put(ic, icHR);

//                             if(mongoCollectionICHR != null)
//                            {
//                                try
//                                {
//                                      WriteResult wr =collMongo.insert(relCandidate.toDBObject());
//                                      this.nbMongoSaved ++;
//                                }
//                                catch(NullPointerException ex)
//                                {
//                                    System.err.println("ERROR Mongo Writer null ...");
//                                    System.err.println(ex);
//                                    //System.exit(0);
//                                }
//                            }
                        //}
                    }
                }
                if(founded) // stop considering other implied elements of the initial candidate if relation has been found
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
    
    private ArrayList<NodeCandidate> getNodeCandidateFromUriImplicate(String uriType, Source s)
    {
        ArrayList<NodeCandidate> ret = new ArrayList<>();
        for(NodeCandidate nc : this.getAllNodeCandidates())
        {
            if(nc.hasElem(s, uriType))
            {
                ret.add(nc);
            }
        }
        return ret;
    }
    
    public void computeTypeCandidate(String mongoCollectionType)
    {
        String relImp = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
//         DBCollection collMongo = null;
//        if(mongoCollectionType != null)
//        {
//            collMongo = connectMongo(mongoCollectionType);
//        }
        int nbtc = 0;
        for(IndividualCandidate ic : this.instCandidates)
        {
            boolean relValid = false;
            for(Entry<Source, String> e : ic.getUriImplicate().entrySet())
            {
                boolean founded = false;
                ArrayList<String> urisType = e.getKey().getRelImportant(e.getValue(), relImp);
                for(String uriTypeCandidate : urisType)
                {
                    //System.out.println(uriTypeCandidate);
                    if(uriTypeCandidate.startsWith(this.uriTypeBase))
                    {
                        HashMap<Source, String> uriType = new HashMap<>();
                        uriType.put(e.getKey(), e.getValue()+" "+relImp+" "+uriTypeCandidate);
                        relValid = true;
                        //System.out.println("TEST : "+uriTypeCandidate);
                        for(Entry<Source, String> elem : ic.getUriImplicate().entrySet())
                        {
                            Source s = elem.getKey();
                            if(s != e.getKey() && relValid)
                            {
                                ArrayList<String> testUri = elem.getKey().getRelImportant(elem.getValue(), relImp);
                                String otherTypeUri = null;
                                if(testUri != null && !testUri.isEmpty())
                                {
                                    if(testUri.contains(uriTypeCandidate))
                                    {
                                            uriType.put(s, elem.getValue()+" "+relImp+" "+uriTypeCandidate);
                                            //System.out.println("\t ADD "+uriTypeCandidate);
                                    }
                                }
                            }
                        }
                        if(relValid && uriType.size() > 0)
                        {
                            founded = true;
                            
//                            System.out.println("TYPE CANDIDATE FOUNDED !");
//                            System.out.println(uriTypeCandidate+" --> ");
//                            System.out.println(uriType);
//                            System.out.println("------------");
                            TypeCandidate tc = new TypeCandidate(ic, uriTypeCandidate);
                            for(Entry<Source, String> eUriType : uriType.entrySet())
                            {
                                tc.addElem(eUriType.getKey(), eUriType.getValue());
                            }
                            if(ic.addTypeCandidate(tc))
                                nbtc ++;

//                             if(mongoCollectionType != null)
//                            {
//                                this.nbMongoSaved ++;
//
//
//                                try
//                                {
//                                      WriteResult wr =collMongo.insert(tc.toDBObject());
//                                }
//                                catch(NullPointerException ex)
//                                {
//                                    System.err.println("ERROR Mongo Writer null ...");
//                                    System.err.println(ex);
//                                    //System.exit(0);
//                                }
//                            }
                            //break;
                        }
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
    
     private void mergeLabelCandidate(NodeCandidate nc, ArrayList<LabelCandidate> lcs, String l1, Source s1, String l2, Source s2, float simValue, String dataProp)
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
            LabelCandidate newLC = new LabelCandidate(nc, dataProp, this.getSumSQ());
            newLC.addElem(s1, l1);
            newLC.addElem(s2, l2);
            newLC.addValue(s1, simValue);
            lcs.add(newLC);
        }
     }
    
     private ArrayList<LabelCandidate> generateLabelCandidate(String dataProp, NodeCandidate nc)
     {
        HashMap<Source, ArrayList<String>> sourcesLabel = new HashMap<>();
        ArrayList<Source> sourcesInv= new ArrayList<>();
        for(Entry<Source, String> e : nc.getUriImplicate().entrySet())
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
                               this.mergeLabelCandidate(nc, lcs, labelS1, s1, labelS2, s2, simValue, dataProp);
                           }
                       }
                   }
                }
            }
        }
        return lcs;
     }
    
    public void computeLabelCandidate(String mongoDb)
    {
        
//         DBCollection collMongo = null;
//        if(mongoDb != null)
//        {
//            String mongoCollectionLabels = this.getMongoDb(mongoDb);
//            collMongo = connectMongo(mongoCollectionLabels);
//        }
        ArrayList<NodeCandidate> allCands = this.getAllNodeCandidates();
        for(NodeCandidate nc : allCands)
        {
            for(String dataProp : this.urisLabelsImp)
            {
                ArrayList<LabelCandidate> lcs = this.generateLabelCandidate(dataProp, nc);
                if(lcs != null && lcs.size() > 0)
                {
                    //ic.addLabelCandidates(dataProp, lcs);
                    nc.addAllLabelsCandidate(lcs, trustLcMax, this.getSumSQ());
                }
            }
            nc.clearLabelsCandidates();
//            if(collMongo != null)
//            {
//                ArrayList<String> lcTreated = new ArrayList<>();
//                 for(LabelCandidate lc : nc.getLabelCandidates())
//                 {
//                    try
//                    {
//                         WriteResult wr =collMongo.insert(lc.toDBObject());
//                    }
//                    catch(NullPointerException ex)
//                    {
//                        System.err.println("ERROR Mongo Writer null (LC) ...");
//                        System.exit(0);
//                    }
//                }
//            }
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
        
        this.dataPrologSources = sData;
        return sData;
    }
    
    public String getDataPrologSources()
    {
        return this.dataPrologSources;
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
