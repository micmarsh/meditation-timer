(ns meditation-timer.events
  (:require
    [re-frame.core :as re-frame]
    [clojure.spec :as s]
    [meditation-timer.config :refer [debug?]]
    [meditation-timer.db :as db :refer [app-db]]
    [meditation-timer.timer.fx]))

;; -- Middleware ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/wiki/Using-Handler-Middleware
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check failed: " explain-data) explain-data)))))

(def validate-spec-mw
  (if debug?
    (re-frame/after (partial check-and-throw ::db/app-db))
    []))

;; -- Handlers --------------------------------------------------------------

(re-frame/reg-event-fx
  :initialize
  validate-spec-mw
  (fn [_ [_ playable-sound]]
    {:db app-db
     :sound/register-new {:id :bell-sound
                          :sound playable-sound}}))

(re-frame/reg-event-fx
 :start-countdown
 validate-spec-mw
 (fn [{:keys [db]} [_ {:keys [initial max min countdowns]}]]
   {:timer/start-new {:id :current-countdown
                      :time initial
                      :countdowns countdowns
                      :on-tick [:initial-timer-update]
                      :on-finished [:initial-timer-done countdowns min max]}
    :db (assoc db :message (str initial " seconds to start") :state :initial-countdown)}))

(re-frame/reg-event-db
 :initial-timer-update
 (fn [db [_ millis-left]]
   (assoc db :message (str (inc (quot millis-left 1000)) " seconds to start"))))

(re-frame/reg-cofx :rand (fn [cofx _] (assoc cofx :rand (rand))))

(defn calculate-time [rand min max]
  (* (if debug? 1 60)
     (+ min (int (* rand (- max min))))))

(re-frame/reg-event-fx
 :initial-timer-done
 [validate-spec-mw (re-frame/inject-cofx :rand)]
 (fn [{:keys [db rand]} [_ countdowns min max]]
   {:db (assoc db :message "Meditating..." :state :main-countdown)
    :sound/play :bell-sound
    :timer/start-new (let [time (calculate-time rand min max)]
                       {:id :current-countdown
                        :countdowns countdowns
                        :time time
                        :on-finished [:main-timer-done time]})}))

(defn result-time-message [time]
  (str "Meditated for "
       (if debug?
         (str time " seconds")
         (let [result (quot time 60)]
           (if (== 1 result)
             "1 minute"
             (str result " minutes"))))))

(re-frame/reg-event-fx
 :main-timer-done
 validate-spec-mw
 (fn [{:keys [db]} [_ time]]
   {:db (assoc db :state :done :message (result-time-message time))
    :sound/play :bell-sound}))

(re-frame/reg-event-fx
 :pause-current-timer
 validate-spec-mw
 (fn [{:keys [db]} _]
   {:db (assoc db :paused? true)
    :timer/pause :current-countdown}))

(re-frame/reg-event-fx
 :resume-current-timer
 validate-spec-mw
 (fn [{:keys [db]} _]
   {:db (assoc db :paused? false)
    :timer/unpause :current-countdown}))

(re-frame/reg-event-fx
 :stop-current-timer
 validate-spec-mw
 (fn [{:keys [db]} _]
   {:db app-db
    :timer/stop :current-countdown}))
