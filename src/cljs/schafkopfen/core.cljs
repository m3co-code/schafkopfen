(ns schafkopfen.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def app-state (atom {:header "Schafkopfen at BarCamp"
                          :players [
                                    {:name "Player 1"}
                                    {:name "Player 2"}
                                    {:name "Player 3"}
                                    {:name "Player 4"}]}))

(defn player-view [player owner]
  (reify
    om/IRender
    (render [this]
            (dom/li nil (:name player)))))

(defn players-view [data owner]
  (reify
    om/IRender
    (render [this]
            (dom/div nil
                     (dom/h2 nil "Players")
                     (apply dom/ul nil
                            (om/build-all player-view (:players data)))
                     (dom/button nil "add player")))))

(defn main []
  (om/root
    (fn [app owner]
      (reify
        om/IRender
        (render [_]
                (dom/div nil
                         (dom/h1 nil (:header app))
                         (om/build players-view app)))))
    app-state
    {:target (. js/document (getElementById "app"))}))
