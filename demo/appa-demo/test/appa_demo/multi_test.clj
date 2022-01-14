(ns appa-demo.multi-test
  (:require [appa-demo.multi :as sut]
            [clojure.test :refer [deftest is]]))


(deftest ^:parallel inside-another-thread-p-test
  (is (nil? (sut/my-code-has-threads-too))))

(deftest inside-another-thread-o-test
  (is (nil? (sut/my-code-has-threads-too))))
