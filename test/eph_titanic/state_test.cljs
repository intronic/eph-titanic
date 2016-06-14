(ns eph-titanic.state-test
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [eph-titanic.state :as state]
            [cljs.test :refer-macros [deftest is testing run-tests async]]
            [cljs.core.async :as async]))

(deftest eval-tag-test
  (let [t1 {:rows 1 :cols 2}]
    (let [s1 (state/eval-tag (atom state/init-state) :create-table t1)]
      (is (= (merge state/init-state {:table-update 1} t1)
             s1))
      (is (= (merge s1 {:table-update 2})
             (state/eval-tag (atom s1) :create-table t1))))))

(defn ^:export run []
  (run-tests))
