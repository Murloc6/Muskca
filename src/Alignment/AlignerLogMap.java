

package Alignment;

import MultiSources.Fusionner;
import Source.Source;
import java.io.File;
import java.util.Set;
import muskca.FilePerso;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

/**
 *
 * @author Murloc
 */
public class AlignerLogMap extends Aligner
{
  
    
    
    public AlignerLogMap(Fusionner fusionner, Source s1, Source s2, Alignments aligns) 
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
        OWLOntology onto1;
        OWLOntology onto2;
        OWLOntologyManager onto_manager;
        int nbIndAlign = 0;
        int nbClassAlign = 0;
        try
        {
            File f1 = new FilePerso ("out/temp/"+fileNameS1);
            File f2 = new FilePerso("out/temp/"+fileNameS2);

            onto_manager = OWLManager.createOWLOntologyManager();

            onto1 = onto_manager.loadOntologyFromOntologyDocument(f1);
            onto2 = onto_manager.loadOntologyFromOntologyDocument(f2);

            LogMap2_Matcher logmap2 = new LogMap2_Matcher(onto1, onto2);
            Set<MappingObjectStr> logmap2_mappings = logmap2.getLogmap2_Mappings();
            System.out.println("Number of mappings computed by LogMap: " + logmap2_mappings.size());

            for (MappingObjectStr mapping: logmap2_mappings)
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
    
    
    
}
