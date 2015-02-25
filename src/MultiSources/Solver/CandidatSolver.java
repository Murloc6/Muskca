package MultiSources.Solver;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import Alignment.Alignment;
import Candidate.NodeCandidate.ClassCandidate;
import Candidate.NodeCandidate.IndividualCandidate;
import MultiSources.Fusionner;
import Source.Source;
import alice.tuprolog.MalformedGoalException;
import alice.tuprolog.NoSolutionException;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.UnknownVarException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author murloc
 */
public class CandidatSolver extends Solver
{
    
    public CandidatSolver(StringBuilder data)
    {
        super(data);
    }
    
    
    public ArrayList<IndividualCandidate> getAllIndCandidates(Fusionner f, int nbSources)
    {
        ArrayList<IndividualCandidate> ret = new ArrayList<>();
        
        try {
                ArrayList<ArrayList<String>> candPrePars = new ArrayList<>();
                for(int i = nbSources; i>1; i--)
                {
                    candPrePars = this.getSolutions("candidat(X,"+i+").", "X", candPrePars);
                }
                
                for(ArrayList<String> candPre : candPrePars)
                {
                    IndividualCandidate ic = new IndividualCandidate();
                    ArrayList<Alignment> aligns = new ArrayList<>();
                    for(int i = 0; i < candPre.size(); i++)
                    {
                        String uriCand = candPre.get(i);
                        String sourceName = this.getSourceNameFromUri(uriCand);
                        Source s = f.getSourceByName(sourceName);
                        ic.addElem(s, uriCand);
                        for(int j = i+1; j< candPre.size(); j++)
                        {
                            Alignment a = s.getAlignmentInd(uriCand, candPre.get(j));
                            if(a != null && !aligns.contains(a))
                            {
                                aligns.add(a);
                                ic.addAlignment(a);
                            }
                        }
                    }
                    
                    ret.add(ic);
                }
                
                
        } catch (MalformedGoalException ex) {
            Logger.getLogger(CandidatSolver.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return ret;
    }
    
    
    private String getSourceNameFromUri(String uri)
    {
        String ret = null;
        Boolean isOk = false;
        try 
        {
            String query = "source_elem(S, \""+uri+"\").";
            SolveInfo si = engine.solve(query);
            if(si.isSuccess())
            {
                ret = si.getTerm("S").toString();
                isOk = true;
            }
            else
            {
                System.out.println("NO SOLUTIOn");
                System.out.println(si);
            }
        } catch (MalformedGoalException ex) {
        } catch (NoSolutionException ex) {
        } catch (UnknownVarException ex) {
        }
        if(!isOk)
        {
            System.err.println("Error during getting source name");
            System.exit(0);
        }
        return ret;
        
    }
    
    
    public ArrayList<ClassCandidate> getAllClassCandidates(Fusionner f)
    {
        ArrayList<ClassCandidate> ret = new ArrayList<>();
        
        try {
                ArrayList<ArrayList<String>> candPrePars = new ArrayList<>();
                candPrePars = this.getSolutions("candidat_max(X).", "X", candPrePars);
                
                for(ArrayList<String> candPre : candPrePars)
                {
                    ClassCandidate cc = new ClassCandidate();
                    ArrayList<Alignment> aligns = new ArrayList<>();
                    for(int i = 0; i < candPre.size(); i++)
                    {
                        String uriCand = candPre.get(i);
                        String sourceName = this.getSourceNameFromUri(uriCand);
                        Source s = f.getSourceByName(sourceName);
                        cc.addElem(s, uriCand);
                        for(int j = i+1; j< candPre.size(); j++)
                        {
                            Alignment a = s.getAlignmentClass(uriCand, candPre.get(j));
                            if(a != null && !aligns.contains(a))
                            {
                                aligns.add(a);
                                cc.addAlignment(a);
                            }
                        }
                    }
                    
                    ret.add(cc);
                }
                
        } catch (MalformedGoalException ex) {
            System.err.println("Error during getting all classes candidates");
        }
        
        return ret;
    }
    
    @Override
    ArrayList<ArrayList<String>> unify(String newCand, ArrayList<ArrayList<String>> sols)
    {
        ArrayList<String> cand = this.parseCand(newCand);
        
	boolean isAlreadyInList = false;
        int i = 0;
        while(!isAlreadyInList && i < sols.size())
        {
            ArrayList<String> cOld = sols.get(i);
            isAlreadyInList = cOld.containsAll(cand);
            i++;
        }
        if(!isAlreadyInList)
        {
            sols.add(cand);
        }
        return sols;
    }

    @Override
    String solverFilerName() {
        return "solvers/candidat.ecl";
    }
    
}
