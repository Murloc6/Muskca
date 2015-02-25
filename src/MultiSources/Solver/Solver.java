/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MultiSources.Solver;

import alice.tuprolog.InvalidTheoryException;
import alice.tuprolog.Library;
import alice.tuprolog.MalformedGoalException;
import alice.tuprolog.NoMoreSolutionException;
import alice.tuprolog.NoSolutionException;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Struct;
import alice.tuprolog.Theory;
import alice.tuprolog.UnknownVarException;
import alice.tuprolog.lib.InvalidObjectIdException;
import alice.tuprolog.lib.JavaLibrary;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author murloc
 */
public abstract class Solver 
{
    
    protected Prolog engine;
    
    
    protected final String dataFileName = "out/temp/data.ecl";
    
    public Solver(StringBuilder data)
    {
        try {
            this.engine = new Prolog();
            Library lib = this.engine.getLibrary("alice.tuprolog.lib.JavaLibrary");
            ((JavaLibrary)lib).register(new Struct("stdout"), System.out);
            
            try {
            Theory t = new Theory(new FileInputStream(this.solverFilerName()));
            
            this.saveDataToNewFile(data);
            Theory tdata = new Theory(new FileInputStream(this.dataFileName));
            
            this.engine.addTheory(t);
            this.engine.addTheory(tdata);
        } catch (IOException | InvalidTheoryException ex) {
            System.err.println("Error during the solver inititation...");
            System.exit(0);
        }
        } catch (InvalidObjectIdException ex) {
            Logger.getLogger(Solver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    abstract String solverFilerName();
    
    
    abstract ArrayList<ArrayList<String>> unify(String newCand, ArrayList<ArrayList<String>> sols);
    
    protected ArrayList<ArrayList<String>> getSolutions(String query, String varName, ArrayList<ArrayList<String>> curSols) throws MalformedGoalException
    {
        ArrayList<ArrayList<String>> ret = curSols;
        System.out.println("QUERY : "+query);
        SolveInfo si = engine.solve(query);
        if(si.isSuccess())
        {
            System.out.println("Solver found solution(s)");
            try
            {
                do
                {
                    String cand = si.getTerm(varName).toString();
                    ret = this.unify(cand, ret);
                    si = engine.solveNext();
                }while(engine.hasOpenAlternatives());
            }
            catch(NoSolutionException | UnknownVarException | NoMoreSolutionException e)
            {}

        }
        else
        {
            System.out.println("NO SOLUTIOn");
            System.out.println(si);
        }
        
        return ret;
    }
    
    
    protected boolean isThereSolution(String query)
    {
        boolean ret = false;
        try 
        {
            SolveInfo si = engine.solve(query);
            ret = si.isSuccess();
        } 
        catch (MalformedGoalException ex) {
            System.out.println("Error during determining if there is a solution");
        }
        
        
        return ret;
    }
    
    protected ArrayList<String> parseCand(String cand)
    {
        ArrayList<String> ret = new ArrayList<>();
        Pattern pattern = Pattern.compile("[\\\"\\']([^\\'\\\"]+)[\\\"\\']");
	Matcher matcher = pattern.matcher(cand);
	while (matcher.find()) 
        {
            ret.add(matcher.group(1));
	}
        
        return ret;
    }
    
    protected void saveDataToNewFile(StringBuilder data)
    {
        try 
        {
            File f = new File(this.dataFileName);
            FileUtils.deleteQuietly(f);
            FileUtils.write(f, data);
        } 
        catch (IOException ex) 
        {
            System.err.println("Error during writing the prolog data file ...");
            System.exit(0);
        }
    }
    
}
