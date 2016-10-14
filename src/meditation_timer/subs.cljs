(ns meditation-timer.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :main-message
  (fn [db _]
    (if (:paused? db)
      "Paused"
      (:message db))))

(reg-sub
 :paused?
 (fn [db _] (:paused? db)))

(reg-sub
 :counting-down?
 (fn [db _]
   (#{:initial-countdown :main-countdown} (:state db))))
