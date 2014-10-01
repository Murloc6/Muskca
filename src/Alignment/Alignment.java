package Alignment;

import java.io.Serializable;

/**
 *
 * @author murloc
 */
public class Alignment implements Serializable
{
    String uri;
    String label;
    String uriAlign;
    String labelAlign;
    float value;
    
    Alignment inverseAlignment;
    
    public Alignment(String uri, String label, String uriAlign, String labelAlign, float value)
    {
        this.uri = uri;
        this.label = label;
        this.uriAlign = uriAlign;
        this.labelAlign = labelAlign;
        this.value = value;
    }
    
    public void setInverseAlignment(Alignment inverse)
    {
        this.inverseAlignment = inverse;
    }
    
    public float getValue()
    {
        return this.value;
    }
    
    public String getUriAlign()
    {
        return this.uriAlign;
    }
    
    public String getUri()
    {
        return this.uri;
    }
    
}
