(ns appa.runner
  (:require
   [appa.api :as a]
   [clojure.tools.cli :as cli]))

(defn- accumulate [m k v]
  (update-in m [k] (fnil conj #{}) v))

(def cli-options
  [["-d" "--dir DIRNAME" "Name of the directory containing tests. Defaults to \"test\"."
    :parse-fn str
    :assoc-fn accumulate]])

(defn -main
  [& args]
  (let [args (cli/parse-opts args cli-options)]
    (try
      (a/test (:options args))
      (finally
        (shutdown-agents)))))
