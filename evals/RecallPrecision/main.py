from DataSetMeasure import DataSetMeasure 

def getFMeasure(precision, recall):
        ret = -1
        if((recall != -1) & (precision != -1)):
            ret = (2*(precision*recall))/(precision+recall)
        return ret

def computeValues(dataSet, limitTrust, all, f):
    #limitNbValidators = 3
    #limitNbValidatorsAegilops = 2
    #limitNbValidatorsOryza = 1
    limitNbValidators = 2

    print "THRESHOLD : "+str(limitTrust)
    
    print "Inst : "
    precisionInst = dataSet.getPrecision(limitTrust, limitNbValidators)
    print "Precision inst : "+str(precisionInst)
    recallInst = dataSet.getRecall(limitTrust, limitNbValidators)
    print "Recall inst : "+str(recallInst)
    fmesureInst = getFMeasure(precisionInst, recallInst)
    print "F-Measure inst : "+str(fmesureInst)
    f.write(", "+str(precisionInst)+", "+str(recallInst)+", "+str(fmesureInst))
    if(all):
        print "Relation : "
        precisionRel = dataSet.getPrecisionRelation(limitTrust, limitNbValidators)
        print "Precision rel : "+str(precisionRel)
        recallRel = dataSet.getRecallRelation(limitTrust, limitNbValidators)
        print "Recall rel  : "+str(recallRel)
        fmesureRel = getFMeasure(precisionRel, recallRel)
        print "F-Measure Relation : "+str(fmesureRel)
        f.write(", "+str(precisionRel)+", "+str(recallRel)+", "+str(fmesureRel))
        print "Type : "
        precisionType = dataSet.getPrecisionType(limitTrust, limitNbValidators)
        print "Precision type  : "+str(precisionType)
        recallType = dataSet.getRecallType(limitTrust, limitNbValidators)
        print "Recall type : "+str(recallType)
        fmesureType = getFMeasure(precisionType, recallType)
        print "F-Measure type : "+str(fmesureType)
        f.write(", "+str(precisionType)+", "+str(recallType)+", "+str(fmesureType))
        print "Labels : "
        precisionLabels = dataSet.getPrecisionLabels(limitTrust, 1)
        print "Precision labels : "+str(precisionLabels)
        recallLabels = dataSet.getRecallLabels(limitTrust, limitNbValidators)
        print "Recall labels : "+str(recallLabels)
        if(precisionLabels > 0 and recallLabels > 0):
            fmesureLabel = getFMeasure(precisionLabels, recallLabels)
            print "F-Measure Labels : "+str(fmesureLabel)
        else:
            print "F-Measure Labels : 0"
            fmesureLabel = 0
        f.write(", "+str(precisionLabels)+", "+str(recallLabels)+", "+str(fmesureLabel))

def computeForTrustScore(dataSet, trustScoreParam, all):

    thresholds = [0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9]
    dataSet.setTrustScoreParam(trustScoreParam)
    f = open('EVALS_Triticum_'+trustScoreParam+".csv", 'w')
    f.write("Threshold , Inst Precision,  Inst Rappel, Inst F-Mesure, Rel Precision,  Rel Rappel, Rel F-Mesure, Type Precision,  Type Rappel, Type F-Mesure, Label Precision,  Label Rappel, Label F-Mesure \n");
    for thres in thresholds:
        f.write(str(thres))
        dataSet.reinit()
        computeValues(dataSet, thres, all, f)
        f.write("\n")
    f.flush()
    f.close()

triticumDataSet = DataSetMeasure("Triticum_All", "triticumCandidate", "triticumValid", "triticumICHR", "triticumRelationValid", "triticumTypeCandidate", "triticumTypeValid", "triticumLabelCandidate", "triticumLabelsValid")
#aegilopsDataSet = DataSetMeasure("Aegilops_All", "aegilopsCandidate", "aegilopsValid", "aegilopsICHR", "aegilopsRelationValid", "loutre", "loutre", "loutre", "loutre")


print "-------SIMPLE TRUST----------"
print "-------------"

computeForTrustScore(triticumDataSet, "trustScoreSimple", True)

#computeValues(0, triticumDataSet, 3)


print "-------TRUST DEGREE----------"
print "-------------"
triticumDataSet = DataSetMeasure("Triticum_All", "triticumCandidate", "triticumValid", "triticumICHR", "triticumRelationValid", "triticumTypeCandidate", "triticumTypeValid", "triticumLabelCandidate", "triticumLabelsValid")
#computeValues(0.6, triticumDataSet, 3)
computeForTrustScore(triticumDataSet, "trustScoreDegree", False)

print "-------TRUST Choquet----------"
print "-------------"
triticumDataSet = DataSetMeasure("Triticum_All", "triticumCandidate", "triticumValid", "triticumICHR", "triticumRelationValid", "triticumTypeCandidate", "triticumTypeValid", "triticumLabelCandidate", "triticumLabelsValid")
#computeValues(0.9, triticumDataSet, 3)
computeForTrustScore(triticumDataSet, "trustScoreChoquet", True)


# print "----------Oryza -------"
# 
# oryzaRecall = oryzaDataSet.getRecall(limitTrust, limitNbValidatorsOryza)
# print "Recall Oryza : "+str(oryzaRecall)
# 
# oryzaPrecision = oryzaDataSet.getPrecision(limitTrust, limitNbValidatorsOryza)
# print "Precision Oryza : "+str(oryzaPrecision)
# 
# print "F-Measure Oryza : "+str(oryzaDataSet.getFMeasure())
# 
# 
# testNathalieDS = DataSetMeasure("Test Nathalie", "testNathalie", "triticumValid")
# oryzaDataSet = DataSetMeasure("Oryza_All", "oryzaCandidate", "oryzaValid")




