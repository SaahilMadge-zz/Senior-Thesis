
# coding: utf-8

# In[8]:

import os
import numpy as np


# In[45]:

class triple_reader:
    
    def __init__(self, filename):
        
#     filename = ("/Users/SaahilM/Documents/Princeton/Academics/Thesis/"
#     "Senior Thesis Code/ModifiedEntityGraph/prod/MCTest/production/MCTest/testOutput.txt")

        tripleList = []

        f = open(filename, 'r')
        for triple in f:
            # remove brackets, commas
        #      print triple
            # print triple[1:-1]
            # print triple[2:-3]
            formattedTriple = np.array(triple[2:-2].strip().split(","))
        #     print formattedTriple
            remPOS = np.vectorize(lambda x: x.strip().split("/")[0])
            # remove part of speech
            newFormattedTriple = remPOS(formattedTriple)
        #     print newFormattedTriple
            tripleList.append(newFormattedTriple)
        self.tripleList = np.array(tripleList)
    #     print tripleList
    #     print len(tripleList)
        # Create the entity-relation tensor
        enSet = set()
        relSet = set()
        for triple in tripleList:
            # Add the two entities and relation to the set
            en1 = triple[0]
            rel = triple[1]
            en2 = triple[2]
            enSet.add(en1)
            enSet.add(en2)
            relSet.add(rel)
        #     for word in triple:
        #         if word not in wordSet:
        #             wordSet.add(word)
#         print enSet
#         print relSet
        numEns = len(enSet)
        numRels = len(relSet)
#         print numEns
#         print numRels

        # create a mapping of entities to integers and reverse
        enMap = {}
        relMap = {}
        counter = 0
        for en in enSet:
            enMap[en] = counter
            counter += 1
        counter = 0
        for rel in relSet:
            relMap[rel] = counter
            counter += 1
        revEn=dict([reversed(i) for i in enMap.items()])
        enMap.update(revEn)
        revRel=dict([reversed(i) for i in relMap.items()])
        relMap.update(revRel)
#         print ''
#         print enMap
#         print relMap

        # make entity-relation tensor now
        tensor = []
        for i in xrange(numRels):
            tensor.append(np.zeros((numEns, numEns)))
        #tensor = np.array(tensor)

        for triple in tripleList:
            en1 = triple[0]
            rel = triple[1]
            en2 = triple[2]

            tensor[relMap[rel]][enMap[en1]][enMap[en2]] = 1
#         print np.sum(tensor)
        
        self.enSet = enSet
        self.relSet = relSet
        self.enMap = enMap
        self.relMap = relMap
        self.tensor = tensor


# In[ ]:



