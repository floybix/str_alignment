# str_alignment

A Clojure library designed to align 2 strings using either
Smith-Waterman for local alignment or Needleman Wunsch for global
alignment. The algorithm returns a vector of tuples where a tuple is
composed of the max score and the 2 optimally aligned strings with gap
characters.

## Installation
Using clojars

[str_alignment "0.1.0-SNAPSHOT"]

## Usage

(use 'str-alignment.aligner)

(align "acacacta" "agcacaca")

;[[12 "a-cacacta" "agcacac-a"]]

(align "gacttac" "cgtgaattcat" :global true :match-weight 1) 

;[[-1 "---gactt-ac" "cgtgaattcat"]]

Other options to use are :global = true uses Needleman Wunsch. False
uses Smith-Waterman, :anchor-right is a boolean which forces alignment
to continue to the right edge of both strings and :anchor-left is a
boolean which forces the alignment to the left most edge.

Scoring weights are set by :match-weight, :mismatch-weight,
:gap-open-weight, :gap-ext-weight. If weights is a penalty
(i.e. mismatch-weight) then the weight is a negative number (i.e. -1).

## TODO
Put in test cases.

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
