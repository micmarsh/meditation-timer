(ns meditation-timer.timer.fx
  (:require [re-frame.core :refer [reg-fx dispatch]]
            [meditation-timer.timer.protocols :as p]))

(def timers
  "id -> impl of p/Pause, Resume, Stop"
  (atom {}))

(defn ->callback [event]
  (fn [& args] (dispatch (into event args))))

(defn ->callbacks
  [{:keys [on-tick on-finished] :as options}]
  (merge options
         (when on-tick {:on-tick (->callback on-tick)})
         (when on-finished {:on-finished (->callback on-finished)})))

(reg-fx
 :timer/start-new
 (fn [{:keys [id countdowns time] :as options}]
   (swap! timers assoc id
          (p/start-countdown countdowns time
                             (->callbacks options)))))

(reg-fx
 :timer/pause
 (fn [id] (swap! timers update id #(when % (p/pause %)))))

(reg-fx
 :timer/unpause
 (fn [id] (swap! timers update id #(when % (p/resume %)))))

(reg-fx
 :timer/stop
 (fn [id]
   (swap! timers update id #(when % (p/stop %)))
   (swap! timers dissoc id)))

