(ns eph-titanic.ui
  (:require [eph-titanic.dom :as dom]))

(defn alert
  "Alert the user with 'msg' string."
  [msg]
  (js/alert msg))

(defn ok-button
  "Return the ok button element."
  []
  (dom/elt "ok"))

(defn rows
  "Number of rows from UI."
  []
  (dom/value->int "rows"))

(defn cols
  "Number of cols from UI."
  []
  (dom/value->int "cols"))

(defn main
  "Return the 'main' element."
  []
  (dom/elt "main"))

#_(defn iframe
  "Get first iframe element under main."
  []
  (dom/first-iframe (main)))

#_(defn iframe-doc
  "Get body of first iframe element under main."
  []
  (some-> (iframe) dom/iframe-doc))

#_(defn iframe-win
  "Get window of first iframe element under main."
  []
  (some-> (iframe) dom/iframe-window))

#_(defn iframe-body
  "Get body of first iframe element under main."
  []
  (some-> (iframe-doc) .-body))

(defn coords
  "Get coords ui element."
  []
  (dom/elt "coords"))

(defn log
  "Return the log element."
  []
  (dom/elt "log"))

(defn setup-iframe-style
  "Add styles on head of document 'doc' if none already added. Add to document
  of first iframe under 'main' if no doc specified."
  ([] (setup-iframe-style (some-> (main) dom/first-iframe dom/iframe-doc)))
  ([doc]
   (if-not (first (dom/get-styles (.-head doc)))
     (dom/install-style! "table { border: 1px solid black; }
  td { border: 1px solid black; width: 12pt; }
  td.sel, tr.sel { background-color: #FF3300; }" doc))))
