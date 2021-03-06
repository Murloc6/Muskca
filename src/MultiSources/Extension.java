/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MultiSources;

import Candidate.NodeCandidate.NodeCandidate;
import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author murloc
 */
public class Extension 
{
    private ArrayList<NodeCandidate> candidates;
    private float score = 0;
    
    public Extension()
    {
        this.candidates = new ArrayList<>();
    }
    
    public void addNodeCandidate(NodeCandidate nc)
    {
        this.candidates.add(nc);
    }
    
    public void computeScore()
    {
        this.score = (float) 0.5;
    }
    
    public ArrayList<NodeCandidate> getCandidates()
    {
        return this.candidates;
    }
    
    public void sortCandidates()
    {
        Collections.sort(this.candidates, new Comparator<NodeCandidate>() {
            @Override
            public int compare(NodeCandidate  nc1, NodeCandidate  nc2)
            {
                float nc1Trust = nc1.getTrustScore()+nc1.getSumArcCandIntr();
                float nc2Trust = nc2.getTrustScore()+nc2.getSumArcCandIntr();
                return (nc1Trust < nc2Trust ? 1 :(nc1Trust == nc2Trust ? 0 : -1));
            }
        });

    }
     
    public String toString()
    {
        String ret = "------------------ \n "
                     + "EXTENSION: \n";
        
        for(NodeCandidate nc : this.candidates)
        {
            ret += nc.toString()+"\n";
        }
        ret += "*****************\n";
        return ret;
    }
    
    
    public BasicDBObject toDBObject()
    {
        ArrayList<BasicDBObject> allCands = new ArrayList<>();
        for(NodeCandidate nc : this.candidates)
        {
            allCands.add(nc.toDBObject());
        }
        BasicDBObject ret = new BasicDBObject();
        ret.append("extension", allCands);
        return ret;
    }
    
}
