/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MultiSources;

import Alignment.Aligner;
import Alignment.AlignerLogMap;
import Alignment.AlignerSeals;
import Alignment.Alignments;
import Alignment.StringDistance;
import Candidate.ArcCandidate.ArcCandidate;
import Candidate.NodeCandidate.ClassCandidate;
import Candidate.NodeCandidate.IndividualCandidate;
import Candidate.ArcCandidate.LabelCandidate;
import Candidate.ArcCandidate.RelationCandidate;
import Candidate.ArcCandidate.TypeCandidate;
import Candidate.NodeCandidate.NodeCandidate;
import MultiSources.Solver.GLPK.ExtensionGlpkSolver;
import Source.OntologicalElement.ClassOntologicalElement;
import Source.OntologicalElement.IndividualOntologicalElement;
import Source.OntologicalElement.OntologicalElement;
import Source.Source;
import Source.SparqlProxy;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import muskca.FilePerso;
import muskca.Muskca;
import org.apache.commons.io.IOUtils;
import org.bson.Document;

/**
 *
 * @author murloc
 */
public class Fusionner implements Serializable
{
   
   
    
    private ArrayList<NodeCandidate> allNodeCands;
    
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
    
    private Alignments aligns;
    
    private ArrayList<String> urisLabelsImp;
    private ArrayList<String> urisRelImp;
    private String uriTypeBase;
    private ArrayList<String> urisTypeImp;
    
    private int idAlign = 0;
    private int idClassAlign = 0;
    
    public int nbMongoSaved = 0;
    
    private String aligner = "";
    private float threshold =0.0f;
    
    private float x0 = 0.0f;
    private float gamma = 0.0f;
    
    private String moduleFile;
    
    public Fusionner(ArrayList<Source> sources, ArrayList<String> urisLabelsImp, ArrayList<String> urisRelImp, String uriTypeBase, ArrayList<String> urisTypeImp, String aligner,float threshold, float x0, float gamma, String moduleFile)
    {
        this.urisLabelsImp = urisLabelsImp;
        this.urisRelImp = urisRelImp;
        this.uriTypeBase = uriTypeBase;
        this.urisTypeImp = urisTypeImp;
        
        this.aligns = new Alignments(this.uriTypeBase);
        
        this.sources = sources;
        
        this.allNodeCands = new ArrayList<>();
        this.aligner = aligner;
        this.threshold = threshold;
        
        this.x0 = x0;
        this.gamma = gamma;
        this.moduleFile = moduleFile;
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
    
    public String getUriTypeBase(){
        return this.uriTypeBase;
    }
    
    
    public void setElemsOnSources(){
        System.out.println("Get ontological elements (classes)");
        for(Source s : this.sources){
            s.setFusionner(this);
            System.out.println("Fill "+s.getName());
            int i = 0;
            for(String uri : s.getAllClassUris(this.uriTypeBase)){
                ClassOntologicalElement coe = new ClassOntologicalElement(uri, s);
                s.addOntoElem(uri, coe);
                i++;
            }
            System.out.println("\t "+i+" Classes added");
        }
        
        System.out.println("Get ontological elements (ind.)");
        for(Source s : this.sources){
            System.out.println("Fill "+s.getName());
            int i = 0;
            for(String typeUri : this.urisTypeImp){
                //System.out.println("Considering : "+typeUri+" inds.");
                for(String uri : s.getAllIndividualUris(typeUri)){
                    IndividualOntologicalElement ioe = new IndividualOntologicalElement(uri, s);
                    s.addOntoElem(uri, ioe);
                    i++;
                }
            }
            System.out.println("\t"+i+" inds. added");
        }
    }
    
    public void setNodeCands(ArrayList<NodeCandidate> allCands){
        this.allNodeCands = allCands;
    }
    
    public Alignments getAlignments(){
        return this.aligns;
    }
    
    public void alignSources(){
        for(int i = 0; i< this.sources.size(); i++)
        {
            Source s1 = this.sources.get(i);
            System.out.println("Start analyze source : "+s1.getName()+" ("+(i+1)+"/"+this.sources.size()+")");
            for(int j = i+1; j< this.sources.size(); j++)
            {
                Source s2 = this.sources.get(j);
                
                System.out.println(s1.getName()+"/"+s2.getName()+ ": ");
                Aligner aligner = new AlignerSeals(this, s1, s2, this.aligns);
                //System.out.println("Aligner ended !");
                aligner.alignSources(0, this.moduleFile); // score min = 0 to keep all alignment (filter will be done by taken the first one)
            }
        }
    }
     
    public NodeCandidate getCandidateFromUris(ArrayList<String> uris)
    {
        NodeCandidate ret = null;
        for(NodeCandidate nc : this.allNodeCands)
        {
            if(nc.isSameCand(uris))
            {
                ret = nc;
                break;
            }
        }
        
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
        float maxSourceQual = 0;
        for(Source s : this.sources)
        {
            maxSourceQual += s.getSourceQualityScore();
        }
        for(NodeCandidate nc : this.getAllNodeCandidates())
        {
            nc.computeTrustScore(nbSources, maxSourceQual, this.x0, this.gamma);
        }
    }
    
    public float getSumArcTrust(NodeCandidate nc1, NodeCandidate nc2)
    {
        float ret = 0;
        
        
        return ret;
    }
   
    
    public String getOwlFileToTtl(String owlFile)
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
        
        ret += "PREFIX : <http://muskca_evals/>\n";
        ret +="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n";
        ret += "PREFIX provo: <http://www.w3.org/ns/prov#>  \n";
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
    
    public SparqlProxy nodeCandidatesToProvo(ArrayList<NodeCandidate> cands,String provoFile, String provoSpOut, String adomFile, String baseUri){
        return this.nodeCandidatesToProvo(cands, provoFile, provoSpOut, adomFile, baseUri, 0.0f);
    }
    
    public SparqlProxy nodeCandidatesToProvoThreshold(ArrayList<NodeCandidate> cands, String provoFile,String provoSpOut, String adomFile, String baseUri){
        return this.nodeCandidatesToProvo(cands, provoFile, provoSpOut, adomFile, baseUri, this.getThreshold());
    }
    
    private SparqlProxy nodeCandidatesToProvo(ArrayList<NodeCandidate> cands, String provoFile, String provoSpOut, String adomFile, String baseUri,float threshold)
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
        
        for(NodeCandidate nc : cands)
        {
            if(nc.getTrustScore()>=threshold){
                spOutProvo.storeData(new StringBuilder(this.setPrefix()+" INSERT DATA {"+nc.toProvO(baseUri, numInst, provoSourceUri, uriKbMerge)+"}"));
                numInst ++;  
            }
            
        }
        
        return spOutProvo;
    }
    
    public SparqlProxy nodeCandidatesToOwl(ArrayList<NodeCandidate> cands, String provoSpOut, String adomFile, String baseUri){
        return this.nodeCandidatesToOWL(cands, provoSpOut, adomFile, baseUri, 0.0f);
    }
    
    public SparqlProxy nodeCandidatesToOwlThreshold(ArrayList<NodeCandidate> cands, String provoSpOut, String adomFile, String baseUri){
        return this.nodeCandidatesToOWL(cands, provoSpOut, adomFile, baseUri, this.getThreshold());
    }
    
    private SparqlProxy nodeCandidatesToOWL(ArrayList<NodeCandidate> cands, String provoSpOut, String adomFile, String baseUri,float threshold)
    {
        SparqlProxy spOutProvo = SparqlProxy.getSparqlProxy(provoSpOut);
        /*SparqlProxy spOut1 = SparqlProxy.getSparqlProxy("http://amarger.murloc.fr:8080/OAEI_allcands_0_1/");
        SparqlProxy spOut2 = SparqlProxy.getSparqlProxy("http://amarger.murloc.fr:8080/OAEI_allcands_0_2/");
        SparqlProxy spOut3 = SparqlProxy.getSparqlProxy("http://amarger.murloc.fr:8080/OAEI_allcands_0_3/");
        SparqlProxy spOut4 = SparqlProxy.getSparqlProxy("http://amarger.murloc.fr:8080/OAEI_allcands_0_4/");
        SparqlProxy spOut5 = SparqlProxy.getSparqlProxy("http://amarger.murloc.fr:8080/OAEI_allcands_0_5/");
        SparqlProxy spOut6 = SparqlProxy.getSparqlProxy("http://amarger.murloc.fr:8080/OAEI_allcands_0_6/");
        SparqlProxy spOut7 = SparqlProxy.getSparqlProxy("http://amarger.murloc.fr:8080/OAEI_allcands_0_7/");
        SparqlProxy spOut8 = SparqlProxy.getSparqlProxy("http://amarger.murloc.fr:8080/OAEI_allcands_0_8/");
        SparqlProxy spOut9 = SparqlProxy.getSparqlProxy("http://amarger.murloc.fr:8080/OAEI_allcands_0_9/");
        
        spOut1.clearSp();
        spOut2.clearSp();
        spOut3.clearSp();
        spOut4.clearSp();
        spOut5.clearSp();
        spOut6.clearSp();
        spOut7.clearSp();
        spOut8.clearSp();
        spOut9.clearSp();
        
        spOut1.storeData(new StringBuilder(this.getModuleAndSameAse()));
        spOut2.storeData(new StringBuilder(this.getModuleAndSameAse()));
        spOut3.storeData(new StringBuilder(this.getModuleAndSameAse()));
        spOut4.storeData(new StringBuilder(this.getModuleAndSameAse()));
        spOut5.storeData(new StringBuilder(this.getModuleAndSameAse()));
        spOut6.storeData(new StringBuilder(this.getModuleAndSameAse()));
        spOut7.storeData(new StringBuilder(this.getModuleAndSameAse()));
        spOut8.storeData(new StringBuilder(this.getModuleAndSameAse()));
        spOut9.storeData(new StringBuilder(this.getModuleAndSameAse()));*/
        
        spOutProvo.clearSp();
        System.out.println("Export with threshold : "+threshold);
        spOutProvo.storeData(new StringBuilder(this.setPrefix()+" INSERT DATA {"+this.getOwlFileToTtl(adomFile)+"}"));
        //spOutProvo.storeData(new StringBuilder(this.getModuleAndSameAse()));
        int numInst = 1;
        for(NodeCandidate nc : cands)
        {
            StringBuilder data = new StringBuilder(this.setPrefix()+" INSERT DATA {"+nc.toOWL(baseUri)+"}");
            if(nc.getTrustScore()>=threshold){
                spOutProvo.storeData(data);
                numInst ++;
            }
            
            /*if(nc.getTrustScore() >= 0.1){
                spOut1.storeData(data);
            }
            if(nc.getTrustScore() >= 0.2){
                spOut2.storeData(data);
            }
            if(nc.getTrustScore() >= 0.3){
                spOut3.storeData(data);
            }
            if(nc.getTrustScore() >= 0.4){
                spOut4.storeData(data);
            }
            if(nc.getTrustScore() >= 0.5){
                spOut5.storeData(data);
            }
            if(nc.getTrustScore() >= 0.6){
                spOut6.storeData(data);
            }
            if(nc.getTrustScore() >= 0.7){
                spOut7.storeData(data);
            }
            if(nc.getTrustScore() >= 0.8){
                spOut8.storeData(data);
            }
            if(nc.getTrustScore() >= 0.9){
                spOut9.storeData(data);
            }*/
        }
        return spOutProvo;
        //return spOut1;
    }
      
    
    public ArrayList<NodeCandidate> getAllNodeCandidates()
    {
        return this.allNodeCands;
    }
    
    public void computeRelationCandidate(String relImp)
    {
        int nbicHR = 0;
        for(NodeCandidate nc : this.allNodeCands) // for each node candidate nc
        {
            if(nc.getClass() == ClassCandidate.class){
                continue;
            }
            IndividualCandidate ic = (IndividualCandidate) nc;
            ArrayList<String> uriHigherRank = new ArrayList<>();
            boolean relValid = false;
            for(Entry<Source, OntologicalElement> e : ic.getUriImplicate().entrySet()) // for each elem in nc
            {
                boolean founded = false;
                ArrayList<String> urisHR = e.getKey().getRelImportant(e.getValue().getUri(), relImp); // get all the uris from <nc> <relIm> <uriHR>
                if(urisHR.size() > 0) // if uriHR exists (at least one)
                {
                    //String uriHR = urisHR.get(0); //consider all objectproperties functionnal (can't be 1:n)
                    founded = false;
                    for(String uriHR : urisHR)
                    {
                        for(NodeCandidate ncHR : this.getNodeCandidateFromUriImplicate(uriHR, e.getKey())) // get all node candidate wich uriHR is implicated
                        {
                            ArrayList<Source> sourcesHR = new ArrayList<>();
                            sourcesHR.add(e.getKey());
                            for(Source s : ic.getUriImplicate().keySet()) // for all sources in nc
                            {
                                if(s != e.getKey()) // if source is different from the initial founded ncHR source
                                {
                                    String uriIC = ic.getUriFromSource(s).getUri();
                                    OntologicalElement oeHR = ncHR.getUriFromSource(s);
                                    if(oeHR != null)
                                    {
                                        String uriICHR = ncHR.getUriFromSource(s).getUri();
                                        if(s.isRelImportant(uriIC, relImp, uriICHR)) // if <nc> <relImp> <uriICHR> exists (rel exsits in other source)
                                        {
                                            sourcesHR.add(s); // add the source on the implied sources
                                            founded = true;
                                        }
                                    }
                                }
                            }
                            //Create a new relation candidate for each nodecandidate whith the associated sourcesHR
                            RelationCandidate relCandidate = new RelationCandidate(ic,relImp, ncHR, sourcesHR);
                            //relCandidate.computeTrustScore(this.trustRcMax);
                            //ic.addIcHR(icHR, sourcesHR);
                            if(ic.addRelationCandidate(relCandidate))
                            {
                                nbicHR++;
                            }
                        }
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
    
    public void computeTypeCandidate()
    {
        String relImpInd = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
        String relImpClass = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
        int nbtc = 0;
        for(NodeCandidate nc : this.allNodeCands)
        {
            String relImp = relImpInd;
            if(nc.getClass() == ClassCandidate.class){
                relImp = relImpClass;
            }
            //IndividualCandidate ic = (IndividualCandidate) nc;
            boolean relValid = false;
            for(Entry<Source, OntologicalElement> e : nc.getUriImplicate().entrySet())
            {
                boolean founded = false;
                ArrayList<String> urisType = e.getKey().getRelImportant(e.getValue().getUri(), relImp);
                for(String uriTypeCandidate : urisType)
                {
                    //System.out.println(uriTypeCandidate);
                    if(uriTypeCandidate.startsWith(this.uriTypeBase) && !nc.containsSameTypeCand(e.getKey(), e.getValue().getUri(), relImp, uriTypeCandidate))
                    {
                        HashMap<Source, String> uriType = new HashMap<>();
                        uriType.put(e.getKey(), e.getValue()+" "+relImp+" "+uriTypeCandidate);
                        relValid = true;
                        //System.out.println("TEST : "+uriTypeCandidate);
                        for(Entry<Source, OntologicalElement> elem : nc.getUriImplicate().entrySet())
                        {
                            Source s = elem.getKey();
                            if(s != e.getKey() && relValid)
                            {
                                ArrayList<String> testUri = elem.getKey().getRelImportant(elem.getValue().getUri(), relImp);
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
                            TypeCandidate tc = new TypeCandidate(nc, relImp, uriTypeCandidate);
                            for(Entry<Source, String> eUriType : uriType.entrySet())
                            {
                                tc.addElem(eUriType.getKey(), eUriType.getValue());
                            }
                            if(nc.addTypeCandidate(tc))
                                nbtc ++;
                        }
                    }
                }
                /*if(founded)
                {
                    break;
                }*/
            }
        }
        System.out.println("NB tc : "+nbtc);
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
            LabelCandidate newLC = new LabelCandidate(nc, dataProp);
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
        for(Entry<Source, OntologicalElement> e : nc.getUriImplicate().entrySet())
        {
            ArrayList<String> labels = e.getKey().getRelImportant(e.getValue().getUri(), dataProp);
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
    
    public void computeLabelCandidate()
    {
       
        ArrayList<NodeCandidate> allCands = this.getAllNodeCandidates();
        for(NodeCandidate nc : allCands)
        {
            for(String dataProp : this.urisLabelsImp)
            {
                ArrayList<LabelCandidate> lcs = this.generateLabelCandidate(dataProp, nc);
                if(lcs != null && lcs.size() > 0)
                {
                    //ic.addLabelCandidates(dataProp, lcs);
                    nc.addAllLabelsCandidate(lcs, trustLcMax);
                }
            }
            nc.clearLabelsCandidates();
        }
    }

    /**
     * @return the aligner
     */
    public String getAligner() {
        return aligner;
    }

    /**
     * @param aligner the aligner to set
     */
    public void setAligner(String aligner) {
        this.aligner = aligner;
    }

    /**
     * @return the threshold
     */
    public float getThreshold() {
        return threshold;
    }
    
    public void exportAllCandsMongoDB(ArrayList<NodeCandidate> allCands)
    {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase db = mongoClient.getDatabase("evals_Triticum");
        db.drop();
        MongoCollection<Document> collection = db.getCollection("triticumCandidate");
        MongoCollection<Document> collectionRel = db.getCollection("triticumICHR");
        MongoCollection<Document> collectionType = db.getCollection("triticumTypeCandidate");
        MongoCollection<Document> collectionLabel = db.getCollection("triticumLabelCandidate");
        for(NodeCandidate nc : allCands)
        {
            if(nc.isIndividual())
            {
                collection.insertOne(new Document(nc.toDBObject()));
                IndividualCandidate ic = (IndividualCandidate) nc;
                for(ArcCandidate ac : ic.getAllArcCandidates())
                {
                    String prop = ac.getDataProp();
                    if(prop.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                    {
                        collectionType.insertOne(new Document(ac.toDBObject()));
                    }
                    else if(prop.startsWith("http://ontology.irstea.fr/AgronomicTaxon#hasHigherRank"))
                    {
                        collectionRel.insertOne(new Document(ac.toDBObject()));
                    }
                    for(LabelCandidate lc : ic.getLabelCandidates())
                    {
                        collectionLabel.insertOne(new Document(lc.toDBObject()));
                    }
                }
            }
            
        }
    }
   
    /*public String getModuleAndSameAse(){
        return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX foaf: <http://xmlsn.com/foaf/0.1#>\n" +
                "PREFIX : <http://muskca_evals/>\n" +
                "INSERT DATA {\n" +
                "\n" +
                ":hasAuthor rdf:type owl:ObjectProperty ;\n" +
                "           \n" +
                "           rdfs:range :Author ;\n" +
                "           \n" +
                "           rdfs:domain :Paper .\n" +
                "\n" +
                "\n" +
                ":reviewWrittenBy rdf:type owl:ObjectProperty .\n" +
                "\n" +
                "\n" +
                ":Attendee rdf:type owl:Class ;\n" +
                "          \n" +
                "          rdfs:label \"Conference atendee\" ,\n" +
                "                     \"Atendee\" ,\n" +
                "                     \"Conference participant\" ,\n" +
                "                     \"Participant\" ;\n" +
                "          \n" +
                "          rdfs:subClassOf :Person .\n" +
                "\n" +
                "\n" +
                ":Author rdf:type owl:Class ;\n" +
                "        \n" +
                "        rdfs:label \"Paper author\" ,\n" +
                "                   \"Writer\" ;\n" +
                "        \n" +
                "        rdfs:subClassOf :Person .\n" +
                "\n" +
                "\n" +
                ":Banquet rdf:type owl:Class ;\n" +
                "         \n" +
                "         rdfs:label \"Banquet Event\" ,\n" +
                "                    \"Banquet\" ;\n" +
                "         \n" +
                "         rdfs:subClassOf :Event .\n" +
                "\n" +
                "\n" +
                ":Chair_PC rdf:type owl:Class ;\n" +
                "          \n" +
                "          rdfs:label \"Chair PC\" ,\n" +
                "                     \"Chair Program Comitee\" ,\n" +
                "                     \"Session chair\" ,\n" +
                "                     \"Program Comitee Chair\" ;\n" +
                "          \n" +
                "          rdfs:subClassOf :Person .\n" +
                "\n" +
                "\n" +
                ":City rdf:type owl:Class ;\n" +
                "      \n" +
                "      rdfs:label \"City\" ;\n" +
                "      \n" +
                "      rdfs:subClassOf :Location .\n" +
                "\n" +
                "\n" +
                ":Comitee rdf:type owl:Class ;\n" +
                "         \n" +
                "         rdfs:label \"Comitee\" .\n" +
                "\n" +
                "\n" +
                ":Conference rdf:type owl:Class ;\n" +
                "            \n" +
                "            rdfs:label \"Conference\" ;\n" +
                "            \n" +
                "            rdfs:subClassOf :Event .\n" +
                "\n" +
                "\n" +
                ":Event rdf:type owl:Class ;\n" +
                "       \n" +
                "       rdfs:label \"Event\" .\n" +
                "\n" +
                "\n" +
                ":Location rdf:type owl:Class ;\n" +
                "          \n" +
                "          rdfs:label \"Place\" ,\n" +
                "                     \"Location\" .\n" +
                "\n" +
                "\n" +
                ":Paper rdf:type owl:Class ;\n" +
                "       \n" +
                "       rdfs:label \"Scientific article\" ,\n" +
                "                  \"Paper\" ,\n" +
                "                  \"Article\" .\n" +
                "\n" +
                "\n" +
                ":Person rdf:type owl:Class .\n" +
                "\n" +
                "\n" +
                ":ProgramComitee rdf:type owl:Class ;\n" +
                "                \n" +
                "                rdfs:label \"Program Comitee\" ;\n" +
                "                \n" +
                "                rdfs:subClassOf :Comitee .\n" +
                "\n" +
                ":Reception rdf:type owl:Class ;\n" +
                "           \n" +
                "           rdfs:label \"Reception\" ;\n" +
                "           \n" +
                "           rdfs:subClassOf :Event .\n" +
                "\n" +
                "\n" +
                ":Review rdf:type owl:Class ;\n" +
                "        \n" +
                "        rdfs:label \"Review\" .\n" +
                "\n" +
                "\n" +
                ":Speaker rdf:type owl:Class ;\n" +
                "         \n" +
                "         rdfs:label \"Active participant\" ,\n" +
                "                    \"Speaker\" ;\n" +
                "         \n" +
                "         rdfs:subClassOf :Attendee .\n" +
                "\n" +
                "\n" +
                ":Trip rdf:type owl:Class ;\n" +
                "      \n" +
                "      rdfs:label \"Trip\" ,\n" +
                "                 \"Excursion\" ;\n" +
                "      \n" +
                "      rdfs:subClassOf :Event .\n" +
                "\n" +
                "\n" +
                "\n" +
                "<http://muskca_evals/ProgramComitee> owl:sameAs <http://cmt#ProgramCommittee>.\n" +
"<http://muskca_evals/ProgramComitee> owl:sameAs <http://conference#Program_committee>."+
                " <http://muskca_evals/Chair_PC> owl:sameAs <http://ekaw#PC_Chair>.\n" +
"        <http://muskca_evals/Banquet> owl:sameAs <http://ekaw#Conference_Banquet>.\n" +
"        <http://muskca_evals/Conference> owl:sameAs <http://ekaw#Conference>.\n" +
"        <http://muskca_evals/Trip> owl:sameAs <http://ekaw#Conference_Trip>.\n" +
"        <http://muskca_evals/Location> owl:sameAs <http://ekaw#Location>.\n" +
"        <http://muskca_evals/Paper> owl:sameAs <http://ekaw#Paper>.\n" +
"        <http://muskca_evals/Speaker> owl:sameAs <http://ekaw#Presenter>.\n" +
"        <http://muskca_evals/Paper> owl:sameAs <http://ekaw#Paper>.\n" +
"        <http://muskca_evals/Review> owl:sameAs <http://ekaw#Review>.\n" +
"        <http://muskca_evals/Attendee> owl:sameAs <http://ekaw#Conference_Participant>.\n" +
"        <http://muskca_evals/Event> owl:sameAs <http://ekaw#Event>.\n" +
"        <http://muskca_evals/Conference> owl:sameAs <http://ekaw#Conference>.\n" +
"        <http://muskca_evals/Author> owl:sameAs <http://ekaw#Paper_Author>.\n" +
"\n" +
"        <http://muskca_evals/Banquet> owl:sameAs <http://iasted#Dinner_banquet>.\n" +
"        <http://muskca_evals/Reception> owl:sameAs <http://iasted#Coctail_reception>.\n" +
"        <http://muskca_evals/Trip> owl:sameAs <http://iasted#Trip_city>.\n" +
"        <http://muskca_evals/Location> owl:sameAs <http://iasted#Place>.\n" +
"        <http://muskca_evals/Paper> owl:sameAs <http://iasted#Submission>.\n" +
"        <http://muskca_evals/Speaker> owl:sameAs <http://iasted#Speaker>.\n" +
"        <http://muskca_evals/Author> owl:sameAs <http://iasted#Author>.\n" +
"        <http://muskca_evals/Review> owl:sameAs <http://iasted#Review>.\n" +
"        <http://muskca_evals/City> owl:sameAs <http://iasted#City>.\n" +
"        <http://muskca_evals/Location> owl:sameAs <http://iasted#Place>.\n" +
"        <http://muskca_evals/Attendee> owl:sameAs <http://iasted#Delegate>.\n" +
"\n" +
"\n" +
"         <http://muskca_evals/Chair_PC> owl:sameAs <http://cmt#ProgramCommitteeChair>.\n" +
"        <http://muskca_evals/Conference> owl:sameAs <http://cmt#Conference>.\n" +
"        <http://muskca_evals/Attendee> owl:sameAs <http://cmt#ConferenceMember>.\n" +
"        <http://muskca_evals/Paper> owl:sameAs <http://cmt#Paper>.\n" +
"        <http://muskca_evals/Author> owl:sameAs <http://cmt#Author>.\n" +
"        <http://muskca_evals/Review> owl:sameAs <http://cmt#Review>.\n" +
"        <http://muskca_evals/Person> owl:sameAs <http://cmt#Person>.\n" +
"        \n" +
"\n" +
"        <http://muskca_evals/Chair_PC> owl:sameAs <http://conference#Chair>.\n" +
"        <http://muskca_evals/Conference> owl:sameAs <http://conference#Conference>.\n" +
"        <http://muskca_evals/Paper> owl:sameAs <http://conference#Paper>.\n" +
"        <http://muskca_evals/Author> owl:sameAs <http://conference#Regular_author>.\n" +
"        <http://muskca_evals/Review> owl:sameAs <http://conference#Review>.\n" +
"        <http://muskca_evals/Speaker> owl:sameAs <http://conference#Active_conference_participant>.\n" +
"        <http://muskca_evals/Attendee> owl:sameAs <http://conference#Conference_participant>.\n" +
"        <http://muskca_evals/Person> owl:sameAs <http://conference#Person>.\n" +
"\n" +
"        <http://muskca_evals/Chair_PC> owl:sameAs <http://confOf#Chair_PC>.\n" +
"        <http://muskca_evals/Banquet> owl:sameAs <http://confOf#Banquet>.\n" +
"        <http://muskca_evals/Conference> owl:sameAs <http://confOf#Conference>.\n" +
"        <http://muskca_evals/Reception> owl:sameAs <http://confOf#Reception>.\n" +
"        <http://muskca_evals/Trip> owl:sameAs <http://confOf#Trip>.\n" +
"        <http://muskca_evals/Paper> owl:sameAs <http://confOf#Paper>.\n" +
"        <http://muskca_evals/Author> owl:sameAs <http://confOf#Author>.\n" +
"        <http://muskca_evals/City> owl:sameAs <http://confOf#City>.\n" +
"        <http://muskca_evals/Attendee> owl:sameAs <http://confOf#Participant>.\n" +
"        <http://muskca_evals/Event> owl:sameAs <http://confOf#Event>.\n" +
"\n" +
"        <http://muskca_evals/Conference> owl:sameAs <http://edas#Conference>.\n" +
"        <http://muskca_evals/Reception> owl:sameAs <http://edas#Reception>.\n" +
"        <http://muskca_evals/Trip> owl:sameAs <http://edas#Excursion>.\n" +
"        <http://muskca_evals/Location> owl:sameAs <http://edas#Place>.\n" +
"        <http://muskca_evals/Paper> owl:sameAs <http://edas#Paper>.\n" +
"        <http://muskca_evals/Speaker> owl:sameAs <http://edas#Presenter>.\n" +
"        <http://muskca_evals/Author> owl:sameAs <http://edas#Author>.\n" +
"        <http://muskca_evals/Review> owl:sameAs <http://edas#Review>.\n" +
"        <http://muskca_evals/Attendee> owl:sameAs <http://edas#Attendee>.\n" +
"        <http://muskca_evals/Person> owl:sameAs <http://edas#Person>.\n" +
"\n" +
"         <http://muskca_evals/Chair_PC> owl:sameAs <http://sigkdd#Program_Chair>.\n" +
"        <http://muskca_evals/Conference> owl:sameAs <http://sigkdd#Conference>.\n" +
"        <http://muskca_evals/Location> owl:sameAs <http://sigkdd#Place>.\n" +
"        <http://muskca_evals/Paper> owl:sameAs <http://sigkdd#Paper>.\n" +
"        <http://muskca_evals/Author> owl:sameAs <http://sigkdd#Author>.\n" +
"        <http://muskca_evals/Review> owl:sameAs <http://sigkdd#Review>.\n" +
"        <http://muskca_evals/ProgramComitee> owl:sameAs <http://sigkdd#Program_Committee>.\n" +
"        <http://muskca_evals/Person> owl:sameAs <http://sigkdd#Person>.\n" +
"        <http://muskca_evals/Attendee> owl:sameAs <http://sigkdd#Listener>.\n" +
"        <http://muskca_evals/Speaker> owl:sameAs <http://sigkdd#Speaker>.\n" +
"\n" +
"\n" +
"          <http://muskca_evals/hasAuthor> owl:sameAs <http://ekaw#writtenBy>.\n" +
"        <http://muskca_evals/reviewWrittenBy> owl:sameAs <http://ekaw#reviewWrittenBy>.\n" +
"        <http://muskca_evals/hasAuthor> owl:sameAs <http://iasted#is_writen_by>.\n" +
"\n" +
"         <http://muskca_evals/hasAuthor> owl:sameAs <http://cmt#hasAuthor>.\n" +
"        <http://muskca_evals/reviewWrittenBy> owl:sameAs <http://cmt#writtenBy>.\n" +
"\n" +
"        <http://muskca_evals/hasAuthor> owl:sameAs <http://conference#hasAuthors>.\n" +
"        <http://muskca_evals/reviewWrittenBy> owl:sameAs <http://conference#has_authors>.\n" +
"        <http://muskca_evals/hasAuthor> owl:sameAs <http://confOf#writtenBy>.\n" +
"        <http://muskca_evals/hasAuthor> owl:sameAs <http://edas#isWrittenBy>.\n" +
"         <http://muskca_evals/hasAuthor> owl:sameAs <http://sigkdd#submit>."+
         "}";
    }*/
    
    
   /* AutoMap */
    public String getModuleAndSameAse(){
        return "PREFIX foaf: <http://xmlsn.com/foaf/0.1#>\n" +
                "PREFIX : <http://muskca_evals/>\n" +
                "INSERT DATA {\n" +
                "\n" +
                ":hasAuthor rdf:type owl:ObjectProperty ;\n" +
                "           \n" +
                "           rdfs:range :Author ;\n" +
                "           \n" +
                "           rdfs:domain :Paper .\n" +
                "\n" +
                "\n" +
                ":reviewWrittenBy rdf:type owl:ObjectProperty .\n" +
                "\n" +
                "\n" +
                ":Attendee rdf:type owl:Class ;\n" +
                "          \n" +
                "          rdfs:label \"Conference atendee\" ,\n" +
                "                     \"Atendee\" ,\n" +
                "                     \"Conference participant\" ,\n" +
                "                     \"Participant\" ;\n" +
                "          \n" +
                "          rdfs:subClassOf :Person .\n" +
                "\n" +
                "\n" +
                ":Author rdf:type owl:Class ;\n" +
                "        \n" +
                "        rdfs:label \"Paper author\" ,\n" +
                "                   \"Writer\" ;\n" +
                "        \n" +
                "        rdfs:subClassOf :Person .\n" +
                "\n" +
                "\n" +
                ":Banquet rdf:type owl:Class ;\n" +
                "         \n" +
                "         rdfs:label \"Banquet Event\" ,\n" +
                "                    \"Banquet\" ;\n" +
                "         \n" +
                "         rdfs:subClassOf :Event .\n" +
                "\n" +
                "\n" +
                ":Chair_PC rdf:type owl:Class ;\n" +
                "          \n" +
                "          rdfs:label \"Chair PC\" ,\n" +
                "                     \"Chair Program Comitee\" ,\n" +
                "                     \"Session chair\" ,\n" +
                "                     \"Program Comitee Chair\" ;\n" +
                "          \n" +
                "          rdfs:subClassOf :Person .\n" +
                "\n" +
                "\n" +
                ":City rdf:type owl:Class ;\n" +
                "      \n" +
                "      rdfs:label \"City\" ;\n" +
                "      \n" +
                "      rdfs:subClassOf :Location .\n" +
                "\n" +
                "\n" +
                ":Comitee rdf:type owl:Class ;\n" +
                "         \n" +
                "         rdfs:label \"Comitee\" .\n" +
                "\n" +
                "\n" +
                ":Conference rdf:type owl:Class ;\n" +
                "            \n" +
                "            rdfs:label \"Conference\" ;\n" +
                "            \n" +
                "            rdfs:subClassOf :Event .\n" +
                "\n" +
                "\n" +
                ":Event rdf:type owl:Class ;\n" +
                "       \n" +
                "       rdfs:label \"Event\" .\n" +
                "\n" +
                "\n" +
                ":Location rdf:type owl:Class ;\n" +
                "          \n" +
                "          rdfs:label \"Place\" ,\n" +
                "                     \"Location\" .\n" +
                "\n" +
                "\n" +
                ":Paper rdf:type owl:Class ;\n" +
                "       \n" +
                "       rdfs:label \"Scientific article\" ,\n" +
                "                  \"Paper\" ,\n" +
                "                  \"Article\" .\n" +
                "\n" +
                "\n" +
                ":Person rdf:type owl:Class .\n" +
                "\n" +
                "\n" +
                ":ProgramComitee rdf:type owl:Class ;\n" +
                "                \n" +
                "                rdfs:label \"Program Comitee\" ;\n" +
                "                \n" +
                "                rdfs:subClassOf :Comitee .\n" +
                "\n" +
                ":Reception rdf:type owl:Class ;\n" +
                "           \n" +
                "           rdfs:label \"Reception\" ;\n" +
                "           \n" +
                "           rdfs:subClassOf :Event .\n" +
                "\n" +
                "\n" +
                ":Review rdf:type owl:Class ;\n" +
                "        \n" +
                "        rdfs:label \"Review\" .\n" +
                "\n" +
                "\n" +
                ":Speaker rdf:type owl:Class ;\n" +
                "         \n" +
                "         rdfs:label \"Active participant\" ,\n" +
                "                    \"Speaker\" ;\n" +
                "         \n" +
                "         rdfs:subClassOf :Attendee .\n" +
                "\n" +
                "\n" +
                ":Trip rdf:type owl:Class ;\n" +
                "      \n" +
                "      rdfs:label \"Trip\" ,\n" +
                "                 \"Excursion\" ;\n" +
                "      \n" +
                "      rdfs:subClassOf :Event .\n" +
                "\n" +
"\n" +
"<http://muskca_evals/Chair_PC> owl:sameAs <http://ekaw#Session_Chair>.\n" +
"        <http://muskca_evals/Speaker> owl:sameAs <http://ekaw#Invited_Speaker>.\n" +
"        <http://muskca_evals/Speaker> owl:sameAs <http://ekaw#Presenter>.\n" +
"        <http://muskca_evals/Review> owl:sameAs <http://ekaw#Review>.\n" +
"        <http://muskca_evals/Trip> owl:sameAs <http://ekaw#Conference_Trip>.\n" +
"        <http://muskca_evals/Chair_PC> owl:sameAs <http://ekaw#PC_Chair>.\n" +
"        <http://muskca_evals/Location> owl:sameAs <http://ekaw#Location>.\n" +
"        <http://muskca_evals/Author> owl:sameAs <http://ekaw#Paper_Author>.\n" +
"        <http://muskca_evals/Paper> owl:sameAs <http://ekaw#Paper>.\n" +
"        <http://muskca_evals/Banquet> owl:sameAs <http://ekaw#Conference_Banquet>.\n" +
"        <http://muskca_evals/Person> owl:sameAs <http://ekaw#Person>.\n" +
"        <http://muskca_evals/Attendee> owl:sameAs <http://ekaw#Conference_Participant>.\n" +
"        <http://muskca_evals/Event> owl:sameAs <http://ekaw#Event>.\n" +
"        <http://muskca_evals/Conference> owl:sameAs <http://ekaw#Conference>.\n" +
"\n" +
"        <http://muskca_evals/hasAuthor> owl:sameAs <http://ekaw#writtenBy>.\n" +
"        <http://muskca_evals/reviewWrittenBy> owl:sameAs <http://ekaw#reviewWrittenBy>.\n" +
"\n" +
"        <http://muskca_evals/City> owl:sameAs <http://iasted#City>.\n" +
"        <http://muskca_evals/Location> owl:sameAs <http://iasted#Place>.\n" +
"        <http://muskca_evals/Person> owl:sameAs <http://iasted#Person>.\n" +
"        <http://muskca_evals/Review> owl:sameAs <http://iasted#Review>.\n" +
"        <http://muskca_evals/Speaker> owl:sameAs <http://iasted#Speaker>.\n" +
"        <http://muskca_evals/Chair_PC> owl:sameAs <http://iasted#Session_chair>.\n" +
"        <http://muskca_evals/Author> owl:sameAs <http://iasted#Author>.\n" +
"\n" +
"         <http://muskca_evals/hasAuthor> owl:sameAs <http://iasted#is_writen_by>.\n" +
"\n" +
"         <http://muskca_evals/Author> owl:sameAs <http://cmt#Author>.\n" +
"        <http://muskca_evals/Person> owl:sameAs <http://cmt#Person>.\n" +
"        <http://muskca_evals/Review> owl:sameAs <http://cmt#Review>.\n" +
"        <http://muskca_evals/Paper> owl:sameAs <http://cmt#Paper>.\n" +
"        <http://muskca_evals/Conference> owl:sameAs <http://cmt#Conference>.\n" +
"\n" +
"        <http://muskca_evals/hasAuthor> owl:sameAs <http://cmt#hasAuthor>.\n" +
"        <http://muskca_evals/reviewWrittenBy> owl:sameAs <http://cmt#writtenBy>.\n" +
"\n" +
"         <http://muskca_evals/Review> owl:sameAs <http://conference#Review>.\n" +
"        <http://muskca_evals/Paper> owl:sameAs <http://conference#Paper>.\n" +
"        <http://muskca_evals/Speaker> owl:sameAs <http://conference#Active_conference_participant>.\n" +
"        <http://muskca_evals/Person> owl:sameAs <http://conference#Person>.\n" +
"        <http://muskca_evals/Attendee> owl:sameAs <http://conference#Conference_participant>.\n" +
"        <http://muskca_evals/Conference> owl:sameAs <http://conference#Conference>.\n" +
"\n" +
"        <http://muskca_evals/hasAuthor> owl:sameAs <http://conference#hasAuthors>.\n" +
"        <http://muskca_evals/reviewWrittenBy> owl:sameAs <http://conference#has_authors>.\n" +
"\n" +
"        <http://muskca_evals/City> owl:sameAs <http://confOf#City>.\n" +
"        <http://muskca_evals/Paper> owl:sameAs <http://confOf#Paper>.\n" +
"        <http://muskca_evals/Review> owl:sameAs <http://confOf#Reviewing_event>.\n" +
"        <http://muskca_evals/Banquet> owl:sameAs <http://confOf#Banquet>.\n" +
"        <http://muskca_evals/Trip> owl:sameAs <http://confOf#Trip>.\n" +
"        <http://muskca_evals/Person> owl:sameAs <http://confOf#Person>.\n" +
"        <http://muskca_evals/Author> owl:sameAs <http://confOf#Author>.\n" +
"        <http://muskca_evals/Conference> owl:sameAs <http://confOf#Conference>.\n" +
"        <http://muskca_evals/Reception> owl:sameAs <http://confOf#Reception>.\n" +
"        <http://muskca_evals/Chair_PC> owl:sameAs <http://confOf#Chair_PC>.\n" +
"        <http://muskca_evals/Attendee> owl:sameAs <http://confOf#Participant>.\n" +
"        <http://muskca_evals/Event> owl:sameAs <http://confOf#Event>.\n" +
"\n" +
"        <http://muskca_evals/hasAuthor> owl:sameAs <http://confOf#writtenBy>.\n" +
"\n" +
"        <http://muskca_evals/Review> owl:sameAs <http://edas#Review>.\n" +
"        <http://muskca_evals/Person> owl:sameAs <http://edas#Person>.\n" +
"        <http://muskca_evals/Attendee> owl:sameAs <http://edas#Attendee>.\n" +
"        <http://muskca_evals/Paper> owl:sameAs <http://edas#Paper>.\n" +
"        <http://muskca_evals/Reception> owl:sameAs <http://edas#Reception>.\n" +
"        <http://muskca_evals/Location> owl:sameAs <http://edas#Place>.\n" +
"        <http://muskca_evals/Chair_PC> owl:sameAs <http://edas#SessionChair>.\n" +
"        <http://muskca_evals/Conference> owl:sameAs <http://edas#Conference>.\n" +
"        <http://muskca_evals/Author> owl:sameAs <http://edas#Author>.\n" +
"        <http://muskca_evals/Trip> owl:sameAs <http://edas#Excursion>.\n" +
"\n" +
"          <http://muskca_evals/hasAuthor> owl:sameAs <http://edas#isWrittenBy>.\n" +
"\n" +
"          <http://muskca_evals/Speaker> owl:sameAs <http://sigkdd#Speaker>.\n" +
"        <http://muskca_evals/Conference> owl:sameAs <http://sigkdd#Conference>.\n" +
"        <http://muskca_evals/ProgramComitee> owl:sameAs <http://sigkdd#Program_Committee>.\n" +
"        <http://muskca_evals/Person> owl:sameAs <http://sigkdd#Person>.\n" +
"        <http://muskca_evals/Review> owl:sameAs <http://sigkdd#Review>.\n" +
"        <http://muskca_evals/Author> owl:sameAs <http://sigkdd#Author>.\n" +
"        <http://muskca_evals/Location> owl:sameAs <http://sigkdd#Place>.\n" +
"        <http://muskca_evals/Paper> owl:sameAs <http://sigkdd#Paper>.\n" +
"\n" +
"         <http://muskca_evals/hasAuthor> owl:sameAs <http://sigkdd#submit>.\n" +
"}";
    }
    
   
}
