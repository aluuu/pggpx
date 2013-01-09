(ns pggpx.core
  (:gen-class :main true)
  (:use pggpx.settings)
  (:use clojure.contrib.command-line)
  (:use clojure.java.io)
  (:use korma.core)
  (:use korma.db)
  (:use hiccup.core)
  (:use hiccup.page)
  (:use clj-time.coerce))

(defdb db (postgres
  {:db (marks-DB :db)
   :user (marks-DB :user)
   :password (marks-DB :password)
   :host (marks-DB :host)
   :port (marks-DB :port)}))

(defentity marks
  (table :marks))

(defn get-marks [date-from date-to device-id]
  (select marks
    (where
     (and
       (>= :datetime (java.sql.Timestamp/valueOf date-from))
       (<= :datetime (java.sql.Timestamp/valueOf date-to))
       (= :device_id (read-string device-id))))))

(defn mark-to-xml [mark]
  [:trkpt
  {:lat (mark :latitude)
   :lon (mark :longitude)}
   [:time (to-string (mark :datetime))]])

(defn -main [& args]
  (with-command-line args
  	"From PostgreSQL to GPX"
  	[[date-from "from date"]
   [date-to "to date"]
   [device-id "device id"]
   [output-filename "filename" "/tmp/output.gpx"]]
   (if (not (and date-from date-to device-id))
    (do
      (println "Введите начальное время, конечное время и id устройства")
      (System/exit 1)))

   (let [gpx-name  (format "Track for device #%s: %s - %s" device-id date-from date-to)]
    (with-open [wrtr (writer output-filename)]
      (.write wrtr
        (html
          (xml-declaration "UTF-8")
          [:gpx {:version "1.0"}
          [:name gpx-name]
          [:trk
          [:name gpx-name]
          [:trkseg (map mark-to-xml
            (get-marks date-from date-to device-id))]]]))))))