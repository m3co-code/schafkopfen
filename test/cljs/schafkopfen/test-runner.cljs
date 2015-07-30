(ns schafkopfen.test-runner
  (:require
   [cljs.test :refer-macros [run-tests]]
   [schafkopfen.core-test]))

(enable-console-print!)

(defn runner []
  (if (cljs.test/successful?
       (run-tests
        'schafkopfen.core-test))
    0
    1))
