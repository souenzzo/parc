(ns br.dev.zz.parc-dev
  (:require [clojure.test :refer [deftest]]
            [io.pedestal.http :as http]
            [clj-http.client :as clj-http]
            [hato.client :as hato]
            [br.dev.zz.parc :as parc]
            [br.dev.zz.parc.ring :as parc.ring]
            [clj-http.lite.client :as chl]
            [io.pedestal.interceptor :as interceptor]
            [clojure.string :as string])
  (:import (java.util Base64)))

(defonce *server
  (atom nil))

(defn start
  []
  (swap! *server
    (fn [st]
      (some-> st http/stop)
      (-> {::http/port  8080
           ::http/interceptors
           [(interceptor/interceptor
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
                                       :status 200})))})]
           ::http/type  :jetty
           ::http/join? false}
        http/create-server
        http/start))))

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

