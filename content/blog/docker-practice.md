+++
title = "KAllan's Telephone and Answering Machine Notes"
author = ["Andy"]
draft = false
date = 2021-04-11T00:00:00Z
description = "Practicing working with Docker"
+++

## Setup {#setup}


### Installing `docker` {#installing-docker}

I always like the DigitalOcean tutorials, so that's what I followed here. [Installing Docker on
Ubuntu 20.04](https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-ubuntu-20-04)

One note: I had to restart my computer instead of just logging out in order to get the docker group
update to take effect.


### Clone the repos {#clone-the-repos}

```shell
git clone git@github.com:KAllan357/answering-machine.git
git clone git@github.com:KAllan357/telephone.git
```

Nothing special here.


### Install Go {#install-go}

I just followed the instructions [here](https://golang.org/doc/install).


## Do the thing {#do-the-thing}


### Start the answering machine outside of `docker` {#start-the-answering-machine-outside-of-docker}

Running `go run main.go` seems to do a thing. And we get the `200` like the instructions say if you
`curl` it. Great, so it seems like we can expect the app to hang and just wait for requests. And to
log them when it handles them.


### Start the answering machine container {#start-the-answering-machine-container}

I need to build the thing first.

My `docker build .` hit this error

```shell
docker build .
Sending build context to Docker daemon  74.24kB
Step 1/10 : FROM debian:wheezy
wheezy: Pulling from library/debian
2b15b7abe8b3: Pull complete
Digest: sha256:2259b099d947443e44bbd1c94967c785361af8fd22df48a08a3942e2d5630849
Status: Downloaded newer image for debian:wheezy
 ---> 10fcec6d95c4
Step 2/10 : RUN apt-get update -y && apt-get install --no-install-recommends -y -q curl build-essential ca-certificates
 ---> Running in 900695c2fa97
Ign http://deb.debian.org wheezy Release.gpg
Ign http://deb.debian.org wheezy-updates Release.gpg
Ign http://deb.debian.org wheezy Release
Ign http://security.debian.org wheezy/updates Release.gpg
Ign http://deb.debian.org wheezy-updates Release
Ign http://security.debian.org wheezy/updates Release
Err http://security.debian.org wheezy/updates/main amd64 Packages

Err http://security.debian.org wheezy/updates/main amd64 Packages

Err http://security.debian.org wheezy/updates/main amd64 Packages

Err http://security.debian.org wheezy/updates/main amd64 Packages

Err http://security.debian.org wheezy/updates/main amd64 Packages
  404  Not Found [IP: 151.101.66.132 80]
Err http://deb.debian.org wheezy/main amd64 Packages
  404  Not Found
Err http://deb.debian.org wheezy-updates/main amd64 Packages
  404  Not Found
W: Failed to fetch http://deb.debian.org/debian/dists/wheezy/main/binary-amd64/Packages  404  Not Found

W: Failed to fetch http://deb.debian.org/debian/dists/wheezy-updates/main/binary-amd64/Packages  404  Not Found

W: Failed to fetch http://security.debian.org/debian-security/dists/wheezy/updates/main/binary-amd64/Packages  404  Not Found [IP: 151.101.66.132 80]

E: Some index files failed to download. They have been ignored, or old ones used instead.
The command '/bin/sh -c apt-get update -y && apt-get install --no-install-recommends -y -q curl build-essential ca-certificates' returned a non-zero code: 100
```

My first thought is maybe it has to do with `wheezy`? So I dropped that tag and it seems to run
fine. `docker images` showed me the successful build. Oh, I also bumped the `go` version because I
noticed my install grabbed `v1.6` instead of `v1.3`. I sure that won't affect this exercise very
much.

```diff
modified   Dockerfile
@@ -1,11 +1,12 @@
-FROM debian:wheezy
+FROM debian

 RUN apt-get update -y && apt-get install --no-install-recommends -y -q curl build-essential ca-certificates
-RUN mkdir /usr/local/go && curl https://storage.googleapis.com/golang/go1.3.linux-amd64.tar.gz | tar xvzf - -C /usr/local/go --strip-components=1
+RUN mkdir /usr/local/go && curl https://storage.googleapis.com/golang/go1.6.linux-amd64.tar.gz | tar xvzf - -C /usr/local/go --strip-components=1
```

Ah a simple `docker run -it <image id>` said it panicked because it's missing a config. So I'll add
it like the other files.

```diff
 ADD messages.html /app/
+ADD config.json /app/
```

A rebuild and a run looks just like the it did when I ran the `main.go`, so
let's try a curl.

It failed. I need to expose a port.

Adding a `-p 8080:8080` seemed to do the trick and I got "Jimbo's answering machine". A quick `ag`
says that's from the config. Seems like the instructions are out of date, it references a line 54
and an environment variable, but 54 is a blank line. And a config file controls the name now.

I don't want to think too hard about this, so I'm moving on to the telephone. But first, I was able
to get the container running in the background and tailing the logs.

```shell
docker run -dit -p 8080:8080 --name andy_am 94f348ecb597
docker logs -f andy_am
```


### Start the telephone container {#start-the-telephone-container}

Nice, telephone's Dockerfile is super bare - literally empty. Since I pulled the `debian` image
earlier, I thought I would use that as a base.

So I started with an interactive debian container to figure out how to install ruby and gems and
whatnot.

```shell
docker run -it debian bash
root@7c91165552bb:/#
```

Installing ruby was easy, and I got rubygems for free. Then installing a gem was easy. So I
threw all of that into the Dockerfile.

```diff
modified   Dockerfile
@@ -1,12 +1,15 @@
-# See the reference doc - http://docs.docker.com/reference/builder/
+FROM debian
+MAINTAINER luandy64

-# FROM - what is the base image for this Dockerfile? http://docs.docker.com/reference/builder/#from
-# MAINTAINER - who owns this Dockerfile? http://docs.docker.com/reference/builder/#maintainer
+RUN apt update

-# Install Ruby - perhaps from a package manager? http://docs.docker.com/reference/builder/#run
+RUN apt install -y ruby
+RUN gem install httparty
```

Now I just need to figure out how to run the ruby file in the container. So first we need to get the
ruby file into the container. Taking inspiration from the `answering-machine/Dockerfile`, I made
these changes to `telephone/Dockerfile`

```diff
modified   Dockerfile
@@ -1,12 +1,15 @@
-# See the reference doc - http://docs.docker.com/reference/builder/
+FROM debian
+MAINTAINER luandy64

-# FROM - what is the base image for this Dockerfile? http://docs.docker.com/reference/builder/#from
-# MAINTAINER - who owns this Dockerfile? http://docs.docker.com/reference/builder/#maintainer
+RUN apt update

-# Install Ruby - perhaps from a package manager? http://docs.docker.com/reference/builder/#run
+RUN apt install -y ruby
+RUN gem install httparty

-# Install the 'httparty' gem - how do you install gems? http://docs.docker.com/reference/builder/#run
+RUN mkdir /app
+ADD telephone.rb /app/

-# ADD the telephone(.rb) application - http://docs.docker.com/reference/builder/#add

-# Write a CMD - http://docs.docker.com/reference/builder/#cmd
+WORKDIR /app
+RUN ruby telephone.rb
```

Attempting the build now results in a failure because the `telephone.rb` script is unhappy. So I
comment it out and try to iterate on it. Here's the error I see

```shell
root@c59f07ad8b04:/app# ruby telephone.rb
Traceback (most recent call last):
        12: from telephone.rb:11:in `<main>'
        11: from /var/lib/gems/2.5.0/gems/httparty-0.18.1/lib/httparty.rb:631:in `post'
        10: from /var/lib/gems/2.5.0/gems/httparty-0.18.1/lib/httparty.rb:524:in `post'
         9: from /var/lib/gems/2.5.0/gems/httparty-0.18.1/lib/httparty.rb:594:in `perform_request'
         8: from /var/lib/gems/2.5.0/gems/httparty-0.18.1/lib/httparty/request.rb:145:in `perform'
         7: from /usr/lib/ruby/2.5.0/net/http.rb:1458:in `request'
         6: from /usr/lib/ruby/2.5.0/net/http.rb:909:in `start'
         5: from /usr/lib/ruby/2.5.0/net/http.rb:920:in `do_start'
         4: from /usr/lib/ruby/2.5.0/net/http.rb:935:in `connect'
         3: from /usr/lib/ruby/2.5.0/timeout.rb:103:in `timeout'
         2: from /usr/lib/ruby/2.5.0/timeout.rb:93:in `block in timeout'
         1: from /usr/lib/ruby/2.5.0/net/http.rb:936:in `block in connect'
/usr/lib/ruby/2.5.0/net/http.rb:939:in `rescue in block in connect': Failed to open TCP connection to localhost:8080 (Cannot assign requested address - connect(2) for "localhost" port 8080) (Errno::EADDRNOTAVAIL)
```

My first thought is maybe it doesn't want `localhost` but will take `127.0.0.1`. But that results in
a similar error. So I just tried the IP address of the `answering-machine` container. That worked, but I wanted
to make it configurable annd based on an environment variable.

After looking up how to grab env vars in ruby, I modified `telephone.rb` for real and rebuilt the
image.

```diff
-# FIXME
-url = "http://localhost:8080/messages"
+url = "http://#{ENV[ANSWERING_MACHINE_HOST]}:8080/messages"
```

It kept erroring on me, but I realized that I was calling `RUN ruby telephone.rb` instead of
`CMD ruby telephone.rb`. So let's make that change now.

```diff
-RUN ruby telephone.rb
+CMD ruby telephone.rb
```

It builds successfully now, but if I run the image I get an error about some constant `ANSWERING_MACHINE_HOST`.

Ha, you have to quote keys for maps. Duh.

```diff
- url = "http://#{ENV[ANSWERING_MACHINE_HOST]}:8080/messages"
+ url = "http://#{ENV["ANSWERING_MACHINE_HOST"]}:8080/messages"
```

I learned that ruby has a "safe get" from a map too. So I used that to set values for the sender and
message.

```diff
-# FIXME
-sender = "Telemarketer Bob"
+sender = ENV.fetch("SENDER", "Telemarketer Bob")

-# FIXME
-message = "I have a credit card offer you can't refuse."
+message = ENV.fetch("MESSAGE", "I have a credit card offer you can't refuse.")
```

Finally, the exercise mentions linking two containers. Since I already named the answering machine
`andy_am`, I start the telephone with

```shell
docker run -it -e ANSWERING_MACHINE_HOST='andy_am' --link andy_am 80ad0c4699d8
```

And we can check the logs of the answering machine with

```shell
docker logs -f andy_am
```

And with that I'm calling it done.


## Reflection {#reflection}

I think this exercise showed me that I know more about Docker than I give myself credit for. Also,
while I don't have the specifics of "my docker workflow" nailed down, my goal to always shrink the
iteration loop seems like a helpful approach. In this case, it led me to stage the container exactly
how I want it, but then I can run various commands iteractively to solve the problems I'm having.

I didn't know the difference between `RUN` and `CMD` in a Dockerfile. But it has completely
different outcomes on the build process. But just being me, I commented out the `RUN` to let the
image build successfully. Then I ran the container and iterated on the bug until I solved it. It's
the same process I used to install ruby on the container in the first place.

This is also a good example of how learning one language makes it easier to learn other languages. I
don't know ruby. But by knowing python, I quickly troubleshot:

-   installing a gem: since gem is to ruby as pip is to python
-   accessing environment variables from the script: I knew there would be an object of some king,
    hopefully a map. But it was also a simple google search
-   using a "safe get" on a map: this one felt like a bigger stretch in my mind, but it was also just
    a quick google search

There was a moment when I debated whether I should even bother with all of this ruby. The goal of
this exercise is to see what I know about docker, not ruby. And given that the `telephone.rb` file
is so simple, I debated just translating it straight to python (or even `curl`). But actully,
because it was so small and the idea of the program is so simple, I chose to stick it out.
