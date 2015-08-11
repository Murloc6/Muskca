/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Alignment;

import MultiSources.Fusionner;
import Source.Source;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author murloc
 */
public abstract class Aligner implements Serializable
{
    
    protected Source s1, s2;
    
    /*protected HashMap<String, ArrayList<Alignment>> s1Aligns;
    protected HashMap<String, ArrayList<Alignment>> s2Aligns;*/
    
    protected HashMap<String, ArrayList<Alignment>> indAligns;
    protected HashMap<String, ArrayList<Alignment>> classAligns;
    
    protected Fusionner fusionner;
    
    protected boolean isAligned;
    
    protected String stats;
    
    protected Alignments aligns;
    
    public Aligner(Fusionner fusionner, Source s1, Source s2, Alignments aligns)
    {
        this.isAligned = false;
      
        this.fusionner = fusionner;
        
        this.s1 = s1;
        this.s2 = s2;
        
        //this.s1Aligns = new HashMap<>();
        //this.s2Aligns = new HashMap<>();
        
        this.indAligns = new HashMap<>();
        this.classAligns = new HashMap<>();
        
        this.aligns = aligns;
    }
    
    public abstract void alignSources(float limitSimScore, String moduleFile);
    
    public int nbIndAligned()
    {
        return this.indAligns.size();
    }
    
    public int nbClassAligned()
    {
        return this.classAligns.size();
    }
    
    protected void addIndAlignment(String uri, String uriAlign, float value)
    {
        Alignment a = new Alignment (uri, uriAlign, value);
        ArrayList<Alignment> res = this.indAligns.get(uri);
        if(res == null)
        {
            res = new ArrayList<>();
            this.indAligns.put(uri, res);
        }
        res.add(a);
        
        this.s1.addIndAlignment(uri, a);
        this.s2.addIndAlignment(uriAlign, a);
        
        this.aligns.addAlignments(this.s1.getElem(uri), this.s2.getElem(uriAlign), a);
    }
    
     protected void addClassAlignment(String uri, String uriAlign, float value)
    {
        if(!uri.startsWith(fusionner.getUriTypeBase())){
            Alignment a = new Alignment (uri, uriAlign, value);
            ArrayList<Alignment> res = this.classAligns.get(uri);
            if(res == null)
            {
                res = new ArrayList<>();
                this.classAligns.put(uri, res);
            }
            res.add(a);

            this.s1.addClassAlignment(uri, a);
            this.s2.addClassAlignment(uriAlign, a);
            this.aligns.addAlignments(this.s1.getElem(uri), this.s2.getElem(uriAlign), a);
        }
    }
    
     public StringBuilder getPrologIndAligns()
     {
        StringBuilder mappingString = new StringBuilder();
        for(Map.Entry<String, ArrayList<Alignment>> e : this.indAligns.entrySet())
        {
            ArrayList<Alignment> as = e.getValue();
            for(Alignment a : as)
            {
                mappingString = mappingString.append(a.toPrologData()).append(" \n");
            }
        }
        return mappingString;
     }
     
     public StringBuilder getPrologClassAligns()
     {
        StringBuilder mappingString = new StringBuilder();
        for(Map.Entry<String, ArrayList<Alignment>> e : this.classAligns.entrySet())
        {
            ArrayList<Alignment> as = e.getValue();
            for(Alignment a : as)
            {
                mappingString = mappingString.append(a.toPrologData()).append("\n");
            }
        }
        return mappingString;
     }
     
    
    /*protected String sortAligns(HashMap<String, ArrayList<Alignment>> aligns)
    {
        String outAlign = "Alignments : \n";
        for(Map.Entry<String, ArrayList<Alignment>> e : aligns.entrySet())
        {
            ArrayList<Alignment> as = e.getValue();
            Collections.sort(as, new AlignmentComparator());
            outAlign += e.getKey()+ " -> \n";
            for(Alignment a : as)
            {
                outAlign += "\t"+a.toString()+"\n";
            }
            outAlign += "---------------";
        }
        return outAlign;
    }*/

    /*public void fusionAlignmentsCandidate()
    {
        if(this.isAligned)
        {
            //System.out.println(this.s1Aligns);
           for(Entry<String, ArrayList<Alignment>> e: this.indAligns.entrySet())
           {
               for(Alignment a : e.getValue())
               {
                   this.fusionner.addAlignmentCandidateSP(a, this.s1, this.s2);
               }
           }
        }
    }
    
    public void fusionClassAlignmentsCandidate()
    {
        if(this.isAligned)
        {
           for(Entry<String, ArrayList<Alignment>> e: this.classAligns.entrySet())
           {
               for(Alignment a : e.getValue())
               {
                   System.out.println("Add class align : "+a.getUri()+" / "+a.getUriAlign()+" -> "+a.getValue());
                   this.fusionner.addAlignmentClassCandidateSP(a, this.s1, this.s2);
               }
           }
        }
    }
    
    public void fusionAlignments()
    {
        this.fusionAlignmentsCandidate();
        this.fusionClassAlignmentsCandidate();
    }*/
    
  
    
    public Source getS1()
    {
        return this.s1;
    }
    
    public Source getS2()
    {
        return this.s2;
    }   
}
