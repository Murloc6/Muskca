#!/usr/bin/python
from Kappa import Kappa
from Sessions import Sessions
 
 
n = 3
nWithoutVincent = 2
k = 3

sessionsList = []
 
# # All (Triticum, Aegilops)
# sessions = ["jabot_1401883308", "JLG_1403680581", "vincent_1404990765", "jabot_1402514361", "JLG_1403686426", "vincent_1404477069", "jabot_1402515589", "JLG_1403882200", "_1404479484", "jabot_1402518917", "JLG_1403689151", "Vincent_1404826194", "jabot_1402520518", "JLG_1403690081", "Vincent_1404826450", "jabot_1402521471", "JLG_1404379085", "Vincent_1404830338"]
# sess = Sessions("All", sessions, n)
# sessionsList.append(sess)
#  
# # All (Triticum, Aegilops) Without Vincent
# sessions = ["jabot_1401883308", "JLG_1403680581", "jabot_1402514361", "JLG_1403686426", "jabot_1402515589", "JLG_1403882200", "jabot_1402518917", "JLG_1403689151", "jabot_1402520518", "JLG_1403690081", "jabot_1402521471", "JLG_1404379085"]
# sess = Sessions("All_WithoutVincent", sessions, nWithoutVincent)
# sessionsList.append(sess)
#  
 
#All Triticum
sessions = ["jabot_1401883308", "JLG_1403680581", "vincent_1404990765", "jabot_1402514361", "JLG_1403686426", "vincent_1404477069", "jabot_1402515589", "JLG_1403882200", "_1404479484"]
sess = Sessions("All_Triticum", sessions, n)
sess.saveMongoDB("triticumValid")
sess.saveRelationMongoDB("triticumRelationValid")
sess.saveTypeMongoDB("triticumTypeValid")
sess.saveLabelsMongoDB("triticumLabelsValid")
exit
sessionsList.append(sess)
 
 
# # All without Vincent Triticum
# sessions = ["jabot_1401883308", "JLG_1403680581", "jabot_1402514361", "JLG_1403686426", "jabot_1402515589", "JLG_1403882200"]
# sess = Sessions("All_WithoutVincent_Triticum", sessions, nWithoutVincent)
# sessionsList.append(sess)
#  
#    
# # Only Agrovoc Triticum
# sessions = ["jabot_1401883308", "JLG_1403680581", "vincent_1404990765"]
# sess = Sessions("OnlyAgrovoc_Triticum", sessions, n)
# sessionsList.append(sess)
#   
# # Only Agrovoc Triticum without Vincent
# sessions = ["jabot_1401883308", "JLG_1403680581"]
# sess = Sessions("OnlyAgrovoc_WithoutVincent_Triticum", sessions, nWithoutVincent)
# sessionsList.append(sess)
#    
# # Only TaxRef Triticum
# sessions = ["jabot_1402514361", "JLG_1403686426", "vincent_1404477069"]
# sess = Sessions("OnlyTaxRef_Triticum", sessions, n)
# sessionsList.append(sess)
#  
# # Only TaxRef Triticum without Vincent
# sessions = ["jabot_1402514361", "JLG_1403686426"]
# sess = Sessions("OnlyTaxRef_WithOutVincent_Triticum", sessions, nWithoutVincent)
# sessionsList.append(sess)
#    
# # Only NCBI Triticum
# sessions = ["jabot_1402515589", "JLG_1403882200", "_1404479484"]
# sess = Sessions("OnlyNCBI_Triticum", sessions, n)
# sessionsList.append(sess)
#  
# # Only NCBI Triticum Without Vincent
# sessions = ["jabot_1402515589", "JLG_1403882200"]
# sess = Sessions("OnlyNCBI_WithoutVincent_Triticum", sessions, nWithoutVincent)
# sessionsList.append(sess)
#    
  
#All Aegilops
# sessions = ["jabot_1402518917", "JLG_1403689151", "Vincent_1404826194", "jabot_1402520518", "JLG_1403690081", "Vincent_1404826450", "jabot_1405412606", "JLG_1404379085", "Vincent_1404830338"]
# sess = Sessions("All_Aegilops", sessions, n)
# sess.saveMongoDB("aegilopsValid")
# sess.saveRelationMongoDB("aegilopsRelationValid")
# sessionsList.append(sess)

# #All Aegilops
# sessions = ["jabot_1402518917", "JLG_1403689151", "Vincent_1404826194", "jabot_1402520518", "JLG_1403690081", "Vincent_1404826450"]
# sess = Sessions("All_Aegilops", sessions, n)
# sessionsList.append(sess)

# #All without Vincent Aegilops
# sessions = ["jabot_1402518917", "JLG_1403689151", "jabot_1402520518", "JLG_1403690081", "jabot_1402521471", "JLG_1404379085"]
# sess = Sessions("AllWithoutVincent_Aegilops", sessions, nWithoutVincent)
# sessionsList.append(sess)
#   
# # Only Agrovoc Aegilops
# sessions = ["jabot_1402518917", "JLG_1403689151", "Vincent_1404826194"]
# sess = Sessions("OnlyAgrovoc_Aegilops", sessions, n)
# sessionsList.append(sess)
# 
# # Only Agrovoc Aegilops Without Vincent
# sessions = ["jabot_1402518917", "JLG_1403689151"]
# sess = Sessions("OnlyAgrovoc_WithoutVincent_Aegilops", sessions, nWithoutVincent)
# sessionsList.append(sess)
#   
# # Only TaxRef Aegilops
# sessions = ["jabot_1402520518", "JLG_1403690081", "Vincent_1404826450"]
# sess = Sessions("OnlyTaxRef_Aegilops", sessions, n)
# sessionsList.append(sess)
# 
# # Only TaxRef Aegilops Without Vincent
# sessions = ["jabot_1402520518", "JLG_1403690081"]
# sess = Sessions("OnlyTaxRef_WithoutVincent_Aegilops", sessions, nWithoutVincent)
# sessionsList.append(sess)
#   
# # Only NCBI Aegilops
# sessions = ["jabot_1402521471", "JLG_1404379085", "Vincent_1404830338"]
# sess = Sessions("OnlyNCBI_Aegilops", sessions, n)
# sessionsList.append(sess)
# 
# # Only NCBI Aegilops Without Vincent
# sessions = ["jabot_1402521471", "JLG_1404379085"]
# sess = Sessions("OnlyNCBI_Aegilops", sessions, nWithoutVincent)
# sessionsList.append(sess)


# #All Oryza
# sessions = ["jabot_1402519311", "Vincent_1404915203", "jabot_1402521189", "vincent_1404990431", "jabot_1402521847", "vincent_1404990660"]
# sess = Sessions("All_Oryza", sessions, n)
# sess.saveMongoDB("oryzaValid")
# sessionsList.append(sess)

#All (without NCBI_Aegilops)
sessions = ["jabot_1402518917", "JLG_1403689151", "Vincent_1404826194", "jabot_1402520518", "JLG_1403690081", "Vincent_1404826450", "jabot_1401883308", "JLG_1403680581", "vincent_1404990765", "jabot_1402514361", "JLG_1403686426", "vincent_1404477069", "jabot_1402515589", "JLG_1403882200", "_1404479484"]
sess = Sessions("All", sessions, n)
sessionsList.append(sess)

for s in sessionsList:
    data = s.getData2()
    kappa = Kappa(data, s.N, s.n, 4)
    print s
    print "DATA : "
    print data
    print "\t Kappa : "+str(kappa.getKappa())
    print "\t Ratio : "+str(kappa.getRatio())
    print "-------------"
    
    
# TEST de WikiPedia
data = [[0, 0, 0, 0, 14], [0,2,6,4,2], [0,0,3,5,6], [0,3,9,2,0], [2,2,8,1,1], [7,7,0,0,0], [3,2,6,3,0], [2,5,3,2,2], [6,5,2,1,0], [0,2,2,3,7]]
N = 10
n = 14
k = 5
print "TEST Wikipedia"
print "Data : "
print data
kappa = Kappa(data, N, n, k)
print "\t Kappa : "+str(kappa.getKappa())