/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Source;

/**
 *
 * @author root
 */
public class SourceQualityScore 
{
    
    public static int freshnessWeight = 2;
    public static int reputationWeight = 4;
    public static int ADOMSimWeight = 1;
    
    public static float computeQualityScore(float freshness, float reputation, float ADOMSim)
    {
        return (freshness*freshnessWeight+reputation*reputationWeight+ADOMSim*ADOMSimWeight)/(freshnessWeight+reputationWeight+ADOMSimWeight);
    }
    
}
