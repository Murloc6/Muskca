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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author murloc
 */
public abstract class Aligner implements Serializable
{
    
    protected Source s1, s2;
    
    protected HashMap<String, ArrayList<Alignment>> s1Aligns;
    protected HashMap<String, ArrayList<Alignment>> s2Aligns;
    
    protected HashMap<String, ArrayList<Alignment>> classAligns;
    
    protected Fusionner fusionner;
    
    protected boolean isAligned;
    
    protected String stats;
    
    public Aligner(Fusionner fusionner, Source s1, Source s2)
    {
        this.isAligned = false;
      
        this.fusionner = fusionner;
        
        this.s1 = s1;
        this.s2 = s2;
        
        this.s1Aligns = new HashMap<>();
        this.s2Aligns = new HashMap<>();
        
        
        this.classAligns = new HashMap<>();
    }
    
    public abstract String alignSources(float limitSimScore);
    
    protected void addAlignment(String uri, String uriAlign, float value)
    {
        Alignment a = new Alignment (uri, uriAlign, value);
        ArrayList<Alignment> res = s1Aligns.get(uri);
        if(res == null)
        {
            res = new ArrayList<>();
            s1Aligns.put(uri, res);
        }
        res.add(a);

        /*Alignment a2 = new Alignment (uriAlign, labelAlign, uri, label, value);
        ArrayList<Alignment> res2 = s2Aligns.get(uriAlign);
        if(res2 == null)
        {
            res2 = new ArrayList<>();
            s2Aligns.put(uriAlign, res2);
        }
        res2.add(a2);*/
    }
    
     protected void addClassAlignment(String uri, String uriAlign, float value)
    {
        Alignment a = new Alignment (uri, uriAlign, value);
        ArrayList<Alignment> res = this.classAligns.get(uri);
        if(res == null)
        {
            res = new ArrayList<>();
            this.classAligns.put(uri, res);
        }
        res.add(a);
    }
    
    
    protected String sortAligns(HashMap<String, ArrayList<Alignment>> aligns)
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
    }

    public void fusionAlignmentsCandidate()
    {
        if(this.isAligned)
        {
            System.out.println(this.s1Aligns);
           for(Entry<String, ArrayList<Alignment>> e: this.s1Aligns.entrySet())
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
    
    public Source getS1()
    {
        return this.s1;
    }
    
    public Source getS2()
    {
        return this.s2;
    }   
}
