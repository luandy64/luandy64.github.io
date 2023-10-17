---
title: "Babashka Datetime"
date: 2023-10-15T14:39:22-04:00
lastmod: 2023-10-15T14:39:22-04:00
tags:
- babashka
---

If I need a datetime in Python, I reach for

```python
from datetime import datetime

datetime.now()
```

So I need something as quick for clojure.

Apparently, it's just

```clojure
(-> (java.time.LocalDate/now)
     str)
;; => "2023-10-15"
```
