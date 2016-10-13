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
  (let [message (subscribe [:main-message])
        initial-countdown (atom "")
        min-minutes (atom "")
        max-minutes (atom "")]
    (fn []
      [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
       [text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}} @message]
       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press #(let [max (number @max-minutes)
                                              min (number @min-minutes)
                                              initial (number @initial-countdown)]
                                          (when (and number (< min max))
                                            (dispatch [:start-countdown {:initial initial
                                                                         :max max
                                                                         :min min}])))}
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Start Timer"]]
       [view {:style {:flex-direction "row" :align-items "center"}}
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
