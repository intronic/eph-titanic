(ns eph-titanic.ui-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [eph-titanic.ui :as ui]
            [eph-titanic.dom :as dom]
            [goog.dom :as gdom])
  (:import [goog.dom DomHelper]))

#_(deftest button-rows-cols-test
  (testing "button-rows-cols"
    (is (= "ok" (.-id (ui/ok-button))))
    (is (= nil (ui/cols)))
    (is (= nil (ui/rows)))
    (is (= "main" (.-id (ui/main))))
    (with-redefs [dom/value (constantly " 99 ")]
      (is (= 99 (ui/rows))))
    (with-redefs [dom/value (constantly " 99 ")]
      (is (= 99 (ui/cols))))))

#_(deftest iframe-test
  (testing "iframe"
    (is (= "IFRAME" (.-tagName (ui/iframe))))
    (is (not (nil? (ui/iframe-doc))))
    (is (not (nil? (ui/iframe-win))))
    (is (= "BODY" (.-tagName (ui/iframe-body))))))

#_(deftest coords-test
  (testing "coords"
    (is (= "coords" (.-id (ui/coords))))))

#_(deftest log-test
  (testing "log"
    (is (= "log" (.-id (ui/log))))))

#_(deftest setup-iframe-style-test
  (let [ifr (some-> (ui/main) dom/first-iframe dom/iframe-doc)
        count-styles (comp count dom/get-styles)]
    ;; iframe is already set up with one style on load, js/document has none
    (is (= [1 0] [(count-styles ifr) (count-styles js/document)]))
    ;; try and add new iframe style, does nothing
    (is (= [1 0] (do (ui/setup-iframe-style)
                     [(count-styles ifr) (count-styles js/document)])))
    ;; try and add new doc style, adds one
    (is (= [1 1] (do (ui/setup-iframe-style js/document)
                     [(count-styles ifr) (count-styles js/document)])))
    ;; try and add new doc style, does nothing
    (is (= [1 1] (do (ui/setup-iframe-style js/document)
                     [(count-styles ifr) (count-styles js/document)])))))

(defn ^:export run []
  (run-tests))
