/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Candidate;

import Source.Source;
import com.mongodb.BasicDBObject;

/**
 *
 * @author fabien.amarger
 */
public class ClassCandidate extends Candidate
{

    
    public ClassCandidate()
    {
        super();
    }
    
    @Override
    public void addElem(Source s, String uriElem)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void computeTrustScore(float trustMax)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BasicDBObject toDBObject()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String toProvO(String baseUri, int numCand)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
