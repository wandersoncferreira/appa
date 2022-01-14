(ns appa.api
  (:refer-clojure :exclude [test])
  (:require
   [appa.worker :as worker]
   [clojure.java.io :as io]
   [clojure.set :as set]
   [clojure.tools.namespace.find :as find]))

(defn group-vars
  [n]
  (let [ns-obj (the-ns n)
        vars (vals (ns-interns ns-obj))
        vars-in-parallel
        (-> (group-by (comp :parallel meta) vars)
            (get true)
            set)
        vars-dedicated
        (-> (group-by (comp :dedicated meta) vars)
            (get true)
            set)
        vars-others
        (set/difference (set vars)
                        (set/union
                         vars-in-parallel
                         vars-dedicated))]
    {:test-vars/parallel vars-in-parallel
     :test-vars/dedicated vars-dedicated
     :test-vars/others vars-others}))

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
      (worker/manager vars))))

(defn test
  [arg-map]
  (println (test-all-test-namespaces arg-map))
  (shutdown-agents)
  (System/exit 0))
