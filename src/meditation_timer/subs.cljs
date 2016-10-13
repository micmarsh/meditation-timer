(ns meditation-timer.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :main-message
  (fn [db _]
    (:greeting db)))
