(ns org.nfrac.str-alignment.core)

(defn- array-keys
  "positions of the array to work on"

  [s1 s2]
  (for [i (range (count s1)) ;initialize scoring array. similar to a sparse matrix
        j (range (count s2))]
    [i j]))

(defn- init-array
  "initial conditions of the array to fill in for the gapped row/col"
  [s1 s2 locations {:as opts
                    gap-open :gap-open-weight
                    gap-ext :gap-ext-weight
                    global? :global?}]
  (->> locations
       (filter #(or (zero? (first %))
                    (zero? (second %))))
       (reduce (fn [m [i j]]
                 (assoc m [i j]
                        (cond
                         (= i j 0) [0 :stop \- \-]
                         (= i 0) [(if global? (* gap-open j) 0) :l \- (.charAt s2 j)]
                         (= j 0) [(if global? (* gap-open i) 0) :u (.charAt s1 i) \-])))
               {})))

(defn- dir->coord [dir i j]
  (case dir
    :d [(dec i) (dec j)]
    :u [(dec i) j]
    :l [i (dec j)]
    :stop))

(defn- get-score [m dir i j] (->> (dir->coord dir i j) m))

(defn- maxa
  "Determine max score and direction for position [i j]"
  [coll]
  (->> coll
       (filter #(= (apply max (map second coll)) (second %)))
       (sort-by first)
       first))


(defn- gapfn
  "For gap extention penalties, the direction of the previous cell must also be checked"
  [from gap-open gap-ext]
  (case from
    :d gap-open
    :u gap-ext
    :l gap-ext))

(defn alignments
  "Calculates the alignment score matrix for two strings.
  Returns a map from [i1 i2] to [score ..other-path-info..]
  where i1 is an index into s1 and i2 is an index into s2
  (both prepended with a gap character, so indexes incremented)."
  [s1 s2 {:as opts
          :keys [match-weight
                 mismatch-weight
                 gap-open-weight
                 gap-ext-weight
                 global?]
          :or {match-weight 2
               mismatch-weight -1
               gap-open-weight -1
               gap-ext-weight -1
               global? false}}]
  (let [s1 (str "-" s1)
        s2 (str "-" s2)
        locations (array-keys s1 s2)]
    (reduce (fn [m [i j]];;score array format
              (let [[d dfrom] (get-score m :d i j) ;;score match/mismatch (diagonal)
                    [u ufrom] (get-score m :u i j) ;;score deletion (above)
                    [l lfrom] (get-score m :l i j) ;;score insertion (left)
                    aa1 (.charAt s1 i) ;;current char in s1
                    aa2 (.charAt s2 j) ;;current char in s2
                    ;;chooses from d, u, l and scores associated with it.
                    [from score] (maxa [(if (= aa1 aa2 )
                                          [:d (+ d match-weight)]
                                          [:d (+ d mismatch-weight)])
                                        [:u (+ u (gapfn ufrom gap-open-weight gap-ext-weight))]
                                        [:l (+ l (gapfn lfrom gap-open-weight gap-ext-weight))]])]
                (assoc m [i j] ;;insertion of the best score into the matrix
                       (case from
                         :d [score :d aa1 aa2]
                         :u [score :u aa1 \-]
                         :l [score :l \- aa2]))))
            (init-array s1 s2 locations {:global? global?
                                         :gap-open-weight gap-open-weight
                                         :gap-ext-weight gap-ext-weight})
            (remove #(or (zero? (first %))
                         (zero? (second %))) locations))))

(defn align-at
  "Align two strings from the given offset positions.
  Returns [string1 string2], each padded with '-'. If anchor-left? is
  true then the alignment continues to the beginning of each string.
  H-mat is the return value from the alignments function."
  [start-loc H-mat anchor-left?]
  (loop [[i j :as loc] start-loc
         aln_s1 (list)
         aln_s2 (list)]
    (let [[cscore dir a1 a2] (get H-mat loc) ;;stores the next location [score[i j] to go to in H]
          next-coord (dir->coord dir i j)]
      (if (or (pos? cscore)
              (and anchor-left? (not= :stop next-coord)))
        (recur next-coord
               (cons a1 aln_s1) ;;builds strings up from the right to left
               (cons a2 aln_s2))
        (if (= \- a1 a2)
          [(apply str aln_s1) (apply str aln_s2)]
          [(apply str a1 aln_s1) (apply str a2 aln_s2)])))))
