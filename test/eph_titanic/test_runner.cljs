(ns eph-titanic.test-runner
  (:require eph-titanic.html-test
            eph-titanic.dom-test
            eph-titanic.ui-test
            eph-titanic.event-test
            eph-titanic.state-test
            [cljs.test :refer-macros [run-all-tests]]))

(enable-console-print!)

(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (if (cljs.test/successful? m)
    (println "Success!")
    (println "FAIL")))

#_(run-all-tests)
(defn ^:export run []
  (run-all-tests #"eph-titanic.*-test"))
