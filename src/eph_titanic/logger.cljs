(ns eph-titanic.logger
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :as async]
            [eph-titanic.component :as com]))

(defn log-msg
  "Put [tag val] message on the channel 'ch'."
  [ch tag val]
  (async/put! ch [tag val]))

(defn start-log-loop
  "Start the log loop. Read a message off channel 'ch'. Message [:init] will
  re-initialize the log-component.  Any other message will be shown on the
  log-component.  Loop will terminate if channel is closed."
  [log-component ch]
  (go-loop [[tag val :as msg] (async/<! ch)]
    (when msg
      (case tag
        :init (com/init! log-component)
        ;; default to show the message
        (com/show! log-component (str (name tag) ": " val)))
      (recur (async/<! ch)))))
