from Elem import Elem
import pymongo
import MySQLdb


class Sessions:
    def __init__(self, sessionsName, sessions, n):
        self.sessionsName = sessionsName
        self.sessions = sessions
        self.validations = {}
        self.validationsRel = {}
        self.validationType = {}
        self.validationLabels = {}
        self.N = 0
        self.NRel = 0
        self.n = n
        db = MySQLdb.connect(host="127.0.0.1",
                             user="root", 
                              db="validRKB") 
        
        cur = db.cursor() 
        for session in sessions:
            cur.execute("SELECT * FROM TripleValidation WHERE idSession='"+session+"'")
            nbTaxon = 0
            nbNewUri = 0
            for row in cur.fetchall() :
                if(row[3] == 'Inst'):
                    nbTaxon += 1
                    elem = None
                    if(self.validations.has_key(row[2])):
                        elem = self.validations[row[2]]
                    else:
                        nbNewUri += 1
                        #print "new uri "+row[2]
                        elem = Elem(row[2], self.n)
                        self.validations[row[2]] = elem
                        self.N += 1
                    if(row[4] == "valid"):
                        elem.majElemValid()
                        if(row[1].endswith("Triticum")):
                            elem.triticum += 1
                        elif(row[1].endswith("Aegilops")):
                            elem.aegilops += 1
                        else:
                            elem.dontKnowCat += 1
                    elif(row[4] == "notValid"):
                        elem.majElemNotValid()
                        elem.dontKnowCat += 1
                    elif(row[4] == "dontKnow"):
                        elem.majElemDontKnow()
                        elem.dontKnowCat += 1
                    else:
                        loutre = 42
                    #print "ERROR !! "
            #print "Session : "+session+" --> "+str(nbTaxon) + "--> "+str(nbNewUri)
                elif(row[3] == 'hasHigherRank'):
                    elem = None
                    if(self.validationsRel.has_key(row[2])):
                        elem = self.validationsRel[row[2]]
                    else:
                        elem = Elem(row[2], self.n)
                        self.validationsRel[row[2]] = elem
                        self.NRel += 1
                    if(row[4] == "valid"):
                        elem.majElemValid()
                    elif(row[4] == "notValid"):
                        elem.majElemNotValid()
                    elif(row[4] == "dontKnow"):
                        elem.majElemDontKnow()
                    else:
                        loutre = 42
                elif(row[3] == 'Type'):
                    elem = None
                    if(self.validationType.has_key(row[2])):
                        elem = self.validationType[row[2]]
                    else:
                        elem = Elem(row[2], self.n)
                        self.validationType[row[2]] = elem
                    if(row[4] == "valid"):
                        elem.majElemValid()
                    elif(row[4] == "notValid"):
                        elem.majElemNotValid()
                    elif(row[4] == "dontKnow"):
                        elem.majElemDontKnow()
                    else:
                        loutre = 42
                elif(row[3] == 'Labels'):
                    elem = None
                    if(self.validationLabels.has_key(row[2])):
                        elem = self.validationLabels[row[2]]
                    else:
                        elem = Elem(row[2], self.n)
                        self.validationLabels[row[2]] = elem
                    if(row[4] == "valid"):
                        elem.majElemValid()
                    elif(row[4] == "notValid"):
                        elem.majElemNotValid()
                    elif(row[4] == "dontKnow"):
                        elem.majElemDontKnow()
                    else:
                        loutre = 42

    def getData(self):
        data = []        
        for elem in self.validations.values():
            valTab =[elem.valid, elem.notValid, elem.dontKnow]
            data.append(valTab)
        return data
    def getData2(self):
        data = []        
        for elem in self.validations.values():
            valTab =[elem.aegilops, elem.triticum, elem.dontKnowCat]
            data.append(valTab)
        return data
    def saveMongoDB(self, collection):
        client = pymongo.MongoClient("localhost", 27017)
        db = client.valRKB
        coll = db[collection]
        coll.remove({})
        for uri, elem in self.validations.items():
            #print elem
            coll.save({"uri": uri, "nbVal":elem.valid, "nbNotVal":elem.notValid, "nbDontKnow": elem.dontKnow})
        print "All "+self.sessionsName+" saved on the collection : "+collection
    def saveRelationMongoDB(self, collection):
        client = pymongo.MongoClient("localhost", 27017)
        db = client.valRKB
        coll = db[collection]
        coll.remove({})
        for uri, elem in self.validationsRel.items():
            #print elem
            coll.save({"uri": uri, "nbVal":elem.valid, "nbNotVal":elem.notValid, "nbDontKnow": elem.dontKnow})
        print "All "+self.sessionsName+" relations saved on the collection : "+collection
    def saveTypeMongoDB(self, collection):
        client = pymongo.MongoClient("localhost", 27017)
        db = client.valRKB
        coll = db[collection]
        coll.remove({})
        for uri, elem in self.validationType.items():
            #print elem
            coll.save({"uri": uri, "nbVal":elem.valid, "nbNotVal":elem.notValid, "nbDontKnow": elem.dontKnow})
        print "All "+self.sessionsName+" type saved on the collection : "+collection
    def saveLabelsMongoDB(self, collection):
        client = pymongo.MongoClient("localhost", 27017)
        db = client.valRKB
        coll = db[collection]
        coll.remove({})
        for uri, elem in self.validationLabels.items():
            #print elem
            coll.save({"uri": uri, "nbVal":elem.valid, "nbNotVal":elem.notValid, "nbDontKnow": elem.dontKnow})
        print "All "+self.sessionsName+" labels saved on the collection : "+collection
    def sessionsToString(self):
        return self.sessions
    def getn(self):
        return self.n
    def __repr__(self):
        return "Sessions "+self.sessionsName+" :"
        