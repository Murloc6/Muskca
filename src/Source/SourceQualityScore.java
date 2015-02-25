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
    
    /*
        the weights are here just in prevision for a better source quality computation. For now all crieteria are same (weight = 1).
    */
    public static int freshnessWeight = 1;
    public static int reputationWeight = 1;
    public static int ADOMSimWeight = 1;
    
    public static float computeQualityScore(float freshness, float reputation, float ADOMSim)
    {
        return (freshness*freshnessWeight+reputation*reputationWeight+ADOMSim*ADOMSimWeight)/(freshnessWeight+reputationWeight+ADOMSimWeight);
    }
    
}
