(ns eph-titanic.component
  (:require))

;; Protocols for accessing UI components
;; IMainIframe to control the main iframe table rendering.

(defprotocol IMainIframeControl
  (init! [_] "Initialize the 'main iframe' styles.")
  (show! [_] "Show the 'main' element.")
  (hide! [_] "Hide the 'main' element.")
  (create-table! [_ [rows cols]] "Create a table in the 'main iframe'."))

(defprotocol ITableControl
  (table-size [_] "Return table size as [rows cols] or nil if size not valid.")
  (invalid-size [_] "If invalid size, show message and return nil, otherwise return table-size."))
