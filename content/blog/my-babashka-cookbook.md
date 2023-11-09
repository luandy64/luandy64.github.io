---
title: "My Babashka Cookbook"
date: 2023-10-29T23:26:22-04:00
lastmod: 2023-10-30T00:08:59-04:00
---

This post is the one I want to reference when I do something in `babashka` and forget syntax or
something.

# Capture a process's `STDOUT` and process the string after

```clojure
(require '[babashka.process :as bp])

(-> (bp/shell {:out :string} "do-the-thing")
    :out
    (clojure.string/split #"\n"))
```

# Import a local library

## Dynamically

You can add deps as one of the first steps in your scripts.

```clojure
(require '[babashka.deps :as deps])
(deps/add-deps
 '{:deps {io.github.lispyclouds/bblgum {:git/sha "1d4de3d49b84f64d1b71930fa1161f8d2622a4d9"}
          dev.andylu/clj-lipgloss      {:local/root "/home/andy/git/clj-lipgloss"}}})

(require '[bblgum.core :as b])
(require '[clj-lipgloss.core :as lg :use [log-info log-warn log-debug]])
```

## Statically

Put this in a `bb.edn`

```clojure
{:deps {io.github.lispyclouds/bblgum {:git/sha "1d4de3d49b84f64d1b71930fa1161f8d2622a4d9"}
          dev.andylu/clj-lipgloss      {:local/root "/home/andy/git/clj-lipgloss"}}}
```

And then in your code

```clojure
(require '[bblgum.core :as b])
(require '[clj-lipgloss.core :as lg :use [log-info log-warn log-debug]])
```
