/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Alignment;

import MultiSources.Fusionner;
import Source.Source;

/**
 *
 * @author fabien.amarger
 */
public class AlignerTEST extends Aligner
{

    public AlignerTEST(Fusionner fusionner, Source s1, Source s2)
    {
        super(fusionner, s1, s2);
    }

    @Override
    public String alignSources(float limitSimScore)
    {
        String ret = "";
        if(this.s1.getName().compareTo("S1") == 0 && this.s2.getName().compareTo("S2") == 0)
        {
             this.addAlignment("http://s1.fr#x11", "", "http://s2.fr#x21", "", 0.9f);
             this.addAlignment("http://s1.fr#x11", "", "http://s2.fr#x22", "", 0.9f);
             this.addAlignment("http://s1.fr#x12", "", "http://s2.fr#x21", "", 0.9f);
        }
        else  if(this.s1.getName().compareTo("S1") == 0 && this.s2.getName().compareTo("S3") == 0)
        {
            this.addAlignment("http://s1.fr#x11", "", "http://s3.fr#x31", "", 0.9f);
            this.addAlignment("http://s1.fr#x12", "", "http://s3.fr#x32", "", 0.9f);
        }
        else  if(this.s1.getName().compareTo("S2") == 0 && this.s2.getName().compareTo("S3") == 0)
        {
            this.addAlignment("http://s2.fr#x21", "", "http://s3.fr#x31", "", 0.9f);
            this.addAlignment("http://s2.fr#x22", "", "http://s3.fr#x32", "", 0.9f);
            this.addAlignment("http://s2.fr#x23", "", "http://s3.fr#x32", "", 0.9f);
        }
        this.isAligned = true;
        return ret;
    }

    
}
