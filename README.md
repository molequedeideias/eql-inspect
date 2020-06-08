# eql-inspect

Utility functions to get information about queries and data.

## Usage

Add to your `deps.edn`
```clojure
net.molequedeideias/eql-inspect {:git/url "https://github.com/molequedeideias/eql-inspect"
                                 :sha     "b7278748e38423dd17e910399632f168af48a2c4"}
```

> TIP: This isn't "eql validation".

### Quick example:

```clojure
(require '[net.molequedeideias.eql-inspect :as eql-inspect])

(eql-inspect/explain-data {::eql-inspect/value     {:a 42 :b 42}
                           ::eql-inspect/alias-key :as
                           ::eql-inspect/query     [:a {:b [:c]} :d {:e [:f]}]})
=> {:net.molequedeideias.eql-inspect/value {:a 42, :b 42},
    :net.molequedeideias.eql-inspect/alias-key :as,
    :net.molequedeideias.eql-inspect/query [:a {:b [:c]} :d {:e [:f]}],
    :net.molequedeideias.eql-inspect/problems ({:property :c,
                                                :dispatch-key :c,
                                                :path [:b],
                                                :value 42,
                                                :problem :net.molequedeideias.eql-inspect/expect-collection}
                                               {:property :d,
                                                :dispatch-key :d,
                                                :path [],
                                                :problem :net.molequedeideias.eql-inspect/missing-value}
                                               {:property :e,
                                                :dispatch-key :e,
                                                :path [],
                                                :query [:f],
                                                :problem :net.molequedeideias.eql-inspect/missing-value})}
```

Checkout `test` dir for more exaples

### Real World usage

My current usage is on a REST API.

My REST HANDLERS describe which data it need in `EQL`

Once this handle throws, I use this library to give a better error to my API Consumer.