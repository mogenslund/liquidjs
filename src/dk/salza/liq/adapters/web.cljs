(ns dk.salza.liq.adapters.web
  (:require [dk.salza.liq.tools.util :as util]
            [dk.salza.liq.keys :as keys]
            [dk.salza.liq.renderer :as renderer]
            [dk.salza.liq.editor :as editor]
            [clojure.string :as str]))

(def old-lines (atom {}))

(defn reset
  []
  (reset! old-lines {}))

(defn update-view
  []
  (let [lineslist (renderer/render-screen)]
    (doseq [line (apply concat lineslist)]
       (let [key (str "w" (if (= (line :column) 1) "0" "1") "-r" (line :row))
             content (str "<span class=\"bgstatusline\"> </span><span class=\"plain bgplain\">"
                     (str/join (for [c (line :line)] (if (string? c) c (str "</span><span class=\"" (name (c :face)) " bg" (name (c :bgface)) "\">"))))
                     "</span>")]
         ;(when (not= (@old-lines key) content)
           (-> js/document
               (.getElementById key)
               (.-innerHTML)
               (set! content))
         ;  (swap! old-lines assoc key content))
         ))))

(defn view-handler
  [key reference old new]
  (remove-watch editor/editor key)
  (when (editor/fullupdate?) (reset))
  (update-view)
  (add-watch editor/updates key view-handler))

(defn key-to-keyword
  [evt]
  (let [key (.-key evt)
        keynum (.-which evt)
        ctrl (if (.-ctrlKey evt) "C-" "")
        meta (if (.-ctrlKey evt) "M-" "")
        resolvekey (cond (= key "Tab") "tab"
                         (= key "Enter") "enter"
                         (= key "Backspace") "backspace"
                         (= key "Escape") "esc"
                         (= key "ArrowUp") "up"
                         (= key "ArrowDown") "down"
                         (= key "ArrowLeft") "left"
                         (= key "ArrowRight") "right"
                         (= key "Delete") "delete"
                         (= key "Home") "home"
                         (= key "End") "end"
                         (= key "F1") "f1"
                         (= key "F2") "f2"
                         (= key "F3") "f3"
                         (= key "F4") "f4"
                         (= key "F5") "f5"
                         (= key "F6") "f6"
                         (= key "F7") "f7"
                         (= key "F8") "f8"
                       ;  (= key "PageDown") ""
                       ;  (= key "PageUp") ""
                         (= keynum 32) "space"
                         (= key "!") "exclamation"
                         (= key "\"") "quote"
                         (= key "#") "hash"
                         (= key "$") "dollar"
                         (= key "%") "percent"
                         (= key "&") "ampersand"
                         (= key "'") "singlequote"
                         (= key "(") "parenstart"
                         (= key ")") "parenend"
                         (= key "*") "asterisk"
                         (= key "+") "plus"
                         (= key ",") "comma"
                         (= key "-") "dash"
                         (= key ".") "dot"
                         (= key "/") "slash"
                         (= key ":") "colon"
                         (= key ";") "semicolon"
                         (= key "<") "lt"
                         (= key "=") "equal"
                         (= key ">") "gt"
                         (= key "?") "question"
                         (= key "@") "at"
                         (= key "[") "bracketstart"
                         (= key "]") "bracketend"
                         (= key "^") "hat"
                         (= key "{") "bracesstart"
                         (= key "_") "underscore"
                         (= key "\\") "backslash"
                         (= key "|") "pipe"
                         (= key "}") "bracesend"
                         (= key "~") "tilde"
                         (= key "¤") "curren"
                         (= key "´") "backtick"
                         (= key "Å") "caa"
                         (= key "Æ") "cae"
                         (= key "Ø") "coe"
                         (= key "å") "aa"
                         (= key "æ") "ae"
                         (= key "ø") "oe"
                         :else key)]
    (keyword (str ctrl meta resolvekey))))

(defn key-handler
  [evt]
  (.preventDefault evt)
  (.stopPropagation evt)
  (js/console.log (str "Key pressed " (pr-str (.-key evt)) "\n" (.-which evt) "\n" (.-ctrlKey evt) "\n" (.-shiftKey evt) "\n" (.-altKey evt) "\n" (key-to-keyword evt)))
  (editor/handle-input (key-to-keyword evt)))

(defn init
  []
  (enable-console-print!)
  (js/console.log "Init Web Adapter ...")
  (add-watch editor/updates "web" view-handler)
  (-> js/document (.-onkeydown) (set! key-handler)))