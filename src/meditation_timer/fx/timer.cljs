(ns meditation-timer.fx.timer
  (:require [re-frame.core :refer [reg-fx dispatch]]))

(def Timer (js/require "timer.js"))

(def timers
  "id -> Timer js object"
  (atom {}))

(reg-fx
 :timer/start-new
 (fn [{:keys [id time on-tick on-finished]}]
   (swap! timers assoc id
          (.start
           (Timer.
            #js {:tick 1
                 :ontick (fn [& args]
                           (when on-tick
                             (dispatch (into on-tick args))))
                 :onend  (fn [& args] (dispatch (into on-finished args)))})
           time))))

(reg-fx
 :timer/pause
 (fn [id] (swap! timers update id #(when % (.pause %)))))

(reg-fx
 :timer/unpause
 (fn [id] (swap! timers update id #(when % (.start %)))))

(reg-fx
 :timer/stop
 (fn [id]
   (swap! timers update id #(when % (.stop %)))
   (swap! timers dissoc id)))

