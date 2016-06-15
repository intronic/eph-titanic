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

(declare setup-iframe-style)

;; TODO: Make HTML components that handle their own local state and
;; only produce valid application state events, like React

(defn main
  []
  (reify
    com/IControl
    (id [_] :main-control)
    (elt [_] (dom/elt "main"))
    (init! [this ch]
      ;;"Install styles on iframe document head, and listeners."
      (some-> this com/elt dom/first-iframe dom/iframe-doc .-head setup-iframe-style))

    com/IMainIframe
    (show! [this] (some-> this com/elt dom/set-visible!))
    (hide! [this] (some-> this com/elt dom/set-hidden!))
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
        ;;"Install click listener on button."
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

(defn- setup-iframe-style
  "Add styles on element 'el' if none already added."
  [el]
  (if-not (first (dom/get-styles el))
    (dom/install-style! *iframe-style* el)))

(defn coords
  "Get coords ui element."
  []
  (dom/elt "coords"))

(defn log
  "Return the log element."
  []
  (dom/elt "log"))
