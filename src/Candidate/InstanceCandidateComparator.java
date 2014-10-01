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
public class InstanceCandidateComparator implements Comparator<InstanceCandidate>
{

    @Override
    public int compare(InstanceCandidate ic1, InstanceCandidate ic2)
    {
        float trustIC1 = ic1.getTrustScore();
        float trustIC2 = ic2.getTrustScore();
        return trustIC1 > trustIC2 ? -1 : trustIC1 == trustIC2 ? 0 : 1;
    }
    
}
