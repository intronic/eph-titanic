(ns eph-titanic.component)

;; Protocols for accessing UI components
;; IMainIframe to control the main iframe table rendering.

(defprotocol IControl
  (init! [_ chan] [_ chan log-chan]
    "Initialize the control with control and log channels 'chan' and 'log-chan'.")
  (id [_] "Return a keyword ID.")
  (elt [_] "Return the element.")
  (show! [_] [_ [x y] html]
    "Show the control, optionally setting [x y] coordinates and innerHTML to 'html'.")
  (hide! [_] "Hide the control."))

(defprotocol IMainIframe
  (create-table! [_ table-spec]
    "Create a table in the 'main iframe' with 'table-spec' map of :rows and :cols."))

(defprotocol ITableControl
  (table-size [_]
    "Return the table-size as map with keys :rows :cols if the size is
    valid, otherwise show a message and return nil."))

(defprotocol ICoords
  (move [_] ""))
