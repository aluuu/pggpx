(ns pggpx.core
  (:gen-class :main true)
  (:use
   [clojure.contrib.command-line :only (with-command-line)]
   [clj-time.core :only (interval in-minutes)]
   [clj-time.coerce :only (to-date-time to-string)]
   korma.core
   korma.db
   hiccup.core
   hiccup.page)
  (:require
   [clojure.java.io :as io]
   [pggpx.settings :as settings]
   [pggpx.utils :as utils]))

(defdb
  db
  (postgres
   {:db (settings/marks-DB :db)
    :user (settings/marks-DB :user)
    :password (settings/marks-DB :password)
    :host (settings/marks-DB :host)
    :port (settings/marks-DB :port)}))

(defentity
  marks
  (table :marks))

(defn get-marks [date-from date-to device-id]
  (select
   marks
   (where
    (and
     (>= :datetime (java.sql.Timestamp/valueOf date-from))
     (<= :datetime (java.sql.Timestamp/valueOf date-to))
     (= :device_id (read-string device-id))
     (not= :latitude 0)
     (not= :longitude 0)))))

(defn get-segments [marks &{:keys [max-distance max-period] :or {max-distance nil max-period nil}}]
  (let [distance-fn (fn [curr prev]
                      (if max-distance
                        (> (utils/distance curr prev) max-distance)
                        false))
        time-fn (fn [curr prev]
                  (if max-period
                    (> (in-minutes (interval (to-date-time (prev :datetime))
                                             (to-date-time (curr :datetime)))) max-period)
                    false))
        split-fn (fn [curr prev]
                   (or (distance-fn curr prev)
                       (time-fn curr prev)))]

    (utils/partition-by-with-prev split-fn marks)))

(defn mark-to-xml [mark]
  [:trkpt
   {:lat (mark :latitude)
    :lon (mark :longitude)}
   [:time (to-string (mark :datetime))]
   [:sat (mark :satcount)]
   [:azimuth (mark :azimuth)]
   [:hdop (mark :hdop)]])

(defn -main [& args]
  (with-command-line
    args
    "From PostgreSQL to GPX"
    [[date-from "from date"]
     [date-to "to date"]
     [device-id "device id"]
     [output-filename "filename" "/tmp/output.gpx"]
     [max-period "max period between marks inside one segment (minutes)" nil]
     [max-distance "maximum distance between marks inside one segment (meters)" nil]]
    (if (not (and date-from date-to device-id))
      (do
        (println "Введите начальное время, конечное время и id устройства")
        (System/exit 1)))
    (let [gpx-name  (format "Track for device #%s: %s - %s" device-id date-from date-to)
          max-distance (if max-distance (read-string max-distance) nil)
          max-period (if max-period (read-string max-period) nil)]
      (with-open [wrtr (io/writer output-filename)]
        (.write
         wrtr
         (html
          (xml-declaration "UTF-8")
          [:gpx {:version "1.0"}
           [:name gpx-name]
           [:trk
            [:name gpx-name]
            (let [marks (get-marks date-from date-to device-id)
                  segments (get-segments marks :max-distance max-distance :max-period max-period)]
              (for [segment segments]
                [:trkseg (map mark-to-xml segment)]))]]))))))
