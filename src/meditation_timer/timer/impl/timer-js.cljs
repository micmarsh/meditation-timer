(ns meditation-timer.timer.impl.timer-js
  (:require [meditation-timer.timer.protocols :as p]))

(def noop (constantly nil))

(defrecord Timer [js-timer]
  p/Pause (pause [this] (.pause js-timer) this)
  p/Resume (resume [this] (.start js-timer) this)
  p/Stop (resume [this] (.stop js-timer) this))

(defrecord countdowns [Constructor]
  p/StartCountdown
  (start-countdown [_ time-secs {:keys [on-tick on-finished tick]
                                 :or {on-tick noop
                                      on-finished noop
                                      tick 1}
                                 :as options}]
    (-> #js {:tick tick
             :ontick on-tick
             :onend on-finished}
        (Constructor.)
        (.start time-secs)
        (->Timer))))
