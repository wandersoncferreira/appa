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
  (let [reports (map #(.get ^FutureTask %) report-futures)]
    (apply (partial merge-with +) reports)))

(defn manager
  [{:vars/keys [parallel dedicated unspecified]
    :keys [parallel-pool-size]
    :or {parallel-pool-size parallel-thread-pool-size}}]
  (let [result (atom nil)
        executors (atom nil)
        exec-parallel-tests (Executors/newFixedThreadPool parallel-pool-size)]


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

      (println (format "\nRunning tests... Found %s vars" (count unspecified)))
      (let [exec (Executors/newSingleThreadExecutor)]
        (swap! executors conj exec)
        (swap! result conj
               (.submit exec
                        ^Callable (fn []
                                    (binding [t/report report/report
                                              report/*report-counters* (ref report/*initial-report-counters*)]
                                      (dotimes [_ (count unspecified)] (report/inc-report-counter :test))
                                      (t/test-vars (vec unspecified))
                                      @report/*report-counters*)))))

      ;; wait for all the report results
      (let [summary (merge-results @result)]

        ;; shutdown sequence
        (doseq [ex @executors] (.shutdown ex))
        (t/do-report (assoc summary :type :summary))
        summary))))
