(ns appa.api
  (:refer-clojure :exclude [test])
  (:require
   [appa.worker :as worker]
   [clojure.java.io :as io]
   [clojure.set :as set]
   [clojure.tools.namespace.find :as find]))

(defn ^:private vars-with-same-key-name
  [vars key-name]
  (-> (group-by (comp key-name meta) vars)
      (get true)
      set))

(defn group-vars
  [n]
  (let [ns-obj (the-ns n)
        vars (vals (ns-interns ns-obj))
        vars-parallel (vars-with-same-key-name vars :parallel)
        vars-dedicated (vars-with-same-key-name vars :dedicated)
        vars-unspecified
        (set/difference
         (set vars) (set/union vars-parallel vars-dedicated))]
    {:vars/parallel vars-parallel
     :vars/dedicated vars-dedicated
     :vars/unspecified vars-unspecified}))

(defn test-all-test-namespaces
  [options]
  (let [dirs (or (:dir options)
                 #{"test"})
        nses (->> dirs
                  (map io/file)
                  (mapcat find/find-namespaces-in-dir)
                  (filter
                   (fn [n]
                     (some #(re-matches % (name n)) [#".*\-test$"]))))]
    (println (format "\nRunning tests in %s" dirs))
    (dorun (map require nses))
    (let [vars (->> (map group-vars nses)
                    (apply (partial merge-with set/union)))]
      (worker/manager (merge vars options)))))

(defn test
  [arg-map]
  (println (test-all-test-namespaces arg-map))
  (shutdown-agents)
  (System/exit 0))
