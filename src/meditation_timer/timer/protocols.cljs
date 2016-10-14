(ns meditation-timer.timer.protocols)

(defprotocol StartCountdown
  (start-countdown [_ time options]
    "Starts a countdown, 
     time is seconds
     options is a map contains 
       {:on-tick (fn [milliseconds-left] ...) 
        :on-finished (fn [] ...)
        :tick <seconds>}, all optional
     returns a object that implements Pause, Resume, and Stop"))

(defprotocol Pause
  (pause [countdown]
    "Pauses given countdown, should return (obviously mutated) self"))

(defprotocol Resume
  (resume [countdown]
    "Resumes given countdown, should return (obviously mutated) self"))

(defprotocol Stop
  (stop [countdown]
    "Cancels given countdown, should return (obviously mutated) self"))
