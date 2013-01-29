# pggpx

A Clojure tiny program to extract GPS tracks from PostgreSQL and save it in GPX files.

## Usage

Create ./src/pggpx/settings.clj with contents from ./src/pggpx/settings.clj.sample. Configure it for your DB server.

    lein deps
    lein run -- --date-from "2012-12-07 00:00:00" --date-to "2012-12-08 00:00:00" --device-id "114100"  --output-filename "/tmp/output.gpx"

Also available options:
    --max-period — split track on segments according to max period (in minutes) between consecutive marks
    --max-distance — split track on segments according to max distance (in meters) between consecutive marks

## License

Copyright © 2013 Alexander Dinu

Distributed under the Eclipse Public License, the same as Clojure.
