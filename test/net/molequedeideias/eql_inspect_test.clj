(ns net.molequedeideias.eql-inspect-test
  (:require [clojure.test :refer [deftest is testing]]
            [net.molequedeideias.eql-inspect :as eql-inspect]))

(defn problems
  [query value]
  (some-> (eql-inspect/explain-data {::eql-inspect/value     value
                                     ::eql-inspect/alias-key :as
                                     ::eql-inspect/query     query})
          #_(doto clojure.pprint/pprint)
          ::eql-inspect/problems
          vec))

(deftest explain-query
  (testing
    "simple"
    (is (= [{:dispatch-key :b
             :path         []
             :problem      ::eql-inspect/missing-value
             :property     :b}]
           (problems [:a :b]
                     {:a 1}))))
  (testing
    "coll of coll of coll"
    (is (= [{:property     :b
             :dispatch-key :b
             :problem      ::eql-inspect/missing-value
             :path         [0 0 0]}]
           (problems [:a :b]
                     [[[{:a 1}]]]))))
  (testing
    "nested"
    (is (= [{:dispatch-key :d
             :property     :d
             :problem      ::eql-inspect/missing-value
             :path         [:b]}]
           (problems [:a {:b [:c :d]}]
                     {:a 1
                      :b {:c 42}}))))
  (testing
    "nested with coll"
    (is (= [{:dispatch-key :c
             :problem      ::eql-inspect/missing-value
             :property     :c
             :path         [:b 1]}
            {:dispatch-key :d
             :property     :d
             :problem      ::eql-inspect/missing-value
             :path         [:b 0]}]
           (problems [:a {:b [:c :d]}]
                     {:a 1
                      :b [{:c 42}
                          {:d 42}]}))))
  (testing
    "nested with coll and join"
    (is (= [{:dispatch-key :c
             :problem      ::eql-inspect/missing-value
             :path         [:b 1]
             :property     :c}
            {:dispatch-key :d
             :problem      ::eql-inspect/missing-value
             :path         [:b 0]
             :property     :d
             :query        [:e :f]}
            {:dispatch-key :f
             :problem      ::eql-inspect/missing-value
             :path         [:b 1 :d 0]
             :property     :f}]
           (problems [:a {:b [:c {:d [:e :f]}]}]
                     {:a 1
                      :b [{:c 42}
                          {:d [{:e 42}]}]}))))
  (testing
    "nested with root missing"
    (is (= [{:dispatch-key :b
             :path         []
             :property     :b
             :problem      ::eql-inspect/missing-value
             :query        [:c :d]}]
           (problems [:a {:b [:c :d]}]
                     {:a 1}))))
  (testing
    "simple with alias"
    (is (= [{:dispatch-key :b
             :property     :c
             :problem      ::eql-inspect/missing-value
             :path         []}]
           (problems `[:a (:b {:as :c})]
                     {:a 1}))))
  (testing
    "join with alias"
    (is (= [{:dispatch-key :d
             :problem      ::eql-inspect/missing-value
             :property     :e
             :path         [:c]}]
           (problems `[:a
                       {(:b {:as :c})
                        [(:d {:as :e})]}]
                     {:a 42
                      :c {:d 42}}))))
  (testing
    "join with value"
    (is (= [{:dispatch-key :c
             :property     :c
             :problem      ::eql-inspect/expect-collection
             :value        42
             :path         [:b]}]
           (problems `[:a
                       {:b [:c]}]
                     {:a 42
                      :b 42})))))
