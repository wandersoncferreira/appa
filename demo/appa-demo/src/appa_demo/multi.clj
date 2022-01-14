(ns appa-demo.multi)


(defn my-code-has-threads-too
  []
  (let [th (Thread. (fn []
                      (Thread/sleep 2000)
                      (println "Inside another thread!")))]
    (.start th)
    (Thread/sleep 4000)
    (.stop th)))
