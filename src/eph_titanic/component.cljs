(ns eph-titanic.component)

;; Protocols for accessing UI components
;; IMainIframe to control the main iframe table rendering.

(defprotocol IControl
  (init! [_ chan] [_ chan log-chan]
    "Initialize the control with control and log channels 'chan' and 'log-chan'.")
  (id [_] "Return a keyword ID.")
  (elt [_] "Return the element.")
  (show! [_] [_ html] [_ html [x y]]
    "Show the control, optionally setting innerHTML to 'html' and [x y] coordinates.")
  (hide! [_] "Hide the control."))

(defprotocol IMainIframe
  (create-table! [_ table-spec]
    "Create a table in the 'main iframe' with 'table-spec' map of :rows and :cols.")
  (select-ids! [_ id-coll] "Show the table row or cell ids in 'id-coll' as 'selected'.")
  (unselect-ids! [_ id-coll] "Show the table row or cell ids in 'id-coll' as 'unselected'.")
  (delete-ids! [_ id-coll] "Delete the table row or cell ids in 'id-coll'."))

(defprotocol ITableControl
  (table-size [_]
    "Return the table-size as map with keys :rows :cols if the size is
    valid, otherwise show a message and return nil."))

(defprotocol ICoords
  (move [_] ""))
