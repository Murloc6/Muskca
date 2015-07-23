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
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.search.solution.ISolutionRecorder;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;


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
        defined = new int[initCands.size()];
        Arrays.fill(defined, -1);
        int nbConflict = 0;
        int nbTotal = 0;
        int n = initCands.size();
        float precision = 100.0f;
        Solver solver= new Solver();
        trust = new float[initCands.size()];
        trustArc = new float[initCands.size()][initCands.size()];
        conflicts = new int[initCands.size()][initCands.size()];
        candidatSeul = new int[n];
        int []sumTrustArc = new int[n];
        int []sumTrustTot = new int[n];//for each node sum trust arc + trust intr
        Arrays.fill(sumTrustArc,0);
        try{
            IntVar[] X = VF.boundedArray("X", n, 0, 1, solver);
            IntVar[] trusti=VF.boundedArray("trusti", n, 0, (int)((n+100)*precision), solver);
            IntVar trustFinal=VF.bounded("trustFinal", 0,(int)((n+100)*n*precision), solver);

            for(int[] a : conflicts)
            {
                Arrays.fill(a, 0);
            }
            Arrays.fill(candidatSeul,1);
            System.out.println("array conflicts filled");
            for(int i = 0; i< initCands.size(); i++)
            {
                NodeCandidate nc = initCands.get(i);
                trust[i] = nc.getTrustScore()+nc.getSumArcCandIntr();
                if(trust[i]>100){
                    trust[i]=100.0f;
                }
                for(int j = i+1; j < initCands.size(); j++)
                {
                    NodeCandidate nc2 = initCands.get(j);
                    trustArc[i][j] = nc.getSumArcCandImplied(nc2)+nc2.getSumArcCandImplied(nc);
                    trustArc[j][i] = trustArc[i][j];
                    nbTotal ++;
                    if(!nc.isCompatible(nc2))
                    {
                        conflicts[i][j] = 1;
                        conflicts[j][i] = 1;
                        candidatSeul[i] = 0;
                        candidatSeul [j] =0;
                        solver.post(ICF.arithm(X[i],"+", X[j],"<=",1));
                        nbConflict ++;
                    }
                }
                trustArc[i][i]=0.0f;
                for(int j =0;j<n;j++){
                    sumTrustArc[i]+=(int)(trustArc[i][j]*precision);
                }
                if(candidatSeul[i]==1 && defined[i] != 0 ){
                    solver.post(ICF.arithm(X[i], "=", 1));
                }
                sumTrustTot [i]= sumTrustArc[i]+(int)(precision*trust[i]);
                solver.post(ICF.times(X[i],sumTrustTot[i],trusti[i]));
                if (defined[i]!=-1){
                    solver.post(ICF.arithm(X[i],"=",defined[i]));
                }
            }
            solver.post(ICF.sum(trusti, trustFinal));
            System.out.println("Constraints taken, solver processing...");
            System.out.println("Nb variables : " + solver.getNbVars());
            System.out.println("Nb contraintes : " + solver.getNbCstrs());

            solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, trustFinal);

            ISolutionRecorder solutionRecorder = solver.getSolutionRecorder();
            for(Solution s : solutionRecorder.getSolutions()){
                //System.out.println("SOLUTION (max : ");
                for(int i=0;i<n;i++){
                    if(s.getIntVal(X[i])==1){
                        ext.addNodeCandidate(initCands.get(i));
                        //System.out.println(i+1+" ");
                    }
                }
                System.out.println("trustFinal : " + trustFinal.getValue() );

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
