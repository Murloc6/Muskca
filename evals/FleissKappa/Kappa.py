class Kappa:
    def __init__(self, data, N, n, k):
        self.data = data
        self.N = float(N)
        self.n = float(n)
        self.k = k
        self.p = [0 for i in range(k)]
        self.P = [0 for i in range(N)]
    def getKappa(self):
        i = 0
        j = 0
        for l in self.data:
            j = 0
            val = 0
            for e in l:
                self.p[j] += e
                val += (e**2)
                j += 1
            val = (val - self.n) / (self.n*(self.n-1))
            self.P[i] = val
            i += 1
        sumP = 0
        for Pi in self.P:
            sumP += Pi
        PBarre = sumP/self.N
        PBarree = 0
        for pi in self.p:
            PBarree += ((pi/(self.N*self.n))**2)
        print "PBarre "+str(PBarre)+" -- PeBarre "+str(PBarree)
        if(PBarree == 1):
            kappa = 0
        else:
            kappa = (PBarre - PBarree)/(1 - PBarree)
        return kappa
    def getRatio(self):
        nbTotal = 0
        nbAgree = 0
        for l in self.data:
            nbTotal += 1
            if ((l[0] == 0) and (l[1] > 1)) or ((l[1] == 0) and (l[0] > 1)):
                nbAgree += 1
        return float(nbAgree)/float(nbTotal)
    
