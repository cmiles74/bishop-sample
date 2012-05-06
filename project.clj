(defproject bishop-sample "1.0"
  :description "A sample Bishop application"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ring/ring-core "1.0.2"]
                 [ring/ring-jetty-adapter "1.0.2"]
                 [cheshire "4.0.0"]
                 [tnrglobal/bishop "1.0"]]
  :dev-dependencies [[swank-clojure/swank-clojure "1.3.3"]]
  :keep-non-project-classes true
  :main com.tnrglobal.bishopsample.core)