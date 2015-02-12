/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Candidate;

import Source.Source;
import com.mongodb.BasicDBObject;
import java.util.HashMap;

/**
 *
 * @author Fabien
 */
public abstract class Candidate 
{
    protected float trustScore = 0;
    protected float trustScoreSimple = 0;
    protected HashMap<Source,  String> uriImplicate;
    protected String sElem;
    
    public Candidate()
    {
        this.sElem = this.getClass().getSimpleName();
    }
    
    public abstract void addElem(Source s, String uriElem);
    
    public  void computeTrustScore(float trustMax)
    {
        this.trustScoreSimple = (float)this.uriImplicate.keySet().size()/(float)3;
    }
    
    public float getTrustScore()
    {
        return this.trustScore;
    }
    
    public abstract String toString();
    
    public abstract BasicDBObject toDBObject();
    
    //public abstract String toProvO(String baseUri, int nbCand, HashMap<Source, String> sourcesUri);
    
}
