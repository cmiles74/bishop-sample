;;
;; A sample Bishop service.
;;
(ns com.tnrglobal.bishopsample.service
  (:use [cheshire.core]
        [hiccup.core]
        [hiccup.page]
        [hiccup.element])
  (:require [com.tnrglobal.bishopsample.application :as app]
            [com.tnrglobal.bishop.core :as bishop])
  (:import [java.text SimpleDateFormat]))

;; the base url for our resource
(def URI-BASE "todos")

;; date format for HTML output
(def DATE-FORMAT (SimpleDateFormat.
                  "MM/dd/yyyy, HH:mm:ss"))

(defn parse-id
  "Parses a String of text into a valid identifier. If the data cannot
  be parsed then nil is returned."
  [id]
  (try (Integer/parseInt id)
       (catch Exception exception
         nil)))

(defn add-resource-links
  "Adds resource links to the provided to-do item."
  [url todo]
  (merge todo
         {:_links {:self (str url "/" (:id todo))}}))

(defn todo-short-html
  [todo]
  [:li [:h4 (link-to (:self (:_links todo)) (:title todo))] (:description todo)])

(defn todo-long-html
  [todo]
  [:span
   [:h1 (link-to (:self (:_links todo)) (:title todo))]
   [:p (str "Created at " (.format DATE-FORMAT (:created todo)))]
   [:p (:description todo)]])

;; this resource returns a list of all to-do items in response to a
;; GET request, if provided a POST or PUT request than the data
;; provided is used to create a new to-do item
(def todo-group-resource
  (bishop/resource

   {;; JSON handler
    "application/json"
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
                 {:headers {"Location" (str URI-BASE "/" (:id todo))}})))

    ;; HTML handler
    "text/html"
    (fn [request]
      {:body (xhtml {:lange "en" }
                    [:body
                     [:h1 "To-Do List"]
                     [:ul (map #(todo-short-html (add-resource-links URI-BASE %))
                               (app/todos))]])})}

   {;; the request methods supported by this resource
    :allowed-methods (fn [request] [:get :head :post :put])

    ;; POSTs with new data will be handled like a PUT
    :post-is-create? (fn [request] true)

    ;; we use the modification date on the most recently modified
    ;; to-do item as the last modification date
    :last-modified (fn [request]
                     (app/most-recent-todo-modified))}))

;; this resource returns a specific to-do item in response to GET
;; requests, if provided a PUT request then the data is used to update
;; the to-do item
(def todo-item-resource
  (bishop/resource
   {;; JSON Handler
    "application/json"
    (fn [request]

      ;; parse out the provided to-do ID
      (let [id (parse-id (:id (:path-info request)))]

        {:body
         (cond

           ;; return the requested to-do item
           (= :get (:request-method request))
           (generate-string (add-resource-links URI-BASE (app/todo-fetch id)))

           ;; update the requested to-do item
           (= :put (:request-method request))
           (let [todo-in (parse-string (slurp (:body request)) true)]
             (generate-string
              (add-resource-links URI-BASE (app/todo-update id todo-in)))))}))

    ;; HTML handler
    "text/html"
    (fn [request]
      (let [id (parse-id (:id (:path-info request)))]
        {:body
         (xhtml {:lange "en" }
                [:body (todo-long-html
                        (add-resource-links (str "/" URI-BASE)
                                            (app/todo-fetch id)))])}))}

   {;; the request methods supported by this resource
    :allowed-methods (fn [request] [:get :head :put :delete])

    ;; verify that we have a todo with the provided id
    :resource-exists? (fn [request]
                        (and (parse-id (:id (:path-info request)))
                             (app/id-present?
                              (parse-id (:id (:path-info request))))))

    ;; only JSON data can be used to update a to-do
    :known-content-type? (fn [request]
                           (if (= :put (:request-method request))
                             (= "application/json" (:content-type request))
                             true))

    ;; handles deleting a resource
    :delete-resource (fn [request]
                       (app/todo-remove
                        (parse-id (:id (:path-info request)))))

    ;; confirms the resource was deleted
    :delete-completed? (fn [request]
                         (not (app/id-present?
                               (parse-id (:id (:path-info request))))))

    ;; makes sure the update doesn't cause a conflict
    :is-conflict? (fn [request]
                    (not (app/id-present?
                          (parse-id (:id (:path-info request))))))

    ;; returns the date for the "Last-Modified" header
    :last-modified (fn [request]
                     (app/todo-modified
                      (parse-id (:id (:path-info request)))))

    ;; returns the value for the "ETag" header
    :generate-etag (fn [request]
                     (let [id (parse-id (:id (:path-info request)))]
                       (str (.getTime (app/todo-modified id)) "-" id)))}))

(def routes
  {[URI-BASE] todo-group-resource
   [URI-BASE :id] todo-item-resource})
