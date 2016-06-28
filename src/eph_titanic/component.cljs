(ns eph-titanic.component)

;; Protocols for accessing UI components
;; IMainIframe to control the main iframe table rendering.

(defprotocol IControl
  (init! [_] [_ event-chan] "Initialize the control with optional event channel.")
  (id [_] "Return the element ID as as keyword.")
  (elt [_] "Return the element.")
  (show! [_] [_ html] [_ html [x y]]
    "Show the control, optionally setting innerHTML to 'html' and [x y] coordinates.")
  (hide! [_] "Hide the control."))

(defprotocol IMainIframe
  (create-table! [_ table-spec]
    "Create a table in the 'main iframe' with 'table-spec' map with :rows and :cols keys.")
  (id->coord-str [_ cols id] "Convert table cell or row id into a coordinate string
  in the format \"(x,y)\" for a cell or \"row n\" for a row number 'n'.")
  (select-ids! [_ id-coll] "Show the table row or cell ids in 'id-coll' as 'selected'.")
  (unselect-ids! [_ id-coll] "Show the table row or cell ids in 'id-coll' as 'unselected'.")
  (delete-ids! [_ id-coll] "Delete the table row or cell ids in 'id-coll'."))

(defprotocol ITableSpec
  (table-size [_]
    "Return the table-size as map with keys :rows :cols if the size is
    valid, otherwise show a message and return nil."))
