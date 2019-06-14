import numpy as np
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import sys

if len(sys.argv) <= 1:
    print("Usage: python plot_fig.py alpha [fontsize]")
selected = sys.argv[1]
fontsize = 18
if len(sys.argv) > 2:
    fontsize = int(sys.argv[2])

explos = ["Explorative", "Slice and Drill", "Goal Oriented", "Slice All", "Page Rank"]
colors = {"Explorative": "green", "Slice and Drill": "yellow", "Goal Oriented": "orange", "Slice All": "cyan",
          "Page Rank": "grey"}
index = {"Explorative": 0, "Slice and Drill": 1, "Goal Oriented": 2, "Slice All": 3, "Page Rank": 4}
matrix = [[], [], [], [], []]

l_count = 0
types = []

with open("result_dolap.csv") as f:
    for line in f:
        line = line.split(";")
        if l_count == 0:
            types = line[2].split(",")
            l_count += 1
            continue

        if line[1] == selected:
            data = []

            p = line[2].split(",")
            tmp = []
            for i in range(len(p)):
                tmp.append(float(p[i]))

            tmp.sort()
            tmp.reverse()
            data.extend(tmp)
            matrix[index[line[0]]].append(data)
        l_count += 1

for explo in explos:
    a = np.matrix(matrix[index[explo]])
    mean = np.mean(a, axis=0).tolist()[0]
    std = np.std(a, axis=0).tolist()[0]
    x = list(range(len(mean)))
    plt.errorbar(x, mean, yerr=std, marker='.', ls='-', color=colors[explo])
# plt.errorbar(x, data, yerr=std, fmt=None)

patches = []
for explo in explos:
    patches.append(mpatches.Patch(color=colors[explo], label=explo))

plt.legend(handles=patches, prop={'size': fontsize})
plt.ylabel("User Belief Probabilities", fontsize=fontsize)
plt.xlabel("Query parts", fontsize=fontsize)

plt.yscale("log")

plt.ylim(0.0, 0.05)
ax = plt.gca()
ax.set_xticklabels([])
ax.yaxis.set_tick_params(labelsize=fontsize)

fig = plt.gcf()
fig.set_size_inches(100, 60)
plt.savefig("fig.jpeg")

plt.show()
