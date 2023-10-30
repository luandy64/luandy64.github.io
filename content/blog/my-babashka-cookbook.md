---
title: "My Babashka Cookbook"
date: 2023-10-29T23:26:22-04:00
lastmod: 2023-10-30T00:08:59-04:00
---

This post is the one I want to reference when I do something in `babashka` and forget syntax or
something.

Capture a process's `STDOUT` and process the string after

```clojure
(require '[babashka.process :as bp])

(-> (bp/shell {:out :string} "do-the-thing")
    :out
    (clojure.string/split #"\n"))
```

