

package Alignment;

import Alignment.mappings.MappingObjectStr;
import Alignment.mappings.RDFAlignReader;
import MultiSources.Fusionner;
import Source.Source;
import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import muskca.FilePerso;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
//import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

/**
 *
 * @author Murloc
 */
public class AlignerSeals extends Aligner
{
    private OWLOntology onto1;
    private OWLOntology onto2;
    private OWLOntologyManager onto_manager;
    private String url1_str;
    private String url2_str;
    private String aligner; //nom de l'aligner seals (ou nom du dossier contenu dans sealshome)
    private File f1;
    private File f2;

    
    public AlignerSeals(Fusionner fusionner, Source s1, Source s2, Alignments aligns) 
    {
        super(fusionner, s1, s2, aligns);
    }

    private String getAbsolutePathTemp(Source s)
    {
        File f = new FilePerso(s.getTempExport());
        return f.getAbsolutePath();
    }
    
    @Override
    public void alignSources(float limitSimScore) 
    {
        System.out.println("Initialize sources ("+this.s1.getName()+" / "+this.s2.getName()+") for LogMap ..."); 
        String fileNameS1 = this.s1.getTempExport();
        String fileNameS2 = this.s2.getTempExport();      
        
        int nbIndAlign = 0;
        int nbClassAlign = 0;
        try
        {
            
            this.f1 = new FilePerso ("out/temp/"+fileNameS1);
            this.f2 = new FilePerso("out/temp/"+fileNameS2);

            this.onto_manager = OWLManager.createOWLOntologyManager();

            this.onto1 = this.onto_manager.loadOntologyFromOntologyDocument(f1);
            this.onto2 = this.onto_manager.loadOntologyFromOntologyDocument(f2);
            try {
                this.url1_str = f1.toURI().toURL().toString();
                this.url2_str = f2.toURI().toURL().toString();
            } catch (MalformedURLException ex) {
                Logger.getLogger(AlignerSeals.class.getName()).log(Level.SEVERE, null, ex);
            }
	
            this.aligner = this.fusionner.getAligner();

            
            String date =  (new SimpleDateFormat("dd-MM_HH-mm").format(new Date()));
            String mappings_file_name ="out/temp/alignment_"+this.s1.getName()+"-"+this.s2.getName()+ "_"+date+".rdf";
            File fmap = new File(mappings_file_name);
           
            mappings_file_name=fmap.getAbsolutePath();
            this.alignement(aligner, url1_str, url2_str, mappings_file_name); //rdfalignement écrit dans le fichier sealshome/retour.rdf
            Set<MappingObjectStr> mappings = readAndCreateMappings(mappings_file_name); 
            
            for (MappingObjectStr mapping: mappings)
            {
                //System.out.println(mapping.getTypeOfMapping()+" -- "+mapping.getConfidence());
                if(mapping.getTypeOfMapping() == MappingObjectStr.INSTANCES && mapping.getConfidence() >= limitSimScore)
                {
                    this.addIndAlignment(mapping.getIRIStrEnt1(),mapping.getIRIStrEnt2(),(float)mapping.getConfidence());
                    //System.out.println("Alignment founded : "+mapping.getIRIStrEnt1()+" --> "+mapping.getIRIStrEnt2()+" ("+mapping.getConfidence()+")");
                    nbIndAlign ++;
                }
                else if(mapping.getTypeOfMapping() == MappingObjectStr.CLASSES && mapping.getConfidence() >= limitSimScore)
                {
                    String uri1 = mapping.getIRIStrEnt1();
                    String uri2 = mapping.getIRIStrEnt2();
                    if(! (uri1.startsWith("http://ontology.irstea.fr/AgronomicTaxon") && uri2.startsWith("http://ontology.irstea.fr/AgronomicTaxon")) && !(uri1.startsWith("http://www.w3.org/2004/02/skos/core") && uri2.startsWith("http://www.w3.org/2004/02/skos/core")))
                    {
                        //System.out.println("NEW CLASS ALIGN !!");
                        //System.out.println(mapping.toString());
                        //System.out.println("Alignment founded : "+mapping.getIRIStrEnt1()+" --> "+mapping.getIRIStrEnt2()+" ("+mapping.getConfidence()+")");
                        //System.out.println(" ----------- ");
                        nbClassAlign ++;
                        this.addClassAlignment(uri1, uri2,(float)mapping.getConfidence());
                    }
                }
            }

        }
        catch (OWLOntologyCreationException ex){
                 System.err.println("Error during the execution of LogMap : "+ex);
                 System.exit(0);
        }

        //this.sortAligns(this.s1Aligns);
        //this.sortAligns(this.s2Aligns);

        this.stats = "Nb alignments(LogMap) between "+this.s1.getName()+" and "+this.s2.getName()+" ==> "+nbIndAlign+" indv. "+nbClassAlign+" class.";
        this.isAligned = true;
            
        System.out.println(stats);
    }   
    
    /*Appel au jar SealsTest.jar qui lance l'aligneur choisi sur les onto
      choisies + définition de l'environnement du process et attente de sa fin*/
    private void alignement(String aligner, String url1_str, String url2_str, String mappings_file_name){
            try {

                ProcessBuilder pb =   new ProcessBuilder("java", "-jar", "SealsAligner.jar", aligner, url1_str, url2_str, mappings_file_name);
                Map<String, String> env = pb.environment();
                //System.out.println(aligner + " "+ url1_str+ " "+url2_str+ " "+ mappings_file_name);
                pb.directory(new File("aligners"));
                env.put("SEALS_HOME", pb.directory().getAbsolutePath());
                File log = new File("out/temp/log_aligner");
                //pb.redirectErrorStream(true);
                pb.inheritIO();
                pb.redirectOutput(Redirect.appendTo(log));
                System.out.println("------------------------------------------------------");
                System.out.println("Alignment of 2 sources : " + this.s1.getName() + " et " + this.s2.getName());
                Process p = pb.start();
                try {
                    p.waitFor();
                    System.out.println("------------------------------------------------------");
                } catch (InterruptedException ex) {
                    Logger.getLogger(AlignerSeals.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (IOException ex) {
                Logger.getLogger(AlignerSeals.class.getName()).log(Level.SEVERE, null, ex);
            }
	}
	
	/*Transformation du fichier RDFAlignment en un mapping*/

    private Set<MappingObjectStr> readAndCreateMappings(String mappings_file_name){
        Set<MappingObjectStr> mappings = null;
        try {
            RDFAlignReader RDF_mappings_reader;
            
            RDF_mappings_reader = new RDFAlignReader(mappings_file_name);
            mappings = RDF_mappings_reader.getMappingObjects();
            
            /*
            Sets the type of Mappings to INSTANCE or CLASS according to the type 
            of the elements in the ontology from which they come from
            */
            for(MappingObjectStr mapping : mappings){
                boolean probleme = false;
                
                //if it's a class
                if(onto1.containsClassInSignature(IRI.create(mapping.getIRIStrEnt1()))){
                    
                    if(onto2.containsClassInSignature(IRI.create(mapping.getIRIStrEnt2()))){
                        mapping.setTypeOfMapping(MappingObjectStr.CLASSES);
                    }
                    else{
                        probleme=true;
                    }
                    
                }
                
                //If it's an instance
                else if (onto1.containsIndividualInSignature(IRI.create(mapping.getIRIStrEnt1()))){
                   
                    if(onto2.containsIndividualInSignature(IRI.create(mapping.getIRIStrEnt2()))){
                        mapping.setTypeOfMapping(MappingObjectStr.INSTANCES);
                    }
                    else{
                        probleme=true;
                    }
                }
                else {
                    //System.out.println("Other type of mapping : not a class nor an instance");
                }
                
                
                if(probleme){
                    System.out.println("PB : elements of different types aligned");
                }
                
            }

        } catch (Exception ex) {
            Logger.getLogger(AlignerSeals.class.getName()).log(Level.SEVERE, null, ex);
        }
                    return mappings;

    }
	

}
