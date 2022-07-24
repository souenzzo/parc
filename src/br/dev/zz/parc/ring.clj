(ns br.dev.zz.parc.ring
  (:refer-clojure :exclude [for])
  (:require [br.dev.zz.parc :as parc]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]))

(s/def ::ring-request
  (s/keys :req-un [::server-name]))

(s/def ::server-name string?)

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
           (let [f (io/file (System/getProperty "user.home")
                     ".netrc")]
             (when (.exists f)
               f)))))

(defn with
  ([request] (with @*std-netrc-file
               request))
  ([netrc {:keys [server-name]
           :as   ring-request}]
   (let [parc (parc/find netrc server-name)]
     (cond-> ring-request
       (and
         (contains? parc :login)
         (contains? parc :password))
       (assoc-in [:headers "Authorization"]
         (parc/authorization-for parc))))))

(s/fdef for
  :args (s/or :default (s/cat :ring-request ::ring-request)
          :explicit (s/cat :netrc any?
                      :ring-request ::ring-request))
  :ret ::ring-request)
