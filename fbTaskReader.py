
# coding: utf-8

# In[56]:

import re
import numpy as np
from sklearn.feature_extraction.text import CountVectorizer
import scipy.sparse

def special_hstack((a, b)):
    if a.shape[1] == 0:
        return b
    elif b.shape[1] == 0:
        return a
    else:
        return scipy.sparse.hstack((a, b))
    
def special_vstack((a, b)):
    if a.shape[0] == 0:
        return b
    elif b.shape[0] == 0:
        return a
    else:
        return scipy.sparse.vstack((a, b))

# This will be a class to read in + format sentences from bAbl tasks
#class vectorize_bAbl_task:

class VectorizeTask:
    def __init__(self, filename):
        self.filename = filename
        self.readFile()
        self.bagOfWords()
        self.createInputMatrices()
    
    def readFile(self):
        f_train = open(self.filename + "_train.txt")
        
        # Add each sentence in the story to the set of stories, resetting when ID hits 1
        self.stories = []
        cur_story = []
        for line in f_train:
            split_line = line.split(' ',1)
            ID = split_line[0]
            if (int(ID) == 1):
                self.stories.append(cur_story)
                cur_story = []
            cur_story.append(split_line[1].lower())
        # append the last line, remove the blank at the beginning. Remove numbers
        self.stories.append(cur_story)
        self.stories = self.stories[1:]
        self.stories = map(lambda x: map(lambda y: re.sub("\d", "", y), x), self.stories)
    
    def printStory(self):
        print self.stories
        
    def bagOfWords(self):
        vectorizer = CountVectorizer(analyzer = "word", tokenizer = None, preprocessor=None, 
                                     stop_words=None,max_features=5000)
        combinedSentences = [' '.join([sentence for story in self.stories for sentence in story])]
        train_data_features = vectorizer.fit_transform(combinedSentences)
#         train_data_features = vectorizer.fit_transform([story for story in self.stories])
#         print self.stories[0]
#         print vectorizer.get_feature_names()
#         print train_data_features
#         print '--------'
        self.num_words = len(vectorizer.get_feature_names())
        self.vectorizer = vectorizer
        self.train_data_features= train_data_features
        
    def createInputMatrices(self):
        text_question_pairs = []
        for story in self.stories:
            text = ""
            textMatrix = scipy.sparse.random(0, self.num_words)
            # If it's not a question, just add it to the text. We are just performing one-hot encoding
            # on each sentence anyway
            for sentence in story:
#                 print sentence
                if "?" not in sentence:
                    text += sentence
                    transformedSentence = self.vectorizer.transform([sentence])
                    textMatrix = special_vstack((textMatrix, transformedSentence))
                else:
                    split_q = sentence.rsplit(' ', 1)
                    query = split_q[0]
                    
                    # NOTE: FOR THEANO ONE-HOT ENCODING, YOU HAVE ONLY 1 ELEMENT PER ROW, with the value
                    # representing the index (0-18) of the 1
                    # So [0,0,1,...0] should be [2] b/c the 1 is at index 2
                    answer = split_q[1]
                    q_vector = self.vectorizer.transform([query]).toarray()
                    # we do a first 0 to get the first row of the 2d array, then we have to do
                    # np.where, which returns a tuple with an array, so we need to index by 0
                    # again
                    transformed_answer = self.vectorizer.transform([answer]).toarray()[0]
                    assert np.sum(transformed_answer) == 1
#                     print 'here'
#                     print transformed_answer
#                     print np.where(transformed_answer == 1)[0][0]
                    a_vector = np.array([np.where(transformed_answer == 1)[0][0]])
                    text_question_pairs.append({"question": q_vector, "answer": a_vector,
                                                "text": textMatrix.toarray(),
                                                "original_question": query, "original_answer": answer,
                                                "original_text": text})
        self.text_question_pairs = text_question_pairs
#         print text_question_pairs
#         print text_question_pairs[0]["text"]
#         print text_question_pairs[0]
#         print text_question_pairs[0]["original_question"]
#         print text_question_pairs[0]["text"]
    
    def getTextQuestionPairs(self):
        return self.text_question_pairs
    
    def getVectorizer(self):
        return self.vectorizer
    
    def getNumWords(self):
        return len(self.vectorizer.vocabulary_)
    
    def getNumTrainingExamples(self):
        return len(self.text_question_pairs)


# In[55]:

task = VectorizeTask("/Users/SaahilM/Documents/Princeton/Academics/Thesis/Data/tasks_1-20_v1-2/en/qa1_single-supporting-fact")
qaPairs = task.getTextQuestionPairs()
# print qaPairs[0]
# print qaPairs[1]
# print type(qaPairs[0]["text"])
# print qaPairs[0]["text"]
# print qaPairs[0]["question"]
# print qaPairs[0]["answer"]
# print task.getNumWords()
# print task.getNumTrainingExamples()


# In[ ]:




# In[ ]:



