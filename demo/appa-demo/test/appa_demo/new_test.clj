(ns appa-demo.new-test
  (:require [appa-demo.new :as sut]
            [clojure.test :refer [deftest is]]))


(deftest random-test
  (is (= 42 (sut/random-math))))
