
# coding: utf-8

# In[19]:

import numpy as np
import io


# In[22]:

class question_reader:
    
    def __init__(self, filename):
#         filename = ("/Users/SaahilM/Documents/Princeton/Academics/Thesis/"
#         "SAT Test Pictures/2005 10 SAT collegeboard/TextFiles/3/short1-questions.txt")
#         questionSet = open(filename, 'r')
        
        with io.open(filename, 'r', encoding='utf-8') as questionSet:
            # read every six lines
            questions = map(lambda x: x.strip().replace("\\xe2\\x80\\x99","'"), questionSet.readlines())
            LINES_PER_QUESTION = 6
            numQuestions = len(questions)/LINES_PER_QUESTION
            print "numQuestions ", numQuestions
            questionCombos = []
            # for i in questions:
            #     print i
            for i in xrange(numQuestions):
                #remove number at top
                start_index = LINES_PER_QUESTION*i
                question = questions[start_index]
                question = " ".join(question.split(" ")[1:])

                #TODO
                #if "?" in question:
                #    continue

                # create a list with each answer combo
                a = " ".join(questions[start_index+1].split(" ")[1:])
                b = " ".join(questions[start_index+2].split(" ")[1:])
                c = " ".join(questions[start_index+3].split(" ")[1:])
                d = " ".join(questions[start_index+4].split(" ")[1:])
                e = " ".join(questions[start_index+5].split(" ")[1:])

                curquestion = []
                curquestion.append(question)
                curquestion.append(a)
                curquestion.append(b)
                curquestion.append(c)
                curquestion.append(d)
                curquestion.append(e)
    #             curquestion.append(question+" " + a)
    #             curquestion.append(question+" " + b)
    #             curquestion.append(question+" " + c)
    #             curquestion.append(question+" " + d)
    #             curquestion.append(question+" " + e)
                questionCombos.append(curquestion)

            self.questions = questions
            self.numQuestions = numQuestions
            self.questionCombos = questionCombos


# In[23]:

q_file = ("/Users/SaahilM/Documents/Princeton/Academics/Thesis/Senior Thesis Code/"
"ModifiedEntityGraph/prod/MCTest/production/MCTest/OCR_text/2/2-small1-q1.txt")
qr = question_reader(q_file)
questions = qr.questionCombos
print questions

