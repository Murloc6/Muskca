/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package muskca;

import Candidate.NodeCandidate.NodeCandidate;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 *
 * @author murloc
 */
public class ExpertsSimulator 
{
    
    private String mongodb;
    private DBCollection collMongo;
    private int nbNotFound = 0;
    private ArrayList<String> uriNotFounded;
    
    public ExpertsSimulator(String mongodb)
    {
        this.mongodb = mongodb;
        this.uriNotFounded = new ArrayList<>();
    }
    
    public DBCollection connectMongo(String mongoCollection)
    {
        try
        {
            MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
            DB db = mongoClient.getDB(this.mongodb);
           this.collMongo = db.getCollection(mongoCollection);
           //collMongo.remove(new BasicDBObject());
        } 
        catch (UnknownHostException ex)
        {
            System.err.println("ERROR during connection to mongoDB ...");
            System.exit(0);
        }
        return collMongo;
    }
    
    
    public boolean isUriValidated(String uri)
    {
        boolean ret = false;
        
        BasicDBObject query = new BasicDBObject("uri", uri);
        DBObject dbo = this.collMongo.findOne(query);
        if(dbo != null)
        {   
            int nbVal = (int) dbo.get("nbVal");
            int nbNotVal = (int) dbo.get("nbNotVal");
            int nbDontKnow = (int) dbo.get("nbDontKnow");
            
            if(nbNotVal == 0 && nbVal >1)
            {
                ret = true;
            }
        }
        else
        {
            System.out.println("Uri "+uri+" not in the val db");
            this.uriNotFounded.add(uri);
            ret = true;
            this.nbNotFound ++;
        }
        return ret;
    }
    
    public boolean isCandidateValidated(NodeCandidate nc)
    {
        boolean ret = true;
        
        for(String uri : nc.getUriImplicate().values())
        {
            ret = this.isUriValidated(uri);
            if(!ret)
            {
                break;
            }
        }
        
        
        return ret;
    }
    
    public int getNbNotFound()
    {
        return this.nbNotFound;
    }
    
    public String statsNotFounded()
    {
        String ret = "Uris not founded : ";
        for(String s : this.uriNotFounded)
        {
            ret += "\n "+s;
        }
        
        return ret; 
    }
    
    
}
