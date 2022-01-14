(ns appa.worker
  (:require
   [appa.report :as report]
   [clojure.test :as t])
  (:import
   (java.util.concurrent Executors FutureTask)))

(def parallel-thread-pool-size
  "Default value for how many threads is available to run tests in parallel."
  10)

(defn merge-results
  [report-futures]
  (let [reports (map
                 #(if (map? %)
                    %
                    (.get ^FutureTask %))
                 report-futures)]
    (apply (partial merge-with +) reports)))

(defn manager
  [{:vars/keys [parallel sequential]
    :keys [parallel-pool-size]
    :or {parallel-pool-size parallel-thread-pool-size}}]
  (let [result (atom nil)
        executors (atom nil)
        exec-parallel-tests (Executors/newFixedThreadPool parallel-pool-size)]

    (println (format "\nRunning sequential tests... Found %s vars" (count sequential)))

    (binding [t/report report/report
              report/*report-counters* (ref report/*initial-report-counters*)]
      (dotimes [_ (count sequential)] (report/inc-report-counter :test))
      (t/test-vars (vec sequential))
      (swap! result conj @report/*report-counters*))


    (swap! executors conj exec-parallel-tests)

    (binding [t/report report/report
              report/*report-counters* (ref report/*initial-report-counters*)]

      (println (format "\nRunning tests in parallel. Found %s vars" (count parallel)))
      (doseq [v parallel]
        (swap! result conj
               (.submit exec-parallel-tests
                        ^Callable (fn []
                                    (binding [t/report report/report
                                              report/*report-counters* (ref report/*initial-report-counters*)]
                                      (report/inc-report-counter :test)
                                      (t/test-vars [v])
                                      @report/*report-counters*)))))

      ;; wait for all the report results
      (let [summary (merge-results @result)]

        ;; shutdown sequence
        (doseq [ex @executors] (.shutdown ex))
        (t/do-report (assoc summary :type :summary))
        summary))))
