(ns eph-titanic.ui
  (:require [eph-titanic.component :as com]
            [eph-titanic.html :as html]
            [eph-titanic.dom :as dom]
            [eph-titanic.event :as event])
  (:import [goog.events EventType KeyCodes KeyHandler]
           [goog.dom DomHelper]))

;;; Web Browser UI controls.

(def ^:dynamic *iframe-style*
  "Document style for iframe."
  "table { border: 1px solid black; }
  td { border: 1px solid black; width: 12pt; }
  td.sel, tr.sel { background-color: #FF3300; }")

(def ^:dynamic *max-cells*
  "Maximum number of cells in table"
  100000)

(def ^:const delta 10)                  ; x/y offset to pointer to show coord element

(declare iframe iframe-doc iframe-win setup-iframe-style)

;; TODO: Make HTML components that handle their own local state and
;; only produce valid application state events, like React

(defn main
  []
  (reify
    com/IControl
    (id [_] :main-control)
    (elt [_] (dom/elt "main"))
    (init! [this ch]
      ;; Install styles on iframe document head
      (some-> this com/elt iframe-doc .-head setup-iframe-style)
      ;; Install listeners for mouse entering and leaving iframe (js/document coords)
      ;; can use Iframe doc and coords, but inconsistent browser
      ;; support. Instead you need mouseover/mouseout events and more fooling around.
      (event/add-listener ch (com/id this) (-> this com/elt iframe) EventType.MOUSEENTER
                          (fn [e] (let [xy [(.-clientX e) (.-clientY e)]
                                        off (dom/get-framed-page-offset-xy
                                             (-> this com/elt iframe-doc))
                                        scroll (dom/scroll-xy
                                                (-> this com/elt iframe-win))]
                                    [:enter [xy off scroll]])))
      (event/add-listener ch (com/id this) (-> this com/elt iframe) EventType.MOUSELEAVE
                          (fn [e] [:leave []]))
      (let [last-mouse-pos (atom [])]
        (event/add-listener ch (com/id this) (-> this com/elt iframe-doc) EventType.MOUSEMOVE
                            (fn [e] (let [xy [(.-clientX e) (.-clientY e)]
                                          off (dom/get-framed-page-offset-xy
                                               (-> this com/elt iframe-doc))
                                          scroll (dom/scroll-xy
                                                  (-> this com/elt iframe-win))]
                                      ;; capture pos for scroll event
                                      (reset! last-mouse-pos xy)
                                      [:move [xy off scroll]])))
        (event/add-listener ch (com/id this) (-> this com/elt iframe-doc) EventType.SCROLL
                            (fn [e] [:scroll [(.-clientX e) (.-clientY e) @last-mouse-pos]]))))
    (show! [this] (some-> this com/elt dom/set-visible!))
    (hide! [this] (some-> this com/elt dom/set-hidden!))

    com/IMainIframe
    (create-table! [this {:keys [rows cols]}]
      (dom/set-innerHTML! (some-> this com/elt dom/first-iframe dom/iframe-doc .-body)
                          (html/table rows cols))
      (com/show! this))))

(defn table-control
  "Table control component."
  []
  (let [rows #(dom/value->int "rows")
        cols #(dom/value->int "cols")]
    (reify
      com/IControl
      (id [_] :table-control)
      (elt [_] (dom/elt "ok"))
      (init! [this ch]
        ;; Install click listener on button.
        (event/add-listener ch (com/id this) (com/elt this) EventType.CLICK
                            (fn [e] (let [r (rows)
                                          c (cols)]
                                      (if-not (and (every? pos? [r c]) (<= (* r c) *max-cells*))
                                        (js/alert (str "Please enter the number of rows and columns for the table (up to "
                                                       (.toLocaleString *max-cells*) " cells).\n"))
                                        [:create-table {:rows r :cols c}])))))

      com/ITableControl
      (table-size [_]
        (let [r (rows)
              c (cols)]
          (if-not (and (every? pos? [r c]) (<= (* r c) *max-cells*))
            (js/alert (str "Please enter the number of rows and columns for the table\n(up to "
                           (.toLocaleString *max-cells*) " cells).\n"))
            {:rows r :cols c}))))))

(defn coords
  "Coords ui element."
  []
  (reify
    com/IControl
    (id [_] :coords-control)
    (elt [_] (dom/elt "coords"))
    (init! [this ch])
    (show! [this [x y] html]
      (doto (com/elt this)
        (dom/set-page-offset! (+ delta x) (+ delta y))
        (dom/set-style! {"position" "fixed", "display" "block"})
        (dom/set-innerHTML! html)))
    (hide! [this] (dom/set-element-shown! (com/elt this) false))

    ;;com/ICoords
    ))

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
    (dom/install-style! *iframe-style* el)))

(defn log
  "Return the log element."
  []
  (dom/elt "log"))
