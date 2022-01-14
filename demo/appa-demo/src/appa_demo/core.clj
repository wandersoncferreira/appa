(ns appa-demo.core)

(defn print-and-sleep
  [s]
  (println "= start = Business logic number " s)
  (Thread/sleep (* 1000 s))
  (println "= end = Business logic updated!"))
