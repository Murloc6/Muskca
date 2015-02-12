package Alignment;

import java.io.Serializable;

/**
 *
 * @author murloc
 */
public class Alignment implements Serializable
{
    String uri;
    String uriAlign;
    float value;
    
    Alignment inverseAlignment;
    
    public Alignment(String uri, String uriAlign, float value)
    {
        this.uri = uri;
        this.uriAlign = uriAlign;
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
