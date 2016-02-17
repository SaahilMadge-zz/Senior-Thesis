# coding: utf-8

# Import the pandas package, then use the "read_csv" function to read the labeled training data
import pandas as pd
train = pd.read_csv('labeledTrainData.tsv', header=0, delimiter="\t",quoting=3)
train.shape
train.columns.values
print train['review']
print train['review'][0]
# Import BeautifulSoup into your workspace
from bs4 import BeautifulSoup
# Initialize the BeautifulSoup object on a single movie review
example1 = BeautifulSoup(train['review'][0])
print example1.get_text()
import re
# Use regular expressions to do a find-and-replace
letters_only = re.sub("[^a-zA-Z]"," ", example1.get_text())
print letters_only
lower_case = letters_only.lower()
words = lower_case.split()
words
from nltk.corpus import stopwords
print stopwords.words("english")
# Remove stop words from "words"
words = [w for w in words if not w in stopwords.words("english")]
print words
def review_to_words(raw_review):
    # Function to convert a raw review to a string of words
    # The input is a single string (a raw movie review), and the output is a single string (preprocessed movie review)
    #1. Remove HTML
    review_text = BeautifulSoup(raw_review).get_text()
    #
    #2. Remove non-letters
    letters_only = re.sub("[^a-zA-Z]", " ", review_text)
    #
    # 3. Convert to lower case, split into individual words
    words = letters_only.lower().split()
    #
    # 4. In python, searching a set is much faster than searching a list, so convert the stopwords to a set
    stops = set(stopwords.words("english"))
    # 
    # 5. Remove stop words
    meaningful_words = [w for w in words if not w in stops]
    #
    # 6. Join the words back into one string separated by space, and return the result
    return ( " ".join(meaningful_words))
clean_review_1 = review_to_words(train["review"][0])
clean_review_1
# Get number of reviews based on the dataframe column size
num_reviews = train["review"].size
# Initialize an empty list to hold the clean reviews
clean_train_reviews = []
# Loop over each review and clean it
for i in xrange(0,num_reviews):
    clean_train_reviews.append(review_to_words(train["review"][i]))
    if (i % 1000 == 0):
        print "cleaning Review %d" % (i*1000)
        
len(clean_train_reviews)
print "Creating the bag of words...\n"
from sklearn.feature_extraction.text import CountVectorizer
# Initialize the "CountVectorizer" object, which is sklearn bag-of-words
vectorizer = CountVectorizer(analyzer = "word", tokenizer = None, preprocessor = None, stop_words = None, max_features = 5000)
vectorizer
train_data_features = vectorizer.fit_transform(clean_train_reviews)
vectorizer.fit_transform(["hello this is a bag of words bag word"])
vectorizer.fit_transform(["hello this is a bag of words bag word"]).toarray()
vectorizer.fit_transform(["hello this is a bag of words bag word", "another this is bag"]).toarray()
vectorizer.get_feature_names()
