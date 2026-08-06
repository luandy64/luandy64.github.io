(ns heatmap
  (:require
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [goog.string :as gstring]))

(defonce state (r/atom nil))

(defn flatten-data
  [raw]
  (reduce (fn [result [date repos]]
            (assoc result
                   date
                   (reduce +
                           (vals repos))))
          {}
          raw))

(defn construct-weeks
  []
  (let [today             (js/Date.)
        _                 (.setHours today 0 0 0 0)
        start             (js/Date. today)
        _                 (.setDate start (- (.getDate today)
                                             (* 52 7)))
        day-of-week       (.getDay start)
        days-since-monday (if (= day-of-week 0)
                            6
                            (- day-of-week 1))
        _                 (.setDate start (- (.getDate start)
                                             days-since-monday))]
    (loop [cur (js/Date. start)
           weeks []]
      (if (<= cur today)
        (let [week (loop [i      0
                          result []]
                     (if (< i 7)
                       (let [day (js/Date. cur)]
                         (.setDate cur (+ 1
                                          (.getDate cur)))
                         (recur (inc i)
                                (conj result day)))
                       result))]
          (recur cur
                 (conj weeks week)))
        weeks))))

(defn get-color
  [num-input]
  (cond
    (=  num-input 0)  "var(--color-empty)"
    (=  num-input 1)  "var(--color-l1)"
    (<= num-input 5) "var(--color-l2)"
    (<= num-input 10) "var(--color-l3)"
    :else             "var(--color-l4)"))

(defn render
  [weeks counts]
  (let [cell       13
        gap        2
        shift      (+ cell gap)
        svgNS      "http://www.w3.org/2000/svg"
        svg-width  (+ (* (count weeks) shift)
                      30)
        svg-height (* shift 7)
        svg        (doto (.createElementNS js/document svgNS "svg")
                     (.setAttribute "viewBox" (gstring/format "0 0 %s %s"
                                                              svg-width
                                                              svg-height))
                     (.setAttribute "width" "100%"))
        labels     (map-indexed vector ["Mon" "Wed" "Fri" "Sun"])]
    (doseq [[i label] labels]
      (->> (doto (.createElementNS js/document svgNS "text")
             (.setAttribute "x" 0)
             (.setAttribute "y" (+ (* 2 i shift)
                                   (* 0.75 cell)))
             (.setAttribute "font-size" 12)
             (.appendChild (.createTextNode js/document label)))
           (.appendChild svg)))
    (doall
     (for [col (range (count weeks))
           row (range (count (first weeks)))]
       (let [week (get weeks col)
             day  (get week row)]

         (when (some? day)
           (let [day-key (-> (.toISOString day)
                             (.slice 0 10))
                 day-value (get counts day-key 0)]
             (->> (doto (.createElementNS js/document svgNS "rect")
                    (.setAttribute "x" (+ (* col shift)
                                          30))
                    (.setAttribute "y" (* row shift))
                    (.setAttribute "height" cell)
                    (.setAttribute "width" cell)
                    (.setAttribute "fill" (get-color day-value))
                    (.setAttribute "rx" 2)
                    (.setAttribute "data-date" day-key)
                    (.setAttribute "data-count" day-value))
                  (.appendChild svg)))))))
    svg))

(defn total-counts
  [counts]
  (->> counts
       (map second)
       (reduce +)))

(defn render-legend
  []
  (let [colors ["var(--color-empty)"
                "var(--color-l1)"
                "var(--color-l2)"
                "var(--color-l3)"
                "var(--color-l4)"]
        cell   11
        gap    3
        shift  (+ cell gap)
        svgNS  "http://www.w3.org/2000/svg"
        svg    (doto (.createElementNS js/document svgNS "svg")
                 (.setAttribute "width" (- (* 5 shift)
                                           gap))
                 (.setAttribute "height" cell))]
    (set! (.. svg -style -display) "inline-block")
    (set! (.. svg -style -verticalAlign) "middle")
    (doall
     (for [[i color] (map-indexed vector colors)]
       (->> (doto (.createElementNS js/document svgNS "rect")
              (.setAttribute "x" (* i shift))
              (.setAttribute "y" 0)
              (.setAttribute "height" cell)
              (.setAttribute "width" cell)
              (.setAttribute "fill" color)
              (.setAttribute "rx" 2))
            (.appendChild svg))))
    svg))

(defn setup-tooltips
  [svg]
  (let [tooltip (.getElementById js/document "tooltip-cljs")]
    (-> (.getElementById js/document "heatmap-cljs")
        (.appendChild tooltip))

    (.addEventListener svg
                       "mouseover"
                       (fn [e]
                         (when (= "rect"
                                  (.. e -target -tagName))
                           (let [content
                                 (gstring/format "%s - %s contribution%s"
                                                 (.. e -target -dataset -date)
                                                 (.. e -target -dataset -count)
                                                 (if (= 1 count)
                                                   ""
                                                   "s"))]
                             (set! (.-textContent tooltip)
                                   content)
                             (-> tooltip
                                 .-classList
                                 (.remove "hidden"))))))

    (.addEventListener svg
                       "mousemove"
                       (fn [e]
                         (set! (.. tooltip -style -left)
                               (str (+ (.-clientX e) 12) "px"))
                         (set! (.. tooltip -style -top)
                               (str (- (.-clientY e) 28) "px"))))

    (.addEventListener svg
                       "mouseout"
                       (fn [e]
                         (when (= "rect"
                                  (.. e -target -tagName))
                           (-> tooltip
                               .-classList
                               (.add "hidden")))))))

((^:async fn []
  (let [response (await (js/fetch "/activity.json"))
        json (await (.json response))
        data (js->clj json)]
    (reset! state data))))

(defn app
  []
  (let [data   @state
        counts (flatten-data data)
        weeks  (construct-weeks)
        svg    (render weeks counts)]

    (if (some? (.getElementById js/document "heatmap-cljs"))
      (do
        (-> js/document
            (.getElementById "heatmap-cljs")
            (.appendChild svg))
        (setup-tooltips svg))
      (js/console.log "NULL heatmap-cljs"))

    (if (.getElementById js/document "contribution-count")
      (set! (-> js/document
                (.getElementById "contribution-count")
                .-textContent)
            (gstring/format "%s contributions in the last year"
                            (total-counts counts))))

    (if (.getElementById js/document "heatmap-cljs-legend")
      (doto (.getElementById js/document "heatmap-cljs-legend")
        (.appendChild (.createTextNode js/document "Less"))
        (.appendChild (render-legend))
        (.appendChild (.createTextNode js/document "More")))
      (js/console.log "NULL heatmap-cljs-legend"))

    [:div
     [:div {:id "heatmap-cljs"}]
     [:div {:id "heatmap-cljs-footer"}
      [:span {:id "contribution-count"}]
      [:span {:id "heatmap-cljs-legend"}]]
     [:div {:id    "tooltip-cljs"
            :class "hidden"}]]))

(rdom/render [app]
             (.getElementById js/document "app-heatmap-cljs"))
