class Elem:
    def __init__(self, uri, n):
        self.valid = 0
        self.notValid = 0
        self.dontKnow = 0
        
        self.aegilops = 0
        self.triticum = 0
        self.oryza = 0
        self.dontKnowCat = 0
        
        self.uri = uri
        self.P = 0
        self.n = n
    def majElemValid(self):
        self.valid += 1
    def majElemNotValid(self):
        self.notValid += 1
    def majElemDontKnow(self):
        self.dontKnow += 1
    def isValid(self):
        return (self.valid>0 and self.notValid == 0)
    def __str__(self):
        return "Element  "+ self.uri+" (V:"+str(self.valid)+" -- NV:"+str(self.notValid)+" -- DK:"+str(self.dontKnow)+"-- P:"+str(self.P)+") \n"
    def __repr__(self):
        return self.__str__()