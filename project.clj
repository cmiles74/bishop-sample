(defproject bishop-sample "1.0"
  :description "A sample Bishop application"
  :dependencies [[ring/ring-core "1.1.5"]
                 [ring/ring-jetty-adapter "1.1.5"]
                 [cheshire "4.0.2"]
                 [hiccup "1.0.1"]
                 [tnrglobal/bishop "1.1.7"]]
  :dev-dependencies [[org.clojure/clojure "1.3.0"]
                     [swank-clojure/swank-clojure "1.3.3"]]
  :warn-on-reflection true
  :ring {:handler com.tnrglobal.bishopsample.core/handler}
  :main com.tnrglobal.bishopsample.core)