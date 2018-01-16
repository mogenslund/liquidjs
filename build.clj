(require 'cljs.build.api)

(cljs.build.api/build "src"
  {:main "dk.salza.liq.webentry"
   :optimizations :simple
   :output-to "out/main.js"})
