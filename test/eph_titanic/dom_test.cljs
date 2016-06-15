(ns eph-titanic.dom-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [eph-titanic.dom :as dom]
            [goog.dom :as gdom]))

(deftest elt-test
  (is (= js/HTMLDivElement (type (dom/elt "main"))))
  (is (= "main" (.-id (dom/elt "main"))))
  (is (= js/HTMLButtonElement (type (dom/elt "ok")))))

(deftest value-tests
  (is (= "99" (dom/value "val99")))
  (is (= "   99   " (dom/value "val99spaces")))
  (is (= "ABC" (dom/value "valABC")))
  (is (= "" (dom/value "valempty")))
  (is (= nil (dom/value "valnil")))
  (is (= 99 (dom/value->int "val99")))
  (is (= 99 (dom/value->int "val99spaces")))
  (is (= nil (dom/value->int "valABC")))
  (is (= nil (dom/value->int "valempty")))
  (is (= nil (dom/value->int "valnil"))))

(deftest get-elements-tests
  (let [iframes (dom/get-elements-by-tag js/document "iframe")]
    (is (= ["IFRAME"] (map #(.-tagName %) iframes)))
    (is (= ["HEAD"] (map #(.-tagName %)
                         (dom/get-elements-by-tag
                          (gdom/getFrameContentDocument (first iframes))
                          "head"))))))

(deftest iframe-test
  ;; works in browser repl: (=  js/HTMLIFrameElement (type (ui/iframe)))
  (is (= (str js/HTMLIFrameElement)
         (str (type (dom/first-iframe (dom/elt "main"))))))
  (is (instance? js/HTMLIFrameElement
                 (dom/first-iframe (dom/elt "main"))))
  ;; iframe doc has same type as js/document
  ;; In phantomjs its not type js/Window.
  #_(is (instance? js/Document (dom/iframe-doc (dom/first-iframe (dom/elt "main")))))
  (is (= (str (type js/document))
         (str (type (dom/iframe-doc (dom/first-iframe (dom/elt "main")))))))
  ;; in the repl, this is a "function HTMLDocument()"
  (is (= "[object HTMLDocumentConstructor]"
         (str (type (dom/iframe-doc (dom/first-iframe (dom/elt "main")))))))
  ;; iframe window has same type string as js/window.
  ;; In phantomjs its not type js/Window.
  (is (= (str (type js/window))
         (str (type (dom/iframe-window (dom/first-iframe (dom/elt "main"))))))))

(deftest set-visible-test
  (is (= "visible" (dom/set-visible! (dom/elt "val99"))))
  (is (= "hidden" (dom/set-hidden! (dom/elt "val99"))))
  (is (= "visible" (dom/set-visible! (dom/elt "main"))))
  (is (= "hidden" (dom/set-hidden! (dom/elt "main")))))

(deftest styles-test
  ;; get/set styles under doc head element, so not to pick up other doc elt styles
  ;; get/set styles under iframe head element
  (let [ifr #(some-> "main" dom/elt dom/first-iframe)]
    (is (= []
           (map (juxt #(.-tagName %) #(subs (.-innerHTML %) 0 5))
                (dom/get-styles (.-head js/document)))))
    (is (= [["STYLE" "table"]]
           (map (juxt #(.-tagName %) #(subs (.-innerHTML %) 0 5))
                (dom/get-styles (.-head (dom/iframe-doc (ifr)))))))

    (is (= [["STYLE" "s1"]]
           (do (dom/install-style! "s1" (.-head js/document))
               (map (juxt #(.-tagName %) #(subs (.-innerHTML %) 0 5))
                    (dom/get-styles (.-head js/document))))))
    (is (= [["STYLE" "table"] ["STYLE" "s2"]]
           (do (dom/install-style! "s2" (.-head (dom/iframe-doc (ifr))))
               (map (juxt #(.-tagName %) #(subs (.-innerHTML %) 0 5))
                    (dom/get-styles (.-head (dom/iframe-doc (ifr))))))))))

(defn ^:export run []
  (run-tests))
