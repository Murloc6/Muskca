/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MultiSources.Solver.GLPK;

import Candidate.NodeCandidate.NodeCandidate;
import MultiSources.Extension;
import MultiSources.Fusionner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import muskca.FilePerso;
import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;

/**
 *
 * @author murloc
 */
public class ExtensionGlpkSolver extends GlpkSolver
{

    private HashMap<String, NodeCandidate> allCands;
    
    private float[] trust;
    private int[][] conflicts;
    private float[][] trustArc;
    private int[] defined;
    
    private String dataTemp="";
    
    
    public ExtensionGlpkSolver() 
    {
        super("solvers/glpk/additif.mod");
        this.allCands = new HashMap<>();
    }

    public void updateDataFile()
    {
        PrintWriter writer = null;
        try 
        {
            writer = new PrintWriter(this.dataFileName, "UTF-8");
            writer.println("data;");
            
            writer.print(this.dataTemp);
            
            writer.print("param defined:= ");
            for(int i = 1; i<= defined.length; i++)
            {
                writer.print(" "+i+" "+this.defined[i-1]);
            }
            writer.print(";\n");
            
            writer.println("end;");
            writer.close();
        } 
        catch (FileNotFoundException | UnsupportedEncodingException ex) 
        {
            System.err.println("Error during generate data file temp ..");
            System.exit(0);
        } 
        finally 
        {
            writer.close();
        }
    }
    
    public void generateDataFile()
    {
        File f = new FilePerso("out/temp/glpk_solver_data.dat");
        this.dataFileName = f.getAbsolutePath();

        this.dataTemp += "param n:="+trust.length+";\n";
        
        this.dataTemp += "param trust:= ";
        for(int i = 1; i<= trust.length; i++)
        {
            this.dataTemp += " "+i+" "+this.trust[i-1];
        }
        this.dataTemp+=";\n";

        this.dataTemp += "param conflict\n";
        this.dataTemp += "\t:";
        for(int i = 1; i<=trust.length; i++)
        {
            this.dataTemp += " "+i;
        }
        this.dataTemp += ":=\n";
        for(int i = 1; i<=trust.length; i++)
        {
            this.dataTemp += "\t"+i;
            for(int j = 1; j<= trust.length; j++)
            {
                this.dataTemp += " "+conflicts[i-1][j-1];
            }
            if(i == trust.length)
            {
                this.dataTemp+=";";
            }
            this.dataTemp += "\n";
        }
        
        this.dataTemp += "param trustArc\n";
        this.dataTemp += "\t:";
        for(int i = 1; i<=trust.length; i++)
        {
            this.dataTemp += " "+i;
        }
        this.dataTemp += ":=\n";
        for(int i = 1; i<=trust.length; i++)
        {
            this.dataTemp += "\t"+i;
            for(int j = 1; j<= trust.length; j++)
            {
                this.dataTemp += " "+trustArc[i-1][j-1];
            }
            if(i == trust.length)
            {
                this.dataTemp+=";";
            }
            this.dataTemp += "\n";
        }
    }
    
    public void initProblem(ArrayList<NodeCandidate> initCands, Fusionner fusionner) 
    {
        for(int i = 1; i<=initCands.size(); i++)
        {
            NodeCandidate nc = initCands.get(i-1);
            nc.setId(i);
            this.allCands.put(""+i, nc);
        }
        
        defined = new int[initCands.size()];
        Arrays.fill(defined, -1);
        
        
        /*conflicts[0][1] = 1; conflicts[1][0] = 1;
        conflicts[0][4] = 1; conflicts[4][0] = 1;
        conflicts[1][4] = 1; conflicts[1][4] = 1;
        conflicts[2][3] = 1; conflicts[3][2] = 1;
        conflicts[2][5] = 1; conflicts[5][2] = 1;
        conflicts[2][6] = 1; conflicts[6][2] = 1;
        conflicts[3][5] = 1; conflicts[5][3] = 1;
        conflicts[3][6] = 1; conflicts[6][3] = 1;
        conflicts[5][6] = 1; conflicts[6][5] = 1;
        conflicts[0][2] = 1; conflicts[2][0] = 1;
        conflicts[0][3] = 1; conflicts[3][0] = 1;
        conflicts[1][6] = 1; conflicts[6][1] = 1;
        conflicts[4][6] = 1; conflicts[6][4] = 1;
        conflicts[5][7] = 1; conflicts[7][5] = 1;
        conflicts[1][2] = 1; conflicts[2][1] = 1;
        conflicts[3][4] = 1; conflicts[4][3] = 1;
        conflicts[4][5] = 1; conflicts[5][4] = 1;*/
        int nbConflict = 0;
        int nbTotal = 0;
        trust = new float[initCands.size()];
        trustArc = new float[initCands.size()][initCands.size()];
        conflicts = new int[initCands.size()][initCands.size()];
        for(int[] a : conflicts)
        {
            Arrays.fill(a, 0);
        }
        for(int i = 0; i< initCands.size(); i++)
        {
            NodeCandidate nc = initCands.get(i);
            trust[i] = nc.getTrustScore()+nc.getSumArcCandIntr();
            for(int j = i+1; j < initCands.size(); j++)
            {
                NodeCandidate nc2 = initCands.get(j);
                trustArc[i][j] = nc.getSumArcCandImplied(nc2)+nc2.getSumArcCandImplied(nc);
                nbTotal ++;
                if(!nc.isCompatible(nc2))
                {
                    conflicts[i][j] = 1;
                    conflicts[j][i] = 1;
                    
                    nbConflict ++;
                }
            }
        }
        System.out.println("STATS : "+nbConflict+" / "+nbTotal);
        this.generateDataFile();
    }

    public void validateCandidate(NodeCandidate nc)
    {
        this.defined[nc.getId()-1] = 1;
        nc.validate();
    }
    
    public void unvalidateCandidate(NodeCandidate nc)
    {
        this.defined[nc.getId()-1] = 0;
    }
    
    
    @Override
    public Extension getSolution() 
    {
        Extension ext = new Extension();
        if(GLPK.glp_mip_status(lp) == GLPKConstants.GLP_OPT)
        {
            int n;
            String name;
            double val;
            n = GLPK.glp_get_num_cols(lp);
            for (int i = 1; i <= n; i++) {
                name = GLPK.glp_get_col_name(lp, i);
                //val = GLPK.glp_get_col_prim(lp, i);
                val = GLPK.glp_mip_col_val(lp, i);
                if(val > 0)
                {
                    Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]"); // all between bracket [] => get the id number of the selected candidate
                    Matcher matcher = pattern.matcher(name);
                    if (matcher.find())
                    {
                        ext.addNodeCandidate(this.allCands.get(matcher.group(1)));
                    }
                }
            }     
        }
        ext.sortCandidates();
        return ext;
    }
    
}
