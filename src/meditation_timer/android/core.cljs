(ns meditation-timer.android.core
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [meditation-timer.config :refer [debug?]]
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

(defn number-input [label text-atom]
  [view {:style {:margin-left 10 :margin-right 10}}
   [text-input {:keyboard-type "numeric"
                :style {:font-size 20 :width 50 :color "#ccc" :text-align "center"}
                :on-change (text-bind-callback text-atom)}
    (str @text-atom)]
   [text {:style {:color "#ccc" :text-align "center" :font-weight "bold"}} label]])

(def sit-unit (if debug? "Seconds" "Minutes"))

(defn gray-button [button-text on-press]
  [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5
                                :margin-left 10 :margin-right 10}
                        :on-press on-press}
   [text {:style {:color "white" :text-align "center" :font-weight "bold"}} button-text]])

(defn app-root []
  (let [message (subscribe [:main-message])
        counting-down? (subscribe [:counting-down?])
        paused? (subscribe [:paused?])
        initial-countdown (atom "")
        min-minutes (atom "")
        max-minutes (atom "")]
    (fn []
      [view {:style {:flex-direction "column" :margin 40 :align-items "center" :background-color "#333" }}
       [text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center" :color "#ccc"}} @message]
       (if @counting-down?
         [view {:style {:flex-direction "row" :align-items "center"}}
          (if @paused?
            [gray-button "Resume Timer" #(dispatch [:resume-current-timer])]
            [gray-button "Pause Timer" #(dispatch [:pause-current-timer])])
          [gray-button "Stop Timer" #(dispatch [:stop-current-timer])]]
         [gray-button "Start Timer"
          #(let [max (number @max-minutes)
                 min (number @min-minutes)
                 initial (number @initial-countdown)]
             (when (and initial (< min max))
               (dispatch [:start-countdown {:initial initial
                                            :max max
                                            :min min}])))])
       (if @counting-down?
         [view {:style {:flex-direction "column" :align-items "center"
                        :margin-top 20}}
          [text {:style {:font-size 20 :font-weight "100" :text-align "center" :color "#ccc"}}
           (str "Minimum " sit-unit ": " @min-minutes)]
          [text {:style {:font-size 20 :font-weight "100" :text-align "center" :color "#ccc"}}
           (str "Maximum " sit-unit ": " @max-minutes)]]
         [view {:style {:flex-direction "row" :align-items "center"}}
          [number-input "Initial\nSeconds" initial-countdown]
          [number-input (str "Minimum\n" sit-unit) min-minutes]
          [number-input (str "Maximum\n" sit-unit) max-minutes]])])))

(defn init []
      (dispatch-sync [:initialize-db])
      (.registerComponent app-registry "MeditationTimer" #(r/reactify-component app-root)))
