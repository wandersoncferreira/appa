(ns appa-demo.core-test
  (:require [appa-demo.core :as sut]
            [clojure.test :refer [deftest is]]))

(deftest ^:parallel test-1
  (is (nil? (sut/print-and-sleep 1))))

(deftest ^:parallel test-2
  (is (true? (sut/print-and-sleep 2))))

(deftest test-3
  (is (nil? (sut/print-and-sleep 3))))

(deftest test-4
  (is (nil? (sut/print-and-sleep 4))))
