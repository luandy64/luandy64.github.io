(ns scittle-app-num-001
  (:require
    [reagent.core :as r]
    [reagent.dom :as rdom]))

(defonce state (r/atom nil))

(defn app []
  (if-let [user  (:user @state)]
    [:main
     [:h1 (str "Hello " user)]
     [:pre "State: " (pr-str @state)]
     [:button
      {:on-click #(swap! state update :counter dec)}
      "Decrement"]
     [:button
      {:on-click #(swap! state update :counter inc)}
      "Increment"]
     [:button
      {:on-click #(swap! state dissoc :user)}
      "Change name"]]
    [:main
     [:div
      [:h2 "Hi, what's your name?"]
      [:input {:type "text"
               :value (:user-input @state)
               :on-change #(swap! state assoc :user-input (.-value (.-target %)))}]
      [:button
       {:on-click (fn []
                    (swap! state assoc :user (:user-input @state))
                    (swap! state dissoc :user-input))}
       "Save!"]]]))

(rdom/render [app] (.getElementById js/document "app"))
