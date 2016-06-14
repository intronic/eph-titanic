(ns eph-titanic.html
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require [hiccups.runtime :as hiccupsrt])
  (:import [goog.string Unicode]))

(declare row-id cell-id cell-num)

(defn table
  "Return HTML string for table of rows and cols."
  [rows cols]
  (html [:table
         (for [r (range rows)]
           [:tr {:id (row-id r)}
            (for [c (range cols)]
              [:td {:id (cell-id (cell-num cols r c))}
               Unicode.NBSP])])]))

(defn row-id
  "id tag for row number."
  [num]
  (str "r" num))

(defn cell-id
  "id tag for cell number."
  [num]
  (str "c" num))

(defn cell-num
  "Unique cell number for cell at position ['row' 'col'], in table
  with 'cols' columns."
  [cols row col]
  (+ (* cols row) col))

#_(defn id->num
  "Row or cell number from id."
  [id]
  (js/parseInt (subs id 1)))

#_(defn cell-coords
  "Return [row col] coordinate from cell num in table with cols columns."
  [cols num]
  [(quot num cols) (rem num cols)])

#_(defn id->coord-str
  "Returns formatted string of row or cell coords if id is a row or
  cell id respectively. Eg. \"row 1\" or \"(1, 0)\"."
  [cols id]
  (case (get id 0)
    "r" (->> id id->num (str "row "))
    "c" (->> id id->num (cell-coords cols) (apply gstring/format "(%d, %d)"))))
