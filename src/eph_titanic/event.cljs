(ns eph-titanic.event
  (:require [goog.events :as gevents]
            [clojure.set :as set]
            [cljs.core.async :as async])
  (:import [goog.events EventType KeyCodes KeyHandler]
           [goog.dom DomHelper]))

(def ^:const ^:private init-state {})

(def ^:dynamic *max-cells*
  "Maximum number of cells in table"
  100000)

;;   "Map of component keys to set of listeners for that component."
(defonce listener-state (atom init-state))

(defn add-listener
  "Create and attach a listener to 'el' for events of 'type'.
  The listener will apply 'event-fn' to a generated event and put any
  non-nil result onto the 'event-chan' channel. The result of
  'event-fn' should be a [tag value] pair.  The listener returns true
  or nil if the channel is already closed.  Records the listener id in
  a set of ids in the listener-ids map under the key 'component-key'.
  Returns listener id.
  If a listener for 'type' on 'el' is already present it will not be
  changed and its key will be returned."
  [event-chan component-key el type event-fn]
  (println :add-listener component-key type)
  (when-let [id (gevents/listen el type (fn [e] (some->> (event-fn e)
                                                         (async/put! event-chan))))]
    (println :listen :on (.-key id))
    (swap! listener-state update-in [component-key] (fnil conj #{}) id)))

(defn remove-listeners
  "Remove listeners for 'component-key' or all if no key supplied."
  ([] (reset! listener-state init-state))
  ([component-key] (swap! listener-state disj component-key)))

(defn watcher
  "Removes (by id) any listeners that are no longer needed (listeners
  in old but not new states)."
  [_ _ old new]
  (let [old-ids (reduce into #{} (vals old))
        new-ids (reduce into #{} (vals new))
        to-del (set/difference old-ids new-ids)] ; delete anything no longer present
    (doseq [id to-del]
      (println :listen :off (.-key id))
      (gevents/unlistenByKey id))))
