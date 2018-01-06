# str-alignment

A Clojure library designed to align 2 strings using either
Smith-Waterman for local alignment or Needleman Wunsch for global
alignment.

## Installation
Using clojars

TODO

## Usage

The first step is to calculate the alignments matrix, which is a map
keyed by a pair of offsets into the two strings: `[i1 i2]`.
Then you can look up similarity scores at different alignment
locations, choose a location to anchor at, and use that to reconstruct
aligned strings (with gap characters inserted).

``` clojure
(require '[org.nfrac.str-alignment.core :as ali])

(def mat (ali/alignments "acacacta" "agcacaca" {}))
(def loc (key (apply max-key #(first (val %)) mat)))
(first (get mat loc)) ;; score
;12
(ali/align-at loc mat false)
;["a-cacacta" "agcacac-a"]
```

Global alignment example:

``` clojure
(let [[s1 s2] ["gacttac" "cgtgaattcat"]
      mat (ali/alignments s1 s2 {:global? true :match-weight 1})
      loc [(count s1) (count s2)]]
  [(first (get mat loc))
   (ali/align-at loc mat true)])
;[-1 ["---gactt-ac" "cgtgaattcat"]]
```

Scoring weights passed to `alignments` are

| key                | default |
| ------------------ | ------- |
| `:match-weight`    |  2      |
| `:mismatch-weight` | -1      |
| `:gap-open-weight` | -1      |
| `:gap-ext-weight`  | -1      |


## TODO
Put in test cases.

## License

Copyright © 2014 Shermin Pei
Copyright © 2018 Felix Andrews

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
