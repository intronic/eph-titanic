(ns eph-titanic.event
  (:require [goog.events :as gevents]
            [cljs.core.async :as async])
  (:import [goog.events EventType KeyCodes KeyHandler]
           [goog.dom DomHelper]))

(def ^:const max-cells 100000)

(defn listen
  "Attach a listener to el for events of 'type'.
  Apply event-fn to event and put any non-nil result onto the event-chan
  channel. The result should be a [tag value] pair.
  The listener returns true or nil if the channel is already closed."
  [event-chan el type event-fn]
  (gevents/listen el type (fn [e] (some->> (event-fn e)
                                           (async/put! event-chan)))))

;; TODO: Make HTML components that handle their own local state and
;; only produce valid application state events
(defn setup-listeners
  "Setup listeners for event-chan channel.
  btn, iframe, rows, cols are functions that return the button and
  iframe elements or the number of table rows and columns
  respectively.  If an event would trigger some invalid [tag value]
  combination then 'error-fn' will be called with an error
  message. 'error-fn' should return nil or a valid [tag value]."
  [event-chan error-fn btn rows cols]
  (let [event-listen (partial listen event-chan)]
    ;; Click btn to create a table
    (event-listen (btn)
                  EventType.CLICK
                  (fn [_] (let [r (rows)
                                c (cols)]
                            (if-not (and (every? pos? [r c]) (<= (* r c) max-cells))
                              (error-fn (str "Please enter the number of rows and columns for the table\n(up to "
                                             (.toLocaleString max-cells) " cells).\n"))
                              [:create-table {:rows r :cols c}]))))))

(defn remove-listeners
  "Remove listeners."
  [ok-btn]
  (gevents/removeAll (ok-btn)))
