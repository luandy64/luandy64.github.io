---
title: "Magit: Multiple Push Remotes"
date: 2023-12-01T11:06:44-05:00
lastmod: 2023-12-01T11:36:54-05:00
tags:
 - emacs
 - magit
---

I wanted to do the equivalent of this, but in magit
```shell
git remote rename origin github

git remote add codeberg https://codeberg.org/jkreeftmeijer/ox-md-title.el.git
git remote add origin https://codeberg.org/jkreeftmeijer/ox-md-title.el.git

git remote set-url --add --push origin https://codeberg.org/jkreeftmeijer/ox-md-title.el.git
git remote set-url --add --push origin https://github.com/jeffkreeftmeijer/ox-md-title.el

git remote show origin

* remote origin
  Fetch URL: https://codeberg.org/jkreeftmeijer/ox-md-title.el.git
  Push  URL: https://codeberg.org/jkreeftmeijer/ox-md-title.el.git
  Push  URL: https://github.com/jeffkreeftmeijer/ox-md-title.el
  HEAD branch: main
  Remote branch:
    main new (next fetch will store in remotes/origin)
  Local ref configured for 'git push':
    main pushes to main (up to date)
```
[Source](https://jeffkreeftmeijer.com/git-multiple-remotes/)


Renaming was easy:
- `M r`, `origin`, `andylu`

Adding another remote was also simple:
- `M a`, `github`, `git@github.com:blah.git`

Adding origin back in was just:
- `M a`, `origin`, `ssh://git@gitea.andylu.dev:1702/blah.git`

Setting multiple push remotes took bit of work to figure out. Ultimately, you just give it a
comma-space separated list of push URLs:
- `M C`, `origin`, `s`, `ssh://git@gitea.andylu.dev:1702/blah.git, git@github.com:blah.git`

Last thing was setting the push remote default back to `origin`:
- `P C`, hit `p` until `origin` is highlighted
