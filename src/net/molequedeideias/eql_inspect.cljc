(ns net.molequedeideias.eql-inspect
  (:require [edn-query-language.core :as eql]))

(defn explain-query-xf
  [{::keys [value path alias-key]
    :or    {path []}
    :as    opts}]
  (comp (map (fn [{:keys [dispatch-key children params] :as node}]
               (let [property (if (contains? opts ::alias-key)
                                (get params alias-key dispatch-key)
                                dispatch-key)
                     problem (cond-> {:property     property
                                      :dispatch-key dispatch-key
                                      :path         path}
                                     children (assoc :query (eql/ast->query {:type :root :children children})))]
                 (cond
                   (map? value) (if-not (contains? value property)
                                  [(assoc problem
                                     :problem ::missing-value)]
                                  (sequence
                                    (explain-query-xf (assoc opts
                                                        ::value (get value property)
                                                        ::path (conj path property)))
                                    children))
                   (coll? value) (into []
                                       (comp (map-indexed
                                               (fn [idx value]
                                                 (sequence
                                                   (explain-query-xf (assoc opts
                                                                       ::value value
                                                                       ::path (conj path idx)))
                                                   [node])))
                                             cat)
                                       value)
                   :else [(assoc problem
                            :value value
                            :problem ::expect-collection)]))))
        cat))

(defn explain-data
  [{::keys [query] :as opts}]
  (let [problems (sequence
                   (explain-query-xf opts)
                   (:children (eql/query->ast query)))]
    (when-not (empty? problems)
      (assoc opts
        ::problems problems))))
