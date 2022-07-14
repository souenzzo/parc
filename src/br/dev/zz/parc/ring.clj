(ns br.dev.zz.parc.ring
  (:refer-clojure :exclude [for])
  (:require [br.dev.zz.parc :as parc]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s])
  (:import (java.nio.charset StandardCharsets)
           (java.util Base64)))

(defn for
  [entries {:keys [server-name]}]
  (or
    (first (filter (comp #{server-name} :machine)
             entries))
    (first (filter :default entries))))

(s/fdef for
  :args (s/cat :entries (s/coll-of map?)
          :ring-request map?)
  :ret (s/? map?))


(def *std-netrc-file
  (delay (or (System/getenv "NETRC")
           (io/file (System/getProperty "user.home")
             ".netrc"))))

(defn with
  ([request] (with @*std-netrc-file
               request))
  ([netrc request]
   (let [{:keys [login password]} (for (parc/parse netrc) request)]
     (cond-> request
       (and login password)
       (assoc-in [:headers "Authorization"]
         (str "Basic "
           (.encodeToString (Base64/getEncoder)
             (.getBytes (str login ":" password)
               StandardCharsets/UTF_8))))))))

(s/fdef for
  :args (s/cat :netrc (s/? any?)
          :ring-request map?)
  :ret (s/? map?))
