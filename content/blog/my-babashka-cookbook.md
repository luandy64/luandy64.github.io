---
title: "My Babashka Cookbook"
date: 2023-10-29T23:26:22-04:00
lastmod: 2023-12-22T13:46:36-05:00
tags:
- babashka
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

## Building a CLI tool

### Super basic call
A basic call to `babashka.cli/parse-args`

```clojure
(cli/parse-args *command-line-args*)
```

```shell
alu-field-selection new-catalog -o new-catalog-selected
{:args [new-catalog], :opts {:o new-catalog-selected}}
```

### Adding a help command
For parsing the help flag, I like to just do a first pass and look the `-h` or `--help`

```clojure

(defn show-help []
  (println "TODO"))

(defn passed-help-arg? [args]
  (let [options (cli/parse-args args {:alias {:h :help}
                                      :coerce {:help :boolean}})]
    (when (-> options :opts :help)
      (show-help)
      (System/exit 0))))

(when (= *file* (System/getProperty "babashka.file"))
  (passed-help-arg? *command-line-args*)

  (apply main *command-line-args*))
```
I think it's a nice approach to say my script will never try to handle your other input if there is
a help arg passed in.

### Supporting short and long option flags

```clojure
(cli/parse-args args
                {:alias {:o :output}})
```

```shell
alu-field-selection new-catalog --output new-catalog-selected
{:args [new-catalog], :opts {:output new-catalog-selected}}
```

### Coerce args to opts

Sometimes you want to consider any args passed in to be grouped into some option keys

```clojure
(cli/parse-args args
                {:alias      {:o :output}
                 :args->opts [:input]})
```

```shell
alu-field-selection new-catalog --output new-catalog-selected
{:opts {:input new-catalog, :output new-catalog-selected}}
```

### Requiring input

You can cause the script to exit when certain input isn't given

```clojure
(defn show-help [& args]
  (println "TODO Write show-help"))

(defn exit-on-error [_]
  (show-help)
  (System/exit 1))

(cli/parse-args args
                {:alias      {:o :output}
                 :args->opts [:input]
                 :require    [:input]
                 :exec-args  {:output "NEW-catalog"}
                 :error-fn   exit-on-error})
```

```shell
alu-field-selection
TODO Write show-help
```

This lets us reuse the help function we already wrote and by adding a custom `:error-fn`, we can
avoid dumping a stack trace for the end user.

Babashka can give a little context to `exit-on-error` too.

```clojure
(defn exit-on-error [{:keys [msg]}]
  (println msg)
  (show-help)
  (System/exit 1))
```

```shell
alu-field-selection
Required option: :input
TODO Write show-help
```

### Interactive fuzzy file picker

By using `gum input` with a placeholder, we can show the user some default directory. If that's the
one they want, then hitting enter will return an empty string to our script. Couple that with the
`(or ... default-dir)` and you can actually save the `default-dir` as the choice.

To get the fuzzy find, we use `gum filter`. It expects newline separated input which is what
`babashka.process/shell` spits out. Finally, for the `filter`, I set a `:height 10` so that `gum`
doesn't try to fill the whole screen when it gets a ton of input

```clojure
(let [default-dir (System/getenv "PWD")
      chosen-dir  (or (-> (b/gum :input
                                 :placeholder default-dir)
                          :result
                          first)
                      default-dir)
      files       (-> {:out :string}
                      (bp/shell "ls" chosen-dir)
                      :out)]
  (->> (b/gum :filter :height 10 :in files)
       :result
       first))
```

```shell

```


















```clojure

```

```shell

```
