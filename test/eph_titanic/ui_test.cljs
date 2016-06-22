(ns eph-titanic.ui-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [eph-titanic.ui :as ui]
            [eph-titanic.dom :as dom]
            [eph-titanic.component :as com]
            [goog.dom :as gdom])
  (:import [goog.dom DomHelper]))

(deftest defs-test
  (is (string? ui/*iframe-style*))
  (is (number? ui/*max-cells*))
  (is (number? ui/delta)))

(deftest ui-helpers-test
  (is (= ["c1" "r1"] (ui/cell-and-row-id #js {:id "c1" :parentElement #js {:id "r1"}})))
  (is (= ["c1" nil] (ui/cell-and-row-id #js {:id "c1"})))
  (is (= [nil nil] (ui/cell-and-row-id nil))))

(deftest iframe-helpers-test
  (is (= "IFRAME" (.-tagName (-> (ui/main) com/elt ui/iframe))))
  (is (-> (ui/main) com/elt ui/iframe-doc))
  (is (-> (ui/main) com/elt ui/iframe-win))
  (is (= 1 (count (dom/get-styles (-> (ui/main) com/elt ui/iframe-doc)))))
  (is (= ui/*iframe-style*
         (.-innerHTML (first (dom/get-styles (-> (ui/main) com/elt ui/iframe-doc))))))
  (is (= 1 (do (ui/setup-iframe-style (-> (ui/main) com/elt ui/iframe-doc))
               (count (dom/get-styles (-> (ui/main) com/elt ui/iframe-doc)))))))

(deftest main-test
  (is (satisfies? com/IControl (ui/main)))
  (is (satisfies? com/IMainIframe (ui/main))))

(deftest table-control-test
  (is (satisfies? com/IControl (ui/table-control)))
  (is (satisfies? com/ITableControl (ui/table-control)))
  (with-redefs [dom/value->int #(condp = % "rows" 1 "cols" 2)]
    (is (= {:rows 1 :cols 2}
           (com/table-size (ui/table-control)))))
  (with-redefs [dom/value->int #(condp = % "rows" 1 "cols" 20)
                ui/*max-cells* 20
                js/alert identity]
    (is (= {:rows 1 :cols 20}
           (com/table-size (ui/table-control)))))
  (with-redefs [dom/value->int #(condp = % "rows" 1 "cols" 20)
                ui/*max-cells* 19
                js/alert identity]
    (is (string? (com/table-size (ui/table-control))))))

(deftest coords-test
  (is (satisfies? com/IControl (ui/coords))))

#_(deftest coords-test
  (testing "coords"
    (is (= "coords" (.-id (ui/coords))))))

#_(deftest log-test
  (testing "log"
    (is (= "log" (.-id (ui/log))))))

(defn ^:export run []
  (run-tests))
