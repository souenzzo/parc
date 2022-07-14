(ns br.dev.zz.parc
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.spec.alpha :as s]))

(defn parse-netrc-lines
  [rf]
  (let [*entry (atom nil)]
    (fn
      ([] (rf))
      ([coll]
       (some->> *entry deref (rf coll))
       (rf coll))
      ([coll el]
       (cond
         (string/starts-with? el "default")
         (let [kvs (rest (string/split el #"\s+"))
               [old _] (reset-vals! *entry (into {:default true}
                                             (map (fn [[k v]]
                                                    [(keyword k) v]))
                                             (apply array-map kvs)))]
           (if old
             (rf coll old)
             coll))
         (string/starts-with? el "machine")
         (let [kvs (string/split el #"\s+")
               [old _] (reset-vals! *entry (into {}
                                             (map (fn [[k v]]
                                                    [(keyword k) v]))
                                             (apply array-map kvs)))]
           (if old
             (rf coll old)
             coll))
         :else (let [kvs (string/split (string/triml el)
                           #"\s+")]

                 (when (even? (count kvs))
                   (swap! *entry merge (into {}
                                         (map (fn [[k v]]
                                                [(keyword k) v]))
                                         (apply array-map kvs))))
                 coll))))))

(defn parse
  [netrc]
  (with-open [rdr (io/reader netrc)]
    (into [] parse-netrc-lines
      (line-seq rdr))))

(s/fdef parse
  :args (s/cat :netrc any?)
  :ret (s/coll-of map?))
