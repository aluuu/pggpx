(defproject pggpx "0.1.0-SNAPSHOT"
  :description "GPS tracks converter (from PostgreSQL database to GPX files)"
  :url "http://tgt72.ru/"
  :main pggpx.core
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [korma "0.3.0-beta9"]
                 [hiccup "1.0.2"]
                 [postgresql/postgresql "9.0-801.jdbc4"]
                 [clj-time "0.4.4"]])
