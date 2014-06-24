# str_alignment

A Clojure library designed to align 2 strings using either
Smith-Waterman for local alignment or Needleman Wunsch for global
alignment. The algorithm returns a vector of tuples where a tuple is
composed of the max score and the 2 optimally aligned strings with gap
characters.

## Usage

(use 'str-alignment.aligner)
(align "acacacta" "agcacaca")

;[[12 "a-cacacta" "agcacac-a"]]

## TODO
Put in test cases.

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
