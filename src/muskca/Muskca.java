/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package muskca;

import Alignment.Aligner;
import Alignment.AlignerLogMap;
import MultiSources.Fusionner;
import Source.Source;
import Source.SparqlProxy;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author murloc
 */
public class Muskca 
{
    
    
    public static String uriMuskca = "http://muscka_system.fr/Alpha1/";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {  
         ArrayList<Source> sources = new ArrayList<>();
         //HashMap<String, String> sourcesAlignStats = new HashMap<>();
         
         ArrayList<String> urisRelImp = new ArrayList<>();
         urisRelImp.add("http://ontology.irstea.fr/AgronomicTaxon#hasHigherRank");
         
         ArrayList<String> urisLabelsImp = new ArrayList<>();
         urisLabelsImp.add("http://ontology.irstea.fr/AgronomicTaxon#hasScientificName");
         urisLabelsImp.add("http://ontology.irstea.fr/AgronomicTaxon#hasVernacularName");
         
         
         String projectName = "mini_Triticum";
         String mongoCollection  = "triticumCandidate"; // null for don't save candidates on mongoDB 
         //String mongoCollection  = null;
         String mongoCollectionICHR = "triticumICHR";
         String mongoCollectionType= "triticumTypeCandidate";
         String mongoCollectionLabel = "triticumLabelCandidate";
         
         String provoFile = "in/prov-o.owl";
         String adomFile = "in/agronomicTaxon.owl";
         String spOutProvo = "http://amarger.murloc.fr:8080/Muskca-provo/";
         String baseUriMuskca = "http://muskca_system.fr/"+projectName+"/";
         
        //Source sAgrovoc = new Source("Agrovoc", "http://aims.fao.org/aos/agrovoc/", "http://localhost:3030/Agrovoc2KB_OUT/");
         Source sAgrovoc = new Source("Agrovoc", "http://aims.fao.org/aos/agrovoc/", "http://amarger.murloc.fr:8080/Agrovoc_mini_Triticum/");
        //sAgrovoc.setScores(0.7f, 0.4f, 0.9f);
        sAgrovoc.setScores(1f, 1f, 1f);
        //sAgrovoc.setScores(0.4f, 0.4f, 0.4f);
        sources.add(sAgrovoc);
        System.out.println("Source : "+sAgrovoc.getName()+" added. SQ = "+sAgrovoc.getSourceQualityScore());
        
        //Source sTaxRef =  new Source("TaxRef", "http://inpn.mnhn.fr/espece/cd_nom/", "http://localhost:3030/TaxRef2RKB_out/");
        Source sTaxRef =  new Source("TaxRef", "http://inpn.mnhn.fr/espece/cd_nom/", "http://amarger.murloc.fr:8080/TaxRef_mini_Triticum/");
        //sTaxRef.setScores(0.6f, 0.9f, 0.9f);
        sTaxRef.setScores(1f, 1f, 1f);
        //sTaxRef.setScores(0.95f, 0.95f, 0.95f);
        sources.add(sTaxRef);
        System.out.println("Source : "+sTaxRef.getName()+" added. SQ = "+sTaxRef.getSourceQualityScore());
        
        //Source sNCBI = new Source("NCBI", "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=", "http://localhost:3030/Ncbi2RKB_out/");
        Source sNCBI = new Source("NCBI", "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=", "http://amarger.murloc.fr:8080/NCBI_mini_Triticum/");
        //sNCBI.setScores(0.9f, 0.6f, 0.9f);
        sNCBI.setScores(1f, 1f, 1f);
        //sNCBI.setScores(0.6f, 0.6f, 0.6f);
        sources.add(sNCBI);
        System.out.println("Source : "+sNCBI.getName()+" added. SQ = "+sNCBI.getSourceQualityScore());

         
         
//          String projectName = "mini_Aegilops";
//            String mongoCollection  = "aegilopsCandidate"; // null for don't save candidates on mongoDB 
//          String mongoCollectionICHR = "aegilopsICHR";
//           //Source sAgrovoc = new Source("Agrovoc", "http://aims.fao.org/aos/agrovoc/", "http://localhost:3030/Agrovoc2KB_OUT/");
//            Source sAgrovoc = new Source("Agrovoc", "http://aims.fao.org/aos/agrovoc/", "http://amarger.murloc.fr:8080/Agrovoc_mini_Aegilops/");
//            //sAgrovoc.setScores(0.4f, 0.4f, 0.4f);
//            sAgrovoc.setScores(0.7f, 0.4f, 0.9f);
//           sources.add(sAgrovoc);
//           System.out.println("Source : "+sAgrovoc.getName()+" added. SQ = "+sAgrovoc.getSourceQualityScore());
//
//           //Source sTaxRef =  new Source("TaxRef", "http://inpn.mnhn.fr/espece/cd_nom/", "http://localhost:3030/TaxRef2RKB_out/");
//           Source sTaxRef =  new Source("TaxRef", "http://inpn.mnhn.fr/espece/cd_nom/", "http://amarger.murloc.fr:8080/TaxRef_mini_Aegilops/");
//           //sTaxRef.setScores(0.9f, 0.9f, 0.9f);
//           sTaxRef.setScores(0.6f, 0.9f, 0.9f);
//           sources.add(sTaxRef);
//           System.out.println("Source : "+sTaxRef.getName()+" added. SQ = "+sTaxRef.getSourceQualityScore());
//
//           //Source sNCBI = new Source("NCBI", "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=", "http://localhost:3030/Ncbi2RKB_out/");
//           Source sNCBI = new Source("NCBI", "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=", "http://amarger.murloc.fr:8080/NCBI_mini_Aegilops/");
//           //sNCBI.setScores(0.6f, 0.6f, 0.6f);
//           sNCBI.setScores(0.9f, 0.6f, 0.9f);
//           sources.add(sNCBI);
//           System.out.println("Source : "+sNCBI.getName()+" added. SQ = "+sNCBI.getSourceQualityScore());
         
//         String projectName = "mini_Oryza";
//            String mongoCollection  = "oryzaCandidate"; // null for don't save candidates on mongoDB 
//
//           //Source sAgrovoc = new Source("Agrovoc", "http://aims.fao.org/aos/agrovoc/", "http://localhost:3030/Agrovoc2KB_OUT/");
//            Source sAgrovoc = new Source("Agrovoc", "http://aims.fao.org/aos/agrovoc/", "http://amarger.murloc.fr:8080/Agrovoc_mini_Oryza/");
//            sAgrovoc.setScores(0.4f, 0.4f, 0.4f);
//           sources.add(sAgrovoc);
//           System.out.println("Source : "+sAgrovoc.getName()+" added. SQ = "+sAgrovoc.getSourceQualityScore());
//
//           //Source sTaxRef =  new Source("TaxRef", "http://inpn.mnhn.fr/espece/cd_nom/", "http://localhost:3030/TaxRef2RKB_out/");
//           Source sTaxRef =  new Source("TaxRef", "http://inpn.mnhn.fr/espece/cd_nom/", "http://amarger.murloc.fr:8080/TaxRef_mini_Oryza/");
//           sTaxRef.setScores(0.9f, 0.9f, 0.9f);
//           sources.add(sTaxRef);
//           System.out.println("Source : "+sTaxRef.getName()+" added. SQ = "+sTaxRef.getSourceQualityScore());
//
//           //Source sNCBI = new Source("NCBI", "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=", "http://localhost:3030/Ncbi2RKB_out/");
//           Source sNCBI = new Source("NCBI", "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=", "http://amarger.murloc.fr:8080/NCBI_mini_Oryza/");
//           sNCBI.setScores(0.6f, 0.6f, 0.6f);
//           sources.add(sNCBI);
//           System.out.println("Source : "+sNCBI.getName()+" added. SQ = "+sNCBI.getSourceQualityScore());
         
          /*String projectName = "TEST_expes";
         
        //Source sAgrovoc = new Source("Agrovoc", "http://aims.fao.org/aos/agrovoc/", "http://localhost:3030/Agrovoc2KB_OUT/");
         Source sAgrovoc = new Source("S1", "http://s1.fr#", "http://amarger.murloc.fr:8080/S1/");
         sAgrovoc.setScores(0.7f, 0.55f, 0.64f);
        sources.add(sAgrovoc);
        System.out.println("Source : "+sAgrovoc.getName()+" added. SQ = "+sAgrovoc.getSourceQualityScore());
        
        //Source sTaxRef =  new Source("TaxRef", "http://inpn.mnhn.fr/espece/cd_nom/", "http://localhost:3030/TaxRef2RKB_out/");
        Source sTaxRef =  new Source("S2", "http://s2.fr#", "http://amarger.murloc.fr:8080/S2/");
        sTaxRef.setScores(0.6f, 0.65f, 0.54f);
        sources.add(sTaxRef);
        System.out.println("Source : "+sTaxRef.getName()+" added. SQ = "+sTaxRef.getSourceQualityScore());
        
        //Source sNCBI = new Source("NCBI", "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=", "http://localhost:3030/Ncbi2RKB_out/");
        Source sNCBI = new Source("S3", "http://s3.fr#", "http://amarger.murloc.fr:8080/S3/");
        sNCBI.setScores(0.8f, 0.8f, 0.57f);
        sources.add(sNCBI);
        System.out.println("Source : "+sNCBI.getName()+" added. SQ = "+sNCBI.getSourceQualityScore());*/
         
         
        Fusionner fusionner = new Fusionner( "http://amarger.murloc.fr:8080/tempAlign/");
        fusionner.initSources(sources);
        
        float trustIcMax = fusionner.getIcTrustMax();
        float trustRcMax = fusionner.getRcTrustMax();
        float trustTcMax = fusionner.getTcTrustMax();
        float trustLcMax = fusionner.getLcTrustMax();
        
        
        for(int i = 0; i< sources.size(); i++)
        {
            Source s1 = sources.get(i);
            System.out.println("Start analyze source : "+s1.getName()+" ("+(i+1)+"/"+sources.size()+")");
            for(int j = i+1; j< sources.size(); j++)
            {
                Source s2 = sources.get(j);
                
                System.out.println(s1.getName()+"/"+s2.getName()+ ": ");
                Aligner aligner = new AlignerLogMap(fusionner, s1, s2);
                //Aligner aligner = new AlignerTEST(fusionner, s1, s2);
                System.out.println("Aligner ended !");
                String stats = aligner.alignSources(0); // score min = 0 to keep all alignment (filter will be done by taken the first one)
                System.out.println(stats);
                aligner.fusionAlignmentsCandidate(); //TEST hypothesis
                System.out.println("Hypothesis updated");
            }
        }
        fusionner.computeInstanceCandidate(mongoCollection, trustIcMax);
        String retRelCandidate = "";
        for(String uriRel : urisRelImp)
        {
            fusionner.computeRelationCandidate(mongoCollectionICHR, uriRel, trustRcMax);
        }
        
        fusionner.computeTypeCandidate(mongoCollectionType, trustTcMax);
        fusionner.computeLabelCandidate(mongoCollectionLabel, urisLabelsImp, trustLcMax);
        
        String retFile = fusionner.allCandidatesToString();
        
        System.out.println("EXPORTING stats file ...");
         Muskca.exportFile(retFile, projectName+"_HypCandidate_stats.txt");
        //AlignRKBAgroTaxon.exportFile(fusionner.allCandidatesToCSV(), projectName+".csv");
        System.out.println("FILE PERSO EXPORTED");
        
        System.out.println("EXPORTING provo owl file ...");
        SparqlProxy spProvo = fusionner.allCandidatesToProvo(provoFile, spOutProvo, adomFile, baseUriMuskca);
        //Muskca.exportFile(spProvo., projectName+"_CandProvo.owl");
        spProvo.writeKBFile(projectName+"_CandProvo.owl");
        System.out.println("FILE PROVO EXPORTED");
        
        System.out.println("NB SAVED ON MONGO : "+fusionner.nbMongoSaved);
        
     }
    
    
    private static void exportFile(String content, String projectName)
    {
        try 
        { 
            FileUtils.cleanDirectory(new File("out/temp/"));
        } 
        catch (IOException ex) 
        {
            System.err.println("Error during cleaning the out/temp/ directory");
        }
        String dateFileName = new SimpleDateFormat("dd-MM_HH-mm_").format(new Date());
        try {
            FileUtils.write(new File("out/"+dateFileName+"_Fusion_"+projectName), content);
        } catch (IOException ex) {
            //System.out.println(triplesFusionStats);
            System.out.println("ERROR DURING OUTPUT FILE");
        }
    }
}
