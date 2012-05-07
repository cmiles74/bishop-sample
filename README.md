This project provides a sample application that uses the
[Bishop library](https://github.com/tnr-global/bishop). Bishop makes
it much easier to provide an API for your web-service that treats HTTP
as a first-class application layer. This sample application provides a
simple web-service that lets you manage a to-do list.

## Building the Application

You can check-out the project and the build it.

```
git clone git://github.com/tnr-global/bishop-sample.git
cd bishop-sample
lein uberjar
...watch Leiningen build...
java -jar bishop-sample-1.0-standalone.jar
```

## How it Works

This application provides a web-service that manages a to-do
list. This web-service speaks JSON, you can retrieve the list of items
with a GET request or add new items with a POST. Existing to-do items
are easily updated by making a PUT request to the to-do item's URL
with new data.

In addition to JSON web-service, this application provides a
bare-bones web application that lets you view the list of to-do items
as well as detail on each item. Once you have the application
up-and-running, you can view the list at...

[http://localhost:3000/todos](http://localhost:3000/todos)

The Bishop library handles content negotiation on behalf of the
application. It compares the headers provided by the client web
browser against the media types provided by the resource and picks the
one that matches.

Bishop also helps you write a web-service that has predictable caching
behavior. If you select a to-do item in your web-browser, you'll
notice that the first request returns a 200 response with the details
on that item. Subsequent re-loading of the page will cause "304 Not
Modified" responses to the client, causing the page to be served out
of cache. In this example the client is providing the "ETag" of the
to-do item and Bishop compares this against the ETag of the to-do
item, since they match the item hasn't changed and can be safely
served from cache.

By using Bishop, creating a web-service that treats HTTP as a
first-class application protocol is a straightforward and painless
process. Take a look at the
[sample code](https://github.com/tnr-global/bishop-sample/blob/master/src/com/tnrglobal/bishopsample/service.clj),
we provide a handful of callback functions and the library handles the
rest.

## Sample Transactions

The sample application provides a web service will respond to the
standard create, update and delete requests. Examples of these
requests are detailed below.

### Listing All To-Do Items

```
$ curl -i http://localhost:3000/todos
HTTP/1.1 200 OK
Date: Sun, 06 May 2012 15:03:54 GMT
Vary: accept
Last-Modified: Sun, 06 May 2012 15:03:52 UTC
Content-Length: 133
Server: Jetty(6.1.x)

[{"_links":{"self":"todos/1"},"id":1,"title":"Your first to-do",
"description":"Write more to-dos!","created":"2012-05-06T15:03:52Z"}]
```

### Updating a To-Do Item

```
$ curl -i http://localhost:3000/todos/1 -H 'Content-Type: application/json' \
-XPUT -d '{
"title" : "More creative writing",
"description" : "Write more blog posts"}'

HTTP/1.1 200 OK
Date: Sun, 06 May 2012 15:13:21 GMT
Vary: accept
Content-Length: 141
Server: Jetty(6.1.x)

{"_links":{"self":"todos/1"},"id":1,"title":"More creative writing",
"description":"Write more blog posts","created":"2012-05-06T15:03:52Z"}
```

### Adding A New To-Do Item

```
$ curl -i http://localhost:3000/todos -H 'Content-Type: application/json' \
-XPOST -d '{
"title" : "Work on web service",
"description" : "Work on your web service error handling"}'

HTTP/1.1 303 See Other
Date: Sun, 06 May 2012 15:06:53 GMT
Vary: accept
Location: todos/2
Debug: N11, switching to PUT
Content-Length: 0
Server: Jetty(6.1.x)
```

### Retrieving a To-Do Item

```
$ curl -i http://localhost:3000/todos/2

HTTP/1.1 200 OK
Date: Mon, 07 May 2012 12:12:33 GMT
Vary: accept
ETag: "1336392745012-2"
Last-Modified: Mon, 07 May 2012 12:12:25 UTC
Content-Length: 157
Server: Jetty(6.1.x)

{"_links":{"self":"todos/2"},
"description":"Work on your web service error handling",
"title":"Work on web service","id":2,"created":"2012-05-07T12:12:25Z"}
```

### Deleting a To-Do Item

```
$ curl -i http://localhost:3000/todos/2 -XDELETE

HTTP/1.1 204 No Content
Date: Sun, 06 May 2012 15:11:01 GMT
Vary: accept
Server: Jetty(6.1.x)
```
