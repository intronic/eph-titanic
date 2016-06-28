(ns eph-titanic.state
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :as async]
            [clojure.set :as set]
            [clojure.string :as str]
            [goog.string :as gstring]
            goog.string.format
            [eph-titanic.component :as com]))

;; Application State:
;;   The initial application state consists of a map of:
;; :rows         - The number of rows in the table (should be >0).
;;                 If this changes, the state should be reset and the table redrawn.
;; :cols         - The number of columns in the table (should be >0).
;;                 If this changes, the state should be reset and the table redrawn.
;; :table-update - The number of times the table has been updated.
;;                 Incrementing this will triggers a reset of the state and redrawing the table.
;; :selected-set - The set of IDs of the currently selected cells or rows.
;;               - Set this to nil to delete the previously selected cells or rows.
(def ^:const init-state
  {:rows nil
   :cols nil
   :table-update 0
   :selected-set nil})

(defn eval-tag
  "Evaluate tag and value, possibly updating app-state or 'coord' ui control."
  [app-state {:keys [coords main]} logger tag val]
  (case tag
    :create-table                      ; val is map of :rows and :cols
    ;; update table dimensions, redraw counter, and undo any selections
    (do (logger :init nil)
        (swap! app-state #(some-> %
                                  (merge {:selected-set nil} val)
                                  (update-in [:table-update] inc))))

    ;; xy is rel to js/document, i-xy is rel to iframe
    (:enter :move)
    (let [[i-xy xy] val
          s (apply gstring/format "(%d, %d)" i-xy)]
      (com/show! coords s xy))

    :scroll
    (let [s (apply gstring/format "(%d, %d)" val)]
      (com/show! coords s))

    :leave
    (do
      (com/hide! coords))

    :cell-click
    ;; A single left-click cancels the selection of any other cells,
    ;; and toggles the selection of the cell at the mouse pointer.
    (let [[id _] val]                   ; cell id is first element
      (logger :toggle-cell-and-clear
              (com/id->coord-str main (:cols @app-state) id))
      (swap! app-state update-in [:selected-set] #(if (get % id) #{} #{id})))

    :cell-double-click
    ;; A double left­click cancels the selection of any other cells,
    ;; and selects the entire row at the mouse pointer.
    ;; Don't bother checking if each cell in row has been selected one-by-one.
    ;; Note that FF on Mac (at least) generates 2 events on double-click:
    ;; a CLICK followed by DBLCLICK
    (let [[_ id] val]                   ; row id is second element
      (logger :select-row (com/id->coord-str main (:cols @app-state) id))
      (swap! app-state update-in [:selected-set] #(if (get % id) #{} #{id})))

    :cell-right-click
    ;; A single right-click toggles the selection of the cell at the
    ;; mouse pointer, and preserves selection of any other cells.
    ;; Don't bother check if the cell is in a row added as a single row.
    ;; Block the context menu popup.
    ;; TODO: also deal with contextmenu keyboard shortcut
    (let [[id _] val]                   ; cell id is first element
      (logger :toggle-cell-and-preserve (com/id->coord-str main (:cols @app-state) id))
      (swap! app-state update-in [:selected-set]
             #(if (get % id) (disj % id) (conj (or % #{}) id))))

    :selection-delete
    ;; Delete any selected cells on BACKSPACE or DELETE key
    (do
      ;; set selected-set to nil to trigger delete
      (logger :delete
              (str/join " " (map (partial com/id->coord-str main (:cols @app-state))
                                 (:selected-set @app-state))))
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
  "Read a [tag value] pair off channel 'ch', evaluate the message possibly
  updating the app-state, and loop. Event loop will terminate if channel
  is closed."
  [app-state component-map logger ch]
  (go-loop [[tag val :as msg] (async/<! ch)]
    (when msg
      (eval-tag app-state component-map logger tag val)
      (recur (async/<! ch)))))
