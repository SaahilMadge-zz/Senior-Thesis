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
