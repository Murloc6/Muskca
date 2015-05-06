/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MultiSources.Solver.Prolog;

import Candidate.NodeCandidate.NodeCandidate;
import MultiSources.Extension;
import MultiSources.Fusionner;
import alice.tuprolog.MalformedGoalException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author murloc
 */
public class ExtensionSolver extends Solver
{
   
    public ExtensionSolver(StringBuilder data)
    {
        super(data);
    }
    
    
    public ArrayList<Extension> getAllExtensions(int nbMax, Fusionner f)
    {
        ArrayList<ArrayList<String>> extsPrePars = new ArrayList<>();
        
        try {
            for(int i = nbMax; i>0; i++ )
            {
                extsPrePars = this.getSolutions("extension(X, "+i+").", "X", extsPrePars);
            }
            //ret = this.getSolutions("candidat(X).", "X");
        } catch (MalformedGoalException ex) {
            Logger.getLogger(ExtensionSolver.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ArrayList<Extension> ret = new ArrayList<>();
        for(ArrayList<String> extPrePars : extsPrePars)
        {
            Extension ext = new Extension();
            for(String s : extPrePars)
            {
                ArrayList<String> candUris = this.parseCand(s);
                NodeCandidate nc = f.getCandidateFromUris(candUris);
                if(nc != null)
                {
                    ext.addNodeCandidate(nc);
                }
                else
                {
                    System.err.println("Error during retrieving node candidate from uris : "+s);
                }
            }
            ret.add(ext);
            
        }
        
        
        
        return ret;
    }

    public int getMaxExtSize(int nbCand)
    {
        int i = 0;
        boolean isThereSolution = true;
        System.out.println("Start compute max nb cand for extensions");
        while(isThereSolution && i <= nbCand)
        {
            i++;
            isThereSolution = this.isThereSolution("test_extension(X, "+i+").");
        }
        i--;
        
        return i;
    }
    
    @Override
    String solverFilerName() {
        return "solvers/extension.ecl";
    }

    @Override
    ArrayList<ArrayList<String>> unify(String newExt, ArrayList<ArrayList<String>> sols) 
    {
        ArrayList<String> extension = new ArrayList<>();
        
        Pattern patternExt = Pattern.compile("");
        Matcher matcherExt = patternExt.matcher(newExt);
        
        
        Pattern pattern = Pattern.compile("(\\[(\\'([^\\']+)\\',?)+\\])");
	Matcher matcher = pattern.matcher(newExt);
	while (matcher.find()) 
        {
            extension.add(matcher.group(1));
	}
	boolean isAlreadyInList = false;
        int i = 0;
        while(!isAlreadyInList && i < sols.size())
        {
            ArrayList<String> eOld = sols.get(i);
            isAlreadyInList = eOld.containsAll(extension);
            i++;
        }
        if(!isAlreadyInList)
        {
            sols.add(extension);
            System.out.println("New extension (ext num "+sols.size()+" with "+extension.size()+" candidates): \n"+extension.toString());
        }
        return sols;
    }
    
}
