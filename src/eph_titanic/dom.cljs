(ns eph-titanic.dom
  (:require [goog.dom :as gdom]
            [goog.style :as gstyle]))

;; Make js collections ISeqable.
;; Note that this only extends types from the main document,
;; not any returned from queries on <iframe> documents.
(extend-protocol ISeqable
  js/NodeList
  (-seq [array] (array-seq array))

  js/HTMLCollection
  (-seq [array] (array-seq array)))

(defn elt
  "Get dom element id under main document"
  [id]
  (gdom/getElement id))

(defn value
  "Value of dom element 'id'."
  [id]
  (some-> id gdom/getElement .-value))

(defn value->int
  "Value of dom element 'id' as int, or nil if not parseable."
  [id]
  (some-> id value js/parseInt (#(if-not (js/isNaN %) %))))

(defn get-elements-by-tag
  "Return a seq of elements with tagName 'tag' under element 'el'."
  ([el tag]
   (some-> (gdom/getElementsByTagNameAndClass tag nil el) array-seq)))

(defn iframe-doc
  "Return document in iframe element."
  [iframe]
  (gdom/getFrameContentDocument iframe))

(defn iframe-window
  "Return window in iframe element."
  [iframe]
  (gdom/getFrameContentWindow iframe))

(defn first-iframe
  "Find first iframe under element 'el'"
  [el]
  (some->> (get-elements-by-tag el "iframe") first))

(defn set-innerHTML!
  "Set element visibility style to visible, if not already."
  [el html]
  (and el (set! (.-innerHTML el) html)))

(defn set-visible!
  "Set element visibility style to visible, if not already."
  [el]
  (if-not (= "visible" (some-> el .-style .-visibility))
    (aset el "style" "visibility" "visible")))

(defn set-hidden!
  "Set element visibility style to hidden, if not already."
  [el]
  (if-not (= "hidden" (some-> el .-style .-visibility))
    (aset el "style" "visibility" "hidden")))

(defn get-styles
  "Find styles under element 'el'."
  [el]
  (get-elements-by-tag el "style"))

(defn install-style!
  "Install css style string 'css-str' into document 'doc'."
  [css-str doc]
  (gstyle/installStyles css-str doc))
