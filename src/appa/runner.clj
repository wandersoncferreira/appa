(ns appa.runner
  (:require
   [appa.api :as a]
   [clojure.tools.cli :as cli]))

(defn- accumulate [m k v]
  (update-in m [k] (fnil conj #{}) v))

(defn- bool [v]
  (Boolean/valueOf v))

(def cli-options
  [["-d" "--dir DIRNAME" "Name of the directory containing tests. Defaults to \"test\"."
    :parse-fn str
    :assoc-fn accumulate]
   ["-p" "--parallelism BOOLEAN" "Turn parallelism on and off. Defaults to `true`."
    :parse-fn bool]])

(defn -main
  [& args]
  (let [args (cli/parse-opts args cli-options)]
    (try
      (a/test (:options args))
      (finally
        (shutdown-agents)))))
