/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package muskca;

import Candidate.NodeCandidate.NodeCandidate;
import MultiSources.Extension;
import MultiSources.Fusionner;
import MultiSources.NodeCandidateGenerator;
import MultiSources.Solver.GLPK.ExtensionGlpkSolver;
import Source.Source;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author murloc
 */
public class Muskca 
{
    public static String muskcaVersion = "V1";
    public static String dateBegin = "";
    public static String dateEnd = "";
    
    public static String configFile = "in/muskca_params_Triticum.json";
    
    
    public static Fusionner init()
    {
        cleanTempDirectory();
        Muskca.dateBegin = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
        
        ParamsReader params = new ParamsReader(configFile);
        
        String projectName = params.getParam("projectName");
        String moduleFile = params.getParam("moduleFile");
        ArrayList<Source> sources = params.getSources();
        ArrayList<String> urisRelImp = params.getArrayParams("relImps");
        ArrayList<String> urisLabelsImp = params.getArrayParams("labelRelImps");
         

        HashMap<String, String> outputParams = params.getSubParams("output");
        String provoFile = outputParams.get("provoFile");
        String spOutProvo = outputParams.get("spOutProvo");
        String baseUriMuskca = outputParams.get("baseUri")+projectName+"/";
        
        String uriTypeBase = params.getParam("uriTypeBase");
        ArrayList<String> urisTypeImp = params.getArrayParams("uriTypeImps");
        
        String aligner = params.getParam("aligner");
        System.out.println("ALIGNER : " + aligner);
        
        Fusionner fusionner = new Fusionner(sources, urisLabelsImp, urisRelImp, uriTypeBase, urisTypeImp, aligner);
        
        System.out.println("Start filling sources");
        fusionner.setElemsOnSources();
        System.out.println("Sources filled");
        
        return fusionner;
    }
    
    
    public static void alignSources(Fusionner fusionner){
        fusionner.alignSources();
    }
    
   
    public static ArrayList<NodeCandidate> computeNodeCandidates(Fusionner fusionner){
        ArrayList<NodeCandidate> ret = new ArrayList<>();
        
        System.out.println("Start computing Node Candidates...");
        NodeCandidateGenerator ncg = new NodeCandidateGenerator(fusionner.getSources(), fusionner.getAlignments());
        ret = ncg.generateNodeCandidates();
        fusionner.setNodeCands(ret);
        
        System.out.println("Node candidates computed! ("+ret.size()+" candidates generated)");
        
        return ret;
    }
    
    public static void computeArcCandidates(Fusionner fusionner)
    {
        /**
         * -> Add some other kind of arcs to be generated
         */
        
        System.out.println("Compute labels ... ");
        fusionner.computeLabelCandidate();
        System.out.println("Labels computed!");
        
        
        for(String rel : fusionner.getImpRels())
        {
            System.out.println("Compute Arc candidates from "+rel+" ...");
            fusionner.computeRelationCandidate(rel);
            System.out.println(rel+" arc candidates computed!");
        }
        
        System.out.println("Compute rdf:type candidates");
        //recheck the compute type algorithm
        fusionner.computeTypeCandidate();
        System.out.println("Candidates computed");
        
    }
    
    public static void computeTrustScore(Fusionner fusionner)
    {
        System.out.println("Start computing trust scores...");
        fusionner.computeTrustScore();
        System.out.println("Trust scores computed");
    }
    
    public static ExtensionGlpkSolver getExtensionSolver(Fusionner fusionner, ArrayList<NodeCandidate> allCands)
    {
        return fusionner.initSolver(allCands);
    }
    
    public static Extension getNextExtension(Fusionner fusionner, ExtensionGlpkSolver solver)
    {
        return fusionner.getNextExtension(solver);
    }
    
    public static void validateCandidate(ExtensionGlpkSolver solver, NodeCandidate nc)
    {
        solver.validateCandidate(nc);
    }
    
     public static void unvalidateCandidate(ExtensionGlpkSolver solver, NodeCandidate nc)
    {
        solver.unvalidateCandidate(nc);
    }
    
    private static Extension getValidatedExtension(Fusionner fusionner, ArrayList<NodeCandidate> allCands)
    {
        ExtensionGlpkSolver solver = Muskca.getExtensionSolver(fusionner, allCands);
        Extension extOpti = null;
        Extension extCur = fusionner.getNextExtension(solver);
        boolean validatedExtensionFounded = false;
        while(extCur != null && !validatedExtensionFounded)
        {            
            int i = 0;
            for(NodeCandidate nc : extCur.getCandidates())
            {
                if(nc.isValidated())
                {
                    i++;
                }
                else
                {
                    System.out.println("New candidate to validate : ");
                    System.out.println(nc);
                    Scanner sc = new Scanner(System.in);
                    String rep = "";
                    while(rep.compareToIgnoreCase("y") != 0 && rep.compareToIgnoreCase("n") != 0)
                    {
                        System.out.println("\"y\" : accept || \"n\" : not accept \n response? :");
                        rep = sc.next();
                    }
                    boolean validated = (rep.compareToIgnoreCase("y") == 0);

                    if(validated)
                    {
                        i++;
                        solver.validateCandidate(nc);
                        nc.validate();
                    }
                    else
                    {
                        solver.unvalidateCandidate(nc);
                        break;
                    }
                }
            }
            if(i == extCur.getCandidates().size())
            {
                validatedExtensionFounded = true;
                extOpti = extCur;
            }
            else
            {
                extCur = fusionner.getNextExtension(solver);
            }
        }
        return extOpti;
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {   
       
        
        Fusionner fusionner = Muskca.init();
        
        Muskca.alignSources(fusionner);
        
        ArrayList<NodeCandidate> allCands = Muskca.computeNodeCandidates(fusionner);
        
        Muskca.computeArcCandidates(fusionner);
        
        Muskca.computeTrustScore(fusionner);
        
        Extension ext = Muskca.getValidatedExtension(fusionner, allCands);
        if(ext != null)
        {
            System.out.println("Solution founded! ");
            System.out.println(ext);
        }
        else
        {
            System.out.println("Error, no extension available with these constraints...");
        }
        System.exit(0);
        
       
        
        /*Muskca.dateEnd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
        Muskca.exportFile(extExport, projectName+"_Extensions_data.txt");
        Muskca.exportFile(extPl, projectName+"_Extensions_data.ecl");
        System.out.println("End treatment ("+extensions.size()+" extensions generated)");
        System.out.println(Muskca.dateBegin+" ----> "+Muskca.dateEnd);
        System.exit(0);
        
        String retRelCandidate = "";
        for(String uriRel : urisRelImp)
        {
            fusionner.computeRelationCandidate(mongoDbs.get("objPropArcCandidateMongoCol"), uriRel);
        }
        fusionner.computeTypeCandidate(mongoDbs.get("typeArcCandidateMongoCol"));
        fusionner.computeLabelCandidate(mongoDbs.get("labelArcCandidateMongoCol"), urisLabelsImp);
        
        
        System.out.println("NB SAVED ON MONGO : "+fusionner.nbMongoSaved);
        
        Muskca.dateEnd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());*/
        
        
        /*String retClassFile = fusionner.allClassCandidatesToString();
        System.out.println("EXPORTING class stats file ...");
         Muskca.exportFile(retClassFile, projectName+"_HypClassCandidate_stats.txt");
        //AlignRKBAgroTaxon.exportFile(fusionner.allCandidatesToCSV(), projectName+".csv");
        System.out.println("FILE CLASS PERSO EXPORTED");
        
        String retFile = retClassFile+"\n\n"+fusionner.allCandidatesToString();
        System.out.println("EXPORTING stats file ...");
         Muskca.exportFile(retFile, projectName+"_HypCandidate_stats.txt");
        //AlignRKBAgroTaxon.exportFile(fusionner.allCandidatesToCSV(), projectName+".csv");
        System.out.println("FILE PERSO EXPORTED");*/
        
       /* System.out.println("EXPORTING provo owl file ...");
        SparqlProxy spProvo = fusionner.allCandidatesToProvo(provoFile, spOutProvo, adomFile, baseUriMuskca);
        //Muskca.exportFile(spProvo., projectName+"_CandProvo.owl");
        spProvo.writeKBFile(projectName+"_CandProvo");
        System.out.println("FILE PROVO EXPORTED");*/
        
     }
    
    
    private static void cleanTempDirectory()
    {
        try 
        { 
            File f = new FilePerso("out/temp/");
            System.out.println(f.getAbsolutePath());
            FileUtils.cleanDirectory(f);
        } 
        catch (IOException ex) 
        {
            System.err.println("Error during cleaning the out/temp/ directory");
        }
    }
    
    private static void exportFile(String content, String projectName)
    {
        cleanTempDirectory();
        String dateFileName = new SimpleDateFormat("dd-MM_HH-mm_").format(new Date());
        try {
            FileUtils.write(new FilePerso("out/"+dateFileName+"_Fusion_"+projectName), content);
        } catch (IOException ex) {
            //System.out.println(triplesFusionStats);
            System.out.println("ERROR DURING OUTPUT FILE");
        }
    }
}
