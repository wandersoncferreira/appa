(ns appa.report
  "This is mainly an overwrite to clojure.test report section in order to get these values available in appa.

  TODO: missing the appropriate copyright noticed for the original source"
  (:require [clojure.test :as t]
            [clojure.stacktrace :as stack]))

(def ^:dynamic
  ^{:doc "The maximum depth of stack traces to print when an Exception
  is thrown during a test.  Defaults to nil, which means print the
  complete stack trace."
    :added "1.1"}
  *stack-trace-depth* nil)

(def ^:dynamic *report-counters* nil)	  ; bound to a ref of a map in test-ns

(def ^:dynamic *initial-report-counters*  ; used to initialize *report-counters*
  {:test 0, :pass 0, :fail 0, :error 0})

(def ^:dynamic *testing-vars* (list))  ; bound to hierarchy of vars being tested

(def ^:dynamic *testing-contexts* (list)) ; bound to hierarchy of "testing" strings

(def ^:dynamic *test-out* *out*)         ; PrintWriter for test reporting output

(defmacro with-test-out
  "Runs body with *out* bound to the value of *test-out*."
  {:added "1.1"}
  [& body]
  `(binding [*out* *test-out*]
     ~@body))

(defn inc-report-counter
  "Increments the named counter in *report-counters*, a ref to a map.
  Does nothing if *report-counters* is nil."
  {:added "1.1"}
  [name]
  (when *report-counters*
    (dosync (commute *report-counters* update-in [name] (fnil inc 0)))))

(defn testing-vars-str
  "Returns a string representation of the current test.  Renders names
  in *testing-vars* as a list, then the source file and line of
  current assertion."
  {:added "1.1"}
  [m]
  (let [{:keys [file line]} m]
    (str
     ;; Uncomment to include namespace in failure report:
     ;;(ns-name (:ns (meta (first *testing-vars*)))) "/ "
     (reverse (map #(:name (meta %)) *testing-vars*))
     " (" file ":" line ")")))

(defn testing-contexts-str
  "Returns a string representation of the current test context. Joins
  strings in *testing-contexts* with spaces."
  {:added "1.1"}
  []
  (apply str (interpose " " (reverse *testing-contexts*))))

(defmulti
  ^{:doc "Generic reporting function, may be overridden to plug in
   different report formats (e.g., TAP, JUnit).  Assertions such as
   'is' call 'report' to indicate results.  The argument given to
   'report' will be a map with a :type key.  See the documentation at
   the top of test_is.clj for more information on the types of
   arguments for 'report'."
    :dynamic true}
  report :type)

(defmethod report :default [m]
  (with-test-out (prn m)))

(defmethod report :pass [m]
  (with-test-out (inc-report-counter :pass)))

(defmethod report :fail [m]
  (with-test-out
    (inc-report-counter :fail)
    (println "\nFAIL in" (testing-vars-str m))
    (when (seq *testing-contexts*) (println (testing-contexts-str)))
    (when-let [message (:message m)] (println message))
    (println "expected:" (pr-str (:expected m)))
    (println "  actual:" (pr-str (:actual m)))))

(defmethod report :error [m]
  (with-test-out
    (inc-report-counter :error)
    (println "\nERROR in" (testing-vars-str m))
    (when (seq *testing-contexts*) (println (testing-contexts-str)))
    (when-let [message (:message m)] (println message))
    (println "expected:" (pr-str (:expected m)))
    (print "  actual: ")
    (let [actual (:actual m)]
      (if (instance? Throwable actual)
        (stack/print-cause-trace actual *stack-trace-depth*)
        (prn actual)))))

(defmethod report :summary [m]
  (with-test-out
    (println "\nRan" (:test m) "tests containing"
             (+ (:pass m) (:fail m) (:error m)) "assertions.")
    (println (:fail m) "failures," (:error m) "errors.")))

(defmethod report :begin-test-ns [m]
  (with-test-out
    (println "\nTesting" (ns-name (:ns m)))))

;; Ignore these message types:
(defmethod report :end-test-ns [m])
(defmethod report :begin-test-var [m])
(defmethod report :end-test-var [m])
