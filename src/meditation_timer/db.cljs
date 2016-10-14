(ns meditation-timer.db
  (:require [clojure.spec :as s]))

;; spec of app-db
(s/def ::message string?)
(s/def ::state #{:initial-countdown :main-countdown :done :start})
(s/def ::paused? boolean?)
(s/def ::app-db
  (s/keys :req-un [::message
                   ::state]))

;; initial state of app-db
(def app-db {:message "Enter your desired parameters"
             :state :start
             :paused? false})
