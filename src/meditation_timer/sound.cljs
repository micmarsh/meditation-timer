(ns meditation-timer.sound
  (:require [re-frame.core :refer [reg-fx]]))

(defprotocol PlaySound (play [this]))

(def sounds
  "id -> thing that implements PlaySound"
  (atom {}))

(reg-fx
 :sound/register-new
 (fn [{:keys [id sound]}]
   (swap! sounds assoc id sound)))

(reg-fx
 :sound/play
 (fn [id]
   (if-let [sound (get @sounds id)]
     (play sound)
     (println "No sound found for id" id))))

