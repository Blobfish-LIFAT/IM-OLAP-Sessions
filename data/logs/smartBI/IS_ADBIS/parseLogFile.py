# -*- coding: utf-8 -*-
"""
Created on Mon Jan 22 15:26:55 2018

@author: mahfoud
"""

import os
import csv
import shutil
import string
import numpy as np
from numpy import genfromtxt

#%%
# file to parse
# fileToParse     = "log-smartBI.csv"
fileToParse     = "fullLog-smartBI.csv"
colToExtract    = ("rowid", "session", "analysis", "userId", "cube" ,"Measures" ,"GroupBy" ,"Filters", "nbCells")

# loaded data
data            = genfromtxt(fileToParse, dtype=None, delimiter=';', names=True, usecols=colToExtract)

# some tests using the data object
type(data)
data[1]["session"]

#
nbOfLines   = data.size

# directory containing session files
directory   = "logs"
if not os.path.exists(directory):
    os.makedirs(directory)
else:
    shutil.rmtree(directory)
    os.makedirs(directory)
    
# split global file into smaller files
for x in np.nditer(data):
    currentFileName     = string.join(("user-", str(x["userId"]) , "_", "session-", str(x["session"]), "_", "analysis-", str(x["analysis"]), ".csv"),"")
    completeFileName    = string.join((directory, "/", currentFileName), "")
    fileAlreadyExists   = os.path.exists(completeFileName)
    # if the file does not exist, we put the first line
    if(not fileAlreadyExists):
        currentFileHandle   = open(completeFileName, "a")
        # /!\ array transform the tuple data.dtype.names into an array
        # and np.newaxis adds a axis of length one so that the array is considered as multidimensional
        # because a 1d vector is treated as a columnd by default with python
        np.savetxt(currentFileHandle, array(data.dtype.names)[np.newaxis], delimiter=";", fmt="%s")
    # header line added
    currentFileHandle   = open(completeFileName, "a")
    np.savetxt(currentFileHandle, array(x).reshape(1,), delimiter=";", fmt="%s")
    #print "hehe"
    #print x["rowid"]
    #print "\n",

#%%
# write the extracted columns to a new file
reducedFileName = "reducedLog.csv"
np.savetxt(reducedFileName, data, delimiter=";", fmt="%s")

#%%




#csvFile     = open(fileToParse)
#reader      = csv.reader(csvFile, delimiter=',', quotechar='"')


