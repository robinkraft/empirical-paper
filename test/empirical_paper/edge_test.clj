(ns empirics.edge-test
  (:use [midje sweet cascalog]
        cascalog.api
        empirics.edge)
    (:require incanter.stats))

(def sample-coords
  ;; Sample coordinates, displayed
  ;; +---+---+---+---+---+
  ;; |                 5 |
  ;; +               4   +
  ;; |                   |
  ;; +                   +
  ;; |         3         |
  ;; +                   +
  ;; |                   |
  ;; +   2               +
  ;; | 1                 |
  ;; 0---+---+---+---+---+
  
  [[0 0 0]
   [1 1 1]
   [2 2 2]
   [3 5 5]
   [4 8 8]
   [5 9 9]])

(facts
  "check that no edges are produced if the threshold is too small"
  (filter-dist sample-coords 0.5)
  => (produces '())
  
  "check that only corner clusters are reflected, not the center
  point; note the duplicates"
  (filter-dist sample-coords 1.5)
  => (produces [[0 1]
                [1 0] [1 2]
                [2 1]
                [4 5]
                [5 4]])

  "check that fully linked clusters are fully represented, note the
  duplicates"
  (filter-dist sample-coords 4.5)
  => (produces [[0 1] [0 2]
                [1 0] [1 2]
                [2 0] [2 1] [2 3]
                [3 2] [3 4] [4 5]
                [4 3]
                [5 4]]))

(facts

  (create-edges sample-coords 0.5)
  => (produces [[0 '(nil)]
                [1 '(nil)]
                [2 '(nil)]
                [3 '(nil)]
                [4 '(nil)]
                [5 '(nil)]])

  (create-edges sample-coords 1.5)
  => (produces [[0 '(1)]
                [1 '(0 2)]
                [2 '(1)]
                [3 '(nil)]
                [4 '(5)]
                [5 '(4)]])

  (create-edges sample-coords 4.5)
  => (produces [[0 '(1 2)]
                [1 '(0 2)]
                [2 '(0 1 3)]
                [3 '(2 4)]
                [4 '(5 3)]
                [5 '(4)]]))

