(ns br.dev.zz.parc.dev
  (:require [br.dev.zz.parc :as parc]
            [br.dev.zz.parc.ring :as parc.ring]
            [clj-http.client :as clj-http]
            [clj-http.lite.client :as chl]
            [clojure.string :as string]
            [hato.client :as hato]
            [io.pedestal.connector :as conn]
            [io.pedestal.http.jetty :as jetty])
  (:import (java.util Base64)))

(defonce *server
  (atom nil))

(defn start
  []
  (swap! *server
    (fn [st]
      (some-> st conn/stop!)
      (-> 8080
        conn/default-connector-map
        (conn/with-interceptor
          {:name  ::echo
           :enter (fn [{:keys [request]
                        :as   ctx}]
                    (let [auth (some-> request
                                 :headers
                                 (get "authorization")
                                 (string/split #"\s" 2)
                                 second
                                 (->> str
                                   (.decode (Base64/getDecoder)))
                                 slurp)]
                      (assoc ctx
                        :response {:body   auth
                                   :status 200})))})
        (jetty/create-connector nil)
        conn/start!))))

(defn req!
  []
  (let [parc-file (.getBytes (string/join "\n"
                               ["machine localhost"
                                "  login hello"
                                "  password world"]))
        hato (-> {:server-name "localhost"
                  :scheme      :http
                  :server-port 8080}
               (->> (parc.ring/with parc-file))
               hato/request
               :body)
        chl (-> {:url     "http://localhost:8080"
                 :method  :get
                 :headers {"Authorization" (parc/authorization-for
                                             {:login    "hello"
                                              :password "world"})}}
              chl/request
              :body)
        clj-http (-> {:url     "http://localhost:8080"
                      :method  :get
                      :headers {"Authorization" (parc/authorization-for
                                                  {:login    "hello"
                                                   :password "world"})}}
                   clj-http/request
                   :body)]
    {:hato     hato
     :chl      chl
     :clj-http clj-http}))

(comment
  (start)
  (req!))

