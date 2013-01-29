(ns pggpx.utils)

(def D2R (/ Math/PI 180.0))

(defn distance [point1 point2]
  ;; distance according Vincenty's formulae for WGS-84 ellipsoid
  (let [; WGS-84 ellipsiod
        a 6378137.0
        b 6356752.3142
        f (/ 1 298.257223563)
        lat1 (* D2R (point1 :latitude))
        lon1 (* D2R (point1 :longitude))
        lat2 (* D2R (point2 :latitude))
        lon2 (* D2R (point2 :longitude))
        L (- lon2 lon1)
        U1 (Math/atan (* (- 1 f) (Math/tan lat1)))
        U2 (Math/atan (* (- 1 f) (Math/tan lat2)))
        sinU1 (Math/sin U1)
        cosU1 (Math/cos U1)
        sinU2 (Math/sin U2)
        cosU2 (Math/cos U2)]
    (loop [eps 1000 cnt 100.0 lambdaPrev L sinSigma 0 cosSigma 0 cosSqAlpha 0 cos2SigmaM 0 sigma 0]
      (if (and (> eps 1e-12) (> cnt 0))
        (let [sinLambda (Math/sin lambdaPrev)
              cosLambda (Math/cos lambdaPrev)
              newSinSigma (Math/sqrt
                            (+ (Math/pow (* cosU2 sinLambda) 2)
                               (Math/pow (- (* cosU1 sinU2)
                                            (* sinU1 cosU2 cosLambda)) 2)))
              newCosSigma (+ (* sinU1 sinU2)
                             (* cosU1 cosU2 cosLambda))
              sigma (Math/atan2 newSinSigma newCosSigma)
              sinAlpha (/ (* cosU1 cosU2 sinLambda) newSinSigma)
              newCosSqAlpha (- 1 (Math/pow sinAlpha 2))
              newCos2SigmaM (- cosSigma (/ (* 2 sinU1 sinU2) newCosSqAlpha))
              C (* (/ f 16) newCosSqAlpha (+ 4 (* f (- 4 (* 3 newCosSqAlpha)))))
              lambda (+ L (* (- 1 C) f sinAlpha
                             (+ sigma (* C newSinSigma
                                         (+ newCos2SigmaM
                                            (* C newCosSigma (- (* 2 (Math/pow newCos2SigmaM 2)) 1)))))))]
          (recur (Math/abs (- lambda lambdaPrev)) (- cnt 1) lambda newSinSigma newCosSigma newCosSqAlpha cos2SigmaM sigma))
        (if (not= cnt 0)
          (let [uSq (/ (* cosSqAlpha (- (* a a) (* b b))) (* b b))
                A (+ 1 (/ uSq (* 16384 (+ 4096 (* uSq (- (* uSq (- 350 (* 174 uSq))) 768))))))
                B (/ uSq (* 1024 (+ 256 (* uSq (+ -128 (* uSq (- 74 (* 47 uSq))))))))
                deltaSigma (* B sinSigma
                              (+ cos2SigmaM
                                 (/ B
                                    (* 4
                                       (- (* cosSigma
                                             (- (* 2 cos2SigmaM cos2SigmaM) 1))
                                          (/ B (* 6 cos2SigmaM
                                                  (- (* 4 sinSigma sinSigma) 3)
                                                  (- (* 4 cos2SigmaM cos2SigmaM) 3))))))))]
            (* b A (- sigma deltaSigma)))
          -1)))))

(defn take-while-with-prev
  [pred coll &{:keys [prev] :or {prev nil}}]
  (lazy-seq
    (when-let [s (seq coll)]
      (when (pred (first s) prev)
        (cons (first s) (take-while-with-prev pred (rest s) :prev (first s)))))))

(defn partition-by-with-prev
  [f coll]
  (lazy-seq
    (when-let [s (seq coll)]
      (let [fst (first s)
            run (cons fst
                      (take-while-with-prev #(not (f %1 %2)) (rest s) :prev fst))]
        (cons run (partition-by-with-prev f (drop (count run) s)))))))
