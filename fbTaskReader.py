import re
from sklearn.feature_extraction.text import CountVectorizer

# This will be a class to read in + format sentences from bAbl tasks
#class vectorize_bAbl_task:

class VectorizeTask:
    def __init__(self, filename):
        self.filename = filename
        self.readFile()
        self.bagOfWords()
        self.createInputMatrices()
        print 'finished constructor'  
    
    def readFile(self):
        f_train = open(self.filename + "_train.txt")
        self.stories = []
        cur_story = []
        for line in f_train:
            split_line = line.split(' ',1)
            ID = split_line[0]
            if (int(ID) == 1):
                self.stories.append(cur_story)
                cur_story = []
            cur_story.append(split_line[1].lower())
        # append the last line, remove the blank at the beginning
        self.stories.append(cur_story)
        self.stories = self.stories[1:]
    
    def printStory(self):
        print self.stories
        
    def bagOfWords(self):
        vectorizer = CountVectorizer(analyzer = "word", tokenizer = None, preprocessor=None, 
                                     stop_words=None,max_features=5000)
        combinedSentences = [' '.join([sentence for story in self.stories for sentence in story])]
        combinedSentences = [re.sub("\d", "", combinedSentences[0])]
#         print combinedSentences
        train_data_features = vectorizer.fit_transform(combinedSentences)
#         train_data_features = vectorizer.fit_transform([story for story in self.stories])
#         print self.stories[0]
#         print vectorizer.get_feature_names()
#         print train_data_features
#         print '--------'
        self.vectorizer = vectorizer
        self.train_data_features= train_data_features
        
    def createInputMatrices(self):
        #print self.vectorizer.get_feature_names()
        #print self.vectorizer.transform(["the to"])
        text_question_pairs = []
        for story in self.stories:
            text = ""
            for sentence in story:
                if "?" not in sentence:
                    text += sentence
                else:
                    text_vector = self.vectorizer.transform([text])
                    q_vector = self.vectorizer.transform([sentence])
                    text_question_pairs.append({"question": q_vector, "text": text_vector,
                                                "original_question": sentence, "original_text": text})
        self.text_question_pairs = text_question_pairs
    
    def getTextQuestionPairs(self):
        return self.text_question_pairs


# In[178]:
'''
task = VectorizeTask("/Users/SaahilM/Documents/Princeton/Academics/Thesis/Data/tasks_1-20_v1-2/en/qa1_single-supporting-fact")
task.readFile()
task.bagOfWords()
task.createInputMatrices()


# In[ ]:
'''


