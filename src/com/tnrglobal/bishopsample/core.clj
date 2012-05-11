;;
;; A sample Bishop application.
;;
(ns com.tnrglobal.bishopsample.core
  (:gen-class)
  (:use [ring.adapter.jetty]
        [clojure.tools.logging])
  (:require [com.tnrglobal.bishop.core :as bishop]
            [com.tnrglobal.bishopsample.service :as service]))

(defn main
  "Exposes the main function for bootstrapping the application."
  [& args]
  (info "Hello from Bishop Sample!")
  (run-jetty (bishop/handler service/routes) {:port 3000}))

(defn -main
  [& args]
  (main args))