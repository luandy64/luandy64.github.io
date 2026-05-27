(ns main
  (:require
   [reagent.core :as r]
   [reagent.dom :as rdom]))

(defonce state (r/atom nil))

(defn split-shared
  [groups]
  (let [num-people       (count (dissoc groups :all))
        shared-total     (->> groups
                              :all
                              (mapv second)
                              (reduce +))
        shared-things    (mapv first
                               (:all groups))
        shared-cost      (/ shared-total
                            num-people
                            #_1.0)
        shared-line-item (conj shared-things
                               shared-cost)]
    (->> (dissoc groups :all)
         (reduce (fn [acc [person things]]
                   (assoc acc
                          person
                          (conj things
                                shared-line-item)))
                 {}))))

(defn divide-split-items
  [items]
  (reduce (fn [acc [item cost & people]]
            (reduce (fn [acc2 person]
                      (update acc2
                              person
                              (fnil conj [])
                              [item (/ cost
                                       (count people))]))
                    acc
                    people))
          {}
          items))

(defn group-subtotal
  [{:keys [subtotal] :as _meal}
   groups]
  (->> groups
       (reduce (fn [acc [person things]]
                 (assoc acc
                        person
                        {:items    things
                         :subtotal (->> things
                                        (mapv second)
                                        (reduce +))}))
               {})))

#_(defn round-cents
  [amt]
  (->> amt
       (format "%.2f")
       parse-double))

(defn split-amount
  [label amount total groups]
  (->> groups
       (reduce (fn [acc [person {person-total :subtotal}]]
                 (update acc
                         person
                         assoc
                         label
                         (* amount
                            (/ person-total
                               total))))
               groups)))

(defn apply-taxes-and-fees
  [meal groups]
  (reduce (fn [acc [label amount]]
            (split-amount label amount
                          (:subtotal meal)
                          acc))
          groups
          (select-keys meal
                       [:surcharge :tax :tip])))

(defn group-total
  [groups]
  (->> groups
       (reduce (fn [acc [person {:keys [subtotal surcharge tax tip]}]]
                 (update acc
                         person
                         assoc
                         :total
                         (+ subtotal
                            surcharge
                            tax
                            tip)))
               groups)))

(defn sum-by-key
  [k coll]
  (->> coll
       vals
       (mapv k)
       (reduce +)))

(defn delete-row
  [i]
  (swap! state
         update
         :items
         dissoc
         i))

(defn render-line-items
  [state]
  [:ul
   (for [[i {:keys [item cost]}] (:items @state)]
     [:li
      [:button
       {:on-click
        #(delete-row i)}
       "X"]
      [:input
       {:type "text"
        :value (get-in @state [:items i :item])
        :on-change #(swap! state
                           update
                           :items
                           assoc-in [i :item] (.-value (.-target %)))}]
      [:input
       {:type "text"
        :value (get-in @state [:items i :cost])
        :on-change #(swap! state
                           update
                           :items
                           assoc-in [i :cost] (.-value (.-target %)))}]])])

(defn app []
  (if-let [user  (:user @state)]
    [:main
     [:h1 (str "Hello " user)]
     [:p "State: " (pr-str @state)]
     [:button
      {:on-click
       (fn []
         (swap! state
                update
                :items
                dissoc
                (apply max (keys (:items @state)))))}
      "Decrement"]
     [:button
      {:on-click
       (fn []
         (swap! state update :counter inc)
         (let [index (:counter @state)
               new-item (str "Line " index)]
           (swap! state update :items assoc index {:item new-item
                                                   :cost 0})))}
      "Add Item"]
     [:button
      {:on-click #(swap! state dissoc :user)}
      "Change name"]
     (render-line-items state)]
    [:main
     [:div
      [:h2 "Hi, what's your name?"]
      [:input {:type "text"
               :value (:user-input @state)
               :on-change #(swap! state assoc :user-input (.-value (.-target %)))}]
      [:button
       {:on-click
        (fn []
          (swap! state assoc :user (:user-input @state))
          (swap! state assoc :items {})
          (swap! state dissoc :user-input))}
       "Save!"]]]))

(rdom/render [app] (.getElementById js/document "app"))
