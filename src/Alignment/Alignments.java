/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Alignment;

import Source.OntologicalElement.OntologicalElement;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author murloc
 */
public class Alignments 
{
    private HashMap<OntologicalElement, ArrayList<OntologicalElement>> aligns;
    private HashMap<String, Alignment> allAlignments;
    
    private String uriBase;
    
    public Alignments(String uriBase){
        this.uriBase = uriBase;
        this.aligns = new HashMap<>();
        this.allAlignments = new HashMap<>();
    }
    
    private void putAlignments(OntologicalElement oe1, OntologicalElement oe2, Alignment a){
        ArrayList<OntologicalElement> oe1Aligns = this.aligns.get(oe1);
        if(oe1Aligns == null){
            oe1Aligns = new ArrayList<>();
        }
        oe1Aligns.add(oe2);
        this.aligns.put(oe1, oe1Aligns);
        this.allAlignments.put(oe1.getUri()+oe2.getUri(), a);
    }
    
    public void addAlignments(OntologicalElement oe1, OntologicalElement oe2, Alignment a){
        if(oe1 != null && oe2 != null){
            this.putAlignments(oe1, oe2, a);
            this.putAlignments(oe2, oe1, a);
        }
    }
    
    public Alignment getAlignment(OntologicalElement oe1, OntologicalElement oe2){
        return this.allAlignments.get(oe1.getUri()+oe2.getUri());
    }
    
    public ArrayList<OntologicalElement> getNeighboors(OntologicalElement oe){
        ArrayList<OntologicalElement> ret = new ArrayList<>();
        
        if(this.aligns.containsKey(oe)){
            ret = this.aligns.get(oe);
        }
        
        return ret;
    }
    
    public Alignment getAlignment(String oe1oe2){
        return this.allAlignments.get(oe1oe2);
    }
    
}
