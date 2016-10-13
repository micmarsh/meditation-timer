(ns meditation-timer.events
  (:require
    [re-frame.core :as re-frame]
    [clojure.spec :as s]
    [meditation-timer.db :as db :refer [app-db]]))

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
  (if goog.DEBUG
    (re-frame/after (partial check-and-throw ::db/app-db))
    []))

;; -- FX (move this shit elsewhere) -----------------------------------------

(def timers
  "id -> Timer js object"
  (atom {}))

(def Timer (js/require "timer"))

(re-frame/reg-fx
 :timer/start-new
 (fn [{:keys [id time on-tick on-finished]}]
   (swap! timers assoc id
          (.start
           (Timer.
            #js {:tick 1
                 :ontick (fn [& args] (re-frame/dispatch (into on-tick args)))
                 :onend  (fn [& args] (re-frame/dispatch (into on-finished args)))})
           time))))

(re-frame/reg-fx
 :timer/pause
 (fn [id] (swap! timers update id #(.pause %))))

(re-frame/reg-fx
 :timer/unpause
 (fn [id] (swap! timers update id #(.start %))))

(re-frame/reg-fx
 :timer/stop
 (fn [id]
   (swap! timers update id #(.stop %))
   (swap! timers dissoc id)))

;; -- Handlers --------------------------------------------------------------

(re-frame/reg-event-db
  :initialize-db
  validate-spec-mw
  (fn [_ _]
    app-db))

(re-frame/reg-event-fx
  :start-countdown
  validate-spec-mw
  (fn [{:keys [db]} [_ {:keys [initial max min]}]]
    {:timer/start-new {:id :initial-countdown
                       :time initial ;; seconds
                       :on-tick [:initial-timer-update]
                       :on-finished [:initial-timer-done min max]}
     :db (assoc db :greeting (str "counting down from " initial " seconds"))}))

(re-frame/reg-event-db
 :initial-timer-update
 (fn [db [_ & args]]
   (assoc db :greeting (str args))))

(re-frame/reg-event-db
 :initial-timer-done
 (fn [db [_ min max]]
   (assoc db :greeting (str "Initial countdown done! "  min " " max))))
;(* 60 (+ min (int (* (rand) (- max min)))))

