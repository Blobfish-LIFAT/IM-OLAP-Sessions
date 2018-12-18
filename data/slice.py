outDir = "session_set_3/"
inFile = "sessions_belief.txt"

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
            
