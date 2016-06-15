(ns eph-titanic.event-test
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [eph-titanic.event :as event]
            [cljs.test :refer-macros [deftest is testing run-tests async]]
            [cljs.core.async :as async])
  (:import [goog.events EventType KeyCodes KeyHandler]
           [goog.dom DomHelper]))

(deftest listener-state
  (is (= {} event/init-state))
  (is (= #{:table-control} (set (keys @event/listener-state)))))

#_(deftest listen-test
  (let [ch (async/chan)
        ;; Test by stubbing goog/listen to immediately call event fn with a simulated event.
        e1 (with-redefs [gevents/listen (fn [_el _type f] (println "OK FIRING EVENT") (f :bogus-event))]
             (event/listen ch nil nil :testme (constantly :ok)))]
    (async done
           (go
             (is (= [:testme :ok :wtf]
                    (let [res (async/<! ch)]
                      (println :WTF res)
                      res)))
             (done)))))

(defn ^:export run []
  (run-tests))
