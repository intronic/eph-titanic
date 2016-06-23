(ns eph-titanic.html-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [eph-titanic.html :as h]))

(deftest table-test
  (testing "cell ids"
    (is (= "r0" (h/row-id 0)))
    (is (= "r100" (h/row-id 100)))
    (is (= "c0" (h/cell-id 0)))
    (is (= "c100" (h/cell-id 100)))
    (is (= 0 (h/cell-num 2 0 0)))
    (is (= 1 (h/cell-num 2 0 1)))
    (is (= 2 (h/cell-num 2 1 0)))
    (is (= 3 (h/cell-num 2 1 1)))
    (is (= "row 1" (h/id->coord-str nil "r1")))
    (is (= "(NaN,NaN)" (h/id->coord-str 0 "c0")))
    (is (= "(0,0)" (h/id->coord-str 1 "c0")))
    (is (= "(1,2)" (h/id->coord-str 3 "c5"))))
  (testing "table html"
    (is (= "<table><tr id=\"r0\"><td id=\"c0\">\u00a0</td></tr></table>"
           (h/table 1 1)))
    (is (= (str "<table>"
                "<tr id=\"r0\"><td id=\"c0\">\u00a0</td><td id=\"c1\">\u00a0</td></tr>"
                "<tr id=\"r1\"><td id=\"c2\">\u00a0</td><td id=\"c3\">\u00a0</td></tr></table>")
           (h/table 2 2)))
    (is (= "<table></table>" (h/table nil nil)))))

(defn ^:export run []
  (run-tests))
