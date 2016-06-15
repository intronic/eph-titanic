(ns eph-titanic.state
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :as async]))

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
  "Evaluate tag and value, possibly changing application state."
  [app-state tag val]
  (case tag
    :create-table                ; val is map of :rows and :cols
    (do ;; update table dimensions, redraw counter, and undo any selections
      (println tag val)
      (swap! app-state #(some-> %
                                (merge {:selected-set nil} val)
                                (update-in [:table-update] inc))))

    :default-ignore))

(defn state-change!
  "Handle changing application state. 'create-table' is the function
  to apply to new table dimensions when they change are to be
  re-drawn. 'old' and 'new' are old and new states respectively."
  [{:keys [create-table]} _ _ old new]
  ;; if rows/cols or has changed, redo the table
  (let [table (select-keys new [:rows :cols :table-update])]
    (if (not= table (select-keys old [:rows :cols :table-update]))
      (do #_(println :state :create-table table)
          (create-table table)))))

(defn start-event-loop
  "Read a [tag value] pair off 'chan', evaluate the message possibly
  updating the app-state, and loop. Event loop will terminate if chan
  is closed."
  [app-state chan]
  (println :start-main-event-loop)
  (go-loop [[tag val :as msg] (async/<! chan)]
    (when msg
      #_(println :loop tag val)
      (eval-tag app-state tag val)
      (recur (async/<! chan)))))
