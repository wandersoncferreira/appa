(ns appa.worker
  (:require
   [appa.report :as report]
   [clojure.test :as t])
  (:import
   (java.util.concurrent Executors FutureTask)))

(def parallel-thread-pool-size 10)

(defn merge-results
  [report-futures]
  (let [reports (map #(.get ^FutureTask %) report-futures)]
    (apply (partial merge-with +) reports)))

(defn manager
  [{:test-vars/keys [parallel dedicated others]}]
  (let [result (atom nil)
        executors (atom nil)
        exec-parallel-tests (Executors/newFixedThreadPool parallel-thread-pool-size)]


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

      (println (format "\nRunning tests in dedicated thread pool. Found %s vars" (count dedicated)))
      (doseq [v dedicated]
        (let [exec (Executors/newSingleThreadExecutor)]
          (swap! executors conj exec)
          (swap! result conj
                 (.submit exec
                          ^Callable (fn []
                                      (binding [t/report report/report
                                                report/*report-counters* (ref report/*initial-report-counters*)]
                                        (report/inc-report-counter :test)
                                        (t/test-vars [v])
                                        @report/*report-counters*))))))

      (println (format "\nRunning tests... Found %s vars" (count others)))
      (let [exec (Executors/newSingleThreadExecutor)]
        (swap! executors conj exec)
        (swap! result conj
               (.submit exec
                        ^Callable (fn []
                                    (binding [t/report report/report
                                              report/*report-counters* (ref report/*initial-report-counters*)]
                                      (dotimes [_ (count others)] (report/inc-report-counter :test))
                                      (t/test-vars (vec others))
                                      @report/*report-counters*)))))

      ;; wait for all the report results
      (let [summary (merge-results @result)]

        ;; shutdown sequence
        (doseq [ex @executors] (.shutdown ex))
        (t/do-report (assoc summary :type :summary))
        summary))))
