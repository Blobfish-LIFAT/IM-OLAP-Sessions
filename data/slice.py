outDir = "session_set_2/"
inFile = "/home/alex/stage/IM-OLAP/lib/Sessions.txt"

n = 1
init = False
current = None
with open(inFile) as f:
    for line in f:
        if init is False or  "---" in line:
            current = open(outDir+"session_"+str(n)+".txt", mode="w")
            if init is False:
                init = True
                current.write(line)
            n += 1
        else:
            current.write(line)
            
