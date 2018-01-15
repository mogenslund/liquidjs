(ns dk.salza.liq.syntaxhl.clojuremdhl
  (:require [dk.salza.liq.slider :as slider]))

(defn next-face
  [sl face]
  (let [ch (slider/look-ahead sl 0)
        pch (or (slider/look-behind sl 1) " ")
        ppch (or (slider/look-behind sl 2) " ")] 
    (cond (= face :string)  (cond (and (= pch "\"") (= ppch "\\")) face
                                  (and (= pch "\"") (string? (-> sl (slider/left 2) (slider/get-char)))) :plain
                                  (and (= pch "\"") (re-matches #"[^#\( \[{\n]" ppch)) :plain
                                  (and (= pch "\"") (re-matches #"[\)\]}]" (or ch " "))) :plain
                                  :else face)
          (= face :plain)   (cond (and (= ch "\"") (re-matches #"[#\( \[{\n]" pch)) :string
                                  (= ch ";") :comment
                                  (and (= ch "#") (or (= pch "\n") (= pch "") (= (slider/get-point sl) 0))) :comment 
                                  (and (= pch "(") (re-find #"def(n|n-|test|record|protocol|macro)? " (slider/string-ahead sl 13))) :type1
                                  (and (= ch ":") (re-matches #"[\( \[{\n]" pch)) :type3
                                  :else face)
          (= face :type1)   (cond (= ch " ") :type2
                                  :else face)
          (= face :type2)   (cond (= ch " ") :plain
                                  :else face)
          (= face :type3)   (cond (re-matches #"[\)\]}\s]" (or ch " ")) :plain
                                  :else face)
          (= face :comment) (cond (= ch "\n") :plain
                                  :else face)
                            :else face)))