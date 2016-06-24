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
  (gdom/getElement (name id)))

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
  (some-> el (get-elements-by-tag "iframe") first))

(defn set-style!
  "Set style of element to properties in 'prop-map'."
  [el prop-map]
  (if el (doseq [[prop val] prop-map] (aset el "style" (name prop) val))))

(defn set-innerHTML!
  "Set innerHTML of element to 'html'."
  [el html]
  (if el (set! (.-innerHTML el) html)))

(defn remove-children!
  "Remove children from element 'el'."
  [el]
  (some-> el gdom/removeChildren))

(defn append-pre!
  "Append 'msg' in <pre> tag as child el."
  [el msg]
  (some-> el (gdom/appendChild (gdom/createDom "pre", "", msg))))

(defn append-text!
  "Append 'msg' as a div child el."
  [el msg]
  (some-> el (gdom/appendChild (gdom/createDom "div" "" msg))))

(defn set-class-by-id!
  "Set class of all elements id(s) found either from the document root
  or parent if specified."
  [doc id-coll class]
  (doseq [id id-coll]
    (println :set-class-by-id! id (.-id (gdom/getElementHelper_ doc id)))
    (if-let [el (gdom/getElementHelper_ doc id)]
      (set! (.-className el) class))))

(defn delete-by-id!
  "Delete the 'id-coll' collection of nodes by element id."
  [doc id-coll]
  (doseq [id id-coll]
    (println :removeNode (some->> id (gdom/getElementHelper_ doc) .-id))
    (some->> id (gdom/getElementHelper_ doc) gdom/removeNode)))

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

(defn set-element-shown!
  "Shows or hides an element from the page. if 'is-shown' is true,
  render the element in its default style, if false, disable rendering
  of the element."
  [el is-shown]
  (gstyle/setElementShown el is-shown))

(defn set-page-offset!
  "Set element 'el' page offset to 'x,y'."
  [el x y]
  (gstyle/setPageOffset el x y))

(defn get-framed-page-offset-xy
  "Return [x y] offset of framed page to the topmost window."
  [doc]
  (let [off (gstyle/getFramedPageOffset doc nil)]
    [(.-x off) (.-y off)]))

(defn scroll-xy
  "Return [x y] distance window win has already been scrolled."
  [win]
  [(.-scrollX win) (.-scrollY win)])
