(ns eph-titanic.state
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :as async]
            [clojure.set :as set]
            [goog.string :as gstring]
            goog.string.format
            [eph-titanic.component :as com]))

;; Application State:
;;   The initial application state consists of a map of:
;; :table-update - The number of times the table has been updated.
;;                 On a table update event, regardless of whether the
;;                 dimensions change, any selected cells or rows
;;                 should be un-selected.
;; :rows         - The number of rows in the table (should be >0).
;;                 If the number of rows changes, the table should be redrawn.
;; :cols         - The number of columns in the table (should be >0).
;;                 If the number of columns changes, the table should be redrawn.
;; :selected-set - The set of IDs of the currently selected cells or rows.
;;               - Set this to nil (without changing the table state
;;                 above) to delete the previously selected cells or rows.
(def ^:const init-state
  {:table-update 0
   :rows nil
   :cols nil
   :selected-set nil})

(defn eval-tag
  "Evaluate tag and value, possibly changing application state or 'coord-ctl'."
  [app-state component-map tag val]
  (case tag
    :create-table                      ; val is map of :rows and :cols
    ;; update table dimensions, redraw counter, and undo any selections
    (swap! app-state #(some-> %
                              (merge {:selected-set nil} val)
                              (update-in [:table-update] inc)))

    ;; xy is rel to js/document, rel-xy is rel to iframe
    :enter
    (let [[rel-xy xy] val
          s (apply gstring/format "(%d, %d)" rel-xy)]
      #_(println tag val s)
      (com/show! (:coords-control component-map) s xy))

    :move
    (let [[rel-xy xy] val
          s (apply gstring/format "(%d, %d)" rel-xy)]
      #_(println tag val s)
      (com/show! (:coords-control component-map) s xy))

    ;; TODO: fix move/leave/enter etc to handle when scrolling of iframe stops and main doc starts scrolling.

    :scroll
    (let [s (apply gstring/format "(%d, %d)" val)]
      #_(println tag val s)
      (com/show! (:coords-control component-map) s))

    :leave
    (do
      #_(println tag val)
      (com/hide! (:coords-control component-map)))

    :cell-click
    ;; Click toggles the cells current selection status. Any other items are un-selected.
    (let [[id _] val]                   ; cell id is first element
      #_(println tag val)
      (swap! app-state update-in [:selected-set] #(if (get % id) #{} #{id})))

    :cell-double-click
    ;; Double-click has same semantics as Click, but for the entire row (by row id).
    ;; The row selection status is toggled. Any other items are un-selected.
    ;; Don't bother checking if each cell in row has been selected one-by-one.
    ;; Note that FF on Mac (at least) generates 2 events on double-click:
    ;; a CLICK followed by DBLCLICK
    (let [[_ id] val]                   ; row id is second element
      #_(println tag val)
      (swap! app-state update-in [:selected-set] #(if (get % id) #{} #{id})))

    :cell-right-click
    ;; Right-click toggles the cell's selection state, without changing other selected items.
    ;; Don't bother check if the cell is in a row added as a single row.
    ;; Block the context menu popup.
    ;; TODO: also deal with contextmenu keyboard shortcut
    (let [[id _] val]                   ; cell id is first element
      #_(println tag val)
      (swap! app-state update-in [:selected-set]
             #(if (get % id) (disj % id) (conj (or % #{}) id))))

    :selection-delete
    ;; Delete any selected cells on BACKSPACE or DELETE key
    (do
      (println tag val)
      ;; set selected-set to nil to trigger delete
      (swap! app-state assoc-in [:selected-set] nil))

    :default-ignore))

(defn state-change!
  "Handle changing application state. 'create-table' is the function
  to apply to new table dimensions when they change are to be
  re-drawn. 'unselect-ids' and 'select-ids' are the functions to be
  called with the collection of cells to be unselected and selected.
  'old' and 'new' are old and new states respectively."
  [{:keys [create-table unselect-ids select-ids delete-ids]} _ _ old new]
  ;; if rows/cols or update has changed, redo the table
  (let [table (select-keys new [:rows :cols :table-update])]
    (if (not= table (select-keys old [:rows :cols :table-update]))
      (create-table table)
      ;; otherwise check selected-set:
      (let [old-sel (:selected-set old)
            new-sel (:selected-set new)]
        ;; if new selection is nil, delete any ids in the old selection
        (if (nil? new-sel)
          (delete-ids old-sel)
          ;; otherwise if selection has changed, call unselect-ids and select-ids
          (when (not= old-sel new-sel)
            (unselect-ids (set/difference old-sel new-sel))
            (select-ids (set/difference new-sel old-sel))))))))

(defn start-event-loop
  "Read a [tag value] pair off 'chan', evaluate the message possibly
  updating the app-state, and loop. Event loop will terminate if chan
  is closed."
  [app-state component-map chan]
  (go-loop [[tag val :as msg] (async/<! chan)]
    (when msg
      #_(println :loop tag val)
      (eval-tag app-state component-map tag val)
      (recur (async/<! chan)))))
