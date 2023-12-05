---
title: "Python Packaging"
date: 2023-12-01T12:13:04-05:00
lastmod: 2023-12-01T12:13:04-05:00
tags:
 - python
---

I didn't know what to use between `poetry` and `hatch`.

I just followed the guide [from the Packaging Guide](https://packaging.python.org/en/latest/guides/writing-pyproject-toml/#).

It ended with me having the following directory structure

```text
.
├── pyproject.toml
├── README.org
├── src
│   └── andy_aoc_2023
│       ├── day1.py
│       └── __init__.py
└── tests
```

Which isn't too crazy I guess.
