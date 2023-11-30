---
title: "Bash `while` loops"
date: 2023-11-27T15:54:59-05:00
lastmod: 2023-11-27T15:54:59-05:00
tags:
- bash
---

Here's a bash snippet I can see myself using one day

```bash
while :; do ls -lh "$( ls -t1 /tmp/tmp* | head -n 1 )"; sleep 10; done
```

- `"$( ls -t1 /tmp/tmp* | head -n 1 )"`: Get a file name
  - `ls -t1 /tmp/tmp*`: List the files in `/tmp/tmp*` ordered by most recently updated
  - `head -n 1`: Get the first one
- `ls -lh <one-file>`: Get the long listing of just one file
- The rest of it is just to do it over and over so that I can see changes in the file size
