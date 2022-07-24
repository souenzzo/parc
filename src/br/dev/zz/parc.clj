(ns br.dev.zz.parc
  (:refer-clojure :exclude [find])
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.string :as string])
  (:import (java.nio.charset StandardCharsets)
           (java.util Base64)))

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

(defn ->netrc
  [x]
  (cond
    (coll? x) x
    :else (parse x)))

(defn authorization-for
  [{:keys [login password]}]
  (str "Basic "
    (.encodeToString (Base64/getEncoder)
      (.getBytes (str login ":" password)
        StandardCharsets/UTF_8))))

(defn find
  [x machine]
  (let [vs (->netrc x)]
    (or
      (first (filter (comp #{machine} :machine)
               vs))
      (first (filter :default vs)))))
