/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Source.OntologicalElement;

import Source.Source;

/**
 *
 * @author murloc
 */
public class OntologicalElement {
    private String uri;
    private Source s;
    
    public OntologicalElement(String uri, Source s){
        this.uri = uri;
        this.s = s;
    }
    
    public String getUri(){
        return this.uri;
    }
    
    public Source getSource(){
        return this.s;
    }
    
    public String toString(){
        return this.uri;
    }
    
    
}
