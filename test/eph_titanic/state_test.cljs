(ns eph-titanic.state-test
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [eph-titanic.state :as state]
            [eph-titanic.component :as com]
            [cljs.test :refer-macros [deftest is testing run-tests async]]
            [cljs.core.async :as async]))

(deftest eval-tag-test
  ;; TODO: add tests for logger
  ;; create-table
  (with-redefs [com/id->coord-str (constantly nil)]
    (let [t1 {:rows 1 :cols 2}
          s1 (state/eval-tag (atom state/init-state) nil (constantly nil) :create-table t1)]
      (is (= (merge state/init-state {:table-update 1} t1)
             s1))
      (is (= (merge s1 {:table-update 2})
             (state/eval-tag (atom s1) nil (constantly nil) :create-table t1)))
      ;; enter
      (is (= [:x "(1, 2)" [3 4]]
             (with-redefs [com/show! #(vector %1 %2 %3)]
               (state/eval-tag nil {:coords :x} nil :enter [[1 2] [3 4]]))))
      ;; move
      (is (= [:x "(1, 2)" [3 4]]
             (with-redefs [com/show! #(vector %1 %2 %3)]
               (state/eval-tag nil {:coords :x} nil :move [[1 2] [3 4]]))))

      ;; scroll
      (is (= [:x "(1, 2)"]
             (with-redefs [com/show! #(vector %1 %2)]
               (state/eval-tag nil {:coords :x} nil :scroll [1 2]))))

      ;; leave
      (is (= :x
             (with-redefs [com/hide! identity]
               (state/eval-tag nil {:coords :x} nil :leave :y))))

      ;; cell-click
      (is (= {:selected-set #{"c1"}}
             (state/eval-tag (atom {:selected-set nil}) nil (constantly nil) :cell-click ["c1" "r0"])))
      (is (= {:selected-set #{"c1"}}
             (state/eval-tag (atom {:selected-set #{"c99"}}) nil (constantly nil) :cell-click ["c1" "r0"])))
      (is (= {:selected-set #{}}
             (state/eval-tag (atom {:selected-set #{"c1"}}) nil (constantly nil) :cell-click ["c1" "r0"])))

      ;; cell-double-click
      (is (= {:selected-set #{"r0"}}
             (state/eval-tag (atom {:selected-set nil}) nil (constantly nil) :cell-double-click ["c1" "r0"])))
      (is (= {:selected-set #{"r0"}}
             (state/eval-tag (atom {:selected-set #{"c99"}}) nil (constantly nil) :cell-double-click ["c1" "r0"])))
      (is (= {:selected-set #{}}
             (state/eval-tag (atom {:selected-set #{"r0"}}) nil (constantly nil) :cell-double-click ["c1" "r0"])))

      ;; cell-right-click
      (is (= {:selected-set #{"c1"}}
             (state/eval-tag (atom {:selected-set nil}) nil (constantly nil) :cell-right-click ["c1" "r0"])))
      (is (= {:selected-set #{"c1" "c99"}}
             (state/eval-tag (atom {:selected-set #{"c99"}}) nil (constantly nil) :cell-right-click ["c1" "r0"])))
      (is (= {:selected-set #{}}
             (state/eval-tag (atom {:selected-set #{"c1"}}) nil (constantly nil) :cell-right-click ["c1" "r0"])))

      ;; selection-delete
      (is (= (merge state/init-state {:selected-set nil})
             (state/eval-tag (atom (merge state/init-state {:selected-set nil})) nil (constantly nil) :selection-delete nil)))
      (is (= (merge state/init-state {:selected-set nil})
             (state/eval-tag (atom (merge state/init-state {:selected-set #{"c1" "r2" "c99"}})) nil (constantly nil) :selection-delete nil)))

      ;; default
      (is (= :default-ignore
             (state/eval-tag nil nil nil :random-command nil))))))

(defn ^:export run []
  (run-tests))
