---
title: "Biff Tutorial"
date: 2023-10-17T19:48:08-04:00
lastmod: 2023-10-17T19:48:08-04:00
---

Here are my notes as I go through [the Biff tutorial][1].

I've made a [github repo here][2].

I spotted something that didn't work quite right. So I opened an issue and PR on the main repo. Here
are those links: [issue][3] and [PR][4].

So I got to a point where the tutorial wanted me to hook up an email sending service to get login
information sent out to users. I knew I already had a Resend account, so I opted to hook that up
instead.

And instead of figuring out the API myself, I'm just using [charm's pop][5] app and shelling out to it.


[1]: https://biffweb.com/docs/tutorial/build-a-chat-app/
[2]: https://github.com/luandy64/biff-tutorial
[3]: https://github.com/jacobobryant/eelchat/issues/2
[4]: https://github.com/jacobobryant/eelchat/pull/3
[5]: https://github.com/charmbracelet/pop
