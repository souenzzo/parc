(ns br.dev.zz.parc.build
  (:require [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as dd]))

(def lib 'br.dev.zz/parc)

(def class-dir "target/classes")

(declare version)

(def *jar-file
  (delay
    (format "target/%s-%s.jar" (name lib) version)))

(defn clean
  []
  (b/delete {:path "target"}))

(defn jar
  []
  (let [basis (b/create-basis {:project "deps.edn"})]
    (b/write-pom {:class-dir class-dir
                  :lib       lib
                  :version   version
                  :basis     basis
                  :src-dirs  ["src"]
                  :scm       {:url                 "https://github.com/souenzzo/parc"
                              :connection          "scm:git:https://github.com/souenzzo/parc.git"
                              :developerConnection "scm:git:ssh:git@github.com:souenzzo/parc.git"
                              :tag                 version}
                  :pom-data  [[:description "netrc support for clojure"]
                              [:url "https://github.com/souenzzo/parc"]
                              [:licenses
                               [:license
                                [:name "Apache-2.0"]
                                [:url "https://www.apache.org/licenses/LICENSE-2.0.txt"]]]]})
    (b/copy-dir {:src-dirs   ["src"]
                 :target-dir class-dir})
    (b/jar {:class-dir class-dir
            :jar-file  @*jar-file})))

(defn deploy
  "Deploy the JAR to Clojars."
  [{:keys [installer]}]
  (dd/deploy {:installer      (or installer :remote)
              :artifact       (str @*jar-file)
              :sign-releases? true
              :pom-file       (b/pom-path
                                {:lib       lib
                                 :class-dir class-dir})}))

(comment

  (require 'br.dev.zz.parc.build)
  (in-ns 'br.dev.zz.parc.build)
  (def version "1.0.0")
  (require 'br.dev.zz.parc.build :reload)
  (b/delete {:path "target"})
  (jar)
  (deploy {:installer :local})
  (deploy {}))
