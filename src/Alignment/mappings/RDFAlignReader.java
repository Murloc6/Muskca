/*******************************************************************************
 * Copyright 2012 by the Department of Computer Science (University of Oxford)
 * 
 *    This file is part of LogMap.
 * 
 *    LogMap is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 * 
 *    LogMap is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 * 
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with LogMap.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package Alignment.mappings;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;


/**
 * This class transforms a RDF alignment (XML) file in a set of 
 * MappingObjectStr objects.
 * 
 * @author Ernesto
 *
 */
public class RDFAlignReader extends MappingsReader {

        public static final String CELL = "Cell";
        /*public static final String ENTITY1 = "entity1";
        public static final String ENTITY2 = "entity2";
        public static final String RELATION = "relation";
        public static final String MEASURE = "measure";*/
      //DIR IMPLICATION
        public static final int L2R=0; //P->Q
        public static final int R2L=-1; //P<-Q
        public static final int EQ=-2; //P<->Qversion = Utilities.LOGMAPMENDUM;
        public static final int NoMap=-3; 
        
        //Used in old gold standards
        private static final String ALIGNMENTENTITY1="alignmententity1";
        private static final String ALIGNMENTENTITY2 ="alignmententity2";
                
        private static final String ALIGNMENTRELATION="alignmentrelation";
        private static final String ALIGNMENTMEASURE="alignmentmeasure";
        
        
        
        
        
        public RDFAlignReader(String rdf_alignment_file) throws Exception {
                
                File xmlFile = new File(rdf_alignment_file);
                InputStream is = new FileInputStream(xmlFile);
                XMLInputFactory factory = XMLInputFactory.newInstance();
                XMLStreamReader reader = factory.createXMLStreamReader(is);
                
                mappings.clear();
                
                String iri_str1="";
                String iri_str2="";
                String relation="";
                double confidence=0.0;
                int dir_relation;
                
                //int next=0;
                while(reader.hasNext())
                {       
                        if(reader.getEventType()==XMLStreamConstants.START_ELEMENT){

                                //System.out.println(next++);
        
                                if (reader.hasName()){
                                
                                 if (reader.getLocalName().equals(RDFAlignReader.CELL)){
                                                iri_str1="";
                                                iri_str2="";
                                                relation="";
                                                confidence=0.0;
                                 }
                                 else if (reader.getLocalName().equals(RDFAlignReader.ENTITY1) ||
                                                 reader.getLocalName().equals(RDFAlignReader.ALIGNMENTENTITY1)){
                                         
                                         if (reader.getAttributeCount()>0){
                                                //System.out.println("Att: " + reader.getAttributeValue(0));
                                                 iri_str1 = reader.getAttributeValue(0); 
                                         }
                                         
                                 }
                                 
                                 else if (reader.getLocalName().equals(RDFAlignReader.ENTITY2) ||
                                                 reader.getLocalName().equals(RDFAlignReader.ALIGNMENTENTITY2)){
                                         
                                         if (reader.getAttributeCount()>0){
                                                //System.out.println("Att: " + reader.getAttributeValue(0));
                                                iri_str2 = reader.getAttributeValue(0);         
                                         }
                                         
                                 }
                                 
                                 else if (reader.getLocalName().equals(RDFAlignReader.RELATION) ||
                                                 reader.getLocalName().equals(RDFAlignReader.ALIGNMENTRELATION)){
                                         
                                         //System.out.println("TExt: " + reader.getElementText());
                                         relation = reader.getElementText();
                                         
                                 }
                                 
                                 else if (reader.getLocalName().equals(RDFAlignReader.MEASURE) ||
                                                 reader.getLocalName().equals(RDFAlignReader.ALIGNMENTMEASURE)){
                                         
                                         //System.out.println("TExt: " + reader.getElementText());
                                         confidence = Double.valueOf(reader.getElementText());
                                         
                                 }
                                
                        }
                            
                            
                        }
                        else if(reader.getEventType()==XMLStreamConstants.END_ELEMENT){
                        
                                 if (reader.hasName()){
                                         
                                         if (reader.getLocalName().equals(RDFAlignReader.CELL)){
                                                  
                                                  /*System.out.println(next++);
                                                  System.out.println(iri_str1);
                                                  System.out.println(iri_str2);
                                                  System.out.println(relation);
                                                  System.out.println(confidence);*/
                                                  
                                                  
                                                  if (relation.equals(">")){
                                                          dir_relation = R2L;
                                                          //System.out.println("R2L");
                                                  }
                                                  else if (relation.equals("<")){
                                                          dir_relation = L2R;
                                                          //System.out.println("L2R");
                                                  }
                                                  else { //Any other case: ie Hertuda doe snot use "="
                                                          dir_relation = EQ;
                                                          //System.out.println("=");
                                                  }
                                                        
                                                  mappings.add(new MappingObjectStr(iri_str1, iri_str2, confidence, dir_relation));
                                                  
                                          }
                                 }
                                 
                                 //Add to object if everything is ok!
                                
                        }
                        
                    
                    
                    reader.next();
                }//end while

                System.out.println("Read RDF Align mapping objects: " + getMappingObjectsSize());
                
        }
        
        
        
        
        
    /*    public static void main(String[] args) {
        
                String mappings_path = "/usr/local/data/DataUMLS/UMLS_Onto_Versions/OAEI_datasets/Mappings_Tools_2012/";
                
                
                try{
                        new RDFAlignReader(mappings_path + "logmap_small_fma2nci_new.rdf");
                }
                catch (Exception e){
                        e.printStackTrace();
                }
        }*/

}