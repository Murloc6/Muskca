from DataSetMeasure import DataSetMeasure 

def getFMeasure(precision, recall):
        ret = -1
        if((recall != -1) & (precision != -1)):
            ret = (2*(precision*recall))/(precision+recall)
        return ret

limitTrust = 0.6
limitNbValidatorsTriticum = 2
limitNbValidatorsAegilops = 3
limitNbValidatorsOryza = 1



triticumDataSet = DataSetMeasure("Triticum_All", "triticumCandidateSQ", "triticumValid", "triticumICHRSQ", "triticumRelationValid", "triticumTypeCandidateSQ", "triticumTypeValid", "triticumLabelCandidateSQ", "triticumLabelsValid")

print "-------Triticum -------"

triticumRecall = triticumDataSet.getRecall(limitTrust, limitNbValidatorsTriticum)
print "Recall Triticum : "+str(triticumRecall)

triticumPrecision = triticumDataSet.getPrecision(limitTrust, limitNbValidatorsTriticum)
print "Precision Triticum : "+str(triticumPrecision)

print "F-Measure Triticum : "+str(getFMeasure(triticumPrecision, triticumRecall))

precisionRelTriticum = triticumDataSet.getPrecisionRelation(limitTrust, limitNbValidatorsTriticum)
print "Precision rel Triticum : "+str(precisionRelTriticum)

recallRelTriticum = triticumDataSet.getRecallRelation(limitTrust, limitNbValidatorsTriticum)
print "Recall rel Triticum : "+str(recallRelTriticum)

print "F-Measure Relation Triticum : "+str(getFMeasure(precisionRelTriticum, recallRelTriticum))

precisionTypeTriticum = triticumDataSet.getPrecisionType(limitTrust, limitNbValidatorsTriticum)
print "Precision type Triticum : "+str(precisionTypeTriticum)

recallTypeTriticum = triticumDataSet.getRecallType(limitTrust, limitNbValidatorsTriticum)
print "Recall type Triticum : "+str(recallTypeTriticum)

print "F-Measure Type Triticum : "+str(getFMeasure(precisionTypeTriticum, recallTypeTriticum))

precisionLabelsTriticum = triticumDataSet.getPrecisionLabels(limitTrust, limitNbValidatorsTriticum)
print "Precision labels Triticum : "+str(precisionLabelsTriticum)

recallLabelsTriticum = triticumDataSet.getRecallLabels(limitTrust, limitNbValidatorsTriticum)
print "Recall labels Triticum : "+str(recallLabelsTriticum)

print "F-Measure Labels Triticum : "+str(getFMeasure(precisionLabelsTriticum, recallLabelsTriticum))


# aegilopsDataSet = DataSetMeasure("Aegilops_All", "aegilopsCandidate", "aegilopsValid", "aegilopsICHR", "aegilopsRelationValid")
# print "----------Aegilops -------"
#  
# aegilopsRecall = aegilopsDataSet.getRecall(limitTrust, limitNbValidatorsAegilops)
# print "Recall Aegilops : "+str(aegilopsRecall)
#  
# aegilopsPrecision = aegilopsDataSet.getPrecision(limitTrust, limitNbValidatorsAegilops)
# print "Precision Aegilops : "+str(aegilopsPrecision)
#  
# print "F-Measure Aegilops : "+str(getFMeasure(aegilopsPrecision, aegilopsRecall))
#  
# precisionRelAegilops = aegilopsDataSet.getPrecisionRelation(limitTrust, limitNbValidatorsAegilops)
# print "Precision rel Aegilops : "+str(precisionRelAegilops)
#  
# recallRelAegilops = aegilopsDataSet.getRecallRelation(limitTrust, limitNbValidatorsAegilops)
# print "Recall rel Aegilops : "+str(recallRelAegilops)
#  
# print "F-Measure Relation Aegilops : "+str(getFMeasure(precisionRelAegilops, recallRelAegilops))


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




