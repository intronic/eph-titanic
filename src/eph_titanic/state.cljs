(ns eph-titanic.state
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [eph-titanic.dom :as dom]
            [eph-titanic.html :as html]
            [eph-titanic.ui :as ui]
            [cljs.core.async :as async])
  (:import [goog.dom DomHelper]))

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
  "Handle changing application state. "
  [{:keys [ui-main create-table]} _ _ old new]
  ;; if rows/cols or has changed, redo the table
  (println :sc :main ui-main)
  (if-let [iframe-doc (some-> (ui-main) dom/first-iframe dom/iframe-doc)]
    (do (println :sc :doc iframe-doc)
        (if (not= (select-keys old [:rows :cols :table-update])
                  (select-keys new [:rows :cols :table-update]))
          (do                               ; table updated, reset things
            (println :create-table (select-keys new [:rows :cols :table-update]))
            #_(logger :reset)
            (set! (.. iframe-doc -body -innerHTML)
                  (html/table (:rows new) (:cols new)))
            (dom/set-visible! (ui-main)))))))

(defn start-event-loop
  [app-state chan]
  (println :start-main-event-loop)
  (go-loop [[tag val :as msg] (async/<! chan)]
    (when msg
      (println :loop tag val)
      (eval-tag app-state tag val)
      (recur (async/<! chan)))))
