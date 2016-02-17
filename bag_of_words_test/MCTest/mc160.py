# coding: utf-8
import pandas as pd
import re
import nltk.data
import operator
from sklearn.feature_extraction.text import CountVectorizer

# Import the dataset
train = pd.read_csv("mc160.train.tsv", delimiter="\t", header=None)
print train.shape
columnArray = ["id",
				"properties",
				"story",
				"Q1: question",
				"Q1: answerA",
				"Q1: answerB",
				"Q1: answerC",
				"Q1: answerD",
				"Q2: question",
				"Q2: answerA",
				"Q2: answerB",
				"Q2: answerC",
				"Q2: answerD",
				"Q3: question",
				"Q3: answerA",
				"Q3: answerB",
				"Q3: answerC",
				"Q3: answerD",
				"Q4: question",
				"Q4: answerA",
				"Q4: answerB",
				"Q4: answerC",
				"Q4: answerD",
				]

train.columns = columnArray

# Preprocess the stories. Make everything lower-case.
# Remove the \\\newline, \\tab, quotes from the stories (column 4)
# Also split it into a list of sentences, so we can easily create the vectorizer

stories = train["story"]
sentence_splitter = nltk.data.load('tokenizers/punkt/english.pickle')

def cleanStory(story):
	story = story.lower()
	tabsOut = re.sub("(\\\\tab)"," ",story)
	newlineOut = re.sub("(\\\\newline)"," ",tabsOut)
	quotesOut = re.sub("\"","", newlineOut)

	# split into list of sentences, remove the punctuation at the end of every sentence
	split = sentence_splitter.tokenize(quotesOut)
	noPunctuation = map(lambda x: x[:-1], split)
	return noPunctuation

# Clean every story
train["story"] = stories.apply(cleanStory)

# Now we can create the bag-of-words arrays and models for each question
bow_models = []
bow_fits = []

for story in stories:
	vectorizer = CountVectorizer(analyzer = 'word', tokenizer = None,preprocessor = None, stop_words = 'english')
	# print i,stories[i]
	bow_fits.append(vectorizer.fit_transform(story).toarray())
	bow_models.append(vectorizer)

train['bow_models'] = bow_models
train['bow_fits'] = bow_fits

# Now we will transform set of words in question and answers
def wordifyQuestionAnswerPair(question, a1, a2, a3, a4):
	# remove question mark, split into words, remove one/multiple annotation, and append answers
	q_list = question[:-1].split(' ')[1:]
	return  + answer.split(' ')

def andArray(a1, a2):
	a1 = a1[0]
	assert (a1.shape[0] == a2.shape[0])
	sum = 0
	for i in xrange(len(a1)):
		if (a1[i] == 1 and a2[i] == 1):
			sum = sum + 1
	return sum

def scoreAnswer(qa, vectorizer, sentences_array):
	# print qa
	transformedIndex = vectorizer.transform(qa).toarray()
	scores = []
	for sentence_array in sentences_array:
		# print 'transformed: ',
		# print transformedIndex 
		# print 'sentence: ',
		# print sentence_array
		score = andArray(transformedIndex,sentence_array)
		scores.append(score)
	# print scores
	return max(scores)

def makeDictionary(kv_list):
	kv_dict = {}
	for (key,value) in kv_list:
		kv_dict[key] = value
	return kv_dict

def pickBestAnswer(question, a_list, vectorizer, sentences_array):
	# print [(1,2) for answer in a_list]
	q = question[:-1].split(' ',1)[1] + ' '
	print q
	# print [q for answer in a_list]
	answer_score_pairs = [(answer, scoreAnswer([q + answer], vectorizer, sentences_array)) for answer in a_list]
	# print answer_score_pairs
	score_dict = makeDictionary(answer_score_pairs)
	return max(score_dict.iteritems(), key=operator.itemgetter(1))[0]

# a_list_01 = [train["Q1: answerA"][0], train["Q1: answerB"][0], train["Q1: answerC"][0], train["Q1: answerD"][0]]
# a_list_02 = [train["Q2: answerA"][0], train["Q2: answerB"][0], train["Q2: answerC"][0], train["Q2: answerD"][0]]
# a_list_03 = [train["Q3: answerA"][0], train["Q3: answerB"][0], train["Q3: answerC"][0], train["Q3: answerD"][0]]
# a_list_04 = [train["Q4: answerA"][0], train["Q4: answerB"][0], train["Q4: answerC"][0], train["Q4: answerD"][0]]
# vect_0 = train['bow_models'][0]
# sentences_array = train['bow_fits'][0]

# print pickBestAnswer(train["Q1: question"][0],a_list_01,vect_0, sentences_array)
# print pickBestAnswer(train["Q2: question"][0],a_list_02,vect_0, sentences_array)
# print pickBestAnswer(train["Q3: question"][0],a_list_03,vect_0, sentences_array)
# print pickBestAnswer(train["Q4: question"][0],a_list_04,vect_0, sentences_array)

for i in xrange(train.shape[0]):
	a_list_i1 = [train["Q1: answerA"][i], train["Q1: answerB"][i], train["Q1: answerC"][i], train["Q1: answerD"][i]]
	a_list_i2 = [train["Q2: answerA"][i], train["Q2: answerB"][i], train["Q2: answerC"][i], train["Q2: answerD"][i]]
	a_list_i3 = [train["Q3: answerA"][i], train["Q3: answerB"][i], train["Q3: answerC"][i], train["Q3: answerD"][i]]
	a_list_i4 = [train["Q4: answerA"][i], train["Q4: answerB"][i], train["Q4: answerC"][i], train["Q4: answerD"][i]]
	vect_i = train['bow_models'][i]
	sentences_array = train['bow_fits'][i]
	print pickBestAnswer(train["Q1: question"][i],a_list_i1,vect_i, sentences_array)
	print pickBestAnswer(train["Q2: question"][i],a_list_i2,vect_i, sentences_array)
	print pickBestAnswer(train["Q3: question"][i],a_list_i3,vect_i, sentences_array)
	print pickBestAnswer(train["Q4: question"][i],a_list_i4,vect_i, sentences_array)



