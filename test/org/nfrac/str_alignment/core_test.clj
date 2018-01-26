(ns org.nfrac.str-alignment.core-test
  (:require [clojure.test :as test :refer [deftest]]
            [org.nfrac.str-alignment.core :as ali]))

(defn align
  [s1 s2 opts]
  (let [anchor-left? (get opts :anchor-left? (:global? opts))
        mat (ali/alignments s1 s2 opts)
        loc (if (:global? opts)
              [(count s1) (count s2)]
              (key (apply max-key #(:score (val %)) mat)))]
    [(:score (get mat loc))
     (ali/align-at loc mat anchor-left?)]))

(deftest opts-test

  (test/is (= (align "acacacta" "agcacaca" {})
              [12 ["a-cacacta" "agcacac-a"]])
           "base case")
  (test/is (= (align "acacacta" "agacacata" {:anchor-left? true})
              [13 ["--acacacta" "agacaca-ta"]])
           "anchor left")
  (test/is (= (align "gacttac" "cgtgaattcat" {:global? false :match-weight 1 :anchor-left? true})
              [3 ["---gactt-a" "cgtgaattca"]])
           "global false anchor-left true")
  (test/is (= (align "gacttac" "cgtgaattcat" {:global? true :match-weight 1})
              [-1 ["---gactt-ac" "cgtgaattcat"]])
           "global true")
  (test/is (= (align "gacttac" "cgtgaattcat" {:global? false :match-weight 1 :anchor-left? false})
              [3 ["-gactt-a" "tgaattca"]])
           "global false weight 1"))

(deftest multi-local-test
  (let [s1 "abcdef.....bcd.fg...bcdf."
        s2 "bcdefg"
        mat (ali/alignments s1 s2 {:match-weight 1})
        min-score 3
        ]
    (test/is (= (ali/distinct-local-matches mat min-score)
                '[([2 1] [3 2] [4 3] [5 4] [6 5])
                  ([12 1] [13 2] [14 3] [15 4] [16 5] [17 6])
                  ([21 1] [22 2] [23 3] [23 4] [24 5])]))
    ))


(deftest perf-test

  (let [s1 (apply str (concat (repeat 200 \a) (repeat 200 \b)))
        s2 (apply str (concat (repeat 100 \a) (repeat 100 \b)
                              (repeat 100 \a) (repeat 100 \b)))]
    (println
     (time
      (dotimes [i 1]
        (align s1 s2 {:match-weight 1}))))
    ))
