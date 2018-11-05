import numpy as np
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import sys
import math

selected = sys.argv[1]
#colors = { 0.1:"blue", 0.2:"blue", 0.3:"yellow", 0.4:"red", 0.5:"orange", 0.5:"cyan", 0.6:"black", 0.7:"magenta", 0.8:"grey", 0.9:"green"}
explos = ["Explorative", "Slice and Drill", "Goal Oriented", "Slice All", "Page Rank"]
colors = {"Explorative":"green", "Slice and Drill":"yellow", "Goal Oriented":"orange", "Slice All":"cyan", "Page Rank":"grey"}
index = {"Explorative":0, "Slice and Drill":1, "Goal Oriented":2, "Slice All":3, "Page Rank": 4}
matrix = [[],[],[],[], []]

with open("log1.txt") as f:
	for line in f:
		line = line.split(";")
		if line[1] == selected:
			data = []
			
			p = line[2].split(",")
			tmp = []
			for i in range(len(p)):
				tmp.append(float(p[i])/1000.0)

			tmp.sort()
			tmp.reverse()
			data.extend(tmp)
			matrix[index[line[0]]].append(data)
			
for explo in explos:
	a = np.matrix(matrix[index[explo]])
	mean = np.mean(a, axis=0).tolist()[0]
	std = np.std(a, axis=0).tolist()[0]
	x = list(range(len(mean)))
	plt.errorbar(x, mean, yerr=std, marker='.', ls='-', color=colors[explo])
	#plt.errorbar(x, data, yerr=std, fmt=None)

patches = []
for explo in explos:
	patches.append(mpatches.Patch(color=colors[explo], label=explo))

plt.legend(handles=patches)
plt.ylabel("PR Probability")
plt.xlabel("alpha = " + selected)
#plt.ylim(0.0, 0.07)
ax  = plt.gca()
ax.set_xticklabels([])
plt.show()