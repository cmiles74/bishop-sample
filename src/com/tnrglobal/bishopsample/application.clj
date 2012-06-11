;;
;; Functions that provide our sample to-do list application.
;;
(ns com.tnrglobal.bishopsample.application
  (:import [org.joda.time DateTime]))

;; our to-do list database, we start with one item
(def DATABASE (ref {1
                    {:id 1
                     :title "Your first to-do"
                     :description "Write more to-dos!"
                     :created (DateTime.)
                     :modified (DateTime.)}}))

;; the time of the most recent database change
(def DATABASE-MODIFIED (ref (DateTime.)))

;; the next available ID
(def NEXT-ID (ref 1))

(defn todos
  "Returns a sequence of to-do list items."
  []

  ;; we remove the :modified key, it's just for housekeeping
  (map #(dissoc %1 :modified) (vals @DATABASE)))

(defn todo-fetch
  "Returns the to-do item with the provided ID, suitable for outside
  consumption."
  [id]

  ;; we remove the :modified key, it's just for housekeeping
  (dissoc (@DATABASE id) :modified))

(defn todo-modified
  "Returns the most recent modification date of the to-do item with
  the provided ID."
  [id]
  (:modified (@DATABASE id)))

(defn most-recently-modified
  "Returns the date the database was most recently modified"
  []
  @DATABASE-MODIFIED)

(defn id-present?
  "Returns true if the provided ID is present in the database."
  [id]
  (some #(= id %) (keys @DATABASE)))

(defn todo-different?
  "Returns true if the content of two to-do items contain different
  content. The :created value is not modifiable."
  [todo-1 todo-2]
  (or (not= (:title todo-1) (:title todo-2))
      (not= (:description todo-1) (:description todo-2))))

(defn todo-add
  "Adds a new item to the to-do list."
  [todo]
  (cond

    ;; don't allow to-do items that have an ID
    (:id todo)
    (throw (Exception. "New to-do items cannot contain an ID"))

    ;; add the new todo to our database
    :else
    (let [id (dosync (alter NEXT-ID inc))]
      (dosync
       (alter DATABASE assoc id (merge {:id id
                                        :created (DateTime.)
                                        :modified (DateTime.)}
                                       todo))
       (ref-set DATABASE-MODIFIED (DateTime.)))

      ;; return our new to-do item
      (todo-fetch id))))

(defn todo-remove
  "Removes an item from the to-do list."
  [id]

  ;; make sure the item exists
  (if (id-present? id)
    (dosync
     (alter DATABASE dissoc id)
     (ref-set DATABASE-MODIFIED (DateTime.))
     true)
    (throw (Exception. (str "No to-do item found with ID '" id "'")))))

(defn todo-update
  "Accepts a to-do item as an update to an existing to-do item. The ID
  of the provided to-do item is used to decide which item in the
  database to update."
  [id todo]

  ;; make sure we have an id of the to-do to update
  (if (not id)
    (throw (Exception. (str "The ID of the to-do item to update was not "
                            "provided"))))

  ;; make sure that if the to-do has an id, it matches our id
  (if (and (:id todo)
           (not= id (:id todo)))
    (throw (Exception. (str "The to-do item has a mis-matched id"))))

  (dosync

   ;; make sure we have a to-do item with the provided id
   (if (not (id-present? id))
     (throw (Exception. (str "There is no to-do item with the ID '"
                             id "'"))))

   ;; update the modified date if our items differ
   (let [old-todo (todo-fetch id)
         updated-todo (if (todo-different? old-todo todo)
                        (assoc todo :modified (DateTime.))
                        todo)]

     ;; update our database
     (alter DATABASE assoc id
            (merge old-todo updated-todo))
     (ref-set DATABASE-MODIFIED (DateTime.))))

  ;; return the updated to-do
  (todo-fetch id))
