(ns ^:figwheel-load eph-titanic.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [eph-titanic.ui :as ui]
            [eph-titanic.event :as event]
            [eph-titanic.html :as html]
            [eph-titanic.state :as state]
            [cljs.core.async :as async]))

(enable-console-print!)

(def ^:const delta 10)                  ; x/y offset to pointer to show coord element

;; global re-loadable app-state
(defonce app-state (atom state/init-state))

(defn setup
  []
  (let [event-chan (async/chan)]
    (println "set up...")
    ;; set up iframe style
    (ui/setup-iframe-style)
    (add-watch app-state :main-watcher (partial state/state-change! {:ui-main ui/main}))
    (state/start-event-loop app-state event-chan)
    (event/setup-listeners event-chan ui/alert ui/ok-button ui/rows ui/cols)))

(defn teardown
  []
  (println "tear down...")
  (event/remove-listeners ui/ok-button))

(defn on-js-reload
  []
  (teardown)
  (setup))

(setup)
