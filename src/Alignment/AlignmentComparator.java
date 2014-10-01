/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Alignment;

import java.util.Comparator;

/**
 *
 * @author murloc
 */
public class AlignmentComparator implements Comparator<Alignment>
{

    @Override
    public int compare(Alignment t, Alignment t1) 
    {
        int ret = 0;
        if(t.getValue() < t1.getValue())
        {
            ret = -1;
        }
        else if(t.getValue()> t1.getValue())
        {
            ret = 1;
        }
       
        return ret;
    }
    
}
