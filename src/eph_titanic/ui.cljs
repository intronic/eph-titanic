(ns eph-titanic.ui
  (:require [eph-titanic.component :as com]
            [eph-titanic.html :as html]
            [eph-titanic.dom :as dom]
            [eph-titanic.event :as event])
  (:import [goog.events EventType KeyCodes KeyHandler]
           [goog.dom DomHelper]))

;;; Web Browser UI controls.

(def ^:dynamic ^:export iframe-style
  "Document style for iframe."
  "table { border: 1px solid black; }
  td { border: 1px solid black; width: 12pt; }
  td.sel, tr.sel { background-color: #FF3300; }")

(def ^:dynamic ^:export max-cells
  "Maximum number of cells in table"
  100000)

(def ^:const delta 10)                  ; x/y offset to pointer to show coord element

(declare iframe iframe-doc iframe-win setup-iframe-style cell-and-row-id valid-spec)

;; TODO: Make HTML components that handle their own local state and
;; only produce valid application state events, like React

(defn main
  []
  (reify
    com/IControl
    (id [_] :main)
    (elt [this] (-> this com/id dom/elt))
    (init! [this ch]
      ;; Install styles on iframe document head
      (some-> this com/elt iframe-doc .-head setup-iframe-style)
      ;; Install listeners for mouse entering and leaving iframe (js/document coords)
      ;; can use Iframe doc and coords, but inconsistent browser
      ;; support. Instead you need mouseover/mouseout events and more fooling around.
      (let [last-mouse-pos (atom nil)]
        (event/add-listener ch (com/id this) (-> this com/elt iframe)
                            EventType.MOUSELEAVE
                            (fn [e]
                              (reset! last-mouse-pos nil)
                              [:leave]))
        (event/add-listener ch (com/id this) (-> this com/elt iframe)
                            EventType.MOUSEENTER
                            (fn [e]
                              (let [[xW yW] [(.-clientX e) (.-clientY e)]
                                    [xOI yOI] (dom/get-framed-page-offset-xy (-> this com/elt iframe-doc))
                                    [xI yI] [(- xW xOI) (- yW yOI)]
                                    [xSW ySW] (dom/scroll-xy js/window)
                                    [xSI ySI] (dom/scroll-xy (-> this com/elt iframe-win))
                                    ifr-pos [(+ xI xSI) (+ yI ySI)]
                                    win-pos [(+ xW xSW) (+ yW ySW)]]
                                ;; Client XY is relative to main window (without scroll)
                                ;; Return pos relative to iframe (including iframe scrolling),
                                ;; and relative to main win including win scrolling.
                                ;; Capture mouse iframe pos for scroll event.
                                (reset! last-mouse-pos [xI yI])
                                [:enter [ifr-pos win-pos]])))
        (event/add-listener ch (com/id this) (-> this com/elt iframe-doc)
                            EventType.MOUSEMOVE
                            (fn [e]
                              (let [[xI yI] [(.-clientX e) (.-clientY e)]
                                    [xOI yOI] (dom/get-framed-page-offset-xy (-> this com/elt iframe-doc))
                                    [xW yW] [(+ xI xOI ) (+ yI yOI)]
                                    [xSW ySW] (dom/scroll-xy js/window)
                                    [xSI ySI] (dom/scroll-xy (-> this com/elt iframe-win))
                                    ifr-pos [(+ xI xSI) (+ yI ySI)]
                                    win-pos [(+ xW xSW) (+ yW ySW)]]
                                ;; Client XY is relative to iframe.
                                ;; Return pos relative to iframe (including scrolling),
                                ;; and relative to main doc.
                                ;; Capture mouse iframe pos for scroll event.
                                (reset! last-mouse-pos [xI yI])
                                [:move [ifr-pos win-pos]])))
        (event/add-listener ch (com/id this) (-> this com/elt iframe-doc)
                            EventType.SCROLL
                            (fn [e] (if-let [[x y] @last-mouse-pos]
                                      (let [;;[xS yS] [(.-clientX e) (.-clientY e)] ; doesnt work on chrome
                                           [xS yS] (dom/scroll-xy (-> this com/elt iframe-win))
                                           ifr-pos [(+ x xS) (+ y yS)]]
                                       ;; The mouse does not move relative to the main window on scroll,
                                       ;; only the iframe coordinate changes.
                                       [:scroll ifr-pos])))))

      ;; Table Cell Click/Double-click/Right-click: pass [cell-id row-id]
      (event/add-listener ch (com/id this) (some-> this com/elt iframe-doc .-body)
                          EventType.CLICK
                          (fn [e] (let [el (.-target e)]
                                    (if (= "TD" (.-tagName el))
                                      [:cell-click (cell-and-row-id el)]))))
      (event/add-listener ch (com/id this) (some-> this com/elt iframe-doc .-body)
                          EventType.DBLCLICK
                          (fn [e] (let [el (.-target e)]
                                    (if (= "TD" (.-tagName el))
                                      [:cell-double-click (cell-and-row-id el)]))))
      (event/add-listener ch (com/id this) (some-> this com/elt iframe-doc .-body)
                          EventType.CONTEXTMENU
                          (fn [e] (let [el (.-target e)]
                                    (.preventDefault e)
                                    (if (= "TD" (.-tagName el))
                                      [:cell-right-click (cell-and-row-id el)]))))

      ;; Delete or Backspace Key anywhere in the iframe body: pass the key code.
      (event/add-listener ch (com/id this) (KeyHandler. (some-> this com/elt iframe-doc .-body))
                          KeyHandler.EventType.KEY
                          (fn [e] (when-let [k (some-> e .-keyCode #{KeyCodes.BACKSPACE KeyCodes.DELETE})]
                                    (.preventDefault e) ; stop 'back' behaviour
                                    [:selection-delete k]))))

    (show! [this]
      (dom/set-visible! (com/elt this)))
    (show! [this html]
      (dom/set-innerHTML! (some-> this com/elt iframe-doc .-body) html)
      (com/show! this))
    (hide! [this]
      (some-> this com/elt dom/set-hidden!))

    com/IMainIframe
    (create-table! [this {:keys [rows cols]}]
      (com/show! this (html/table rows cols)))
    (id->coord-str [_ cols id] (html/id->coord-str cols id))
    (select-ids! [this id-coll]
      (println :select id-coll)
      (dom/set-class-by-id! (some-> this com/elt iframe-doc) id-coll "sel"))
    (unselect-ids! [this id-coll]
      (println :unselect id-coll)
      (dom/set-class-by-id! (some-> this com/elt iframe-doc) id-coll ""))
    (delete-ids! [this id-coll]
      (println :delete id-coll)
      (dom/delete-by-id! (some-> this com/elt iframe-doc) id-coll))))

(defn table-spec
  "Table specification component."
  []
  (let [rows #(dom/value->int "rows")
        cols #(dom/value->int "cols")
        ]
    (reify
      com/IControl
      (id [_] :table-spec)
      (elt [this] (-> this com/id dom/elt))
      (init! [this ch]
        ;; Install click listener on button.
        (event/add-listener ch (com/id this) (com/elt this) EventType.CLICK
                            (fn [e] (if-let [s (com/table-size this)] [:create-table s]))))

      com/ITableSpec
      (table-size [_] (valid-spec (rows) (cols))))))

(defn- valid-spec
  [r c]
  (if (and (every? pos? [r c]) (<= (* r c) max-cells))
    {:rows r :cols c}
    (js/alert (str "Please enter the number of rows and columns for the table (up to "
                   (.toLocaleString max-cells) " cells).\n"))))

(defn coords
  "Coords ui element."
  []
  (reify
    com/IControl
    (id [_] :coords)
    (elt [this] (-> this com/id dom/elt))
    (init! [this ch])
    (show! [this html]
      (doto (com/elt this)
        (dom/set-innerHTML! html)))
    (show! [this html [x y]]
      (doto (com/elt this)
        (dom/set-page-offset! (+ delta x) (+ delta y))
        (dom/set-style! {"position" "fixed", "display" "block"})
        (dom/set-innerHTML! html)))
    (hide! [this] (dom/set-element-shown! (com/elt this) false))))

(defn log
  "Log ui element."
  []
  (reify
    com/IControl
    (id [_] :log)
    (elt [this] (-> this com/id dom/elt))
    (init! [this] (some-> this com/elt dom/remove-children!))
    (init! [this _] (com/init! this))
    (show! [this msg] (some-> this com/elt (dom/append-text! msg)))))

;;; helpers

(defn- iframe
  "Return first iframe under el."
  [el]
  (some-> el dom/first-iframe))

(defn- iframe-doc
  "Return first iframe under el."
  [el]
  (some-> el dom/first-iframe dom/iframe-doc))

(defn- iframe-win
  "Return window of first iframe doc under el."
  [el]
  (some-> el dom/first-iframe dom/iframe-window))

(defn- setup-iframe-style
  "Add styles on element 'el' if none already added."
  [el]
  (if-not (first (dom/get-styles el))
    (dom/install-style! iframe-style el)))

(defn- cell-and-row-id
  "Return a vector of the element ID and parent ID (eg. [cell-id row-id])."
  [el]
  [(some-> el .-id) (some-> el .-parentElement .-id)])
