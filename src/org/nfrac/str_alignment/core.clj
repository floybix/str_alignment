(ns org.nfrac.str-alignment.core)

(defrecord Alignment [score direction char1 char2])

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
  (let [s1 (str s1)
        s2 (str s2)]
    (->> locations
         (filter #(or (zero? (first %))
                      (zero? (second %))))
         (reduce (fn [m [i j]]
                   (assoc! m [i j]
                           (cond
                             (= i j 0) (Alignment. 0 :stop \- \-)
                             (= i 0) (Alignment. (if global? (* gap-open j) 0) :l \- (.charAt s2 j))
                             (= j 0) (Alignment. (if global? (* gap-open i) 0) :u (.charAt s1 i) \-))))
                 (transient {}))
         (persistent!))))

(defn- dir->coord [dir i j]
  (case dir
    :d [(dec i) (dec j)]
    :u [(dec i) j]
    :l [i (dec j)]
    :stop))

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
        gapfn #(if (= % :d) gap-open-weight gap-ext-weight)
        locations (array-keys s1 s2)]
    (persistent!
     (reduce (fn [m [i j :as loc]];;score array format
               (let [from-d (get m (dir->coord :d i j)) ;; match/mismatch (diagonal)
                     from-u (get m  (dir->coord :u i j)) ;; deletion (above)
                     from-l (get m (dir->coord :l i j)) ;; insertion (left)
                     char1 (.charAt s1 i) ;;current char in s1
                     char2 (.charAt s2 j) ;;current char in s2
                     d-score (cond->
                                 (+ (:score from-d) (if (= char1 char2) match-weight mismatch-weight))
                               (not global?) (max 0))
                     u-score (cond->
                                 (+ (:score from-u) (gapfn (:direction from-u)))
                               (not global?) (max 0))
                     l-score (cond->
                                 (+ (:score from-l) (gapfn (:direction from-l)))
                               (not global?) (max 0))
                     from (cond (and (>= d-score u-score)
                                     (>= d-score l-score)) :d
                                (>= u-score l-score) :u
                                :else :l)]
                 (assoc! m loc
                         (case from
                           :d (Alignment. d-score :d char1 char2)
                           :u (Alignment. u-score :u char1 \-)
                           :l (Alignment. l-score :l \- char2)))))
             (transient
              (init-array s1 s2 locations {:global? global?
                                           :gap-open-weight gap-open-weight
                                           :gap-ext-weight gap-ext-weight}))
             (remove #(or (zero? (first %))
                          (zero? (second %))) locations)))))

(defn align-at
  "Align two strings from the given offset positions.
  Returns [string1 string2], each padded with '-'. If anchor-left? is
  true then the alignment continues to the beginning of each string.
  H-mat is the return value from the alignments function."
  [start-loc H-mat anchor-left?]
  (loop [[i j :as loc] start-loc
         aln_s1 (list)
         aln_s2 (list)]
    (let [{:keys [score direction char1 char2]} (get H-mat loc)
          next-coord (dir->coord direction i j)]
      (if (or (pos? score)
              (and anchor-left? (not= :stop next-coord)))
        (recur next-coord
               (cons char1 aln_s1) ;;builds strings up from the right to left
               (cons char2 aln_s2))
        (if (= \- char1 char2)
          [(apply str aln_s1) (apply str aln_s2)]
          [(apply str char1 aln_s1) (apply str char2 aln_s2)])))))
