/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Candidate;

import java.util.Comparator;

/**
 *
 * @author fabien.amarger
 */
public class CandidateComparator implements Comparator<Candidate>
{

    @Override
    public int compare(Candidate c1, Candidate c2)
    {
        float trustIC1 = c1.getTrustScore();
        float trustIC2 = c2.getTrustScore();
        return trustIC1 > trustIC2 ? -1 : trustIC1 == trustIC2 ? 0 : 1;
    }
    
}
