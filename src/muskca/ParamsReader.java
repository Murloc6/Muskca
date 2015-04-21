/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package muskca;

import Source.Source;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
/**
 *
 * @author murloc
 */
public class ParamsReader 
{
    JsonNode root;
    
    public ParamsReader()
    {
        try 
        {
            byte[] jsonData = Files.readAllBytes(Paths.get("in/muskca_params.json"));
            ObjectMapper objectMapper = new ObjectMapper();
            root = objectMapper.readTree(jsonData);
            
        } 
        catch (IOException ex) 
        {
            System.err.println("Error during reading the params file ... ");
            System.exit(0);
        }
        
    }
    
    public ArrayList<String> getArrayParams(String field)
    {
        ArrayList<String> ret = new ArrayList<>();
        Iterator<JsonNode> elem = this.root.path(field).elements();
        while(elem.hasNext())
        {
            JsonNode relImp = elem.next();
            ret.add(relImp.asText());
        }
         return ret;
    }
    
    public String getParam(String field)
    {
        return this.root.path(field).asText();
    }
    
    public HashMap<String, String> getSubParams(String field)
    {
        HashMap<String, String> ret = new HashMap<>();
        
        JsonNode elem = this.root.path(field);
        Iterator<String> subFields = elem.fieldNames();
        while(subFields.hasNext())
        {
            String subField = subFields.next();
            ret.put(subField, elem.path(subField).asText());
        }
        
        return ret;
    }
    
    public ArrayList<Source> getSources()
    {
        ArrayList<Source> ret = new ArrayList<>();
        
        Iterator<JsonNode> elem = this.root.path("sources").elements();
        while(elem.hasNext())
        {
            JsonNode sNode = elem.next();
            Source s = new Source(sNode.path("name").asText(), sNode.path("baseUri").asText(), sNode.path("spIn").asText());
            float sq = sNode.path("sourceQuality").floatValue();
            s.setScores(sq,sq,sq);
            ret.add(s);
            System.out.println("Add Source : "+s.getName()+" added. SQ = "+s.getSourceQualityScore());
        }
        
        return ret;
    }
    
}
