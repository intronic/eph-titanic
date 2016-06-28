(ns eph-titanic.event-test
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [eph-titanic.event :as event]
            [cljs.test :refer-macros [deftest is testing run-tests async]]
            [goog.events :as gevents]
            [cljs.core.async :as async])
  (:import [goog.events EventType KeyCodes KeyHandler]
           [goog.dom DomHelper]))

(deftest listener-state
  (is (= {} event/init-state))
  ;; after setup there are two controls with listeners
  (is (= {:main 8 :table-spec 1}
         (into {} (map (fn [[k v]] [k (count v)]) @event/listener-state)))))

(deftest watcher-state
  (let [a (atom #{})]
    (with-redefs [gevents/unlistenByKey (partial swap! a conj)]
      (event/watcher nil nil {} {})
      (is (= #{} @a))
      (reset! a #{})
      (event/watcher nil nil {:x [1 2 3]} {})
      (is (= #{1 2 3} @a))
      (reset! a #{})
      (event/watcher nil nil {:x [1 2 3] :y [4 5 6]} {})
      (is (= #{1 2 3 4 5 6} @a))
      (reset! a #{})
      (event/watcher nil nil {:x [1 2 3] :y [4 5 6]} {:y [4 5 6]})
      (is (= #{1 2 3} @a))
      (reset! a #{})
      (event/watcher nil nil {:x [1 2 3] :y [4 5 6]} {:x [1 3] :y [4 5]})
      (is (= #{2 6} @a)))))

(defn ^:export run []
  (run-tests))
