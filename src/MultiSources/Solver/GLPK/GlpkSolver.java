/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MultiSources.Solver.GLPK;

import java.io.File;
import java.util.ArrayList;
import muskca.FilePerso;
import org.gnu.glpk.*;


/**
 *
 * @author murloc
 */
public abstract class GlpkSolver implements GlpkCallbackListener, GlpkTerminalListener
{
    
    private boolean hookUsed = false;
    
    protected glp_prob lp;
    private glp_tran tran;
    
    
    private String modelFileName;
    protected String dataFileName;
    
    public GlpkSolver(String modelFileName)
    {
        File f = new FilePerso(modelFileName);
        this.modelFileName = f.getAbsolutePath();
        try {
            GLPK.glp_java_set_numeric_locale("C");
        } catch (UnsatisfiedLinkError ex) {
        //DO NOTHING
            System.out.println("Exception catched!");
        }
    }
    
    public abstract Object getSolution();
    
    public boolean solve() {
        lp = null;
        boolean succeed = false;
        glp_iocp iocp;

        String fname = this.modelFileName;
        String datafName = dataFileName;
        int skip = 0;
        int ret;

        // listen to callbacks
        //GlpkCallback.addListener(this);
        // listen to terminal output
        //GlpkTerminal.addListener(this);

        lp = GLPK.glp_create_prob();
        System.out.println("Problem created");

        tran = GLPK.glp_mpl_alloc_wksp();
        ret = GLPK.glp_mpl_read_model(tran, fname, skip);
        ret = GLPK.glp_mpl_read_data(tran, datafName);
        if (ret != 0) {
            GLPK.glp_mpl_free_wksp(tran);
            GLPK.glp_delete_prob(lp);
            throw new RuntimeException("Model file not found: " + fname);
        }

        // generate model
        GLPK.glp_mpl_generate(tran, null);
        // build model
        GLPK.glp_mpl_build_prob(tran, lp);
        // set solver parameters
        iocp = new glp_iocp();
        GLPK.glp_init_iocp(iocp);
        iocp.setPresolve(GLPKConstants.GLP_ON);
        iocp.setClq_cuts(GLPKConstants.GLP_ON);
        iocp.setGmi_cuts(GLPKConstants.GLP_ON);
        iocp.setMir_cuts(GLPKConstants.GLP_ON);
        iocp.setCov_cuts(GLPKConstants.GLP_ON);
        
        // solve model
        ret = GLPK.glp_intopt(lp, iocp);                                                                
        // postsolve model
        if (ret == 0) {
            GLPK.glp_mpl_postsolve(tran, lp, GLPKConstants.GLP_MIP);
        }
        
        if(GLPK.glp_mip_status(lp) == GLPKConstants.GLP_OPT)
        {
            System.out.println("Solution opti founded!");
            
            System.out.println("z opti : "+GLPK.glp_mip_obj_val(lp));
            succeed = true;
            /*int n;
            String name;
            double val;
            n = GLPK.glp_get_num_cols(lp);
            for (int i = 1; i <= n; i++) {
                name = GLPK.glp_get_col_name(lp, i);
                //val = GLPK.glp_get_col_prim(lp, i);
                val = GLPK.glp_mip_col_val(lp, i);
                if(val > 0)
                {
                    System.out.print(name);
                    System.out.print(" = ");
                    System.out.println(val);
                }
            } */    
        }
        
        // do not listen for callbacks anymore
        GlpkCallback.removeListener(this);
        
        
        return succeed;
    }

    
    public void freeMemory()
    {
        GLPK.glp_mpl_free_wksp(tran);
        GLPK.glp_delete_prob(lp);
    }
    
    
    @Override
    public boolean output(String str) {
        hookUsed = true;
        System.out.print(str);
        return true;
    }

    @Override
    public void callback(glp_tree tree) {
        int reason = GLPK.glp_ios_reason(tree);
        if (reason == GLPKConstants.GLP_IBINGO) {
            System.out.println("Better solution found");
        }
    }
}
