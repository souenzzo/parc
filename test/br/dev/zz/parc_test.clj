(ns br.dev.zz.parc-test
  (:require [br.dev.zz.parc :as parc]
            [br.dev.zz.parc.ring :as parc.ring]
            [clojure.string :as string]
            [clojure.test :refer [deftest is]]))

(deftest parse
  (let [in (.getBytes (string/join "\n"
                        ["machine example.com login daniel password qwerty"
                         "default login foo"
                         "machine example.com"
                         "login daniel"
                         "password qwerty"]))]
    (is (= [{:machine "example.com", :login "daniel", :password "qwerty"}
            {:default true, :login "foo"}
            {:machine "example.com", :login "daniel", :password "qwerty"}]
          (-> in
            parc/parse
            #_(doto clojure.pprint/pprint))))))

(deftest ring-with
  (let [in (.getBytes (string/join "\n"
                        ["machine example.com login daniel password qwerty"
                         "default login foo"
                         "machine example.com"
                         "login daniel"
                         "password qwerty"]))]
    (is (= {:server-name "example.com",
            :headers     {"Authorization" "Basic ZGFuaWVsOnF3ZXJ0eQ=="}}
          (-> in
            (parc.ring/with {:server-name "example.com"})
            #_(doto clojure.pprint/pprint))))))
(comment
  (parc.ring/with
    {:server-name "api.heroku.com"
     :uri         "/apps/atemoia"
     :scheme      :https
     :headers     {"Accept" "application/vnd.heroku+json; version=3"}}))
