---
title: "Clojurescript Project Setup"
date: 2020-07-06T07:52:15-05:00
description: "Noting how I like to setup cljs projects"
---

#### Outcomes

By the end of this post, you will be able to setup a new `clojurescript`
project, setup `emacs` to be your development environment, and deploy your
app to Github Pages

#### Background

I've written two clojurescript projects now, [Sundial][] and
[DataBooze][], and I want to document the set up process. The first time
was a massive undertaking because it was my first time. But frustrating
enough, so was the second time, even though I thought I nailed down the
process and took notes on it. The process didn't work and the notes don't
exist, so here we are today.

#### Pre-requisites

There are only three things you need to start: an editor, a terminal, and
[leiningen][]. For me, this means [Emacs][], [bash][], and `leiningen`
installed through my package manager.

#### Instructions

In your terminal, navigate to where you want the project to live and run

```bash
$ lein new figwheel-main my-awesome-project -- --reagent
```

You should see a new directory called `my-awesome-project`. There's a
basic skeleton that is created for you, so we want to check that it works

```bash
$ cd my-awesome-project
$ lein fig:build
```

This will compile the clojurescript into javascript, serve the project on
(by default) `http://localhost:9500`, and give you a clojurescript REPL.

If you don't use emacs, I recommend you leave this REPL open and start
editing the code in `src/my_awesome_project/core.cljs`. If you do use
emacs, I recommend setting up [CIDER][] and getting [nREPL][] to work.

First we open emacs and install CIDER

```
M-x package-refresh-contents [RET]
M-x package-install [RET] cider [RET]
```

When I run `M-x cider-version`, I get `CIDER 0.26.0-snapshot`.

If you haven't already, kill the REPL we opened from `lein fig:build`.
We're going to start a new one in emacs. 

Open `src/my_awesome_project/core.cljs` in a buffer. Then run

```
M-x cider-jack-in-cljs [RET]
```

A series of prompts will appear, answer with:
* `lein`
  * This one most likely will not appear, but I saw it once and the other
    option was `shadow-cljs`. I didn't try to pick `shadow-cljs` because
    it just worked when I picked `lein`
* `figwheel-main`
* `dev`

This will give you a buffer with output similar to what we got at the
terminal. You can edit the code in `core.cljs` and see the UI reload when
you save. More importantly, you can evaluate the clojurescript in your
buffer.

##### Deploying to Github Pages

I'm assuming this would work for Gitlab Pages or Bitbucket Pages, but I
use Github and didn't test those options.

Create a branch called `gh-pages`. You'll just need 3 files at the root of
your project: `index.html`, `style.css`, and `main.js`. Obviously, the
names are up to you, but here's where I got the files.

`index.html` is just some boilerplate HTML. Here's one you can use:

```html
<!DOCTYPE html>
<html>
  <head>
    <title>My title</title>
    <link rel="stylesheet" type="text/css" href="style.css"/>
  </head>
  <body>
    <div id="app"></div>
    <script src="main.js"></script>
  </body>
</html>
```

The important part here is that you have some element with `id="app"`
because that's how we hook up the javascript to the project. But again,
even that is configurable.

To get `style.css` and `main.js`, I run these commands at the root of the
project

```bash
$ lein fig:min
2020-07-06 09:13:50.378:INFO::main: Logging initialized @4805ms to org.eclipse.jetty.util.log.StdErrLog
[Figwheel] Validating figwheel-main.edn
[Figwheel] figwheel-main.edn is valid \(ツ)/
[Figwheel] Compiling build dev to "resources/public/cljs-out/dev-main.js"
[Figwheel] Successfully compiled build dev to "resources/public/cljs-out/dev-main.js" in 12.307 seconds.
$ cp resources/public/cljs-out/dev-main.js main.js
$ cp resources/public/css/style.css .
```

So now you can commit the HTML, CSS, and JS to the `gh-pages` branch,
push, turn on Github Pages deploys in the Settings tab of your repo, and
see your live project.








[Sundial]: /sundial
[DataBooze]: /databooze
[leiningen]: https://github.com/technomancy/leiningen/wiki/Packaging
[Emacs]: https://www.gnu.org/software/emacs/
[bash]: https://www.gnu.org/software/bash/
[CIDER]: https://github.com/clojure-emacs/cider
[nREPL]: https://github.com/nrepl/nrepl
