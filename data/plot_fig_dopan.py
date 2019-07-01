import numpy as np
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import sys
from typing import List
from pprint import pprint

if len(sys.argv) <= 1:
    print("Usage: python plot_fig.py alpha [fontsize]")
selected = sys.argv[1]

fontsize = 72
if len(sys.argv) > 2:
    fontsize = int(sys.argv[2])

explos = ["dibstudent03", "dibstudent04", "dibstudent05", "dibstudent06", "dibstudent07", "dibstudent06_08",
          "dibstudent09", "dibstudent10", "dibstudent12", "dibstudent14", "dibstudent16", "Page Rank"]

colors = {"dibstudent03": "green", "dibstudent04": "red", "dibstudent05": "yellow", "dibstudent06": "orange",
          "dibstudent07": "navy", "dibstudent06_08": "gold",
          "dibstudent09": "pink", "dibstudent10": "purple", "dibstudent12": "lime", "dibstudent14": "teal",
          "dibstudent16": "darkgoldenrod", "Page Rank": "grey"}

index = {"dibstudent03": 0, "dibstudent04": 1, "dibstudent05": 2, "dibstudent06": 3, "dibstudent07": 4,
         "dibstudent06_08": 5,
         "dibstudent09": 6, "dibstudent10": 7, "dibstudent12": 8, "dibstudent14": 9, "dibstudent16": 10,
         "Page Rank": 11}

cubes = ["Cube1MobProInd", "Cube2MobScoInd", "Cube4Chauffage"]

for cube in cubes:
    matrix: List[List[List[float]]] = [[], [], [], [], [], [], [], [], [], [], [], []]
    with open("result_dopan_v5.csv") as f:
        for line in f:
            line = line.split(";")

            if line[2] == selected and line[0] == cube:
                data = []

                p = line[3].split(",")
                tmp = []
                for i in range(len(p)):
                    tmp.append(float(p[i]))

                #tmp.sort()
                tmp.reverse()
                data.extend(tmp)
                matrix[index[line[1]]].append(data)

    for explo in explos:
        mexplo = matrix[index[explo]]
        #Maybe there is no session for this user in the cube selected
        if len(mexplo) == 0:
            continue
        minSize = min(map(len, mexplo))

        # pprint(matrix[index[explo]])
        cmatrix = [np.array(l[0:minSize]) for l in mexplo]
        # pprint([arr.shape for arr in cmatrix])
        a = np.array(cmatrix)

        mean = np.mean(a, axis=0).tolist()
        # pprint(mean)
        std = np.std(a, axis=0).tolist()
        x = list(range(minSize))
        plt.errorbar(x, mean, yerr=std, marker='.', ls='-', color=colors[explo])
    # plt.errorbar(x, data, yerr=std, fmt=None)

    patches = []
    for explo in explos:
        patches.append(mpatches.Patch(color=colors[explo], label=explo))

    plt.legend(handles=patches, prop={'size': fontsize})
    plt.ylabel("User Belief Probabilities", fontsize=fontsize)
    plt.xlabel("Query parts", fontsize=fontsize)

    plt.yscale("log")

    plt.title("Estimated belief distributions for users on the '" + cube + "' cube. alpha=" + selected, fontdict={"fontsize" : fontsize})

    #plt.ylim(0.0, 0.05)
    plt.xlim(0, 5000)
    ax = plt.gca()
    ax.set_xticklabels([])
    ax.yaxis.set_tick_params(labelsize=fontsize)

    fig = plt.gcf()
    fig.set_size_inches(150, 75)
    plt.savefig("fig_dopan_"+cube+".jpeg")
    plt.clf()
    print(cube, "done")
