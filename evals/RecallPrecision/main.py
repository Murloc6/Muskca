from DataSetMeasure import DataSetMeasure 

def getFMeasure(precision, recall):
        ret = -1
        if((recall != -1) & (precision != -1)):
            ret = (2*(precision*recall))/(precision+recall)
        return ret

def computeValues(limitTrust, dataSet, limitNbValidators):
    #limitNbValidators = 3
    #limitNbValidatorsAegilops = 2
    #limitNbValidatorsOryza = 1
    print "-------"+dataSet.getName()+" -------"
    print "Inst : "
    recallInst = dataSet.getRecall(limitTrust, limitNbValidators)
    print "Recall inst : "+str(recallInst)
    precisionInst = dataSet.getPrecision(limitTrust, limitNbValidators)
    print "Precision inst : "+str(precisionInst)
    print "F-Measure inst : "+str(getFMeasure(precisionInst, recallInst))
    print "Relation : "
    precisionRel = dataSet.getPrecisionRelation(limitTrust, limitNbValidators)
    print "Precision rel : "+str(precisionRel)
    recallRel = dataSet.getRecallRelation(limitTrust, limitNbValidators)
    print "Recall rel  : "+str(recallRel)
    print "F-Measure Relation : "+str(getFMeasure(precisionRel, recallRel))
    print "Type : "
    precisionType = dataSet.getPrecisionType(limitTrust, limitNbValidators)
    print "Precision type  : "+str(precisionType)
    recallType = dataSet.getRecallType(limitTrust, limitNbValidators)
    print "Recall type : "+str(recallType)
    print "F-Measure type : "+str(getFMeasure(precisionType, recallType))
    print "Labels : "
    precisionLabels = dataSet.getPrecisionLabels(limitTrust, 1)
    print "Precision labels : "+str(precisionLabels)
    recallLabels = dataSet.getRecallLabels(limitTrust, limitNbValidators)
    print "Recall labels : "+str(recallLabels)
    if(precisionLabels > 0 and recallLabels > 0):
        print "F-Measure Labels : "+str(getFMeasure(precisionLabels, recallLabels))
    else:
        print "F-Measure Labels : 0"


triticumDataSet = DataSetMeasure("Triticum_All", "triticumCandidate", "triticumValid", "triticumICHR", "triticumRelationValid", "triticumTypeCandidate", "triticumTypeValid", "triticumLabelCandidate", "triticumLabelsValid")
#aegilopsDataSet = DataSetMeasure("Aegilops_All", "aegilopsCandidate", "aegilopsValid", "aegilopsICHR", "aegilopsRelationValid", "loutre", "loutre", "loutre", "loutre")


print "-------SIMPLE TRUST----------"
print "-------------"

computeValues(0, triticumDataSet, 3)


print "-------TRUST DEGREE 0.6----------"
print "-------------"

computeValues(0.6, triticumDataSet, 3)


print "-------TRUST DEGREE 0.9----------"
print "-------------"

computeValues(0.9, triticumDataSet, 3)


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




