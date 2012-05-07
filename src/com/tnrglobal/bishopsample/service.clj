;;
;; A sample Bishop service.
;;
(ns com.tnrglobal.bishopsample.service
  (:gen-class)
  (:use [cheshire.core]
        [clojure.tools.logging])
  (:require [com.tnrglobal.bishopsample.application :as app]
            [com.tnrglobal.bishop.core :as bishop]
            [ring.util.response :as ring-utils]
            [clojure.string :as string])
  (:import [java.util Date]))

;; the base url for our resource
(def URI-BASE "todos")

(defn add-resource-links
  "Adds resource links to the provided to-do item."
  [url todo]
  (merge todo
         {:_links {:self (str url "/" (:id todo))}}))

;; this resource returns a list of all to-do items in response to a
;; GET request, if provided a POST or PUT request than the data
;; provided is used to create a new to-do item
(def todo-group-resource
  (bishop/resource

   ;; returns a list of to-do items
   {"application/json"
    (fn [request]
      (cond

               ;; return a sequence of to-do items
               (= :get (:request-method request))
               {:body (generate-string (map (partial add-resource-links URI-BASE)
                                            (app/todos)))}

               ;; create the new to-do item
               (= :put (:request-method request))
               (let [todo-in (parse-string (slurp (:body request)) true)
                     todo (app/todo-add todo-in)]
                 {:headers {"Location" (str URI-BASE "/" (:id todo))}})))}

   {;; the request methods supported by this resource
    :allowed-methods (fn [request] [:get :head :post :put])

    ;; POSTs with new data will be handled like a PUT
    :post-is-create? (fn [request] true)

    ;; temporary resource path for new items
    :create-path (fn [request] "/pending")

    ;; we use the modification date on the most recently modified
    ;; to-do item as the last modification date
    :last-modified (fn [request]
                     (.getTime (app/most-recent-todo-modified)))}))

;; this resource returns a specific to-do item in response to GET
;; requests, if provided a PUT request then the data is used to update
;; the to-do item
(def todo-item-resource
  (bishop/resource
   {"application/json"
    (fn [request]
      {:body

       ;; parse out the provided to-do ID
       (let [id (Integer/parseInt (:id (:path-info request)))]
         (cond

           ;; return the requested to-do item
           (= :get (:request-method request))
           (generate-string (add-resource-links URI-BASE (app/todo-fetch id)))

           ;; update the requested to-do item
           (= :put (:request-method request))
           (let [id (Integer/parseInt (:id (:path-info request)))
                 todo-in (parse-string (slurp (:body request)) true)]
             (generate-string
              (add-resource-links URI-BASE (app/todo-update id todo-in))))))})}

   {;; the request methods supported by this resource
    :allowed-methods (fn [request] [:get :head :put :delete])

    ;; verify that we have a todo with the provided id
    :resource-exists? (fn [request]
                        (app/id-present?
                         (Integer/parseInt (:id (:path-info request)))))

    ;; only JSON data can be used to update a to-do
    :known-content-type? (fn [request]
                           (if (= :put (:request-method request))
                             (= "application/json" (:content-type request))
                             true))

    ;; handles deleting a resource
    :delete-resource (fn [request]
                       (app/todo-remove
                        (Integer/parseInt (:id (:path-info request)))))

    ;; confirms the resource was deleted
    :delete-completed? (fn [request]
                         (not (app/id-present?
                               (Integer/parseInt (:id (:path-info request))))))

    ;; returns the date for the "Last-Modified" header
    :last-modified (fn [request]
                     (.getTime
                      (app/todo-modified
                       (Integer/parseInt (:id (:path-info request))))))

    ;; returns the value for the "ETag" header
    :generate-etag (fn [request]
                     (let [id (Integer/parseInt (:id (:path-info request)))]
                       (str (.getTime (app/todo-modified id)) "-" id)))}))

(def routes
  {["todos"] todo-group-resource
   ["todos" :id] todo-item-resource})
