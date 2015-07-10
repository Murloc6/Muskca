/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MultiSources;

import Alignment.Aligner;
import Alignment.AlignerLogMap;
import Alignment.Alignments;
import Alignment.StringDistance;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import muskca.FilePerso;
import muskca.Muskca;
import org.apache.commons.io.IOUtils;

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
    
    
    public Fusionner(ArrayList<Source> sources, ArrayList<String> urisLabelsImp, ArrayList<String> urisRelImp, String uriTypeBase, ArrayList<String> urisTypeImp)
    {
        
        
        this.urisLabelsImp = urisLabelsImp;
        this.urisRelImp = urisRelImp;
        this.uriTypeBase = uriTypeBase;
        this.urisTypeImp = urisTypeImp;
        
        this.aligns = new Alignments(this.uriTypeBase);
        
        this.sources = sources;
        
        this.allNodeCands = new ArrayList<>();
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
                Aligner aligner = new AlignerLogMap(this, s1, s2, this.aligns);
                System.out.println("Aligner ended !");
                aligner.alignSources(0); // score min = 0 to keep all alignment (filter will be done by taken the first one)
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
            nc.computeTrustScore(nbSources, maxSourceQual);
        }
    }
    
    public float getSumArcTrust(NodeCandidate nc1, NodeCandidate nc2)
    {
        float ret = 0;
        
        
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
    
    public SparqlProxy extCandidatesToProvo(Extension ext, String provoFile, String provoSpOut, String adomFile, String baseUri)
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
        
        for(NodeCandidate nc : ext.getCandidates())
        {
            spOutProvo.storeData(new StringBuilder(this.setPrefix()+" INSERT DATA {"+nc.toProvO(baseUri, numInst, provoSourceUri, uriKbMerge)+"}"));
            numInst ++;
        }
        
        return spOutProvo;
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
        String relImp = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
        int nbtc = 0;
        for(NodeCandidate nc : this.allNodeCands)
        {
            if(nc.getClass() == ClassCandidate.class){
                break;
            }
            IndividualCandidate ic = (IndividualCandidate) nc;
            boolean relValid = false;
            for(Entry<Source, OntologicalElement> e : ic.getUriImplicate().entrySet())
            {
                boolean founded = false;
                ArrayList<String> urisType = e.getKey().getRelImportant(e.getValue().getUri(), relImp);
                for(String uriTypeCandidate : urisType)
                {
                    //System.out.println(uriTypeCandidate);
                    if(uriTypeCandidate.startsWith(this.uriTypeBase))
                    {
                        HashMap<Source, String> uriType = new HashMap<>();
                        uriType.put(e.getKey(), e.getValue()+" "+relImp+" "+uriTypeCandidate);
                        relValid = true;
                        //System.out.println("TEST : "+uriTypeCandidate);
                        for(Entry<Source, OntologicalElement> elem : ic.getUriImplicate().entrySet())
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
                            TypeCandidate tc = new TypeCandidate(ic, uriTypeCandidate);
                            for(Entry<Source, String> eUriType : uriType.entrySet())
                            {
                                tc.addElem(eUriType.getKey(), eUriType.getValue());
                            }
                            if(ic.addTypeCandidate(tc))
                                nbtc ++;
                        }
                    }
                }
                if(founded)
                {
                    break;
                }
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
   
    
   
}
