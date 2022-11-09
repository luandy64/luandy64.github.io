---
title: "Git Submodules"
date: 2022-11-08T23:00:56-05:00
lastmod: 2022-11-08T23:00:56-05:00
tags:
- hugo
- git
- golang
---

The first time I learned about `git submodules` was actually from working with
[hugo][1].

[The documentation used to][2] describe how you can have your main branch live
in a directory that points to a diffent origin than your current branch.

It's a cool feature that I never remember how to use, so here are notes I took
after rediscovering it recently.

---

To add a submodule

```shell
git submodule add https://github.com/some-other-repo
```

To clone a submodule locally

```shell
git submodule update
```

To remove a submodule

```shell
git rm path/to/the/submodule
git commit -m 'Removed a submodule'
```

---

Related to the use case described by [the hugo docs][2], `git worktree` can also
make another branch live in a directory.

Take the repo I use to manage this site, for example. I want to write on the
`dev` branch, so my repo is checking out `dev` most of the time.

But `hugo` builds to a folder called `public`. And I want all of the contents of
`public` to be the stuff that lives on my `master` branch. So all I have to do
is add a worktree in my directory under the name `public`. That makes the folder
exist as `master`, so then every time `hugo` builds and changes something in
there, I just add everything and commit to deploy the site.

[1]: https://gohugo.io/
[2]: https://github.com/gohugoio/hugo/blob/e48ffb763572814a3788780bb9653dfa2daeae22/content/en/hosting-and-deployment/hosting-on-github.md#step-by-step-instructions
