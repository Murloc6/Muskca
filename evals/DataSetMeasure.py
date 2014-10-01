import pymongo
import re

class DataSetMeasure:
    def __init__(self, name, collCandidate, collValid, collRelCandidate, collRelValid, collTypeCandidate, collTypeValid, collLabelsCandidate, collLabelsValid):
        self.name = name
        #print("Connecting to mongoDB (localhost) server...")
        client = pymongo.MongoClient("localhost", 27017)
        db = client.valRKB
        self.collCandidate = db[collCandidate]
        self.collValid = db[collValid]
        
        self.collRelValid = db[collRelValid]
        self.collRelCandidate = db[collRelCandidate]
        
        self.collTypeCandidate = db[collTypeCandidate]
        self.collTypeValid = db[collTypeValid]
        
        self.collLabelsCandidate = db[collLabelsCandidate]
        self.collLabelsValid = db[collLabelsValid]
        
        #print "Connected to "+db.name+"!"
        self.nbValidate = 0
        self.nbCandidate = 0
        
        self.nbRelValidate = 0
        self.nbRelCandidate = 0
        
        self.nbTaxonValidate = 0
        self.nbTaxonCandidate = 0
        self.precision = -1
        self.recall = -1
    def getPrecision(self, trustScore, limitNbValidator):
        for item in self.collCandidate.find({"trustScore" : {"$gt": trustScore}}):
            self.nbCandidate += 1
            #print item["elemCandidates"]
            #print"\t\t\t --> "+str(item["trustScore"])
            allValid = True
            for elem in item["elemCandidates"]:
                validElem = self.collValid.find_one({"uri": elem})
                if(validElem != None):
                    #print validElem
                    if((validElem["nbVal"] >= limitNbValidator) and (validElem["nbNotVal"] == 0)):
                        #print "\t VALIDATE : "+elem+" --> "+str(validElem["nbVal"])
                        loutre = 42
                    else:
                        #print"\t NOT VALIDATE!"+elem+"(nbVal:"+str(validElem["nbVal"])+", nbNotVal:"+str(validElem["nbNotVal"])+", nbDontKnow:"+str(validElem["nbDontKnow"])+")"
                        allValid = False
                else:
                    allValid = False
                    #print "\t NOT VALIDATE!"+elem+" (not exists)"
                
            if(allValid):
                self.nbValidate +=1
                
        #print "Nb validate :"+str(self.nbValidate)
        #print "Nb candidate : "+str(self.nbCandidate)
        print str(self.nbValidate)+ " / "+str(self.nbCandidate)
        self.precision = float(self.nbValidate)/float(self.nbCandidate)
        return self.precision
    def getRecall(self, limitTrustScore, limitNbValidators):
        for item in self.collValid.find():
            if((item["nbVal"] >= limitNbValidators) and (item["nbNotVal"] == 0)):
                self.nbTaxonValidate += 1
#                 print "TEST : "+item['uri']+": "
                for elem in self.collCandidate.find({'elemCandidates' : item["uri"]}):
                    if(elem['trustScore'] > limitTrustScore):
    #                     print "\t("+str(elem['trustScore'])+" -->"
    #                     print elem['elemCandidates']
                        self.nbTaxonCandidate +=1
                        break
        print str(self.nbTaxonCandidate)+" / "+str(self.nbTaxonValidate)
        self.recall =float(self.nbTaxonCandidate)/float(self.nbTaxonValidate) 
        return self.recall
    def __getUriFromSource(self, ic, source):
        ret = None
        for elem in ic:
            if(elem["source"] == source):
                ret = elem["uri"]
                break
        return ret
    def __findSubString(self, raw_string, start_marker, end_marker):
        return raw_string[raw_string.find(start_marker)+4:raw_string.find(end_marker)]
    def __findSubStringLast(self, raw_string, start_marker, end_marker):
        return raw_string[raw_string.rfind(start_marker)+4:raw_string.rfind(end_marker)]
    def getPrecisionRelation(self, trustScore, limitNbValidator):
        relCandidate = 0
        relCandidateValidated = 0
        for item in self.collRelCandidate.find({"trustScore" : {"$gt": trustScore}}):
            relCandidate += 1
            allValid = True
            ic = item["ic"]
            icHR = item["icHR"]
            for elem in ic:
                uri = elem["uri"]
                source = elem["source"]
                uriHR = self.__getUriFromSource(icHR, source)
                if(uriHR != None):
                    uriRel = "&lt;"+uri+"&gt; &lt;http://ontology.irstea.fr/AgronomicTaxon#hasHigherRank&gt; &lt;"+uriHR+"&gt;"
                    validElem = self.collRelValid.find_one({"uri": uriRel})
                    if(validElem != None):
                        if((validElem["nbVal"] >= limitNbValidator) and (validElem["nbNotVal"] == 0)):
                            loutre = 42
                        else:
                            allValid = False
                    else:
                        allValid = False

            if(allValid):
                relCandidateValidated +=1
        print str(relCandidateValidated)+ " / "+str(relCandidate)
        precisionRel = float(relCandidateValidated)/float(relCandidate)
        return precisionRel
    def getRecallRelation(self, limitTrustScore, limitNbValidators):
        nbRelValidate = 0
        nbRelCandidate = 0
        for item in self.collRelValid.find():
            if((item["nbVal"] >= limitNbValidators) and (item["nbNotVal"] == 0)):
                nbRelValidate += 1
                uri1 = self.__findSubString(item["uri"], "&lt;", "&gt;")
                uri2 =  self.__findSubStringLast(item["uri"], "&lt;", "&gt;")
                for elem in self.collRelCandidate.find({"ic":{"$elemMatch":{"uri" : uri1} }, "icHR":{"$elemMatch":{"uri":uri2}}}):
                    if(elem['trustScore'] > limitTrustScore):
                        nbRelCandidate +=1
                        break
        print str(nbRelCandidate)+" / "+str(nbRelValidate)
        recall =float(nbRelCandidate)/float(nbRelValidate) 
        return recall
    def getPrecisionType(self, trustScore, limitNbValidator):
        typeCandidate = 0
        typeCandidateValidated = 0
        for item in self.collTypeCandidate.find({"trustScore" : {"$gt": trustScore}}):
            typeCandidate += 1
            allValid = True
            ic = item["ic"]
            typeURI = item["typeURI"]
            for elem in ic:
                uri = elem["uri"]
                #source = elem["source"]
                uriType = "&lt;"+uri+"&gt; a &lt;"+typeURI+"&gt;"
                validElem = self.collTypeValid.find_one({"uri": uriType})
                if(validElem != None):
                    if((validElem["nbVal"] >= limitNbValidator) and (validElem["nbNotVal"] == 0)):
                        loutre = 42
                    else:
                        allValid = False
                else:
                    allValid = False

            if(allValid):
                typeCandidateValidated +=1
        print str(typeCandidateValidated)+ " / "+str(typeCandidate)
        precisionRel = float(typeCandidateValidated)/float(typeCandidate)
        return precisionRel
    def getRecallType(self, limitTrustScore, limitNbValidators):
        nbTypeValidate = 0
        nbTypeCandidate = 0
        for item in self.collTypeValid.find():
            if((item["nbVal"] >= limitNbValidators) and (item["nbNotVal"] == 0)):
                nbTypeValidate += 1
                uri = self.__findSubString(item["uri"], "&lt;", "&gt;")
                typeUri =  self.__findSubStringLast(item["uri"], "&lt;", "&gt;")
                for elem in self.collTypeCandidate.find({"ic":{"$elemMatch":{"uri" : uri} }, "typeURI": typeUri}):
                    if(elem['trustScore'] > limitTrustScore):
                        nbTypeCandidate +=1
                        break
        print str(nbTypeCandidate)+" / "+str(nbTypeValidate)
        recall =float(nbTypeCandidate)/float(nbTypeValidate) 
        return recall
    def __getLabelFromSource(self, labels, source):
        ret = None
        for label in labels:
            if label["source"] == source:
                ret = label["label"]
                break
        return ret
    def getPrecisionLabels(self, trustScore, limitNbValidator):
        labelsCandidate = 0
        labelsCandidateValidated = 0
        for item in self.collLabelsCandidate.find({"trustScore" : {"$gt": trustScore}}):
            allValid = True
            ic = item["ic"]
            typeURI = item["type"]
            for elem in ic:
                labelsCandidate += 1
                uri = elem["uri"]
                source = elem["source"]
                uriIC = "&lt;"+uri+"&gt;"
                label = self.__getLabelFromSource(item["labels"], source)
                if label != None:
                    validElem = self.collLabelsValid.find_one({"uri":  {"$regex" : uriIC+".*"+label}})
                    if(validElem != None):
                        if((validElem["nbVal"] >= limitNbValidator) and (validElem["nbNotVal"] == 0)):
                            loutre = 42
                        else:
                            allValid = False
                    else:
                        allValid = False

                if(allValid):
                    labelsCandidateValidated +=1
        print str(labelsCandidateValidated)+ " / "+str(labelsCandidate)
        precisionRel = float(labelsCandidateValidated)/float(labelsCandidate)
        return precisionRel
    def getRecallLabels(self, limitTrustScore, limitNbValidators):
        nbLabelsValidate = 0
        nbLabelsCandidate = 0
        for item in self.collLabelsValid.find():
            if((item["nbVal"] >= limitNbValidators) and (item["nbNotVal"] == 0)):
                nbLabelsValidate += 1
                uri = self.__findSubString(item["uri"], "&lt;", "&gt;")
                for elem in self.collLabelsCandidate.find({"ic":{"$elemMatch":{"uri" : uri} }}):
                    if(elem['trustScore'] >= limitTrustScore):
                        nbLabelsCandidate +=1
                        break
        print str(nbLabelsCandidate)+" / "+str(nbLabelsValidate)
        recall =float(nbLabelsCandidate)/float(nbLabelsValidate) 
        return recall
    def removeCollCandidate(self):
        self.collCandidate.remove({})
    def removeCollValid(self):
        self.collValid.remove({})
