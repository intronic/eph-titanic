(ns eph-titanic.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [eph-titanic.component :as com]
            [eph-titanic.ui :as ui]
            [eph-titanic.event :as event]
            [eph-titanic.html :as html]
            [eph-titanic.state :as state]
            [cljs.core.async :as async]))

(enable-console-print!)

(def ^:const delta 10)                  ; x/y offset to pointer to show coord element

;; global re-loadable app-state
(defonce app-state (atom state/init-state))

(def components
  (into {} (map (juxt com/id identity) [(ui/main)
                                        (ui/table-control)
                                        (ui/coords)])))

(def watchers {app-state [:main-watcher (partial state/state-change!
                                                 {:create-table (partial com/create-table! (ui/main))
                                                  :unselect-ids (partial com/unselect-ids! (ui/main))
                                                  :select-ids (partial com/select-ids! (ui/main))
                                                  :delete-ids (partial com/delete-ids! (ui/main))})]
               event/listener-state [:listener-watcher event/watcher]})

(defn setup
  []
  (let [event-chan (async/chan)]
    (println "set up...")
    ;; set up controls/add watchers/start event loop
    (doseq [c (vals components)]
      (com/init! c event-chan))
    (doseq [[r [k f]] watchers]
      (println :add-watchers k)
      (add-watch r k f))
    (state/start-event-loop app-state
                            components
                            event-chan)))

(defn teardown
  []
  (println "tear down...")
  (event/remove-listeners)
  (doseq [[r [k f]] watchers]
    (println :remove-watchers k)
    (remove-watch r k)))

(defn on-js-reload
  []
  (teardown)
  (setup))

(teardown)
(setup)
