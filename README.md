# parc

> `.netrc` support for clojure

This library implements a parser to turn `.netrc` files into idiomatic clojure datastructures.

It also bundles some utilities functions, like ring utilities.

## usage

Add to your `deps.edn` file:

```clojure
br.dev.zz/parc {:git/url "https://github.com/souenzzo/parc"
                :git/sha "..."}
```

## references

- https://www.gnu.org/software/inetutils/manual/html_node/The-_002enetrc-file.html
- https://everything.curl.dev/usingcurl/netrc
- https://www.labkey.org/Documentation/wiki-page.view?name=netrc
