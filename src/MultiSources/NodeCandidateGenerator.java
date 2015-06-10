/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MultiSources;

import Alignment.Alignments;
import Candidate.NodeCandidate.*;
import Candidate.NodeCandidate.NodeCandidate;
import Source.OntologicalElement.*;
import Source.Source;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author murloc
 */
public class NodeCandidateGenerator {
    
    private ArrayList<Source> sources;
    private Alignments aligns;
    
    public static HashMap<String, NodeCandidate> candAdded = new HashMap<>();
    
    public NodeCandidateGenerator(ArrayList<Source> sources, Alignments aligns){
        this.sources = sources;
        this.aligns = aligns;
    }
    
    
     public ArrayList<NodeCandidate> generateNodeCandidates(){
        ArrayList<NodeCandidate> ret = new ArrayList<>();
        
        HashMap<OntologicalElement, ArrayList<NodeCandidate>> color = new HashMap<>();
        
        for(Source s : sources){
            for(OntologicalElement oe : s.getElems()){
                ArrayList<NodeCandidate> impCand = color.get(oe);
                if(impCand == null || impCand.isEmpty()){
                    NodeCandidate cand =  this.createNewNC(oe);
                    cand.addElem(s, oe);
                    ret.addAll(this.dfsFC(cand, sources,  color,  aligns));
                }
            }
        }
        return ret;
    }
    
    private NodeCandidate createNewNC(OntologicalElement oe){
        NodeCandidate ret = null;
        if(oe.getClass() == IndividualOntologicalElement.class){
            ret = new IndividualCandidate();
        }
        else if(oe.getClass() == ClassOntologicalElement.class){
            ret = new ClassCandidate();
        }
        return ret;
    }
    
    
     private ArrayList<OntologicalElement> getAllNbs(NodeCandidate candCur,Alignments aligns){
        ArrayList<OntologicalElement> ret = new ArrayList<>();
        for(Map.Entry<Source, OntologicalElement> e : candCur.getUriImplicate().entrySet()){
            Source s = e.getKey();
            OntologicalElement elem = e.getValue();
            ArrayList<OntologicalElement> align = aligns.getNeighboors(elem);
            for(OntologicalElement nb : align){
                if(!ret.contains(nb)){
                    ret.add(nb);
                }
            }
        }
        
        return ret;
    }
     
     private ArrayList<NodeCandidate> getIntersectColor(NodeCandidate candCur, HashMap<OntologicalElement, ArrayList<NodeCandidate>> color){
        ArrayList<NodeCandidate> ret = null;
        
        ArrayList<OntologicalElement> elems = new ArrayList<>(candCur.getUriImplicate().values());
        if(!elems.isEmpty()){
            for(OntologicalElement elem : elems){
                if(ret == null){
                    if(color.containsKey(elem))
                        ret = (ArrayList<NodeCandidate>) color.get(elem).clone();
                }
                else{
                    ArrayList<NodeCandidate> retTemp = new ArrayList<>();
                    if(color.containsKey(elem)){
                        for(NodeCandidate cand : color.get(elem)){
                            if(ret.contains(cand)){
                                retTemp.add(cand);
                            }
                        }
                        ret = retTemp;
                    }
                }
            }
        }
        if(ret == null){
            ret = new ArrayList<>();
        }
        
        
        
        return ret;
    }
     
     private boolean isElemMarkedBy(ArrayList<NodeCandidate> colorElem, ArrayList<NodeCandidate> colorIntersect){
        Boolean ret = false;
        
        if(colorElem!= null){
            for(NodeCandidate cand : colorElem){
                if(colorIntersect.contains(cand)){
                    ret = true;
                    break;
                }
            }
        }
        
        return ret;
    }
     
    private void addCand(NodeCandidate candCur, ArrayList<NodeCandidate> ret, ArrayList<Source> sources){
        if(!candAdded.containsKey(candCur.getCandId(sources))){
            ret.add(candCur);
            candAdded.put(candCur.getCandId(sources), candCur);
            candCur.getImpliedAligns(this.aligns);
        }
    }
    
    private ArrayList<NodeCandidate> dfsFC(NodeCandidate candCur, ArrayList<Source> sources,  HashMap<OntologicalElement, ArrayList<NodeCandidate>> color, Alignments aligns){
        ArrayList<NodeCandidate> ret = new ArrayList<>();
        ArrayList<OntologicalElement> nbs = getAllNbs(candCur, aligns);
        ArrayList<NodeCandidate> colorIntersect = getIntersectColor(candCur, color);
        if(candCur.getUriImplicate().size() < sources.size() && nbs != null && !nbs.isEmpty()){
            boolean hasNbSuitable = false;
            for(OntologicalElement nb : nbs){
                Source s = nb.getSource();
                if(!candCur.hasElemForSource(s)){
                    hasNbSuitable = true;
                    boolean hasNbsMarked = false;
                    for(OntologicalElement nbsNb: aligns.getNeighboors(nb)){
                        if(isElemMarkedBy(color.get(nbsNb), colorIntersect)){
                            hasNbsMarked = true;
                            break;
                        }
                    }
                    
                    if(!hasNbsMarked){
                        candCur.addElem(s, nb);
                        ret.addAll(dfsFC(candCur, sources, color, aligns));
                    }
                }
            }
            if(!hasNbSuitable){
                addCand(candCur, ret, sources);
            }
        }
        else{
            addCand(candCur, ret, sources);
        }
        
        return ret;
    }
    
    
}
