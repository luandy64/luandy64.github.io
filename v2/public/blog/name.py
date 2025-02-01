#!/usr/bin/env python3

import subprocess as sp

with open("a.txt", "r") as fileList:
  for line in fileList:
    
    
    cmd = "cat ./b.txt >> " + line.strip()
    
    sp.run(cmd, shell=True)
