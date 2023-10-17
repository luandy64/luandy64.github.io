---
title: "Terminal Logging With Color"
date: 2023-10-14T21:25:56-04:00
lastmod: 2023-10-14T21:25:56-04:00
tags:
- story
- projects
- babashka
- clojure
---

These days I've been writing a lot of babashka instead of reaching for bash or python to do minor
scripting tasks at work.

I am also a big fan of the work done at [charm][1] and the relevant products in this case are
[gum][2] and [lipgloss][3].

Using `gum style` I can log any text in basically any color I want. And while there's not a ton of
functional benefits to this, I think there's something to be said about adding color into the logs
we write.

I just wanted to take the time to outline the steps I took to do all of the following
- Add a local dependency to a babashka script and import library code from a local directory
- How to write color to shell streams

## Colored Logging

I picked the two color palettes from [log][4] to emulate first.

## Local Dependencies





[1]: https://charm.sh
[2]: https://github.com/charmbracelet/gum
[3]: https://github.com/charmbracelet/lipgloss
[4]: https://github.com/charmbracelet/log
