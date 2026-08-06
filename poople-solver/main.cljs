(ns poople.core
  (:require
   [clojure.string :as string]
   [reagent.core :as r]
   [reagent.dom :as rdom]))

(defonce allowed-words (r/atom {"POOP" 0}))

(defn parse-words
  "Parse the raw poople-words text into a {word distance} map.
   The file uses literal \"\\r\\n\" separators and \"word,distance\" pairs."
  [raw]
  (->> (re-seq #"([A-Z]+),(\d+)" raw)
       (map (fn [[_ word dist]]
              [word (js/parseInt dist 10)]))
       (into {"POOP" 0})))

(defn load-allowed-words! []
  (-> (js/fetch "/poople-words")
      (.then (fn [resp] (.text resp)))
      (.then (fn [raw] (->> raw
                             parse-words
                             (reset! allowed-words))))
      (.catch (fn [err] (js/console.error "Failed to load poople-words:" err)))))

(defn valid?
  [aa bb]
  (-> (->> (mapv vector
                 aa
                 bb)
           (mapv (fn [[a b]]
                   (if (= a b)
                     1
                     0)))
           frequencies)
      (get 0)
      (= 1)))

(defn look-down
  [word]
  (let [prev-level (-> @allowed-words
                       (get word 0)
                       dec)]
    (->> @allowed-words
         (filter (fn [[candidate dist]]
                   (and (= prev-level dist)
                        (valid? candidate word)))))))

(defn solve
  [starting-word]
  (let [dist (get @allowed-words starting-word 0)]
    (println "\n" starting-word dist)
    (->> starting-word
         (iterate (fn [word]
                    (let [new-words (look-down word)]
                      (println new-words)
                      (-> new-words
                          first
                          first))))
         (take (inc dist))
         vec)))

(defn solve-all
  ([starting-word]
   (solve-all starting-word
              (look-down starting-word)
              [starting-word]))
  ([starting-word stack chains]
   (if (seq stack)
     (reduce (fn [acc [word _]]
               (->> (solve-all word
                               (look-down word)
                               [word])
                    (mapv (fn [chain]
                            (into [starting-word]
                                  chain)))
                    (into acc)))
             []
             stack)
     [chains])))

(def app-state
  (r/atom {:input ""
           :words []
           :variation-count 0}))

(defn process-input
  [input-text]
  (if (empty? input-text)
    []
    (solve input-text)))

(defn submit-handler
  []
  (let [input-text (:input @app-state)
        result     (process-input input-text)
        variations (solve-all input-text)]
    (when (< 0 (count input-text))
      (swap! app-state
             assoc
             :words           result
             :variations      variations
             :variation-count (count variations)))))

(defn stats
  []
  (let [vs (:variation-count @app-state)]
    (when-not (zero? vs)
      [:p "Variations: " vs])))

(defn word-grid
  [words]
  (when-not (empty? words)
    (js/console.log words)
    [:table.grid
     [:tbody
      (for [word words]
        ^{:key word}
        (let [[a b c d] word]
          [:tr
           [:td (if (= "P" a)
                  {:class "poop"}
                  {})
            a]
           [:td (if (= "O" b)
                  {:class "poop"}
                  {})
            b]
           [:td (if (= "O" c)
                  {:class "poop"}
                  {})
            c]
           [:td (if (= "P" d)
                  {:class "poop"}
                  {})
            d]]))]]))

(defn grids
  []
  (let [all-words (:variations @app-state)
        indexed-words (map-indexed vector all-words)]
    (js/console.log "grids: indexed-words" indexed-words)
    (when-not (empty? all-words)
      [:div
       (for [[i v] indexed-words]
         ^{:key i}
         [:div
          [:p (inc i)]
          (word-grid v)])])))

(defn app
  []
  [:<>
   [:div.input-section
    [:h1 "Poople Solver"]
    [:input
     {:type "text"
      :placeholder "Enter text"
      :value (:input @app-state)
      :on-change (fn [e]
                   (swap! app-state assoc :input (-> (.. e -target -value)
                                                     string/upper-case)))
      :on-key-down (fn [e]
                     (when (-> e
                               .-key
                               (= "Enter"))
                       (submit-handler)))}]
    [:button {:on-click submit-handler}
     "Submit"]]
   [stats]
   [grids]
   ])

(load-allowed-words!)

(rdom/render [app] (js/document.getElementById "app"))
