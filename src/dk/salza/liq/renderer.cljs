(ns dk.salza.liq.renderer
  (:require ;;[dk.salza.liq.tools.fileutil :as fileutil]
            [dk.salza.liq.coreutil :as coreutil]
            [dk.salza.liq.buffer :as buffer]
            [dk.salza.liq.window :as window]
            [dk.salza.liq.editor :as editor]
            [clojure.string :as str]
            [dk.salza.liq.slider :as slider]))

(defn insert-token
  [sl token]
  (assoc sl
    ::slider/before (conj (sl ::slider/before) token)
    ::slider/point (+ (sl ::slider/point) 1)
    ::slider/marks (slider/slide-marks (sl ::slider/marks) (+ (sl ::slider/point) 0) 1)))

(defn apply-syntax-highlight
  [sl rows towid cursor-color syntaxhighlighter active]
  (loop [sl0 sl n 0 face :plain bgface :plain pch "" ppch ""]
     (if (> n rows)
       (slider/set-point sl0 (editor/get-top-of-window towid))
       (let [ch (slider/get-char sl0)
             p (slider/get-point sl0)
             selection (slider/get-mark sl0 "selection")
             cursor (slider/get-mark sl0 "cursor")
             paren-start (slider/get-mark sl0 "hl0")
             paren-end (slider/get-mark sl0 "hl1")
             nextface (syntaxhighlighter sl0 face)
             nextbgface (cond (and (= p cursor) (not= cursor-color :off))
                                (cond (not active) :cursor0
                                      (= cursor-color :green) :cursor1
                                      :else :cursor2)
                              (= p paren-start) :hl
                              (= (+ p 1) paren-end) :hl
                              (and selection (>= p (min selection cursor)) (< p (max selection cursor))) :selection
                              (and selection (>= p (max selection cursor))) :plain
                              (or (= bgface :cursor0) (= bgface :cursor1) (= bgface :cursor2) (= bgface :hl)) :plain
                              :else bgface)
             next (if (and (= nextface face)
                           (= nextbgface bgface)
                           (not (and (= pch "\n") (or (= nextface :string) (= nextbgface :selection)))))
                      sl0
                      (if (and (or (= nextbgface :cursor0) (= nextbgface :cursor1) (= nextbgface :cursor2)) (or (= ch "\n") (slider/end? sl0)))
                          (slider/insert (insert-token sl0 {:face nextface :bgface nextbgface}) " ")
                          (insert-token sl0 {:face nextface :bgface nextbgface})))
                      ]
         (recur (slider/right next 1)
                (if (or (= ch "\n") (= ch nil)) (inc n) n)
                nextface
                nextbgface
                ch
                pch
                )))))

(defn split-to-lines
  "Takes a list of chars and splits into
  a list of lists. Splitting where the char
  is a newline character."
  [charlist n]
  (map #(if (empty? (first %)) '("") (first %))
       (take n (iterate
                 (fn [x]
                   (split-with #(not= % "\n") (rest (second x))))
                 (split-with #(not= % "\n") charlist)))))

;;; ("a" "\n" "\n" "\n" "b" "c" "\n" "d") ---> (("a") ("") ("") ("b" "c") ("d"))

(defn render-window
  [window buffer]
  (let [;bmode (buffer/get-mode buffer)
        ;cursor-color (-> bmode ::mode/actionmapping first :cursor-color)
        cursor-color (buffer/get-action buffer :cursor-color)
        rows (window ::window/rows)
        columns (window ::window/columns)
        towid (str (window ::window/name) "-" (window ::window/buffername))
        tow (or (editor/get-top-of-window towid) 0)
        sl (slider/set-mark (buffer/get-slider buffer) "cursor")

        sl0 (slider/update-top-of-window sl rows columns tow)
        tmp-tmp-tmp (editor/set-top-of-window towid (slider/get-point sl0))

        filename (or (buffer/get-filename buffer) (buffer/get-name buffer) "")
        syntaxhighlighter  (or (-> buffer ::buffer/highlighter) (fn [sl face] :plain))
        active (= window (editor/current-window))
        sl1 (apply-syntax-highlight sl0 rows towid cursor-color syntaxhighlighter active)
        timestamp "9999-12-12 10:45" ;(.format (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm") (new java.util.Date))
        dirty (buffer/get-dirty buffer)
        statuslinecontent "--------"
;;                             (str "L" (format "%-6s" (buffer/get-linenumber buffer))
;;                               timestamp
;;                               (if (and filename dirty) "  *  " "     ") filename)
        statusline "------"
;;                   (conj (map str (seq (subs (format (str "%-" (+ columns 3) "s") statuslinecontent)
;;                                             0 (+ columns 2)))) {:face :plain :bgface :statusline})
        lines (concat (split-to-lines (sl1 ::slider/after) rows) [statusline])]
      (map #(hash-map :row (+ %1 (window ::window/top))
                       :column (window ::window/left)
                       :line %2) (range (inc rows)) lines)))

(defn render-screen
  []
  (let [windows (reverse (editor/get-windows))
        buffers (map #(editor/get-buffer (window/get-buffername %)) windows)]
     (doall (map render-window windows buffers))))