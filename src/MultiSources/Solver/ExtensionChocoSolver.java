/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MultiSources.Solver;

import Candidate.NodeCandidate.NodeCandidate;
import MultiSources.Extension;
import MultiSources.Fusionner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.LCF;
import org.chocosolver.solver.search.solution.ISolutionRecorder;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;

/**
 *
 * @author Administrateur
 */
public class ExtensionChocoSolver {
      
    private float[] trust;
    private int[][] conflicts;
    private float[][] trustArc;
    private int[] defined;
    private int[]candidatSeul;
    
    
    public ExtensionChocoSolver() 
    {
        //System.out.println("choco solver créé !");
    }
    
    
      public Extension getSolution(ArrayList<NodeCandidate> initCands, Fusionner fusionner) 
    {
        Extension ext = new Extension();
        //System.out.println("je suis dans get Solution, extension créée");
        
        defined = new int[initCands.size()];
        Arrays.fill(defined, -1);

        int nbConflict = 0;
        int nbTotal = 0;
        int n = initCands.size();
        float precision = 100.0f;
        int conflictTemp =0;
        Solver solver= new Solver();
        trust = new float[initCands.size()];
        trustArc = new float[initCands.size()][initCands.size()];
        conflicts = new int[initCands.size()][initCands.size()];
        candidatSeul = new int[n];
        try{
            IntVar[] X = VF.enumeratedArray("X", n, 0, 1, solver);
                 //trust1 = trust arc or 0
            IntVar[][] trust1=VF.boundedMatrix("trust1",n,n, 0, (int)(precision), solver);
                //trust2 : somme of trust arc by node
            IntVar[] trust2=VF.enumeratedArray("trust2", n, 0, (int)(n*precision), solver);
                //trust3 : trust node or 0
            IntVar[] trust3=VF.enumeratedArray("trust3", n, 0, (int)(100*precision), solver);
                //trust4[0] : "points" of the objective function earned by trust arc
                //trust4[1] : "points" earned by trust node
            IntVar[] trust4;
            //sum of trust4[1] and trust4[0] : objective function
            IntVar trustFinal;
            if(n>=100){
                trust4=VF.enumeratedArray("trust4", 2, 0, (int)(n*n*precision), solver); 
                trustFinal=VF.enumerated("trustFinal", 0,(int)(2*n*n*precision), solver);
            }
            else{
                trust4=VF.enumeratedArray("trust4", 2, 0, (int)(100*n*precision), solver); 
                trustFinal=VF.enumerated("trustFinal", 0,(int)(200*n*precision), solver);
            }
            for(int[] a : conflicts)
            {
                Arrays.fill(a, 0);
               // System.out.println("boucle remplissage de aaaaa en 00000");
            }
            System.out.println("array conflicts filled");
            for(int i = 0; i< initCands.size(); i++)
            {
                NodeCandidate nc = initCands.get(i);
                trust[i] = nc.getTrustScore()+nc.getSumArcCandIntr();
                for(int j = i+1; j < initCands.size(); j++)
                {
                    NodeCandidate nc2 = initCands.get(j);
                    trustArc[i][j] = nc.getSumArcCandImplied(nc2)+nc2.getSumArcCandImplied(nc);
                    trustArc[j][i] = 0.0f;
                    nbTotal ++;
                    if(!nc.isCompatible(nc2))
                    {
                        conflicts[i][j] = 1;
                        conflicts[j][i] = 1;

                        nbConflict ++;
                    }
                }
                conflictTemp=0;
                if(trust[i]>100*(int)precision){
                    trust[i]=100;
                }
                for (int j=0;j<n;j++ ){
                     if(conflicts[i][j]==1){
                         conflictTemp++;
                        solver.post(ICF.arithm(X[i],"+", X[j],"<=",1));
                    }
                    solver.post(LCF.ifThenElse_reifiable(ICF.arithm(X[j],"=",0), ICF.arithm(trust1[i][j], "=", 0),ICF.arithm(trust1[i][j],"=",(int)(precision*trustArc[i][j]))));
                }

                solver.post(LCF.ifThenElse_reifiable(ICF.arithm(X[i], "=", 0), ICF.arithm(trust2[i], "=", 0), ICF.sum(trust1[i], trust2[i])));
                solver.post(LCF.ifThenElse_reifiable(ICF.arithm(X[i], "=", 0), ICF.arithm(trust3[i], "=", 0),ICF.arithm(trust3[i], "=", (int)(trust[i]*precision)) ));
                solver.post(ICF.sum(trust3, trust4[1]));
                solver.post(ICF.sum(trust2, trust4[0]));

                if(conflictTemp==0){
                    candidatSeul[i]=1;
                    if(defined[i] != 0) {
                        solver.post(ICF.arithm(X[i], "=", 1));
                    }
                }
                else{
                    candidatSeul[i]=0;
                }
                if (defined[i]!=-1){
                    solver.post(ICF.arithm(X[i],"=",defined[i]));
                }
            }
            solver.post(ICF.sum(trust4, trustFinal));
            System.out.println("Constraints taken, solver processing...");
            System.out.println("Nb variables : " + solver.getNbVars());
            System.out.println("Nb contraintes : " + solver.getNbCstrs());

            solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, trustFinal);

            ISolutionRecorder solutionRecorder = solver.getSolutionRecorder();
            for(Solution s : solutionRecorder.getSolutions()){
                System.out.println("SOLUTION (max : ");
                for(int i=0;i<n;i++){
                    //System.out.println(X[i].getName()+" -> "+s.getIntVal(X[i]));
                    if(s.getIntVal(X[i])==1){
                        ext.addNodeCandidate(initCands.get(i));
                    }
                }

                System.out.println("trustFinal : " + trustFinal.getValue() );
                System.out.println("trust earned by links : "+trust4[0].getValue());
                System.out.println("trust earned by nodes : "+trust4[1].getValue());

            }


            System.out.println("STATS : "+nbConflict+" / "+nbTotal);
        }catch(Error e){
            System.err.println("Java heap space out of memory");
            System.out.println("Java heap space : outOfMemoryException");
        }
        return ext;
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
   
}
