(ns str-alignment.test-cases
  (:require [clojure.test :as test])
  (:use str-alignment.aligner))

(test/is (= (align "acacacta" "agcacaca") '([12 "a-cacacta" "agcacac-a"])) "base case")
(test/is (= (align "acacacta" "agacacata" :anchor-left true) '([13 "--acacacta" "agacaca-ta"])) "anchor left")
(test/is (= (align "gacttac" "cgtgaattcat" :global false :match-weight 1 :anchor-left true)
            '([3 "---gactt-a" "cgtgaattca"])) "global false anchor-left true")
(test/is (= (align "gacttac" "cgtgaattcat" :global true :match-weight 1) [[-1 "---gactt-ac" "cgtgaattcat"]])
         "global true")
(test/is (= (align "gacttac" "cgtgaattcat" :global false :match-weight 1 :anchor-left false)
            '([3 "-gactt-a" "tgaattca"])) "global false")
