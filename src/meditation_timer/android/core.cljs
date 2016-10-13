(ns meditation-timer.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [meditation-timer.events]
            [meditation-timer.subs]))

(def ReactNative (js/require "react-native"))

(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def text-input (r/adapt-react-class (.-TextInput ReactNative)))


(def logo-img (js/require "./images/cljs.png"))

(defn number [string]
  (when-not (empty? string)
    (let [result (js/Number. string)]
      (when-not (js/isNaN result)
        result))))

(defn alert [title]
  (.alert (.-Alert ReactNative) title))

(defn text-bind-callback [atom]
  (fn [event]
    (->> event
         (.-nativeEvent)
         (.-text)
         (reset! atom))))

(defn app-root []
  (let [greeting (subscribe [:get-greeting])
        initial-countdown (atom "")
        min-minutes (atom "")
        max-minutes (atom "")]
    (fn []
      [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
       [text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}} @greeting]
       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press #(alert (str (< (number @min-minutes) (number @max-minutes))))}
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "press me"]]
       [view {:style {:flex-direction "row" :margin 20 :align-items "center"}}
        [text-input {:keyboard-type "numeric"
                     :style {:font-size 20 :width 50}
                     :on-change (text-bind-callback initial-countdown)}]
        [text-input {:keyboard-type "numeric"
                     :style {:font-size 20 :width 50}
                     :on-change (text-bind-callback min-minutes)}]
        [text-input {:keyboard-type "numeric"
                     :style {:font-size 20 :width 50}
                     :on-change (text-bind-callback max-minutes)}]]])))

(defn init []
      (dispatch-sync [:initialize-db])
      (.registerComponent app-registry "MeditationTimer" #(r/reactify-component app-root)))

(comment
  ;; Dumb timer scratchwork
  {:timer/start-new {:id 'blah
                     :time [12 :minutes]
                     :tick [1 :seconds] ;; optional, this is default
                     :on-tick [:blah-tick]
                     :on-finished [:blah-finished]}}

  {:timer/pause 'blah}
  {:timer/unpause 'blah}
  {:timer/stop 'blah}

  (re-frame/reg-event-fx
   :blah-tick
   (fn [cofx [_ {:keys [total-time current-time id]}]]
     ;; total-time is millis? Some kind of fancy-ass unit relating to
     ;; the DSL above?
     ;; Yeah fuck that^, these will be fucking millis. Provide
     ;; convenience converters if they're that sorely needed
     ))

  (re-frame/reg-event-fx
   :blah-finished
   (fn [cofx [_ {:keys [total-time id]}]]
     ;; total-time is millis? Yes
     ))
  
  )
