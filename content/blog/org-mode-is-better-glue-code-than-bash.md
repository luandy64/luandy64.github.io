---
title: "Org Mode Is Better Glue Code Than Bash"
date: 2023-12-04T21:47:32-05:00
lastmod: 2023-12-04T22:44:17-05:00
---

# TLDR
This post is just to document a scenario I put myself in. But I wanted to write it down in case I
need to use it again in the future.

# Problem
- You have a CSV file from your bank with some expenses listed.

- You want to generate an PDF invoice file to send to someone and collect some money.

# Solution

I think the solution is to
1. import the CSV into an org table
2. use your favorite programming language to format the data
3. use [maaslalani/invoice][1] to quickly make a PDF

## Deep Dive

When I'm dealing with CSV files, I often reach for `org-mode` in `emacs` just to make the file
easier to read.

For example, this is a mess

```csv
Transaction Date,Post Date,Description,Category,Type,Amount,Memo
12/01/2023,12/02/2023,Big Expense Number 1,Shopping,Sale,-2414.73,
12/02/2023,12/03/2023,Some Other Expense,Personal,Sale,-95.00,
12/03/2023,12/04/2023,This Expense You Don't Recognize,Groceries,Sale,-20.82,
```

But if you copy this file -- honestly I just open the original --, open in `org-mode`, `C-x h` to
select the entire buffer, `M-x org-mode`, and `M-x org-table-create-or-convert-from-region`, then
you get this:

```org-mode
| Transaction Date | Post Date  | Description                      | Category  | Type |   Amount | Memo |
| 12/01/2023       | 12/02/2023 | Big Expense Number 1             | Shopping  | Sale | -2414.73 |      |
| 12/02/2023       | 12/03/2023 | Some Other Expense               | Personal  | Sale |   -95.00 |      |
| 12/03/2023       | 12/04/2023 | This Expense You Don't Recognize | Groceries | Sale |   -20.82 |      |
```

I always separate the header row from the body, so just add `|-|` as a new line 2 and hit tab. Which
turns this

```org-mode
| Transaction Date | Post Date  | Description                      | Category  | Type |   Amount | Memo |
|-|
| 12/01/2023       | 12/02/2023 | Big Expense Number 1             | Shopping  | Sale | -2414.73 |      |
| 12/02/2023       | 12/03/2023 | Some Other Expense               | Personal  | Sale |   -95.00 |      |
| 12/03/2023       | 12/04/2023 | This Expense You Don't Recognize | Groceries | Sale |   -20.82 |      |
```

into this

```org-mode
| Transaction Date | Post Date  | Description                      | Category  | Type |   Amount | Memo |
|------------------+------------+----------------------------------+-----------+------+----------+------|
| 12/01/2023       | 12/02/2023 | Big Expense Number 1             | Shopping  | Sale | -2414.73 |      |
| 12/02/2023       | 12/03/2023 | Some Other Expense               | Personal  | Sale |   -95.00 |      |
| 12/03/2023       | 12/04/2023 | This Expense You Don't Recognize | Groceries | Sale |   -20.82 |      |
```

If you look through the `README` for [invoice][1], you'll see that you can specify multiple `--item
.. --quantity ...` lines in one `invoice generate call`. I took a guess that this applies to
`--rate` as well. But this is perfect for the problem I have. I just need to turn my table into a
big block of shell code.

`org-mode` lets you name the tables you create and pass them to various code blocks as input. I
won't go through the following clojure at all, but the intent is "given an org table as input,
select the relevant columns, and format them into code we can hand to `invoice`".

```clojure
(let [[header & data] input
      rows            (->> data
                           (reduce (fn [state row]
                                     (conj state
                                           (zipmap header
                                                   row)))
                                   []))]
  (doseq [{:strs [Description Amount]} (butlast rows)]
    (println (format "--item \"%s\" --quantity 1 --rate %.2f \\"
                     Description Amount)))
  (let [{:strs [Description Amount]} (last rows)]
    (print (format "--item \"%s\" --quantity 1 --rate %.2f \\"
                   Description Amount))))
```

Here's the org block I needed
```org-mode
#+NAME: table-to-lines
#+begin_src clojure :colnames no :var input=table-of-data :exports results :results output
  <<all of the clojure from above>>
#+end_src
```
- `#+NAME: table-to-lines` is the variable name for this code block
  - This will help me export the code to a particular spot later
- `:colnames no` tells `org-babel` to not strip the header row
- `:var input=table-of-data` binds `input` to some thing I named `table-of-data`
  - I just slapped a `#+NAME: table-of-data` onto the org table from before
  - Because this is a clojure source, I think it turned the table into seq of seqs
    - `((header1 header2 header3) (r1c1 r1c2 r1c3) (r2c1 r2c2 r2c3) ...)`
- `:exports results` tells `org-babel` later to export this block as only the results, not the code
  itself
- `:results output` says to collect everything printed as the result, not just the final return
  value

The one thing I do want to say about the clojure is that I needed to separate the final row from the
others because the final `println` and whatever `org-babel` is doing resulted in my output having a
blank line at the end. This caused issues in my final script, and I couldn't find the answer on how
to clean that up through `org-mode`. So the next best thing was to just modify my code a bit.
Thankfully clojure has `butlast` and `last`.

You can run this and should see the folowing output:

```org-mode
#+RESULTS: table-to-lines
: --item "Big Expense Number 1" --quantity 1 --rate -2414.73 \
: --item "Some Other Expense" --quantity 1 --rate -95.00 \
: --item "This Expense You Don't Recognize" --quantity 1 --rate -20.82 \
```

This is the meat of the script I want to generate. The next part is to write the two source blocks
that are needed to round out the command. You can imagine it looks something like this

```org-mode
#+NAME: make-invoice-start
#+begin_src shell
  invoice generate \
    --from "Andy, Inc." \
    --to "Owes Me Money" \
#+end_src

#+NAME: make-invoice-end
#+begin_src shell
    --tax 0 --discount 0.99 \
    --note "TEST Don't actually pay this"
#+end_src
```

So now that I have 3 named source code blocks, I can create a script that is generated from this org
file I've been working in

```org-mode
#+begin_src shell :dir /tmp/ :tangle make-invoice :noweb yes
  <<make-invoice-start>>
    <<table-to-lines()>>
    <<make-invoice-end>>
#+end_src
```

This says, on tangle, create a file called `make-invoice` and concatenates the contents of the
following named blocks as the file. The parens in `table-to-lines()` say to evaluate that block
before tangling. And remember that I set an option on that block to export the results instead of
the code. So `make-invoice` has the command I want to call.

To actually generate that file, run `M-x org-babel-tangle`.

You can run it from inside `org-mode` too. Just add a block like this and `C-c C-c`
```org-mode
#+begin_src shell :dir /tmp/
  bash ./make-invoice
#+end_src
```

I'm going to leave it as a challenge for the reader to tweak this solution so that I only need to
combine two source blocks instead of my three.

[1]: https://github.com/maaslalani/invoice/tree/0fb2e9d84385c6393ca6925bc6d25a89555b0b2d
