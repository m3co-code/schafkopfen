(ns schafkopfen.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]))

(enable-console-print!)

(defonce app-state (atom {:header "Schafkopfen at BarCamp"
                          :players [{:name "Player 1"}
                                    {:name "Player 2"}
                                    {:name "Player 3"}
                                    {:name "Player 4"}]}))

(defn player-view [player owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [delete]}]
      (dom/li nil
              (:name player)
              (dom/button #js {:type "button"
                               :onClick (fn [e] (put! delete player))
                               :style #js {:marginLeft "10px"}
                               :className "btn btn-danger btn-xs"}
                          "delete")))))


(defn players-view [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:delete (chan)
       :warning nil})
    om/IWillMount
    (will-mount [_]
      (let [delete (om/get-state owner :delete)]
        (go-loop []
          (let [player (<! delete)]
            (om/transact! data :players
                          (fn [xs] (vec (remove #(= player %) xs))))
            (recur)))))
    om/IRenderState
    (render-state [this {:keys [delete warning]}]
      (let [on-name-submit
            (fn [e]
              (let [input (om/get-node owner "new-player-name")
                    name (-> input .-value)]
                (cond
                  (or (not name) (= "" name)) (om/set-state! owner :warning "Invalid name!")
                  (some #(= name (:name %)) (:players data)) (om/set-state! owner :warning "Duplicate user!")
                  :default (do
                             (om/transact! data :players #(conj % {:name name}))
                             (set! (.-value input) "")
                             (om/set-state! owner :warning nil))))
              false)]
        (dom/div nil
                 (dom/h2 nil "Players")
                 (apply dom/ul nil
                        (om/build-all player-view (:players data) {:init-state {:delete delete}}))
                 (when-not (nil? warning) (dom/div #js {:className "alert alert-danger" :role "alert"} warning))
                 (dom/form #js {:onSubmit on-name-submit}
                           (dom/input #js {:type :text :placeholder "Name" :ref "new-player-name"})
                           (dom/button
                            #js {:onClick on-name-submit}
                            "add player")))))))

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
