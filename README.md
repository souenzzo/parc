# parc

> `.netrc` support for clojure

This library implements a parser to turn `.netrc` files into idiomatic clojure data structures.

It also bundles some utilities functions, like ring utilities.

## Background

- [What is ring](https://github.com/ring-clojure/ring#ring)
- [What is netrc](https://everything.curl.dev/usingcurl/netrc)

## The problem

Many providers, like AWS, create custom `credentials` files, custom formats, etc, to store its credentials.

These credentials are all stored at our home folder.

This is a problem to manage access, do backups, etc.

## The solution

One standard credentials description format, many implementations.

The `.netrc` file tries to addess this issue in a unix way.

A few, but relevant, clients like `curl`, `GNU Inetutils`, `heroku-cli` support it.

Using `netrc` file, you can specify credentials for multiple domains, and any client can use it with any transport
protocol (http, ftp, etc...).

You can also encrypt your `.netrc` file with `gnupg`, avoiding writing credentials in plaintext 
(not implemented, yet). 

## Usage

Add to your `deps.edn` file:

```clojure
br.dev.zz/parc {:git/url "https://github.com/souenzzo/parc"
                :git/sha "..."}
```

Default namespaces for the examples

```clojure
(require '[br.dev.zz.parc :as parc]
          [br.dev.zz.parc.ring :as parc.ring])
```

You can add the credentials to a `ring-request` using `parc.ring/with`

By default, it will search in your `~/.netrc` file. If it find a  `machine` that matches with your `server-name`, it
will add the `Authorization` header.

```clojure
(parc.ring/with {:server-name "example.com"})
=> {:server-name "example.com",
    :headers     {"Authorization" "Basic ZGFuaWVsOnF3ZXJ0eQ=="}}
```

You can also specify a custom `netrc` file if you want to.

```clojure
(parc.ring/with (io/file "custom.netrc") {:server-name "example.com"})
=> {:server-name "example.com",
    :headers     {"Authorization" "Basic ZGFuaWVsOnF3ZXJ0eQ=="}}
```

A custom `netrc` file should look like this:

```netrc
machine api.example.com
  login my-username
  password my-password
```

You can also parse the `netrc` file. 

```clojure
(parc/parse (io/file "my-netrc"))
=> [{:machine  "api.example.com"
     :login    "my-username"
     :password "my-password"}]
```

## Usage with hato

> [hato](https://github.com/gnarroway/hato) is a http client for clojure that
> implements [ring](https://github.com/ring-clojure/ring)

Just call `parc.ring/with` in the request map, before pass it to `hato/request`

```clojure
(-> {:server-name "example.com"
     :scheme      :https
     :server-port 8080}
  ;; `with` will try to find `machine example.com` in your ~/.netrc
  ;; if it exists, it will add the credentials to the headers.
  parc.ring/with
  hato/request) 
```

## references

- [inetutils](https://www.gnu.org/software/inetutils/manual/html_node/The-_002enetrc-file.html)
- [curl](https://everything.curl.dev/usingcurl/netrc)
- [netrc](https://www.labkey.org/Documentation/wiki-page.view?name=netrc)
- [go](https://github.com/heroku/go-netrc)
- [ruby](https://github.com/heroku/netrc)
- [javascript](https://github.com/CamShaft/netrc)
- [javascript (node)](https://github.com/jdxcode/node-netrc-parser)
- [haskell](https://hackage.haskell.org/package/netrc)
- [man page](https://linux.die.net/man/5/netrc)

## credentials file

> See this [xkcd comic about standards](https://xkcd.com/927/)

- [aws](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html)
- [gcp](https://cloud.google.com/docs/authentication/getting-started)
- [azure](https://docs.microsoft.com/en-us/cli/azure/authenticate-azure-cli)

## TODO

- Final API with better names
- Check `java.net.Authenticator` API
- `cljc` support
- Support clj-http
- Support clj-http.lite
- Support comments
- Support macros
- Support gpg
