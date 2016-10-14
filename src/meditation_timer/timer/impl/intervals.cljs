(ns meditation-timer.timer.impl.intervals
  (:require [meditation-timer.timer.protocols :as p]))

(def noop (constantly nil))

(defn ->throw-ex
  ([message] (->throw-ex message {}))
  ([message data] (fn [& _] (throw (ex-info message data)))))

(def finished? (some-fn neg? zero?))

(defn countdown [time-ms {:keys [on-tick on-finished tick
                                 set-interval clear-interval]
                          :or {on-tick noop
                               on-finished noop
                               tick 1
                               set-interval (->throw-ex "Need to provide a setInterval implementation")
                               clear-interval (->throw-ex "Need to provide a clearInterval implementation")}
                          :as options}]
  (let [current-time (atom time-ms)
        paused? (atom false)
        tick-ms (* 1000 tick)
        interval-store (atom nil)
        update-fn (fn [time-ms tick-ms]
                    (if (finished? time-ms)
                      (do (clear-interval @interval-store)
                          (on-finished)
                          0)
                      (let [ms-left (- time-ms tick-ms)]
                        (on-tick ms-left)
                        ms-left)))
        interval (set-interval
                  #(when-not @paused? (swap! current-time update-fn tick-ms))
                  tick-ms)]
    (reset! interval-store interval)
    (swap! current-time update-fn tick-ms)
    (reify
      p/Pause (pause [this] (reset! @paused? true) this)
      p/Resume (resume [this] (reset! @paused? false) this)
      p/Stop (stop [this] (clear-interval @interval-store) this))))

(defrecord countdowns [set-interval clear-interval]
  p/StartCountdown
  (start-countdown [this time-secs options]
    (countdown (* 1000 time-secs) (merge options this))))
